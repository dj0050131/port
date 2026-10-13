package syy.portPilotage.constant;

public class SystemConstant {
    
    public static final String SYSTEM_NAME = "港口引航及计费系统";// 系统名称
    public static final String SYSTEM_VERSION = "1.0.0";// 系统版本
    public static final int DEFAULT_PAGE_SIZE = 10;// 默认分页大小
    public static final int MAX_PAGE_SIZE = 100;// 最大分页大小
    public static final String DATE_FORMAT = "yyyy-MM-dd";// 日期格式
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";// 日期时间格式
    public static final String TIME_FORMAT = "HH:mm:ss";// 时间格式
    public static final String UTF8 = "UTF-8";// 字符编码
    public static final Integer SUCCESS = 200;// 成功状态码
    public static final Integer FAIL = 500;// 失败状态码
    public static final int EXCEL_MAX_ROWS = 10000;// Excel最大行数
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;// 最大文件大小(10MB)
    public static final String[] ALLOWED_FILE_TYPES = {
        ".jpg", ".jpeg", ".png", ".gif", ".pdf", ".doc", ".docx", ".xls", ".xlsx"
    };// 允许上传的文件类型
}