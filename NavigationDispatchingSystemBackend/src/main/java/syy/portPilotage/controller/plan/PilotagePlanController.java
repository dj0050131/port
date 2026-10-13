package syy.portPilotage.controller.plan;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.plan.PilotBoatAssignmentDTO;
import syy.portPilotage.dto.base.PlanDateDTO;
import syy.portPilotage.dto.base.SaveAssignmentsResultDTO;
import syy.portPilotage.dto.base.TugboatAssignmentDTO;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.dto.pilotage.*;
import syy.portPilotage.entity.Berths;
import syy.portPilotage.entity.Drivers;
import syy.portPilotage.entity.Pilots;
import syy.portPilotage.entity.PilotagePlans;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.rabbitmq.service.MessageService;
import syy.portPilotage.service.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/pilotage/plans")
@RequiredArgsConstructor
@Tag(name = "引航计划管理", description = "引航计划相关接口")
public class PilotagePlanController {

    @Autowired
    private PilotagePlansService pilotagePlansService;

    @Autowired
    private PlanConfirmationsService planConfirmationsService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private PilotsService pilotsService;
    
    @Autowired
    private DriversService driversService;
    
    @Autowired
    private BerthsService berthsService;

    @PostMapping("/batch")
    @Operation(summary = "批量创建引航计划（专门用于批量创建）")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<BatchCreateResultDTO> createBatch(@Valid @RequestBody List<PilotagePlanDTO> dtos) {
        log.info("批量创建引航计划: 数量={}", dtos.size());

        BatchCreateResultDTO result = pilotagePlansService.createPlansWithResult(dtos);

        log.info("批量引航计划创建结果: 成功{}条，失败{}条", result.getSuccessCount(), result.getFailedCount());
        return Result.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新引航计划")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PilotagePlanDTO dto) {
        log.info("更新引航计划: id={}", id);

        boolean success = pilotagePlansService.updatePlan(id, dto);
        if (!success) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        log.info("引航计划更新成功: id={}", id);
        return Result.success();
    }

    @PutMapping("/{id}/replan")
    @Operation(summary = "重排引航计划（重新排资源/时间）")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> replan(@PathVariable Long id, @Valid @RequestBody ReplanPlanDTO dto) {
        log.info("重排引航计划: id={}", id);

        boolean success = pilotagePlansService.replanPlan(id, dto);
        if (!success) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        log.info("引航计划重排成功: id={}", id);
        return Result.success();
    }

//
//        }
//
//    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除引航计划")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        log.info("批量删除引航计划: ids={}", ids);

        boolean success = pilotagePlansService.deletePlansBatch(ids);
        if (!success) {
            throw new BusinessException(ErrorCode.PLAN_DELETE_FAILED);
        }

        log.info("批量删除引航计划成功: 数量={}", ids.size());
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取引航计划详情")
    public Result<PilotagePlanDTO> getById(@PathVariable Long id) {
        PilotagePlanDTO dto = pilotagePlansService.getPlanDetail(id);
        if (dto == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }
        return Result.success(dto);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询引航计划")
    public Result<IPage<PilotagePlanDTO>> page(
            PageQueryDTO query,
            @RequestParam(required = false) String planStatus,
            @RequestParam(required = false) String voyageType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String vesselName,
            @RequestParam(required = false) String pilotName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        if (query.getParams() == null) {
            query.setParams(new HashMap<>());
        }
        Map<String, Object> params = query.getParams();
        if (planStatus != null && !planStatus.trim().isEmpty()) {
            params.put("planStatus", planStatus.trim());
        }
        if (voyageType != null && !voyageType.trim().isEmpty()) {
            params.put("voyageType", voyageType.trim());
        }
        if (priority != null && !priority.trim().isEmpty()) {
            params.put("priority", priority.trim());
        }
        if (vesselName != null && !vesselName.trim().isEmpty()) {
            params.put("vesselName", vesselName.trim());
        }
        if (pilotName != null && !pilotName.trim().isEmpty()) {
            params.put("pilotName", pilotName.trim());
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            Date parsedStart = parseDateStart(startDate.trim());
            if (parsedStart != null) {
                params.put("startDate", parsedStart);
            }
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            Date parsedEnd = parseDateEnd(endDate.trim());
            if (parsedEnd != null) {
                params.put("endDate", parsedEnd);
            }
        }
        IPage<PilotagePlanDTO> pageResult = pilotagePlansService.getPageWithJoin(query);
        return Result.success(pageResult);
    }

    private Date parseDateStart(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDateTime start = date.atStartOfDay();
            return Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Date parseDateEnd(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDateTime end = date.atTime(23, 59, 59);
            return Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) {
            return null;
        }
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "发布引航计划")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> publish(@PathVariable Long id) {
        log.info("发布引航计划: id={}", id);

        boolean success = pilotagePlansService.publishPlan(id);
        if (!success) {
            throw new BusinessException(ErrorCode.PLAN_PUBLISH_FAILED);
        }

        log.info("引航计划发布成功: id={}", id);
        return Result.success();
    }

    @PutMapping("/batchPublish")
    @Operation(summary = "批量发布引航计划")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<BatchPublishResultDTO> publishBatch(@RequestBody List<Long> planIds) {
        log.info("批量发布引航计划: ids={}", planIds);

        BatchPublishResultDTO result = pilotagePlansService.publishPlansBatch(planIds);

        log.info("批量发布引航计划结果: 成功{}条，失败{}条",
                result.getSuccessCount(), result.getFailedCount());
        return Result.success(result);
    }

    @PutMapping("/{id}/start")
    @Operation(summary = "开始执行引航")
    @PreAuthorize("hasAnyRole('ADMIN','PILOT')")
    public Result<Void> start(@PathVariable Long id, @RequestParam(required = false) Long actualStartTime) {
        log.info("开始执行引航: id={}, actualStartTime={}", id, actualStartTime);

        boolean success = (actualStartTime != null)
                ? pilotagePlansService.startPlan(id, new Date(actualStartTime))
                : pilotagePlansService.startPlan(id);
        if (!success) {
            throw new BusinessException(ErrorCode.PLAN_START_FAILED);
        }

        log.info("引航开始执行: id={}", id);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "完成引航")
    @PreAuthorize("hasAnyRole('ADMIN','PILOT')")
    public Result<Void> complete(@PathVariable Long id, @RequestParam(required = false) Long actualEndTime) {
        log.info("完成引航: id={}, actualEndTime={}", id, actualEndTime);

        boolean success = (actualEndTime != null)
                ? pilotagePlansService.completePlan(id, new Date(actualEndTime))
                : pilotagePlansService.completePlan(id);
        if (!success) {
            throw new BusinessException(ErrorCode.PLAN_COMPLETE_FAILED);
        }

        log.info("引航完成: id={}", id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消引航计划")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        log.info("取消引航计划: id={}, reason={}", id, reason);

        boolean success = pilotagePlansService.cancelPlan(id, reason);
        if (!success) {
            throw new BusinessException(ErrorCode.PLAN_CANCEL_FAILED);
        }

        log.info("引航计划取消成功: id={}", id);
        return Result.success();
    }

    @PostMapping("/{id}/assigntugboat")
    @Operation(summary = "分配拖轮到引航计划")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> assignTugboat(@PathVariable Long id, @Valid @RequestBody TugboatAssignmentDTO dto) {
        log.info("分配拖轮到引航计划: planId={}, tugboatId={}", id, dto.getTugboatId());

        boolean success = pilotagePlansService.assignTugboat(id, dto);
        if (!success) {
            throw new BusinessException(ErrorCode.TUGBOAT_ASSIGN_FAILED);
        }

        log.info("拖轮分配成功: planId={}", id);
        return Result.success();
    }

    @PostMapping("/{id}/assignPilotBoat")
    @Operation(summary = "分配引航艇到引航计划")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> assignPilotBoat(@PathVariable Long id, @Valid @RequestBody PilotBoatAssignmentDTO dto) {
        log.info("分配引航艇到引航计划: planId={}, pilotBoatId={}", id, dto.getPilotBoatId());

        boolean success = pilotagePlansService.assignPilotBoat(id, dto);
        if (!success) {
            throw new BusinessException(ErrorCode.PILOT_BOAT_ASSIGN_FAILED);
        }

        log.info("引航艇分配成功: planId={}", id);
        return Result.success();
    }

    @DeleteMapping("/{id}/removeTugboat/{assignmentId}")
    @Operation(summary = "移除拖轮分配")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> removeTugboat(@PathVariable Long id, @PathVariable Long assignmentId) {
        log.info("移除拖轮分配: planId={}, assignmentId={}", id, assignmentId);

        boolean success = pilotagePlansService.removeTugboatAssignment(id, assignmentId);
        if (!success) {
            throw new BusinessException(ErrorCode.TUGBOAT_REMOVE_FAILED);
        }

        log.info("拖轮移除成功: planId={}", id);
        return Result.success();
    }

    @DeleteMapping("/{id}/removePilotBoat/{assignmentId}")
    @Operation(summary = "移除引航艇分配")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> removePilotBoat(@PathVariable Long id, @PathVariable Long assignmentId) {
        log.info("移除引航艇分配: planId={}, assignmentId={}", id, assignmentId);

        boolean success = pilotagePlansService.removePilotBoatAssignment(id, assignmentId);
        if (!success) {
            throw new BusinessException(ErrorCode.PILOT_BOAT_REMOVE_FAILED);
        }

        log.info("引航艇移除成功: planId={}", id);
        return Result.success();
    }

    @PostMapping("/checkResourceAvailability")
    @Operation(summary = "检查资源可用性")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<ResourceAvailabilityResultDTO> checkResourceAvailability(
            @Valid @RequestBody ResourceCheckRequestDTO request) {
        log.info("检查资源可用性: resourceType={}, resourceId={}",
                request.getResourceType(), request.getResourceId());

        ResourceAvailabilityResultDTO result = pilotagePlansService.checkResourceAvailability(request);

        return Result.success(result);
    }

    @PostMapping("/batchCheckResourceAvailability")
    @Operation(summary = "批量检查资源可用性")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<List<ResourceAvailabilityResultDTO>> batchCheckResourceAvailability(
            @Valid @RequestBody List<ResourceCheckRequestDTO> requests) {
        log.info("批量检查资源可用性: 数量={}", requests.size());

        List<ResourceAvailabilityResultDTO> results = pilotagePlansService.batchCheckResourceAvailability(requests);

        return Result.success(results);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取引航计划统计信息")
    public Result<PlanStatisticsDTO> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long pilotId,
            @RequestParam(required = false) String vesselType) {

        PlanStatisticsDTO statistics = pilotagePlansService.getPlanStatistics(startDate, endDate, pilotId, vesselType);

        return Result.success(statistics);
    }

    @GetMapping("/export")
    @Operation(summary = "导出引航计划", description = "导出指定时间范围内的引航计划，支持Excel和PDF格式")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "导出成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "导出失败")
    })
    public void exportPlans(
            @Parameter(description = "开始日期，格式：yyyy-MM-dd", example = "2024-01-01") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期，格式：yyyy-MM-dd", example = "2024-12-31") @RequestParam(required = false) String endDate,
            @Parameter(description = "导出格式：excel或pdf", example = "excel") @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) throws IOException {

        log.info("导出引航计划: startDate={}, endDate={}, format={}", startDate, endDate, format);

        byte[] data = pilotagePlansService.exportPlans(startDate, endDate, format);

        String filename = "引航计划_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        if ("excel".equals(format)) {
            filename += ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } else {
            filename += ".pdf";
            response.setContentType("application/pdf");
        }

        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        response.getOutputStream().write(data);
        response.flushBuffer();
    }

    @PutMapping("/{id}/date")
    @Operation(summary = "设置计划日期时间")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> setPlanDate(@PathVariable Long id, @Valid @RequestBody PlanDateDTO dto) {
        log.info("设置计划日期时间: id={}", id);

        boolean success = pilotagePlansService.setPlanDate(id, dto);
        if (!success) {
            throw new BusinessException(ErrorCode.PLAN_UPDATE_FAILED);
        }

        log.info("计划日期时间设置成功: id={}", id);
        return Result.success();
    }

    @PostMapping("/{id}/tugboatAssignments")
    @Operation(summary = "保存拖轮分配")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<SaveAssignmentsResultDTO> saveTugboatAssignments(
            @PathVariable Long id,
            @Valid @RequestBody List<TugboatAssignmentDTO> assignments) {
        log.info("保存拖轮分配: planId={}, assignmentsCount={}", id, assignments.size());

        SaveAssignmentsResultDTO result = pilotagePlansService.saveTugboatAssignments(id, assignments);

        log.info("拖轮分配保存完成: planId={}, 成功={}, 失败={}",
                id, result.getSuccessCount(), result.getFailedCount());
        return Result.success(result);
    }

    @PostMapping("/{id}/pilotBoatAssignments")
    @Operation(summary = "保存引航艇分配")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<SaveAssignmentsResultDTO> savePilotBoatAssignments(
            @PathVariable Long id,
            @Valid @RequestBody List<PilotBoatAssignmentDTO> assignments) {
        log.info("保存引航艇分配: planId={}, assignmentsCount={}", id, assignments.size());

        SaveAssignmentsResultDTO result = pilotagePlansService.savePilotBoatAssignments(id, assignments);

        log.info("引航艇分配保存完成: planId={}, 成功={}, 失败={}",
                id, result.getSuccessCount(), result.getFailedCount());
        return Result.success(result);
    }

    @GetMapping("/{planId}/confirmation-status")
    @Operation(summary = "获取引航计划的确认状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Map<String, Object>> getConfirmationStatus(@PathVariable Long planId) {
        log.info("获取引航计划确认状态: planId={}", planId);

        Map<String, Object> status = planConfirmationsService.getPlanFullConfirmationStatus(planId);

        return Result.success(status);
    }

    @GetMapping("/unconfirmed")
    @Operation(summary = "获取未完全确认的计划列表")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Page<PilotagePlans>> getUnconfirmedPlans(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取未完全确认的计划列表: pageNum={}, pageSize={}", pageNum, pageSize);

        // 由于涉及复杂的关联查询，建议在Service中实现

        return Result.success(null); // 需要实现具体逻辑
    }

    @PostMapping("/sendConfirmationReminder")
    @Operation(summary = "发送确认提醒")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Boolean> sendConfirmationReminder(@RequestBody SendConfirmationRequest request) {
        log.info("发送确认提醒: resourceId={}, resourceType={}", request.getResourceId(), request.getResourceType());

        String recipientEmail = getEmailByResourceTypeAndId(request.getResourceType(), request.getResourceId());
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            log.warn("未能获取到 {} 类型资源(ID: {})的有效邮箱地址", request.getResourceType(), request.getResourceId());
            throw new BusinessException(ErrorCode.EMAIL_ADDRESS_NOT_FOUND, "未能获取到有效的邮箱地址");
        }

        PilotagePlanDTO plan = pilotagePlansService.getPlanDetail(request.getPlanId());
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        String subject = "引航计划确认提醒";
        String content = String.format(
            "您好，\n\n这是关于引航计划 #%d (%s) 的确认提醒。\n\n请尽快确认该计划。\n\n谢谢！",
            plan.getId(), 
            plan.getPlanCode()
        );

        try {
            messageService.sendEmail(recipientEmail, subject, content, false);
            log.info("已发送确认提醒邮件至: {}", recipientEmail);
            return Result.success(true);
        } catch (Exception e) {
            log.error("发送确认提醒邮件失败: {}", e.getMessage(), e);
            return Result.success(false);
        }
    }

    private String getEmailByResourceTypeAndId(String resourceType, Long resourceId) {
        if (resourceType == null || resourceId == null) {
            return null;
        }
        
        switch (resourceType.toUpperCase()) {
            case "PILOT":
                Pilots pilot = pilotsService.getById(resourceId);
                return pilot != null ? pilot.getEmail() : null;
            case "DRIVER":
                Drivers driver = driversService.getById(resourceId);
                return driver != null ? driver.getEmail() : null;
            case "BERTH":
                Berths berth = berthsService.getById(resourceId);
                return berth != null ? berth.getContactPhone() : null; // 泊位表中没有邮箱字段，暂时使用联系电话
            default:
                log.warn("不支持的资源类型: {}", resourceType);
                return null;
        }
    }
}