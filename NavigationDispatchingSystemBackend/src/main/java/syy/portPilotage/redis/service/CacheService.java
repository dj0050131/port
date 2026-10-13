package syy.portPilotage.redis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.redis.RedisService;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisService redisService;

        public <T> T get(String key, Class<T> clazz) {
        return redisService.get(key, clazz);
    }

        public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisService.set(key, value, timeout, unit);
    }

        public void set(String key, Object value) {
        redisService.set(key, value, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

        public void delete(String key) {
        redisService.delete(key);
    }

        public void deleteByPattern(String pattern) {
        try {
            var keys = redisService.getKeysByPattern(pattern);
            if (keys != null && !keys.isEmpty()) {
                keys.forEach(redisService::delete);
                log.debug("批量删除缓存: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.warn("批量删除缓存失败: pattern={}, error={}", pattern, e.getMessage());
        }
    }

        public boolean hasKey(String key) {
        return redisService.hasKey(key);
    }

        public <T> T getVessel(Long vesselId, Class<T> clazz) {
        String key = RedisConstant.VESSEL_CACHE + vesselId;
        return redisService.get(key, clazz);
    }

        public void setVessel(Long vesselId, Object value) {
        String key = RedisConstant.VESSEL_CACHE + vesselId;
        redisService.set(key, value, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

        public void deleteVessel(Long vesselId) {
        String key = RedisConstant.VESSEL_CACHE + vesselId;
        redisService.delete(key);
    }

        public <T> T getAgent(Long agentId, Class<T> clazz) {
        String key = RedisConstant.AGENT_CACHE + agentId;
        return redisService.get(key, clazz);
    }

        public void setAgent(Long agentId, Object value) {
        String key = RedisConstant.AGENT_CACHE + agentId;
        redisService.set(key, value, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

        public void deleteAgent(Long agentId) {
        String key = RedisConstant.AGENT_CACHE + agentId;
        redisService.delete(key);
    }

        public <T> T getPilot(Long pilotId, Class<T> clazz) {
        String key = RedisConstant.PILOT_CACHE + pilotId;
        return redisService.get(key, clazz);
    }

        public void setPilot(Long pilotId, Object value) {
        String key = RedisConstant.PILOT_CACHE + pilotId;
        redisService.set(key, value, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

        public void deletePilot(Long pilotId) {
        String key = RedisConstant.PILOT_CACHE + pilotId;
        redisService.delete(key);
    }

        public <T> T getBerth(Long berthId, Class<T> clazz) {
        String key = RedisConstant.BERTH_CACHE + berthId;
        return redisService.get(key, clazz);
    }

        public void setBerth(Long berthId, Object value) {
        String key = RedisConstant.BERTH_CACHE + berthId;
        redisService.set(key, value, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

        public void deleteBerth(Long berthId) {
        String key = RedisConstant.BERTH_CACHE + berthId;
        redisService.delete(key);
    }

        public <T> T getRate(String rateKey, Class<T> clazz) {
        String key = RedisConstant.RATE_CACHE + rateKey;
        return redisService.get(key, clazz);
    }

        public void setRate(String rateKey, Object value) {
        String key = RedisConstant.RATE_CACHE + rateKey;
        redisService.set(key, value, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

        public void deleteRate(String rateKey) {
        String key = RedisConstant.RATE_CACHE + rateKey;
        redisService.delete(key);
    }

    
    public void clearAllRates() {
        deleteByPattern(RedisConstant.RATE_CACHE + "*");
    }

        public <T> T getStat(String statKey, Class<T> clazz) {
        String key = RedisConstant.STAT_CACHE + statKey;
        return redisService.get(key, clazz);
    }

        public void setStat(String statKey, Object value) {
        String key = RedisConstant.STAT_CACHE + statKey;
        redisService.set(key, value, 300, TimeUnit.SECONDS);
    }

        public void deleteStat(String statKey) {
        String key = RedisConstant.STAT_CACHE + statKey;
        redisService.delete(key);
    }
}
