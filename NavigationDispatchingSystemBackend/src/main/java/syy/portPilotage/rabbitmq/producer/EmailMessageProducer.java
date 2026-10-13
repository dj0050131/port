package syy.portPilotage.rabbitmq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RabbitMQConstant;
import syy.portPilotage.rabbitmq.domain.EmailMessage;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RabbitTemplate.class)
public class EmailMessageProducer {
    
    @Autowired
    RabbitTemplate rabbitTemplate;
    
    public void send(EmailMessage message) {
        try {
            log.info("发送邮件消息到队列: to={}, subject={}", message.getTo(), message.getSubject());
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.EXCHANGE_NOTIFICATION,
                    RabbitMQConstant.ROUTING_KEY_EMAIL,
                    message
            );
        } catch (Exception e) {
            log.error("发送邮件消息失败: {}", e.getMessage(), e);
        }
    }
}