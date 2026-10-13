package syy.portPilotage.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@RequiredArgsConstructor
public class RedisService {
    
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    // 释放锁
    private static final String LOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end";

    // 抢锁
    private static final String ACQUIRE_LOCK_SCRIPT =
            "if redis.call('exists', KEYS[1]) == 0 then " +
                    "return redis.call('set', KEYS[1], ARGV[1], 'EX', ARGV[2]) " +
                    "else return 0 end";

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 获取指定key的值并转换为指定类型
     * @param key 键
     * @param clazz 类型Class
     * @param <T> 泛型类型
     * @return 指定类型的值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (T) value : null;
    }
    
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
    
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }
    
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }
    
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
    
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }
    
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }
    
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    public Long leftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    public Long rightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public List<Object> range(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    public void trim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    public Long size(String key) {
        return redisTemplate.opsForList().size(key);
    }

    public boolean tryLock(String lockKey, String lockValue, long expireTime) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(ACQUIRE_LOCK_SCRIPT);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script,
                Collections.singletonList(lockKey),
                lockValue,
                String.valueOf(expireTime));
        return result != null && result == 1L;
    }

    public boolean releaseLock(String lockKey, String lockValue) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(LOCK_SCRIPT);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script,
                Collections.singletonList(lockKey),
                lockValue);
        return result != null && result == 1L;
    }

    public Long incrementWithExpire(String key, long windowSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return count;
    }
    
    public Set<String> getKeysByPattern(String pattern) {
        Set<Object> keys = redisTemplate.keys(pattern);
        if (keys == null) {
            return null;
        }
        return keys.stream()
                .map(key -> key != null ? key.toString() : null)
                .collect(Collectors.toSet());
    }
}