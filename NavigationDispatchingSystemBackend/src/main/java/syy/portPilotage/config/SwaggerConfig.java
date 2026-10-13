package syy.portPilotage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("港口引航及计费系统API")
                        .version("1.0.0")
                        .description("港口引航及计费系统后端接口文档\n\n" +
                                "## 访问说明\n" +
                                "- Swagger UI: http://localhost:8099/swagger-ui.html\n" +
                                "- OpenAPI JSON: http://localhost:8099/v3/api-docs\n\n" +
                                "## 认证说明\n" +
                                "大部分接口需要JWT Token认证，请在登录接口获取Token后，在右上角点击" +
                                "Authorize按钮，输入: Bearer {your_token}")
                        .contact(new Contact()
                                .name("Port Pilotage")
                                .email("contact@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入JWT Token，格式: Bearer {token}")));
    }
}