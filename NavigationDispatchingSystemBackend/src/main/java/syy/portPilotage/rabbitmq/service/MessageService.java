package syy.portPilotage.rabbitmq.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.rabbitmq.domain.*;
import syy.portPilotage.rabbitmq.producer.*;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    @Autowired
    private SmsMessageProducer smsMessageProducer;

    @Autowired
    private EmailMessageProducer emailMessageProducer;

    @Autowired
    private BillingMessageProducer billingMessageProducer;

    @Autowired
    private PlanNotifyMessageProducer planNotifyMessageProducer;

    @Autowired
    private CertificateReminderProducer certificateReminderProducer;

    public void sendSms(String phone, String content) {
        SmsMessage message = SmsMessage.builder()
                .phone(phone)
                .content(content)
                .build();
        smsMessageProducer.send(message);
    }

    public void sendEmail(String to, String subject, String content, boolean isHtml) {
        EmailMessage message = EmailMessage.builder()
                .to(to)
                .subject(subject)
                .content(content)
                .html(isHtml)
                .build();
        emailMessageProducer.send(message);
    }

    public void sendBillingMessage(Long certificateId, String action, String message) {
        BillingMessage billingMessage = BillingMessage.builder()
                .certificateId(certificateId)
                .action(action)
                .message(message)
                .build();
        billingMessageProducer.send(billingMessage);
    }

    public void sendPlanNotifyMessage(Long planId, String planCode, Long userId, String userName,
                                      String action, String message) {
        PlanNotifyMessage planNotifyMessage = PlanNotifyMessage.builder()
                .planId(planId)
                .planCode(planCode)
                .userId(userId)
                .userName(userName)
                .action(action)
                .message(message)
                .build();
        planNotifyMessageProducer.send(planNotifyMessage);
    }

    public void sendCertificateReminder(Long certificateId, String certificateCode, Long agentId,
                                        String agentName, String reminderType, String message) {
        CertificateReminderMessage certificateReminderMessage = CertificateReminderMessage.builder()
                .certificateId(certificateId)
                .certificateCode(certificateCode)
                .agentId(agentId)
                .agentName(agentName)
                .reminderType(reminderType)
                .message(message)
                .build();
        certificateReminderProducer.send(certificateReminderMessage);
    }
}