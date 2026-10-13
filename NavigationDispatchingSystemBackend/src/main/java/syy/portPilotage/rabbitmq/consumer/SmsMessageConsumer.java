package syy.portPilotage.rabbitmq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RabbitMQConstant;
import syy.portPilotage.rabbitmq.domain.SmsMessage;
import syy.portPilotage.util.SmsUtil;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class SmsMessageConsumer {
    
    @RabbitListener(queues = RabbitMQConstant.QUEUE_SMS)
    public void receive(SmsMessage message) {
        try {
            log.info("接收短信消息: phone={}", message.getPhone());
            SmsUtil.sendSms(message.getPhone(), message.getContent());
            log.info("短信发送成功: phone={}", message.getPhone());
        } catch (Exception e) {
            log.error("短信发送失败: {}", e.getMessage(), e);
        }
    }
}
