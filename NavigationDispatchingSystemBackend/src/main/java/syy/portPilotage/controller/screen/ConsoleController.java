package syy.portPilotage.controller.screen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.Result;
import syy.portPilotage.service.screen.ConsoleService;

import java.util.List;
import java.util.Map;

@Tag(name = "控制台数据接口", description = "控制台数据相关接口")
@RestController
@RequestMapping("/api/monitor/console")
public class ConsoleController {

    @Autowired
    private ConsoleService consoleService;

    @Operation(summary = "获取系统日志")
    @GetMapping("/systemLogs")
    public Result<List<Map<String, Object>>> getSystemLogs() {
        return consoleService.getSystemLogs();
    }

    @Operation(summary = "获取用户统计信息")
    @GetMapping("/userStats")
    public Result<Map<String, Object>> getUserStats() {
        return consoleService.getUserStats();
    }
}