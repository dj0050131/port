package syy.portPilotage.controller.screen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.Result;
import syy.portPilotage.service.screen.ScreenService;
import syy.portPilotage.vo.screen.*;

import java.util.List;

@Tag(name = "大屏数据接口", description = "大屏数据相关接口")
@RestController
@RequestMapping("/api/monitor/dashboard")
public class ScreenController {
    
    @Autowired
    private ScreenService screenService;
    
    @Operation(summary = "获取港口地图全量数据")
    @GetMapping("/portMap")
    public Result<PortMapPayloadVO> getPortMapPayload() {
        return screenService.getPortMapPayload();
    }
    
    @Operation(summary = "获取船舶动态列表")
    @GetMapping("/shipDynamics")
    public Result<List<ShipDynamicVO>> getShipDynamics() {
        return screenService.getShipDynamics();
    }
    
    @Operation(summary = "获取引航任务列表")
    @GetMapping("/pilotTasks")
    public Result<List<PilotPlanVO>> getPilotTasks() {
        return screenService.getPilotTasks();
    }
    
    @Operation(summary = "获取财务营收统计（计费中心)")
    @GetMapping("/financialStats")
    public Result<FeeStatisticsVO> getFinancialStats() {
        return screenService.getFinancialStats();
    }

    @Operation(summary = "获取实时告警列表")
    @GetMapping("/alerts")
    public Result<List<AlertItemVO>> getAlerts() {
        return screenService.getAlerts();
    }

    @Operation(summary = "获取系统资源状态")
    @GetMapping("/systemStatus")
    public Result<List<SystemStatusItemVO>> getSystemStatus() {
        return screenService.getSystemStatus();
    }

    @Operation(summary = "获取泊位状态列表")
    @GetMapping("/berthStatus")
    public Result<List<BerthStatusVO>> getBerthStatus() {
        return screenService.getBerthStatus();
    }
    
    @Operation(summary = "获取系统性能指标和汇总统计")
    @GetMapping("/systemPerformanceData")
    public Result<SystemPerformanceDataVO> getSystemPerformanceData() {
        return screenService.getSystemPerformanceData();
    }

}