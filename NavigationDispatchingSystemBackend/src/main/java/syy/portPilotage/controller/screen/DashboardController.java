package syy.portPilotage.controller.screen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.Result;
import syy.portPilotage.service.screen.DashboardService;
import syy.portPilotage.vo.billing.FinancialStatVO;
import syy.portPilotage.vo.pilotage.PilotageStatVO;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/monitor/dashboard")
@RequiredArgsConstructor
@Tag(name = "数据看板", description = "数据统计看板接口")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping("/pilotageStat")
    @Operation(summary = "获取引航统计数据")
    public Result<PilotageStatVO> getPilotageStat() {
        log.info("获取引航统计数据");
        return dashboardService.getPilotageStat();
    }
    
    @GetMapping("/financialStat")
    @Operation(summary = "获取财务统计数据")
    public Result<FinancialStatVO> getFinancialStat() {
        log.info("获取财务统计数据");
        return dashboardService.getFinancialStat();
    }
    
    @GetMapping("/throughputStat")
    @Operation(summary = "获取吞吐量统计数据")
    public Result<Map<String, Object>> getThroughputStat() {
        log.info("获取吞吐量统计数据");
        return dashboardService.getThroughputStat();
    }
    
    @GetMapping("/resourceUsageStat")
    @Operation(summary = "获取资源利用率统计数据")
    public Result<Map<String, Object>> getResourceUsageStat() {
        log.info("获取资源利用率统计数据");
        return dashboardService.getResourceUsageStat();
    }
    
    @GetMapping("/alertStat")
    @Operation(summary = "获取告警统计数据")
    public Result<Map<String, Object>> getAlertStat() {
        log.info("获取告警统计数据");
        return dashboardService.getAlertStat();
    }
    
    @GetMapping("/overview")
    @Operation(summary = "获取综合概览")
    public Result<Map<String, Object>> getOverview() {
        log.info("获取综合概览");
        return dashboardService.getOverview();
    }
}