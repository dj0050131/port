package syy.portPilotage;

import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableRabbit
@MapperScan("syy.portPilotage.mapper")
public class PortPilotageApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PortPilotageApplication.class, args);
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8099");
        
        System.out.println("========================================");
        System.out.println("港口引航及计费系统启动成功!");
        System.out.println("服务端口: " + port);
        System.out.println("API文档地址: http://localhost:" + port + "/swagger-ui.html");
        System.out.println("API文档地址(备用): http://localhost:" + port + "/swagger-ui/index.html");
        System.out.println("OpenAPI JSON: http://localhost:" + port + "/v3/api-docs");
        System.out.println("========================================");
    }
    @PostConstruct
    public void checkConfigs() {
        System.out.println("=== 检查配置类加载 ===");
    }
}