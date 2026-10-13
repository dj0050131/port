package syy.portPilotage.rabbitmq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RabbitMQConstant;
import syy.portPilotage.rabbitmq.domain.CertificateReminderMessage;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class CertificateReminderConsumer {

    @RabbitListener(queues = RabbitMQConstant.QUEUE_CERTIFICATE_REMINDER)
    public void receive(CertificateReminderMessage message) {
        try {
            log.info("接收签证单提醒消息: certificateId={}, reminderType={}",
                    message.getCertificateId(), message.getReminderType());

            switch (message.getReminderType()) {
                case "SUBMIT_REMINDER" -> handleSubmitReminder(message);
                case "APPROVE_REMINDER" -> handleApproveReminder(message);
                case "EXPIRE_REMINDER" -> handleExpireReminder(message);
                default -> log.warn("未知的提醒类型: {}", message.getReminderType());
            }

            log.info("签证单提醒处理成功: certificateId={}", message.getCertificateId());
        } catch (Exception e) {
            log.error("签证单提醒处理失败: {}", e.getMessage(), e);
        }
    }

    private void handleSubmitReminder(CertificateReminderMessage message) {
        log.info("处理签证单提交提醒: certificateCode={}", message.getCertificateCode());
    }

    private void handleApproveReminder(CertificateReminderMessage message) {
        log.info("处理签证单审批提醒: certificateCode={}", message.getCertificateCode());
    }

    private void handleExpireReminder(CertificateReminderMessage message) {
        log.info("处理签证单过期提醒: certificateCode={}", message.getCertificateCode());
    }
}
