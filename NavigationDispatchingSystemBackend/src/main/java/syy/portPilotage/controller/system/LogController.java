package syy.portPilotage.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/system/logs")
@RequiredArgsConstructor
@Tag(name = "日志管理", description = "系统日志管理接口")
public class LogController {
    
    @GetMapping("/operation")
    @Operation(summary = "获取操作日志")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Object>> getOperationLogs(PageQueryDTO query) {
        log.info("获取操作日志");
        
        List<Object> logs = new ArrayList<>();
        
        return Result.success(logs);
    }
    
    @GetMapping("/login")
    @Operation(summary = "获取登录日志")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Object>> getLoginLogs(PageQueryDTO query) {
        log.info("获取登录日志");
        
        List<Object> logs = new ArrayList<>();
        
        return Result.success(logs);
    }
}
