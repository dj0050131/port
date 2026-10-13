package syy.portPilotage.redis.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import syy.portPilotage.event.SystemLogEvent;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnBean(RedisTemplate.class)
public class RedisMessageListener implements MessageListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());
            log.info("接收到Redis消息: channel={}, message={}", channel, body);

            handleMessageByChannel(channel, body);
        } catch (Exception e) {
            log.error("处理Redis消息失败: {}", e.getMessage(), e);
        }
    }

    private void handleMessageByChannel(String channel, String body) {
        try {
            switch (channel) {
                case "user.login" -> handleUserLogin(body);
                case "user.logout" -> handleUserLogout(body);
                case "system.log" -> handleSystemLog(body);
                case "billing.complete" -> handleBillingComplete(body);
                default -> log.info("未知频道消息: channel={}, body={}", channel, body);
            }
        } catch (Exception e) {
            log.error("处理频道消息失败: channel={}, body={}", channel, body, e);
        }
    }

    private void handleUserLogin(String body) {
        log.info("处理用户登录事件: {}", body);
    }

    private void handleUserLogout(String body) {
        log.info("处理用户登出事件: {}", body);
    }

    private void handleSystemLog(String body) throws Exception {
        Map<String, String> logData = objectMapper.readValue(body, Map.class);
        String action = logData.get("action");
        String details = logData.get("details");
        log.info("处理系统日志事件: action={}, details={}", action, details);
    }

    private void handleBillingComplete(String body) {
        log.info("处理计费完成事件: {}", body);
    }
}
