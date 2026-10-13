package syy.portPilotage.rabbitmq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RabbitMQConstant;
import syy.portPilotage.rabbitmq.domain.ExportMessage;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RabbitTemplate.class)
public class ExportMessageProducer {
    
    @Autowired
    RabbitTemplate rabbitTemplate;
    
    public void send(ExportMessage message) {
        try {
            log.info("发送导出消息到队列: exportType={}, format={}", message.getExportType(), message.getFormat());
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.EXCHANGE_NOTIFICATION,
                    "routing.export",
                    message
            );
        } catch (Exception e) {
            log.error("发送导出消息失败: {}", e.getMessage(), e);
        }
    }
}
