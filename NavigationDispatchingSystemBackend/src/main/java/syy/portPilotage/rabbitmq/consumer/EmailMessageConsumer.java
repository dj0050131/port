package syy.portPilotage.rabbitmq.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import syy.portPilotage.constant.RabbitMQConstant;
import syy.portPilotage.rabbitmq.domain.EmailMessage;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class EmailMessageConsumer {
    
    @Autowired
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;
    
    @RabbitListener(queues = RabbitMQConstant.QUEUE_EMAIL)
    public void receive(EmailMessage message) {
        try {
            String messageId = System.identityHashCode(message) + "-" + System.currentTimeMillis();
            log.info("接收邮件消息[ID:{}]: to={}, subject={}", messageId, message.getTo(), message.getSubject());
            
            if (Boolean.TRUE.equals(message.getHtml())) {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setTo(message.getTo());
                helper.setFrom(mailUsername);
                helper.setSubject(message.getSubject());
                helper.setText(message.getContent(), true);
                mailSender.send(mimeMessage);
            } else {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(message.getTo());
                mailMessage.setFrom(mailUsername);
                mailMessage.setSubject(message.getSubject());
                mailMessage.setText(message.getContent());
                mailSender.send(mailMessage);
            }
            
            log.info("邮件发送成功[ID:{}]: to={}", messageId, message.getTo());
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage(), e);
        }
    }
}