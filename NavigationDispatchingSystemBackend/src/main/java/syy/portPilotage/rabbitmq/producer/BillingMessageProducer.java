package syy.portPilotage.rabbitmq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RabbitMQConstant;
import syy.portPilotage.rabbitmq.domain.BillingMessage;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RabbitTemplate.class)
public class BillingMessageProducer {
    
    @Autowired
    RabbitTemplate rabbitTemplate;
    
    public void send(BillingMessage message) {
        try {
            log.info("发送计费消息到队列: certificateId={}", message.getCertificateId());
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.EXCHANGE_BILLING,
                    RabbitMQConstant.ROUTING_KEY_BILLING,
                    message
            );
        } catch (Exception e) {
            log.error("发送计费消息失败: {}", e.getMessage(), e);
        }
    }
}
