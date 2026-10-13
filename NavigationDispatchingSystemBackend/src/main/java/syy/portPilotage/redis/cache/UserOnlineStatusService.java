package syy.portPilotage.redis.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.redis.RedisService;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOnlineStatusService {

    @Autowired
    RedisService redisService;

    /**
     * 设置用户在线状态
     * @param userId 用户ID
     */
    public void setUserOnline(Long userId) {
        String key = RedisConstant.USER_ONLINE_STATUS + userId;
        redisService.set(key, "online", RedisConstant.ONLINE_STATUS_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("设置用户在线状态: userId={}", userId);
    }

    /**
     * 移除用户在线状态（相当于设为离线）
     * @param userId 用户ID
     */
    public void removeUserOnlineStatus(Long userId) {
        String key = RedisConstant.USER_ONLINE_STATUS + userId;
        redisService.delete(key);
        log.info("移除用户在线状态: userId={}", userId);
    }

    /**
     * 获取用户在线状态
     * @param userId 用户ID
     * @return 在线状态字符串（"online" 或 "offline"），如果不存在则返回"offline"
     */
    public String getUserOnlineStatus(Long userId) {
        String key = RedisConstant.USER_ONLINE_STATUS + userId;
        String status = redisService.get(key, String.class);
        return status != null ? status : "offline";
    }

    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        return "online".equals(getUserOnlineStatus(userId));
    }
}