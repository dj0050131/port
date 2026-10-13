package syy.portPilotage.controller.plan;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.plan.PlanConfirmationDTO;
import syy.portPilotage.dto.plan.PlanConfirmationQueryDTO;
import syy.portPilotage.dto.plan.ResourceConfirmationResultDTO;
import syy.portPilotage.entity.PlanConfirmations;
import syy.portPilotage.service.PlanConfirmationsService;
import syy.portPilotage.util.SecurityUtil;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pilotage/planConfirmations")
@RequiredArgsConstructor
@Tag(name = "引航计划确认管理", description = "引航计划确认相关接口")
@Validated
public class PlanConfirmationController {

    private final PlanConfirmationsService planConfirmationsService;

    @PostMapping
    @Operation(summary = "创建确认记录")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'PILOT')")
    public Result<Boolean> create(@Valid @RequestBody PlanConfirmationDTO dto) {
        log.info("创建引航计划确认记录: planId={}, resourceType={}",
                dto.getPlanId(), dto.getResourceType());

        boolean result = planConfirmationsService.createConfirmation(dto);

        return result ? Result.success() :
                Result.error("确认失败");
    }

    @PostMapping("/batch")
    @Operation(summary = "批量确认")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Boolean> batchConfirm(@Valid @RequestBody List<PlanConfirmationDTO> dtos) {
        log.info("批量确认引航计划: 数量={}", dtos.size());

        boolean result = planConfirmationsService.batchConfirm(dtos);

        return result ? Result.success() :
                Result.error("批量确认失败");
    }

    @GetMapping
    @Operation(summary = "查询确认记录列表（已经确认的信息而非分配表）")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Map<String, Object>> getList(@Valid PlanConfirmationQueryDTO query) {
        log.info("查询引航计划确认记录列表: planId={}, resourceType={}",
                query.getPlanId(), query.getResourceType());

        Map<String, Object> result = planConfirmationsService.getConfirmationList(query);

        return Result.success(result);
    }
//    根据类型PlanConfirmationQueryDTO query查询查询各项资源的信息
    @GetMapping("/assignmentsResources")
    @Operation(summary = "查询各项资源的确认情况", description = "根据计划编号、资源类型等条件查询各项资源的确认情况")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Map<String, Object>> getAssignmentsResourceList(@Valid PlanConfirmationQueryDTO query) {
        Map<String, Object> result = planConfirmationsService.getAssignmentsResourceList(query);
        return Result.success(result);
    }

    @GetMapping("/plan/{planId}/stats")
    @Operation(summary = "获取计划的确认统计")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Map<String, Object>> getPlanConfirmationStats(@PathVariable Long planId) {
        log.info("获取引航计划确认统计: planId={}", planId);

        Map<String, Object> stats = planConfirmationsService.getPlanConfirmationStats(planId);

        return Result.success(stats);
    }

    @GetMapping("/{planId}/StatsByType")
    @Operation(summary = "获取计划的资源确认统计（按类型分组）", description = "获取指定计划的各类资源确认统计信息")
    public List<Map<String, Object>> getPlanConfirmationStatsByType(
            @Parameter(description = "计划ID", required = true) @PathVariable Long planId) {
        return planConfirmationsService.getPlanConfirmationStatsByType(planId);
    }

    @GetMapping("/plan/{planId}/fullStatus")
    @Operation(summary = "获取计划的完整确认状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Map<String, Object>> getPlanFullConfirmationStatus(@PathVariable Long planId) {
        log.info("获取引航计划完整确认状态: planId={}", planId);

        Map<String, Object> status = planConfirmationsService.getPlanFullConfirmationStatus(planId);

        return Result.success(status);
    }

    @PutMapping("/batch/cancel")
    @Operation(summary = "批量取消确认")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Boolean> batchCancelConfirmation(
            @Valid @RequestBody List<PlanConfirmationDTO> dtos,
            @RequestParam String reason) {
        log.info("批量取消确认: 数量={}, reason={}", dtos.size(), reason);

        boolean result = planConfirmationsService.batchCancel(dtos, reason);

        return result ? Result.success() :
                Result.error("批量取消确认失败");
    }

    @GetMapping("/check/{planId}/{resourceType}/{resourceId}")
    @Operation(summary = "检查是否已确认")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'PILOT')")
    public Result<Boolean> checkConfirmation(
            @PathVariable Long planId,
            @PathVariable String resourceType,
            @PathVariable Long resourceId) {
        log.info("检查确认状态: planId={}, resourceType={}, resourceId={}",
                planId, resourceType, resourceId);

        long count = planConfirmationsService.lambdaQuery()
                .eq(PlanConfirmations::getPlanId, planId)
                .eq(PlanConfirmations::getResourceType, resourceType)
                .eq(PlanConfirmations::getResourceId, resourceId)
                .eq(PlanConfirmations::getConfirmationStatus, "CONFIRMED")
                .count();

        return Result.success(count > 0);
    }

    @GetMapping("/myTasks")
    @Operation(summary = "获取我的任务列表")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DASHBOARD:MY_TASKS:VIEW')")
    public Result<PageResult<ResourceConfirmationResultDTO>> getMyTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("获取我的任务列表: status={}, page={}, pageSize={}", status, page, pageSize);

        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未获取到当前用户信息");
        }

        PageResult<ResourceConfirmationResultDTO> result = planConfirmationsService.getMyTasks(userId, status, page, pageSize);

        return Result.success(result);
    }

    @GetMapping("/myTasks/{assignmentId}")
    @Operation(summary = "获取任务详情")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DASHBOARD:MY_TASKS:VIEW')")
    public Result<ResourceConfirmationResultDTO> getTaskDetail(
            @PathVariable Long assignmentId) {
        log.info("获取任务详情: assignmentId={}", assignmentId);

        // 获取当前用户ID
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未获取到当前用户信息");
        }

        try {
            ResourceConfirmationResultDTO taskDetail = planConfirmationsService.getTaskDetail(userId, assignmentId);
            return Result.success(taskDetail);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}