package syy.portPilotage.redis;

import syy.portPilotage.constant.RedisConstant;

public class RedisKeyGenerator {
    
    public static String getUserInfoKey(Long userId) {
        return RedisConstant.USER_INFO + userId;
    }
    
    public static String getUserTokenKey(String username) {
        return RedisConstant.USER_TOKEN + username;
    }
    
    public static String getLoginFailCountKey(String username) {
        return RedisConstant.LOGIN_FAIL_COUNT + username;
    }
    
    public static String getAccountLockKey(String username) {
        return RedisConstant.ACCOUNT_LOCK + username;
    }
    
    public static String getCaptchaKey(String captchaKey) {
        return RedisConstant.CAPTCHA + captchaKey;
    }
    
    public static String getDictCacheKey(String dictType) {
        return RedisConstant.DICT_CACHE + dictType;
    }
    
    public static String getRateCacheKey(String rateType) {
        return RedisConstant.RATE_CACHE + rateType;
    }
    
    public static String getVesselCacheKey(Long vesselId) {
        return RedisConstant.VESSEL_CACHE + vesselId;
    }
    
    public static String getBerthCacheKey(Long berthId) {
        return RedisConstant.BERTH_CACHE + berthId;
    }
    
    public static String getPilotCacheKey(Long pilotId) {
        return RedisConstant.PILOT_CACHE + pilotId;
    }
    
    public static String getAgentCacheKey(Long agentId) {
        return RedisConstant.AGENT_CACHE + agentId;
    }
}
