package syy.portPilotage.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.redis.RedisService;
import syy.portPilotage.redis.cache.UserOnlineStatusService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * 用户在线状态清理任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserOnlineStatusTask {

    @Autowired
    private RedisService redisService;
    
    @Autowired
    private UserOnlineStatusService userOnlineStatusService;

        @Scheduled(fixedRate = 600000) // 10分钟执行一次
    public void cleanExpiredOnlineStatus() {
        log.info("开始清理过期的用户在线状态");
        
        try {
            String pattern = RedisConstant.USER_ONLINE_STATUS + "*";
            Set<String> keys = redisService.getKeysByPattern(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                int cleanedCount = 0;
                
                for (String key : keys) {
                    Long ttl = redisService.getExpire(key);
                    
                    // 如果TTL小于等于0，说明已经过期，但实际可能因为Redis的惰性删除机制而未删除
                    // 所以我们主动检查并删除
                    if (ttl != null && ttl <= 0) {
                        redisService.delete(key);
                        cleanedCount++;
                    }
                }
                
                log.info("清理完成，共清理 {} 个过期的在线状态记录", cleanedCount);
            } else {
                log.info("没有找到用户在线状态记录");
            }
        } catch (Exception e) {
            log.error("清理用户在线状态时发生异常", e);
        }
    }
}