package syy.portPilotage.constant;

public class RedisConstant {
    
    public static final String KEY_PREFIX = "port_pilotage:";// Redis key 前缀
    public static final String USER_INFO = KEY_PREFIX + "user:info:";// 用户信息
    public static final String USER_TOKEN = KEY_PREFIX + "user:token:";// 用户Token
    public static final String USER_ONLINE_STATUS = KEY_PREFIX + "user:online:";// 用户在线状态
    public static final String LOGIN_FAIL_COUNT = KEY_PREFIX + "login:fail:";// 登录失败次数
    public static final String ACCOUNT_LOCK = KEY_PREFIX + "account:lock:";// 账户锁定
    public static final String CAPTCHA = KEY_PREFIX + "captcha:";
    public static final String DICT_CACHE = KEY_PREFIX + "dict:";// 字典缓存
    public static final String RATE_CACHE = KEY_PREFIX + "rate:";// 费率缓存
    public static final String VESSEL_CACHE = KEY_PREFIX + "vessel:";// 船舶缓存
    public static final String BERTH_CACHE = KEY_PREFIX + "berth:";// 泊位缓存
    public static final String PILOT_CACHE = KEY_PREFIX + "pilot:";// 引航员缓存
    public static final String AGENT_CACHE = KEY_PREFIX + "agent:";// 代理缓存
    public static final String PLAN_NOTIFY = KEY_PREFIX + "plan:notify:";// 计划通知
    public static final String STAT_CACHE = KEY_PREFIX + "stat:";
    
    public static final long CACHE_EXPIRE_SECONDS = 3600;// 缓存过期时间(秒)
    public static final long TOKEN_EXPIRE_SECONDS = 14400;// Token过期时间(秒)
    public static final long ONLINE_STATUS_EXPIRE_SECONDS = 1800;// 在线状态过期时间(秒)
    public static final long CAPTCHA_EXPIRE_SECONDS = 300;
}