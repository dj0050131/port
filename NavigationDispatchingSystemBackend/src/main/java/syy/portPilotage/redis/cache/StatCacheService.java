package syy.portPilotage.redis.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.redis.RedisService;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RedisService.class)
public class StatCacheService {

    @Autowired
    RedisService redisService;

    /**
     * 缓存统计结果
     * @param statType 统计类型
     * @param statData 统计数据
     */
    public void cacheStatResult(String statType, Object statData) {
        String key = RedisConstant.STAT_CACHE + statType;
        redisService.set(key, statData, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("缓存统计结果: statType={}", statType);
    }

    /**
     * 获取缓存的统计结果
     * @param statType 统计类型
     * @return 统计数据
     */
    public Object getCachedStatResult(String statType) {
        String key = RedisConstant.STAT_CACHE + statType;
        return redisService.get(key);
    }

    /**
     * 获取缓存的统计结果并转换为指定类型
     * @param statType 统计类型
     * @param clazz 类型Class
     * @return 指定类型的统计数据
     */
    public <T> T getCachedStatResult(String statType, Class<T> clazz) {
        String key = RedisConstant.STAT_CACHE + statType;
        return redisService.get(key, clazz);
    }

    /**
     * 删除缓存的统计结果
     * @param statType 统计类型
     */
    public void removeCachedStatResult(String statType) {
        String key = RedisConstant.STAT_CACHE + statType;
        redisService.delete(key);
        log.info("删除缓存的统计结果: statType={}", statType);
    }

    /**
     * 缓存Map类型的统计结果
     * @param statType 统计类型
     * @param statMap 统计Map
     */
    public void cacheStatMap(String statType, Map<String, Object> statMap) {
        String key = RedisConstant.STAT_CACHE + statType;
        redisService.set(key, statMap, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("缓存统计Map: statType={}", statType);
    }
}