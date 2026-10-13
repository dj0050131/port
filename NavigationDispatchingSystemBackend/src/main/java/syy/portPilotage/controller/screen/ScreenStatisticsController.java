package syy.portPilotage.controller.screen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.Result;
import syy.portPilotage.service.screen.ScreenStatisticsService;
import syy.portPilotage.vo.screen.OperationsOverviewVO;
import syy.portPilotage.vo.screen.TrafficOverviewVO;

@Tag(name = "大屏统计接口", description = "大屏统计数据相关接口")
@RestController
@RequestMapping("/api/monitor/dashboard")
public class ScreenStatisticsController {
    
    @Autowired
    private ScreenStatisticsService screenStatisticsService;

    @Operation(summary = "获取交通流/通航统计")
    @GetMapping("/trafficStats")
    public Result<TrafficOverviewVO> getTrafficStats() {
        return screenStatisticsService.getTrafficStats();
    }

    @Operation(summary = "获取运营综合作业统计(辅助监控菜单)")
    @GetMapping("/operationsStats")
    public Result<OperationsOverviewVO> getOperationsStats() {
        return screenStatisticsService.getOperationsStats();
    }
    
    @Operation(summary = "获取港口负荷指数")
    @GetMapping("/portLoadIndex")
    public Result<Integer> getPortLoadIndex() {
        return screenStatisticsService.getPortLoadIndex();
    }
}