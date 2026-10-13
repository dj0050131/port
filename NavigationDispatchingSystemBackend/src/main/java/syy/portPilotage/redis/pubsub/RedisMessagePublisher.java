package syy.portPilotage.redis.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RedisTemplate.class)
public class RedisMessagePublisher {

    @Autowired
    RedisTemplate<Object, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void publish(String channel, Object message) {
        try {
            redisTemplate.convertAndSend(channel, message);
            log.info("Redis消息发布成功: channel={}", channel);
        } catch (Exception e) {
            log.error("Redis消息发布失败: {}", e.getMessage(), e);
        }
    }

    public void publishUserLogin(String username) {
        Map<String, Object> message = new HashMap<>();
        message.put("username", username);
        message.put("timestamp", System.currentTimeMillis());
        message.put("event", "login");

        publish("user.login", message);
    }

    public void publishUserLogout(String username) {
        Map<String, Object> message = new HashMap<>();
        message.put("username", username);
        message.put("timestamp", System.currentTimeMillis());
        message.put("event", "logout");

        publish("user.logout", message);
    }

    public void publishSystemLog(String action, String details) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("action", action);
            message.put("details", details);
            message.put("timestamp", System.currentTimeMillis());

            publish("system.log", objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.error("发布系统日志消息失败: {}", e.getMessage(), e);
        }
    }

    public void publishBillingComplete(Long billId, Double amount) {
        Map<String, Object> message = new HashMap<>();
        message.put("billId", billId);
        message.put("amount", amount);
        message.put("timestamp", System.currentTimeMillis());
        message.put("event", "billing_complete");

        publish("billing.complete", message);
    }
}
