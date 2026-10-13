package syy.portPilotage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("需要本地基础设施（MySQL/Redis/RabbitMQ）才能启动完整 Spring 容器；默认跳过以保证构建可用")
class PortPilotageApplicationTests {

    @Test
    void contextLoads() {
    }
}
