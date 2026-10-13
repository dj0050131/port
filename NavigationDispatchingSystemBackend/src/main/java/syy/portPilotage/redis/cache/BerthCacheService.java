package syy.portPilotage.redis.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.entity.Berths;
import syy.portPilotage.redis.RedisService;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RedisService.class)
public class BerthCacheService {

    @Autowired
    RedisService redisService;

    /**
     * 缓存泊位信息
     * @param berth 泊位实体
     */
    public void cacheBerth(Berths berth) {
        String key = RedisConstant.BERTH_CACHE + berth.getId();
        redisService.set(key, berth, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("缓存泊位信息: berthId={}", berth.getId());
    }

    /**
     * 获取缓存的泊位信息
     * @param berthId 泊位ID
     * @return 泊位实体
     */
    public Berths getCachedBerth(Long berthId) {
        String key = RedisConstant.BERTH_CACHE + berthId;
        return redisService.get(key, Berths.class);
    }

    /**
     * 删除缓存的泊位信息
     * @param berthId 泊位ID
     */
    public void removeCachedBerth(Long berthId) {
        String key = RedisConstant.BERTH_CACHE + berthId;
        redisService.delete(key);
        log.info("删除缓存的泊位信息: berthId={}", berthId);
    }

    /**
     * 更新缓存的泊位信息
     * @param berth 泊位实体
     */
    public void updateCachedBerth(Berths berth) {
        removeCachedBerth(berth.getId());
        cacheBerth(berth);
        log.info("更新缓存的泊位信息: berthId={}", berth.getId());
    }
}