package syy.portPilotage.redis.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.redis.RedisService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RedisService.class)
public class DictCacheService {

    @Autowired
    RedisService redisService;

    public void cacheDictList(String dictType, List<?> dictList) {
        String key = RedisConstant.DICT_CACHE + dictType;
        redisService.set(key, dictList, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getCachedDictList(String dictType, Class<T> clazz) {
        String key = RedisConstant.DICT_CACHE + dictType;
        return (List<T>) redisService.get(key);
    }

    public void removeCachedDict(String dictType) {
        String key = RedisConstant.DICT_CACHE + dictType;
        redisService.delete(key);
    }
}
