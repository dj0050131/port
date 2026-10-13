package syy.portPilotage.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.util.SecurityUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/system/menus")
@RequiredArgsConstructor
@Tag(name = "菜单管理", description = "系统菜单管理接口")
public class MenuController {
    
    @GetMapping("/current")
    @Operation(summary = "获取当前用户菜单")
    public Result<List<Map<String, Object>>> getCurrentUserMenus() {
        String userType = SecurityUtil.getCurrentUserType();
        log.info("获取用户菜单: userType={}", userType);
        
        List<Map<String, Object>> menus = new ArrayList<>();
        
        return Result.success(menus);
    }
}
