package syy.portPilotage.redis.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.entity.Agents;
import syy.portPilotage.redis.RedisService;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RedisService.class)
public class AgentCacheService {

    @Autowired
    RedisService redisService;

    /**
     * 缓存代理公司信息
     * @param agent 代理公司实体
     */
    public void cacheAgent(Agents agent) {
        String key = RedisConstant.AGENT_CACHE + agent.getId();
        redisService.set(key, agent, RedisConstant.CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("缓存代理公司信息: agentId={}", agent.getId());
    }

    /**
     * 获取缓存的代理公司信息
     * @param agentId 代理公司ID
     * @return 代理公司实体
     */
    public Agents getCachedAgent(Long agentId) {
        String key = RedisConstant.AGENT_CACHE + agentId;
        return redisService.get(key, Agents.class);
    }

    /**
     * 删除缓存的代理公司信息
     * @param agentId 代理公司ID
     */
    public void removeCachedAgent(Long agentId) {
        String key = RedisConstant.AGENT_CACHE + agentId;
        redisService.delete(key);
        log.info("删除缓存的代理公司信息: agentId={}", agentId);
    }

    /**
     * 更新缓存的代理公司信息
     * @param agent 代理公司实体
     */
    public void updateCachedAgent(Agents agent) {
        removeCachedAgent(agent.getId());
        cacheAgent(agent);
        log.info("更新缓存的代理公司信息: agentId={}", agent.getId());
    }
}