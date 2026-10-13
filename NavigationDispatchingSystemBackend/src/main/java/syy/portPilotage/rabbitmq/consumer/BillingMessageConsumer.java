package syy.portPilotage.rabbitmq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RabbitMQConstant;
import syy.portPilotage.rabbitmq.domain.BillingMessage;
import syy.portPilotage.service.BillingCalculationService;
import syy.portPilotage.service.PilotageCertificatesService;
import syy.portPilotage.entity.PilotageCertificates;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class BillingMessageConsumer {

    @Autowired
    private BillingCalculationService billingCalculationService;
    
    @Autowired
    private PilotageCertificatesService pilotageCertificatesService;

    @RabbitListener(queues = RabbitMQConstant.QUEUE_BILLING_CALCULATE)
    public void receive(BillingMessage message) {
        try {
            log.info("接收计费消息: certificateId={}, action={}", message.getCertificateId(), message.getAction());

            switch (message.getAction()) {
                case "CALCULATE" -> handleBillingCalculate(message);
                case "GENERATE_VOUCHER" -> handleGenerateVoucher(message);
                default -> log.warn("未知的计费操作类型: {}", message.getAction());
            }

            log.info("计费消息处理成功: certificateId={}", message.getCertificateId());
        } catch (Exception e) {
            log.error("计费消息处理失败: certificateId={}, error={}", message.getCertificateId(), e.getMessage(), e);
            throw e;
        }
    }

    private void handleBillingCalculate(BillingMessage message) {
        log.info("处理计费计算: certificateId={}", message.getCertificateId());
        if (message.getCertificateId() == null) {
            log.warn("计费计算消息缺少 certificateId");
            return;
        }
        
        PilotageCertificates certificate = pilotageCertificatesService.getById(message.getCertificateId());
        if (certificate == null) {
            log.warn("未找到签证单: certificateId={}", message.getCertificateId());
            return;
        }
        
        billingCalculationService.generateBillingVoucherFromCertificate(certificate);
        log.info("计费计算完成: certificateId={}", message.getCertificateId());
    }

    private void handleGenerateVoucher(BillingMessage message) {
        log.info("处理生成计费凭证: certificateId={}", message.getCertificateId());
        if (message.getCertificateId() == null) {
            log.warn("生成凭证消息缺少 certificateId");
            return;
        }
        
        PilotageCertificates certificate = pilotageCertificatesService.getById(message.getCertificateId());
        if (certificate == null) {
            log.warn("未找到签证单: certificateId={}", message.getCertificateId());
            return;
        }
        
        billingCalculationService.generateBillingVoucherFromCertificate(certificate);
        log.info("计费凭证生成完成: certificateId={}", message.getCertificateId());
    }
}
