package syy.portPilotage.redis.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.entity.StandardRates;
import syy.portPilotage.redis.RedisService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RedisService.class)
public class RateCacheService {

    @Autowired
    RedisService redisService;

    public void cacheRateList(List<StandardRates> rateList) {
        String key = RedisConstant.RATE_CACHE + "all";
        redisService.set(key, rateList, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public List<StandardRates> getCachedRateList() {
        String key = RedisConstant.RATE_CACHE + "all";
        return (List<StandardRates>) redisService.get(key);
    }

    public void removeCachedRate() {
        String key = RedisConstant.RATE_CACHE + "all";
        redisService.delete(key);
    }
}
