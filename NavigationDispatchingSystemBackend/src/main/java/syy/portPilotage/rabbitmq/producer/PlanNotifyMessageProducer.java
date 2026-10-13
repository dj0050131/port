package syy.portPilotage.rabbitmq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RabbitMQConstant;
import syy.portPilotage.rabbitmq.domain.PlanNotifyMessage;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RabbitTemplate.class)
public class PlanNotifyMessageProducer {

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void send(PlanNotifyMessage message) {
        try {
            log.info("发送计划通知消息到队列: planId={}, action={}", message.getPlanId(), message.getAction());
            rabbitTemplate.convertAndSend(
                    RabbitMQConstant.EXCHANGE_PILOTAGE,
                    RabbitMQConstant.ROUTING_KEY_PLAN_NOTIFY,
                    message
            );
        } catch (Exception e) {
            log.error("发送计划通知消息失败: {}", e.getMessage(), e);
        }
    }
}