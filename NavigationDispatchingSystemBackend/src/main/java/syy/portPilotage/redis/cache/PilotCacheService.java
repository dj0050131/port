package syy.portPilotage.redis.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.entity.Pilots;
import syy.portPilotage.redis.RedisService;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RedisService.class)
public class PilotCacheService {

    @Autowired
    RedisService redisService;

    /**
     * 缓存引航员信息
     * @param pilot 引航员实体
     */
    public void cachePilot(Pilots pilot) {
        String key = RedisConstant.PILOT_CACHE + pilot.getId();
        redisService.set(key, pilot, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("缓存引航员信息: pilotId={}", pilot.getId());
    }

    /**
     * 获取缓存的引航员信息
     * @param pilotId 引航员ID
     * @return 引航员实体
     */
    public Pilots getCachedPilot(Long pilotId) {
        String key = RedisConstant.PILOT_CACHE + pilotId;
        return redisService.get(key, Pilots.class);
    }

    /**
     * 删除缓存的引航员信息
     * @param pilotId 引航员ID
     */
    public void removeCachedPilot(Long pilotId) {
        String key = RedisConstant.PILOT_CACHE + pilotId;
        redisService.delete(key);
        log.info("删除缓存的引航员信息: pilotId={}", pilotId);
    }

    /**
     * 更新缓存的引航员信息
     * @param pilot 引航员实体
     */
    public void updateCachedPilot(Pilots pilot) {
        removeCachedPilot(pilot.getId());
        cachePilot(pilot);
        log.info("更新缓存的引航员信息: pilotId={}", pilot.getId());
    }
}