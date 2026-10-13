package syy.portPilotage.redis.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.entity.Users;
import syy.portPilotage.redis.RedisService;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RedisService.class)
public class UserCacheService {
    
    @Autowired
    RedisService redisService;
    
    public void cacheUser(Users user) {
        String key = RedisConstant.USER_INFO + user.getId();
        redisService.set(key, user, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }
    
    public Users getCachedUser(Long userId) {
        String key = RedisConstant.USER_INFO + userId;
        return redisService.get(key, Users.class);
    }
    
    public void removeCachedUser(Long userId) {
        String key = RedisConstant.USER_INFO + userId;
        redisService.delete(key);
    }
}
