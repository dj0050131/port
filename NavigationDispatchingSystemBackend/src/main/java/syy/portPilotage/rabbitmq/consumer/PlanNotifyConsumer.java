package syy.portPilotage.rabbitmq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RabbitMQConstant;
import syy.portPilotage.rabbitmq.domain.PlanNotifyMessage;
import syy.portPilotage.util.SmsUtil;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class PlanNotifyConsumer {

    @RabbitListener(queues = RabbitMQConstant.QUEUE_PLAN_NOTIFY)
    public void receive(PlanNotifyMessage message) {
        try {
            log.info("接收计划通知消息: planId={}, action={}", message.getPlanId(), message.getAction());

            switch (message.getAction()) {
                case "CREATE":
                    handlePlanCreate(message);
                    break;
                case "UPDATE":
                    handlePlanUpdate(message);
                    break;
                case "CONFIRM":
                    handlePlanConfirm(message);
                    break;
                default:
                    log.warn("未知的计划操作类型: {}", message.getAction());
            }

            log.info("计划通知消息处理成功: planId={}", message.getPlanId());
        } catch (Exception e) {
            log.error("计划通知消息处理失败: {}", e.getMessage(), e);
        }
    }

    private void handlePlanCreate(PlanNotifyMessage message) {
        log.info("处理引航计划创建通知: planCode={}", message.getPlanCode());
    }

    private void handlePlanUpdate(PlanNotifyMessage message) {
        log.info("处理引航计划更新通知: planCode={}", message.getPlanCode());
    }

    private void handlePlanConfirm(PlanNotifyMessage message) {
        log.info("处理引航计划确认通知: planCode={}", message.getPlanCode());
    }
}