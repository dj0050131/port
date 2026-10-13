package syy.portPilotage.service.screen;

import syy.portPilotage.common.Result;

import java.util.List;
import java.util.Map;

public interface ConsoleService {

    /**
     * 获取系统日志
     * @return 系统日志数据列表
     */
    Result<List<Map<String, Object>>> getSystemLogs();

    /**
     * 获取用户统计信息
     * @return 用户统计数据映射
     */
    Result<Map<String, Object>> getUserStats();
}