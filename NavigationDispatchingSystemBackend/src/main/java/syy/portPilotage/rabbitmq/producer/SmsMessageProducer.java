package syy.portPilotage.rabbitmq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RabbitMQConstant;
import syy.portPilotage.rabbitmq.domain.SmsMessage;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RabbitTemplate.class)
public class SmsMessageProducer {
    
    @Autowired
    RabbitTemplate rabbitTemplate;
    
    public void send(SmsMessage message) {
        try {
            log.info("发送短信消息到队列: phone={}", message.getPhone());
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.EXCHANGE_NOTIFICATION,
                    RabbitMQConstant.ROUTING_KEY_SMS,
                    message
            );
        } catch (Exception e) {
            log.error("发送短信消息失败: {}", e.getMessage(), e);
        }
    }
}