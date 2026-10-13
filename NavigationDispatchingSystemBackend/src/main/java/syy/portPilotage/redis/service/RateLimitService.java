package syy.portPilotage.redis.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.redis.RedisService;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    @Autowired
    private RedisService redisService;

    public boolean isRateLimited(String key, int maxRequests, int windowSeconds) {
        Long currentRequests = redisService.incrementWithExpire(key, windowSeconds);
        if (currentRequests != null && currentRequests > maxRequests) {
            log.warn("访问频率超限: key={}, currentRequests={}, maxRequests={}",
                    key, currentRequests, maxRequests);
            return true;
        }
        return false;
    }

    public boolean isApiRateLimited(String apiKey, String clientId, int maxRequests, int windowSeconds) {
        String key = "api_rate_limit:" + apiKey + ":" + clientId;
        return isRateLimited(key, maxRequests, windowSeconds);
    }

    public boolean isUserOperationRateLimited(Long userId, String operation, int maxRequests, int windowSeconds) {
        String key = "user_operation_limit:" + userId + ":" + operation;
        return isRateLimited(key, maxRequests, windowSeconds);
    }
}