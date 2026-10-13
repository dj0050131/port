package syy.portPilotage.constant;

public class SecurityConstant {
    

    public static final String TOKEN_HEADER = "Authorization";// JWT令牌头
    public static final String TOKEN_PREFIX = "Bearer ";// 令牌前缀
    public static final String LOGIN_URL = "/api/auth/login";
    public static final String REGISTER_URL = "/api/auth/register";
    
    public static final String[] WHITE_LIST = {
        "/api/auth/**",
        "/swagger-ui/**",// Swagger UI资源
        "/swagger-ui.html",// Swagger首页（Springdoc兼容路径）
        "/swagger-ui/index.html",// Swagger UI首页
        "/v3/api-docs/**",// OpenAPI 3.0 文档
        "/v3/api-docs",// OpenAPI JSON
        "/swagger-resources/**",// Swagger资源（兼容旧版）
        "/webjars/**",// Webjars静态资源
        "/actuator/**",// Spring Boot Actuator端点
        "/health",           // 确保包含健康检查
        "/test",             // 确保包含测试接口
        "/api/monitor/dashboard/**", // 大屏数据接口
        "/api/public/**",     // 公共数据接口

        // 模拟外部系统与回调（内部联调使用）
        "/api/billing/simulation/**",
        "/api/billing/charges/paymentCallback/**",
        "/api/billing/charges/transferCallback/**",
        "/api/files/raw/**",
        "/uploads/**"
    };// 白名单路径数组
    
    public static final String ROLE_PREFIX = "ROLE_";// 角色前缀
    public static final String DEFAULT_PASSWORD = "";
    public static final int MAX_LOGIN_ATTEMPTS = 5;// 最大登录尝试次数
    public static final int LOCK_DURATION_MINUTES = 30;// 账户锁定时长(分钟)
}