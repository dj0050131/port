package syy.portPilotage.redis.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.entity.Vessels;
import syy.portPilotage.redis.RedisService;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RedisService.class)
public class VesselCacheService {

    @Autowired
    RedisService redisService;

    /**
     * 缓存船舶信息
     * @param vessel 船舶实体
     */
    public void cacheVessel(Vessels vessel) {
        String key = RedisConstant.VESSEL_CACHE + vessel.getId();
        redisService.set(key, vessel, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("缓存船舶信息: vesselId={}", vessel.getId());
    }

    /**
     * 获取缓存的船舶信息
     * @param vesselId 船舶ID
     * @return 船舶实体
     */
    public Vessels getCachedVessel(Long vesselId) {
        String key = RedisConstant.VESSEL_CACHE + vesselId;
        return redisService.get(key, Vessels.class);
    }

    /**
     * 删除缓存的船舶信息
     * @param vesselId 船舶ID
     */
    public void removeCachedVessel(Long vesselId) {
        String key = RedisConstant.VESSEL_CACHE + vesselId;
        redisService.delete(key);
        log.info("删除缓存的船舶信息: vesselId={}", vesselId);
    }

    /**
     * 更新缓存的船舶信息
     * @param vessel 船舶实体
     */
    public void updateCachedVessel(Vessels vessel) {
        removeCachedVessel(vessel.getId());
        cacheVessel(vessel);
        log.info("更新缓存的船舶信息: vesselId={}", vessel.getId());
    }
}