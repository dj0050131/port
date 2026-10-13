import requests
import json
import hashlib
import re
import os
import pickle
import pymysql
from datetime import datetime, timedelta, date  # 修复：添加date导入
import time
import logging
from typing import List, Dict, Any, Optional

# ==================== 新增配置区域 ====================
# 文档保留策略：只保留最近 N 次同步的文档，删除更旧的
KEEP_LAST_N_DOCS = 1  # 保留最近 3 个版本，设为 1 则只保留最新版
DOC_TITLE_PREFIX = "港口数据同步_"  # 用于识别自动同步的文档
# 删除重试：部分环境下 UI/接口表现为“删两次才真正消失”（可能是异步/缓存/二阶段解绑）
DELETE_RETRY_TIMES = 2
DELETE_RETRY_SLEEP_SECONDS = 1.0

# ==================== 配置区域 ====================
# AnythingLLM 配置
ANYTHINGLLM_API_BASE = os.environ.get("ANYTHINGLLM_URL", "")
API_KEY = os.environ.get("ANYTHINGLLM_API_KEY", "")
WORKSPACE_SLUG = os.environ.get("ANYTHINGLLM_WORKSPACE_ID", "default")

# 数据库配置
DATABASE_CONFIG = {
    "host": os.environ.get("DB_HOST", "127.0.0.1"),
    "port": 3306,
    "user": os.environ.get("DB_USER", "root"),
    "password": os.environ.get("DB_PASSWORD", ""),
    "database": "port_pilotage",
    "charset": "utf8mb4"
}

# 同步设置
SYNC_INTERVAL = 300
SYNC_MODE = "ALL_TABLES"
MAX_RECORDS_PER_SYNC = 500
INCREMENTAL_SYNC = True  # 首次运行可设为False进行全量测试

# 排除表配置
EXCLUDE_TABLES = []  # 暂时不排除任何表

# 特殊字段映射 - 根据您的日志显示，某些表的时间字段需要特别指定
TIME_FIELD_ALIASES = {
    "drivers": "updated_at",  # 将birth_date改为updated_at
    "pilots": "updated_at",   # 将career_start_date改为updated_at
    "vessels": "updated_at",  # 将build_date改为updated_at
    "vehicles": "updated_at", # 将purchase_date改为updated_at
    "pilot_boats": "updated_at", # 将build_date改为updated_at
}

# 日志配置
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('anythingllm_sync.log', encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)




# ==================== 新增函数 ====================
def extract_doc_api_path(doc_location_or_path: str) -> Optional[str]:
    """
    AnythingLLM 官方 update-embeddings API 的 adds/deletes 需要“文档相对路径”，例如：
      custom-documents/my-file.pdf-hash.json
    这里从 docpath（相对路径）或 location（绝对路径）中提取。
    """
    if not doc_location_or_path:
        return None
    normalized = str(doc_location_or_path).replace("\\", "/")
    idx = normalized.find("custom-documents")
    if idx != -1:
        return normalized[idx:]
    # 若已是相对路径（无盘符/协议），兜底返回
    if "/" in normalized and ":" not in normalized and not normalized.startswith(("http://", "https://")):
        # 有些接口可能只返回文件名（无目录），按官方约定默认放在 custom-documents 下
        if "/" not in normalized:
            return f"custom-documents/{normalized}"
        return normalized
    return None

def calculate_data_hash(data_records: List[Dict]) -> str:
    """计算数据哈希值，用于判断数据是否真正变化"""
    if not data_records:
        return "empty"
    
    # 提取关键字段生成哈希（忽略 _record_time 等元数据）
    hash_content = []
    for record in data_records[:50]:  # 只取前50条避免过大
        key_data = {k: str(v) for k, v in record.items() if not k.startswith('_')}
        hash_content.append(json.dumps(key_data, sort_keys=True))
    
    content_str = "|".join(hash_content)
    return hashlib.md5(content_str.encode()).hexdigest()

def get_workspace_documents(workspace_slug: str, api_key: str, api_base: str) -> List[Dict]:
    """获取工作区中的所有文档"""
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Accept": "application/json",
    }
    try:
        response = requests.get(
            f"{api_base}/v1/workspace/{workspace_slug}/documents",
            headers=headers,
            timeout=30
        )
        if response.status_code == 200:
            try:
                result = response.json()
            except Exception:
                # AnythingLLM 偶尔会返回非 JSON（如 HTML/纯文本），这里输出片段便于排障
                logger.error(
                    "获取工作区文档列表失败: 非JSON响应 status=%s, text=%s",
                    response.status_code,
                    (response.text or "")[:300].replace("\n", "\\n")
                )
                return []

            return result.get("documents", []) if result.get("success") else []

        logger.error(
            "获取工作区文档列表失败: status=%s, text=%s",
            response.status_code,
            (response.text or "")[:300].replace("\n", "\\n")
        )
        return []
    except Exception as e:
        logger.error(f"获取工作区文档列表失败: {e}")
        return []

def cleanup_old_sync_docs(workspace_slug: str, api_key: str, api_base: str, keep_last: int = KEEP_LAST_N_DOCS):
    """清理旧的同步文档，只保留最近 N 个"""
    headers = {"Authorization": f"Bearer {api_key}"}
    
    # 获取所有文档
    docs = get_workspace_documents(workspace_slug, api_key, api_base)
    
    # 筛选出自动同步的文档（按标题前缀识别）
    sync_docs = []
    for doc in docs:
        # 兼容：部分接口返回 name，部分返回 title
        doc_name = doc.get("name") or doc.get("title") or ""
        if not doc_name.startswith(DOC_TITLE_PREFIX):
            continue

        docpath = doc.get("docpath") or doc.get("location") or doc.get("path") or ""
        api_path = extract_doc_api_path(docpath)
        sync_docs.append({
            "name": doc_name,
            "id": doc.get("id"),
            "docpath": doc.get("docpath"),
            "api_path": api_path
        })
    
    if len(sync_docs) <= keep_last:
        logger.info(f"当前工作区有 {len(sync_docs)} 个同步文档，无需清理")
        return
    
    # 按名称排序（时间戳降序），保留最新的
    sync_docs.sort(key=lambda x: x["name"], reverse=True)
    docs_to_delete = sync_docs[keep_last:]
    
    logger.info(f"发现 {len(sync_docs)} 个历史同步文档，将删除 {len(docs_to_delete)} 个旧版本")
    
    # 删除旧文档（批量）
    deletes: List[str] = []
    for old_doc in docs_to_delete:
        api_path = old_doc.get("api_path")
        if not api_path:
            logger.warning(f"  跳过删除（无法解析 docpath）: {old_doc.get('name')} docpath={old_doc.get('docpath')}")
            continue
        deletes.append(api_path)

    if not deletes:
        logger.info("没有可删除的 docpath（可能是 documents 接口未返回 docpath/location）")
        return

    try:
        payload = {"adds": [], "deletes": deletes}
        logger.info(f"准备从工作区移除 {len(deletes)} 个旧文档")
        logger.debug(f"删除 payload: {json.dumps(payload, ensure_ascii=False)}")
        response = requests.post(
            f"{api_base}/v1/workspace/{workspace_slug}/update-embeddings",
            headers=headers,
            json=payload,
            timeout=30
        )
        if response.status_code == 200:
            logger.info("旧文档已提交移除请求（200 OK）")
        else:
            logger.warning(f"移除旧文档失败: {response.status_code} {response.text[:300]}")
    except Exception as e:
        logger.error(f"移除旧文档异常: {e}")

def load_sync_state():
    """加载同步状态（增加哈希值存储）"""
    state_file = "sync_state.pkl"
    if os.path.exists(state_file):
        try:
            with open(state_file, 'rb') as f:
                return pickle.load(f)
        except:
            pass
    
    # 首次运行：同步最近7天的数据
    default_time = (datetime.now() - timedelta(days=7)).strftime("%Y-%m-%d %H:%M:%S")
    return {
        "last_sync_time": default_time,
        "last_data_hash": None,  # 新增：上次数据哈希
        "sync_count": 0,         # 新增：同步计数
        "last_doc_api_path": None  # 新增：上次写入工作区的 docpath（custom-documents/...）
    }

def save_sync_state(state):
    """保存同步状态（pickle持久化）"""
    state_file = "sync_state.pkl"
    try:
        with open(state_file, 'wb') as f:
            pickle.dump(state, f)
    except Exception as e:
        logger.error(f"保存状态失败: {e}")

def perform_sync():
    """执行同步（智能增量 + 自动清理）"""
    logger.info("=" * 60)
    logger.info("开始数据同步")

    state = load_sync_state()
    last_sync_time = state["last_sync_time"]
    last_data_hash = state.get("last_data_hash")

    # 查询数据
    new_data = query_all_tables_data(last_sync_time)

    if not new_data:
        logger.info("无新数据，跳过上传")
        return

    # 计算本次数据哈希，判断是否真正变化（避免重复上传相同数据）
    current_hash = calculate_data_hash(new_data)
    logger.info(f"数据哈希: {current_hash[:8]}... (上次: {last_data_hash[:8] if last_data_hash else '无'})")
    
    if current_hash == last_data_hash:
        logger.info("数据哈希未变化，跳过上传（内容未改变）")
        # 仍然更新时间戳，避免下次重复查询相同数据
        state["last_sync_time"] = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        save_sync_state(state)
        return

    logger.info(f"发现 {len(new_data)} 条新数据（哈希已变化，准备上传）")
    
    # ========== 关键：同步前清理 ==========
    # 说明：官方 update-embeddings 的 deletes 需要 docpath，列文档接口偶尔不可用；
    # 因此这里不强依赖“列文档接口”，会在上传成功后用本地 state 精确删除上一份文档。
    if KEEP_LAST_N_DOCS > 0:
        cleanup_old_sync_docs(WORKSPACE_SLUG, API_KEY, ANYTHINGLLM_API_BASE, KEEP_LAST_N_DOCS)
    
    # 格式化并上传
    document_text = format_as_document(new_data)
    uploaded_doc_api_path = upload_to_anythingllm(document_text, len(new_data))
    success = bool(uploaded_doc_api_path)

    if success:
        # 更新同步时间和哈希
        state["last_sync_time"] = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        state["last_data_hash"] = current_hash
        state["sync_count"] = state.get("sync_count", 0) + 1
        prev_doc_api_path = state.get("last_doc_api_path")
        state["last_doc_api_path"] = uploaded_doc_api_path
        save_sync_state(state)
        logger.info(f"✅ 同步完成！当前版本: #{state['sync_count']}")

        # ========= 同步后删除上一份文档：不依赖“列文档接口”，保证只保留最新 =========
        if KEEP_LAST_N_DOCS == 1 and prev_doc_api_path and prev_doc_api_path != uploaded_doc_api_path:
            try:
                headers = {"Authorization": f"Bearer {API_KEY}", "Content-Type": "application/json"}
                payload = {"adds": [], "deletes": [prev_doc_api_path]}
                logger.info(f"删除上一份同步文档(将重试{DELETE_RETRY_TIMES}次): {prev_doc_api_path}")
                for i in range(max(1, int(DELETE_RETRY_TIMES))):
                    resp = requests.post(
                        f"{ANYTHINGLLM_API_BASE}/v1/workspace/{WORKSPACE_SLUG}/update-embeddings",
                        headers=headers,
                        json=payload,
                        timeout=30
                    )
                    if resp.status_code == 200:
                        logger.info(f"移除上一份同步文档请求成功（第{i+1}次，200 OK）")
                    else:
                        logger.warning(f"移除上一份同步文档失败（第{i+1}次）: {resp.status_code} {resp.text[:300]}")
                    if i < max(1, int(DELETE_RETRY_TIMES)) - 1:
                        time.sleep(float(DELETE_RETRY_SLEEP_SECONDS))
            except Exception as e:
                logger.error(f"移除上一份同步文档异常: {e}")
    else:
        logger.error("❌ 同步失败")
# ==================== 数据库查询函数（修复版） ====================
def find_best_time_field(table_name: str, columns_info: List) -> Optional[str]:
    """智能寻找最佳时间字段"""
    # 1. 检查预定义别名
    if table_name in TIME_FIELD_ALIASES:
        logger.debug(f"  使用预定义时间字段: {TIME_FIELD_ALIASES[table_name]}")
        return TIME_FIELD_ALIASES[table_name]

    # 2. 优先寻找更新时间类字段
    update_patterns = ["update", "modif", "last_"]
    create_patterns = ["create", "insert"]
    date_patterns = ["date", "time", "at", "timestamp"]

    all_columns = []
    for column in columns_info:
        field_name = column["Field"].lower()
        field_type = str(column["Type"]).lower()
        all_columns.append((field_name, field_type, column["Field"]))

    # 优先级1: 更新时间类字段
    for field_name, field_type, original_name in all_columns:
        if any(pattern in field_name for pattern in update_patterns):
            if any(time_type in field_type for time_type in ["timestamp", "datetime"]):
                logger.debug(f"  找到更新时间字段: {original_name}")
                return original_name

    # 优先级2: 创建时间类字段
    for field_name, field_type, original_name in all_columns:
        if any(pattern in field_name for pattern in create_patterns):
            if any(time_type in field_type for time_type in ["timestamp", "datetime"]):
                logger.debug(f"  找到创建时间字段: {original_name}")
                return original_name

    # 优先级3: 任何时间/日期类型字段
    for field_name, field_type, original_name in all_columns:
        if any(pattern in field_name for pattern in date_patterns):
            if any(time_type in field_type for time_type in ["timestamp", "datetime", "date"]):
                logger.debug(f"  找到日期字段: {original_name}")
                return original_name

    # 优先级4: 第一个字段
    if all_columns:
        logger.debug(f"  使用第一个字段作为时间字段: {all_columns[0][2]}")
        return all_columns[0][2]

    return None

def query_table_data(cursor, table_name: str, columns_info: List, last_sync_time: str) -> List[Dict]:
    """查询单表数据（修复date处理）"""
    time_field = find_best_time_field(table_name, columns_info)

    if INCREMENTAL_SYNC and time_field:
        # 增量查询
        sql = f"SELECT * FROM {table_name} WHERE {time_field} > %s ORDER BY {time_field} ASC LIMIT 50"
        params = (last_sync_time,)
        query_type = "增量"
    else:
        # 全量查询
        primary_key = "id"
        for column in columns_info:
            if column.get("Key") == "PRI":
                primary_key = column["Field"]
                break

        sql = f"SELECT * FROM {table_name} ORDER BY {primary_key} DESC LIMIT 30"
        params = ()
        query_type = "全量"

    logger.info(f"  [{query_type}查询] 使用字段: {time_field or primary_key}")
    logger.debug(f"  SQL: {sql}")
    logger.debug(f"  参数: {params}")

    try:
        cursor.execute(sql, params)
        records = cursor.fetchall()

        formatted_records = []
        for record in records:
            formatted = {}
            for key, value in record.items():
                if value is None:
                    formatted[key] = ""
                elif isinstance(value, datetime):
                    formatted[key] = value.strftime("%Y-%m-%d %H:%M:%S")
                elif isinstance(value, date):  # 修复：处理date类型
                    formatted[key] = value.strftime("%Y-%m-%d")
                elif isinstance(value, (int, float, bool)):
                    formatted[key] = value
                else:
                    # 限制字符串长度
                    str_value = str(value)
                    # if len(str_value) > 150:
                    #     str_value = str_value[:150] + "..."
                    formatted[key] = str_value
            formatted_records.append(formatted)

        logger.debug(f"  获取到 {len(formatted_records)} 条记录")
        return formatted_records

    except Exception as query_error:
        logger.error(f"  查询失败: {str(query_error)}")
        return []

def query_all_tables_data(last_sync_time: str) -> List[Dict[str, Any]]:
    """查询所有表数据（增强调试）"""
    connection = None
    all_records = []

    try:
        logger.info(f"连接数据库 {DATABASE_CONFIG['host']}...")
        connection = pymysql.connect(
            host=DATABASE_CONFIG['host'],
            port=DATABASE_CONFIG.get('port', 3306),
            user=DATABASE_CONFIG['user'],
            password=DATABASE_CONFIG['password'],
            database=DATABASE_CONFIG['database'],
            charset=DATABASE_CONFIG.get('charset', 'utf8mb4'),
            cursorclass=pymysql.cursors.DictCursor
        )

        with connection.cursor() as cursor:
            # 获取所有表
            cursor.execute("SHOW TABLES")
            table_key = f"Tables_in_{DATABASE_CONFIG['database']}"
            all_tables = [row[table_key] for row in cursor.fetchall()]

            # 过滤排除表
            tables_to_sync = []
            for table in all_tables:
                exclude = False
                for pattern in EXCLUDE_TABLES:
                    if pattern.lower() in table.lower():
                        exclude = True
                        break
                if not exclude:
                    tables_to_sync.append(table)

            logger.info(f"数据库共 {len(all_tables)} 张表，将同步 {len(tables_to_sync)} 张表")
            logger.info(f"最后同步时间: {last_sync_time}")
            logger.info(f"增量同步模式: {INCREMENTAL_SYNC}")

            # 记录每张表的查询情况
            table_stats = {"成功": 0, "失败": 0, "无数据": 0}

            for table_index, table_name in enumerate(tables_to_sync, 1):
                try:
                    logger.info(f"[{table_index}/{len(tables_to_sync)}] 处理表: {table_name}")

                    # 获取表结构
                    cursor.execute(f"DESCRIBE {table_name}")
                    columns_info = cursor.fetchall()

                    # 查询数据
                    table_records = query_table_data(cursor, table_name, columns_info, last_sync_time)

                    if table_records:
                        logger.info(f"  → 获取 {len(table_records)} 条记录")
                        table_stats["成功"] += 1

                        # 添加表信息
                        for record in table_records:
                            record["_table_name"] = table_name
                            record["_record_time"] = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

                        all_records.extend(table_records)

                        if len(all_records) >= MAX_RECORDS_PER_SYNC:
                            logger.warning(f"达到最大记录数限制 ({MAX_RECORDS_PER_SYNC})")
                            break
                    else:
                        logger.info(f"  → 无新记录")
                        table_stats["无数据"] += 1

                except Exception as table_error:
                    logger.error(f"  处理表失败: {str(table_error)[:100]}")
                    table_stats["失败"] += 1
                    continue

            # 输出统计信息
            logger.info(f"表处理统计: 成功{table_stats['成功']}张, 无数据{table_stats['无数据']}张, 失败{table_stats['失败']}张")

        logger.info(f"查询完成，共获取 {len(all_records)} 条记录")

        # 如果没有数据，显示一些调试建议
        if len(all_records) == 0 and INCREMENTAL_SYNC:
            logger.warning("未获取到数据，可能原因：")
            logger.warning("1. 自上次同步时间以来确实无新数据")
            logger.warning("2. 时间字段选择不正确")
            logger.warning("3. 可尝试关闭增量同步（INCREMENTAL_SYNC=False）进行全量测试")

        return all_records

    except pymysql.Error as db_error:
        logger.error(f"数据库连接错误: {db_error}")
        return []
    except Exception as e:
        logger.error(f"查询数据库错误: {e}")
        return []
    finally:
        if connection:
            connection.close()

# ==================== 数据格式化函数 ====================
def format_as_document(data_records: List[Dict[str, Any]]) -> str:
    """格式化数据为文档（完整版）"""
    if not data_records:
        return "本次同步未发现新数据。"

    tables_data = {}
    for record in data_records:
        table_name = record.get("_table_name", "未知表")
        if table_name not in tables_data:
            tables_data[table_name] = []
        tables_data[table_name].append(record)

    doc_content = f"""# 港口引航系统数据同步报告（完整版）

## 同步概览
- **同步时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
- **数据源**: {DATABASE_CONFIG['database']}数据库
- **涉及表数**: {len(tables_data)}张表
- **总记录数**: {len(data_records)}条记录

## 数据详情
"""

    for table_name, records in tables_data.items():
        doc_content += f"\n### 📊 {table_name} ({len(records)}条记录)\n"

        for i, record in enumerate(records, 1):
            doc_content += f"\n**记录{i}:**\n"

            # 显示所有字段（排除元字段）
            for key, value in record.items():
                if not key.startswith("_") and value:
                    # 处理过长内容
                    if isinstance(value, str) and len(value) > 500:
                        doc_content += f"- {key}: {value[:500]}...\n"
                    else:
                        doc_content += f"- {key}: {value}\n"

            doc_content += "-" * 40 + "\n"  # 添加分隔线

    doc_content += f"\n*报告生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}*"
    return doc_content

# ==================== API上传函数 ====================
def upload_to_anythingllm(text_content: str, record_count: int = 0) -> Optional[str]:
    """上传文档到AnythingLLM（修复路径问题）"""
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json"
    }

    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    doc_title = f"港口数据同步_{timestamp}"

    # 1. 上传文档到系统
    upload_payload = {
        "textContent": text_content,
        "addToWorkspaces": WORKSPACE_SLUG,
        "metadata": {
            "title": doc_title,
            "docAuthor": "港口系统同步器",
            "description": f"包含{record_count}条记录的数据同步",
            "docSource": "港口引航数据库"
        }
    }

    try:
        logger.info(f"[1/3] 上传文档({record_count}条记录)...")
        upload_response = requests.post(
            f"{ANYTHINGLLM_API_BASE}/v1/document/raw-text",
            headers=headers,
            json=upload_payload,
            timeout=60
        )

        if upload_response.status_code != 200:
            logger.error(f"上传失败: {upload_response.text[:200]}")
            return None

        upload_result = upload_response.json()

        if not upload_result.get("success"):
            logger.error(f"API失败: {upload_result.get('error')}")
            return None

            # 2. 获取文档信息 - 使用location字段（raw-text接口不返回name字段）
        uploaded_docs = upload_result.get("documents", [])
        if not uploaded_docs:
            logger.error("未返回文档信息")
            return None

        first_doc = uploaded_docs[0]

        # 【关键修复】使用location字段而不是name字段
        doc_location = first_doc.get("location")  # 完整路径
        doc_id = first_doc.get("id")
        doc_title = first_doc.get("title", "未命名文档")

        logger.info(f"[2/3] 文档上传成功")
        logger.info(f"   文档ID: {doc_id}")
        logger.info(f"   文档标题: {doc_title}")
        logger.info(f"   完整路径(location): {doc_location}")

        if not doc_location:
            logger.error("[ERROR] API未返回文档路径(location字段)")
            logger.debug(f"[DEBUG] 完整响应: {json.dumps(first_doc, indent=2, ensure_ascii=False)}")
            return None

        # 3. 【关键修复】从绝对路径中提取API需要的相对路径
        # location示例: C:\Users\...\custom-documents\raw-_20260122_141505-982461ce-40e5-430c-b30e-7dc87ece3545.json
        # 目标: custom-documents/raw-_20260122_141505-982461ce-40e5-430c-b30e-7dc87ece3545.json

        # 使用更健壮的跨平台路径处理
        import os
        from pathlib import PurePath
        
        # 使用PurePath来处理不同系统的路径而不受当前操作系统影响
        doc_path = PurePath(doc_location)
        
        # 尝试找到包含custom-documents的路径段并构建相对路径
        path_parts = list(doc_path.parts)
        doc_api_path = None
        
        # 查找包含"custom-documents"的路径部分
        for i, part in enumerate(path_parts):
            if "custom-documents" in part:
                # 从"custom-documents"开始截取后续所有部分
                relevant_parts = path_parts[i:]
                doc_api_path = "/".join(relevant_parts)
                break
        
        # 如果没找到"custom-documents"，尝试查找其他可能的目录名
        if not doc_api_path:
            possible_dirs = ["documents", "uploads", "files", "data"]
            for possible_dir in possible_dirs:
                for i, part in enumerate(path_parts):
                    if possible_dir in part:
                        relevant_parts = path_parts[i:]
                        doc_api_path = "/".join(relevant_parts)
                        break
                if doc_api_path:
                    break
        
        # 如果还是没找到，尝试使用字符串方法作为最后手段
        if not doc_api_path:
            # 将路径标准化为使用正斜杠
            normalized_path = doc_location.replace('\\', '/').replace('//', '/')
            
            # 查找关键标识符
            for identifier in ["custom-documents", "documents", "uploads", "files"]:
                idx = normalized_path.find(identifier)
                if idx != -1:
                    doc_api_path = normalized_path[idx:]
                    break
        
        if not doc_api_path:
            logger.error(f"[ERROR] 无法从路径中提取API路径: {doc_location}")
            logger.debug(f"[DEBUG] 完整响应: {json.dumps(first_doc, indent=2, ensure_ascii=False)}")
            return None

        logger.info(f"[路径提取] 从绝对路径提取相对路径: {doc_api_path}")
        logger.info(f"[验证] API将使用的路径: {doc_api_path}")

        # 4. 将文档添加到工作区
        logger.info(f"[3/3] 添加到工作区 '{WORKSPACE_SLUG}'...")

        update_payload = {
            "adds": [doc_api_path],
            "deletes": []
        }

        # 记录实际发送的请求
        logger.debug(f"[DEBUG] 发送到update-embeddings的payload: {json.dumps(update_payload)}")

        update_response = requests.post(
            f"{ANYTHINGLLM_API_BASE}/v1/workspace/{WORKSPACE_SLUG}/update-embeddings",
            headers=headers,
            json=update_payload,
            timeout=30
        )

        logger.info(f"工作区更新响应: {update_response.status_code}")

        if update_response.status_code == 200:
            logger.info("✅ 文档已成功添加到工作区！")
            # 返回 docpath，供外部做“只保留最新”删除上一份
            return doc_api_path
        else:
            logger.error(f"❌ 添加到工作区失败: {update_response.text}")
            return None

    except Exception as e:
        logger.error(f"上传异常: {e}")
        return None

def main():
    """主函数"""
    logger.info("港口引航系统数据同步服务启动")

    # 检查依赖
    try:
        import pymysql
    except ImportError:
        logger.error("请安装pymysql: pip install pymysql")
        return

    import sys
    if len(sys.argv) > 1 and sys.argv[1] == "--once":
        logger.info("单次运行模式")
        perform_sync()
    elif len(sys.argv) > 1 and sys.argv[1] == "--full":
        logger.info("全量同步模式")
        global INCREMENTAL_SYNC
        INCREMENTAL_SYNC = False
        perform_sync()
    else:
        logger.info("定时运行模式")
        try:
            while True:
                perform_sync()
                logger.info(f"等待 {SYNC_INTERVAL} 秒...")
                time.sleep(SYNC_INTERVAL)
        except KeyboardInterrupt:
            logger.info("服务已停止")

if __name__ == "__main__":
    main()
