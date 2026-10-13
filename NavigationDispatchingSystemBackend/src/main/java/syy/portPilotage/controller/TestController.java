package syy.portPilotage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.Result;

@Tag(name = "测试接口", description = "用于测试系统是否正常运行")
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    @Operation(summary = "测试接口", description = "返回Hello World，用于验证系统是否正常运行")
    public Result<String> hello() {
        return Result.success("Hello World! 系统运行正常！");
    }

    @GetMapping("/swagger-check")
    @Operation(summary = "Swagger检查", description = "检查Swagger配置是否正确")
    public Result<String> swaggerCheck() {
        return Result.success("Swagger配置正常！可以访问 /swagger-ui.html 查看API文档");
    }
}
