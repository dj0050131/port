package syy.portPilotage.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "系统角色管理接口")
public class RoleController {
    
    @GetMapping("/list")
    @Operation(summary = "获取角色列表")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Map<String, Object>>> list() {
        log.info("获取角色列表");
        
        List<Map<String, Object>> roles = new ArrayList<>();
        
        Map<String, Object> admin = new HashMap<>();
        admin.put("code", "ADMIN");
        admin.put("name", "系统管理员");
        roles.add(admin);
        
        Map<String, Object> dispatcher = new HashMap<>();
        dispatcher.put("code", "DISPATCHER");
        dispatcher.put("name", "调度员");
        roles.add(dispatcher);
        
        Map<String, Object> pilot = new HashMap<>();
        pilot.put("code", "PILOT");
        pilot.put("name", "引航员");
        roles.add(pilot);
        
        Map<String, Object> agent = new HashMap<>();
        agent.put("code", "AGENT");
        agent.put("name", "船代公司");
        roles.add(agent);
        
        Map<String, Object> finance = new HashMap<>();
        finance.put("code", "FINANCE");
        finance.put("name", "财务人员");
        roles.add(finance);
        
        return Result.success(roles);
    }
}
