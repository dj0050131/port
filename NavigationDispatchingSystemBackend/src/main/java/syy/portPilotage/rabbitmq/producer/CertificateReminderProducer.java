package syy.portPilotage.rabbitmq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RabbitMQConstant;
import syy.portPilotage.rabbitmq.domain.CertificateReminderMessage;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RabbitTemplate.class)
public class CertificateReminderProducer {

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void send(CertificateReminderMessage message) {
        try {
            log.info("发送签证单提醒消息到队列: certificateId={}, reminderType={}",
                    message.getCertificateId(), message.getReminderType());
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.EXCHANGE_PILOTAGE,
                    RabbitMQConstant.ROUTING_KEY_CERTIFICATE,
                    message
            );
        } catch (Exception e) {
            log.error("发送签证单提醒消息失败: {}", e.getMessage(), e);
        }
    }
}