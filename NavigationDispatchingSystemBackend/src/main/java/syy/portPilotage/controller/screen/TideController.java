package syy.portPilotage.controller.screen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.TideInfo;
import syy.portPilotage.service.screen.TideService;

@Slf4j
@RestController
@RequestMapping("/api/monitor/tide")
@RequiredArgsConstructor
@Tag(name = "潮汐信息", description = "潮汐信息相关接口")
public class TideController {
    
    @Autowired
    private TideService tideService;
    
    @GetMapping("/current")
    @Operation(summary = "获取当前潮汐信息")
    public Result<TideInfo> getCurrent() {
        log.info("获取当前潮汐信息");
        return tideService.getCurrent();
    }
    
    @GetMapping("/history")
    @Operation(summary = "获取历史潮汐信息")
    public Result<PageResult<TideInfo>> history(PageQueryDTO query) {
        log.info("获取历史潮汐信息");
        return tideService.history(query);
    }
}
