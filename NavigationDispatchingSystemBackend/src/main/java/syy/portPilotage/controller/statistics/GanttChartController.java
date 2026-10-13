package syy.portPilotage.controller.statistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.gantt.GanttChartDataDTO;
import syy.portPilotage.dto.gantt.ResourceTimelineDTO;
import syy.portPilotage.dto.gantt.ConflictCheckRequestDTO;
import syy.portPilotage.dto.gantt.ResourceConflictDTO;
import syy.portPilotage.service.screen.GanttChartService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/gantt")
@RequiredArgsConstructor
@Tag(name = "甘特图调度管理", description = "调度甘特图相关接口")
public class GanttChartController {
    
    @Autowired
    private GanttChartService ganttChartService;

    /**
     * 获取资源时间轴数据
     * 用于甘特图展示各类资源（引航员、拖轮、引航艇、驾驶员、车辆）的时间安排
     */
    @GetMapping("/resourceTimeline")
    @Operation(summary = "获取资源时间轴数据")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DASHBOARD:GANTT:VIEW')")
    public Result<List<ResourceTimelineDTO>> getResourceTimeline(
            @RequestParam String startDate, 
            @RequestParam String endDate,
            @RequestParam(required = false) String resourceType) {
        log.info("获取资源时间轴数据: startDate={}, endDate={}, resourceType={}", 
                startDate, endDate, resourceType);
        
        try {
            Date parsedStartDate = parseDateTimeParam(startDate);
            Date parsedEndDate = parseDateTimeParam(endDate);
            
            List<ResourceTimelineDTO> result = ganttChartService.getResourceTimeline(
                    parsedStartDate, parsedEndDate, resourceType);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("日期格式解析错误: {}", e.getMessage());
            return Result.error("日期格式错误，请使用ISO 8601格式");
        }
    }
    
    @GetMapping("/data")
    @Operation(summary = "获取甘特图数据")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DASHBOARD:GANTT:VIEW')")
    public Result<GanttChartDataDTO> getGanttData(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("获取甘特图数据: startDate={}, endDate={}", startDate, endDate);

        try {
            Date parsedStartDate = parseDateTimeParam(startDate);
            Date parsedEndDate = parseDateTimeParam(endDate);

            GanttChartDataDTO result = ganttChartService.getGanttData(parsedStartDate, parsedEndDate);

            return Result.success(result);
        } catch (Exception e) {
            log.error("日期格式解析错误: {}", e.getMessage());
            return Result.error("日期格式错误，请使用格式: yyyy-MM-dd'T'HH:mm:ss");
        }
    }

    
    @PutMapping("/updatePlanTime")
    @Operation(summary = "更新计划时间")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DASHBOARD:GANTT:EDIT')")
    public Result<Boolean> updatePlanTime(
            @RequestParam Long planId,
            @RequestParam String newStartTime,
            @RequestParam String newEndTime) {
        log.info("更新计划时间: planId={}, newStartTime={}, newEndTime={}",
                planId, newStartTime, newEndTime);

        try {
            Date parsedStartTime = parseDateTimeParam(newStartTime);
            Date parsedEndTime = parseDateTimeParam(newEndTime);

            Boolean result = ganttChartService.updatePlanTime(planId, parsedStartTime, parsedEndTime);

            return Result.success(result);
        } catch (Exception e) {
            log.error("日期格式解析错误: {}", e.getMessage());
            return Result.error("日期格式错误，请使用格式: yyyy-MM-dd'T'HH:mm:ss");
        }
    }

    
    @PostMapping("/checkConflicts")
    @Operation(summary = "检查资源时间冲突")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DASHBOARD:GANTT:EDIT')")
    public Result<List<ResourceConflictDTO>> checkConflicts(
            @RequestBody ConflictCheckRequestDTO request) {
        log.info("检查资源时间冲突: request={}", request);
        
        List<ResourceConflictDTO> result = ganttChartService.checkConflicts(request);
        
        return Result.success(result);
    }
    
    
    @GetMapping("/resourceOverview")
    @Operation(summary = "获取资源概览")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DASHBOARD:GANTT:VIEW')")
    public Result<Object> getResourceOverview(
            @RequestParam String startDate, 
            @RequestParam String endDate) {
        log.info("获取资源概览: startDate={}, endDate={}", startDate, endDate);
        
        try {
            Date parsedStartDate = parseDateTimeParam(startDate);
            Date parsedEndDate = parseDateTimeParam(endDate);
            
            Object result = ganttChartService.getResourceOverview(parsedStartDate, parsedEndDate);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("日期格式解析错误: {}", e.getMessage());
            return Result.error("日期格式错误，请使用ISO 8601格式");
        }
    }

    /**
     * 兼容多种 ISO-8601 日期时间参数：
     * - 2026-04-26T00:00:00.000Z（前端 toISOString）
     * - 2026-04-26T00:00:00+08:00
     * - 2026-04-26T00:00:00 / 2026-04-26T00:00:00.000（本地时间）
     *
     * 避免 SimpleDateFormat 把 'Z' 当作字面量导致的时区偏差，从而出现“今日甘特图无数据”。
     */
    private Date parseDateTimeParam(String raw) {
        if (raw == null) throw new IllegalArgumentException("dateTime param is null");
        String s = raw.trim();
        if (s.isEmpty()) throw new IllegalArgumentException("dateTime param is blank");

        // 带 Z 或 offset 的标准 ISO：优先按瞬时时间解析，保证时区正确
        try {
            return Date.from(Instant.parse(s));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Date.from(OffsetDateTime.parse(s).toInstant());
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDateTime ldt = LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignored) {
        }

        // 兼容仅到秒的格式（部分调用方可能不带毫秒）
        try {
            LocalDateTime ldt = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignored) {
        }

        throw new IllegalArgumentException("Unrecognized datetime format: " + raw);
    }
}