package syy.portPilotage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import syy.portPilotage.rabbitmq.domain.EmailMessage;
import syy.portPilotage.rabbitmq.producer.EmailMessageProducer;

@SpringBootTest
@Disabled("需要本地基础设施（MySQL/Redis/RabbitMQ/SMTP）才能启动完整 Spring 容器；默认跳过以保证构建可用")
public class TestEmailFunctionTest {

    @Autowired
    private EmailMessageProducer emailProducer;

    @Test
    public void testSendEmail() throws InterruptedException {
        EmailMessage message = EmailMessage.builder()
                .to("test@example.com")
                .subject("引航信息通知")
                .content("<h2>引航计划详情</h2>" +
                        "<p><strong>计划编号:</strong> PH2023001234</p>" +
                        "<p><strong>船舶名称:</strong> 海洋先锋号</p>" +
                        "<p><strong>预计引航时间:</strong> 2023-10-15 09:00</p>" +
                        "<p><strong>泊位:</strong> 5#泊位</p>" +
                        "<p>请按时到达指定地点进行引航作业。</p>")
                .html(true)
                .build();

        System.out.println("测试邮件消息已发送至队列");

        Thread.sleep(5000);
    }
}