package syy.portPilotage.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.plan.PlanDriverAssignmentDTO;
import syy.portPilotage.dto.plan.PlanPilotAssignmentDTO;
import syy.portPilotage.dto.plan.PlanVehicleAssignmentDTO;
import syy.portPilotage.service.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pilotage/assignmentManager")
@RequiredArgsConstructor
@Tag(name = "资源分配管理器", description = "统一管理各种资源分配")
public class PlanAssignmentManagerController {

    private final PlanPilotAssignmentsService planPilotAssignmentsService;
    private final PlanVehicleAssignmentsService planVehicleAssignmentsService;
    private final PlanDriverAssignmentsService planDriverAssignmentsService;
    private final PlanPilotBoatAssignmentsService planPilotBoatAssignmentsService;
    private final PlanTugboatAssignmentsService planTugboatAssignmentsService;

    @PostMapping("/{planId}/pilots/batch")
    @Operation(summary = "批量分配引航员")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Boolean> batchAssignPilots(
            @PathVariable Long planId,
            @RequestBody List<PlanPilotAssignmentDTO> assignments) {
        log.info("批量分配引航员: planId={}, 数量={}", planId, assignments.size());

        boolean result = planPilotAssignmentsService.createBatchAssignments(planId, assignments);

        return result ? Result.success() :
                Result.error("引航员分配失败");
    }

    @PostMapping("/{planId}/vehicles/batch")
    @Operation(summary = "批量分配车辆")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Boolean> batchAssignVehicles(
            @PathVariable Long planId,
            @RequestBody List<PlanVehicleAssignmentDTO> assignments) {
        log.info("批量分配车辆: planId={}, 数量={}", planId, assignments.size());

        boolean result = planVehicleAssignmentsService.createBatchAssignments(planId, assignments);

        return result ? Result.success() :
                Result.error("车辆分配失败");
    }

    @PostMapping("/{planId}/drivers/batch")
    @Operation(summary = "批量分配驾驶员")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Boolean> batchAssignDrivers(
            @PathVariable Long planId,
            @RequestBody List<PlanDriverAssignmentDTO> assignments) {
        log.info("批量分配驾驶员: planId={}, 数量={}", planId, assignments.size());

        boolean result = planDriverAssignmentsService.createBatchAssignments(planId, assignments);

        return result ? Result.success() :
                Result.error("驾驶员分配失败");
    }

    @GetMapping("/checkAvailability")
    @Operation(summary = "检查资源可用性")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Boolean> checkResourceAvailability(
            @RequestParam String resourceType,
            @RequestParam Long resourceId,
            @RequestParam Date startTime,
            @RequestParam Date endTime) {
        log.info("检查资源可用性: type={}, id={}, start={}, end={}",
                resourceType, resourceId, startTime, endTime);

        boolean available = false;

        switch (resourceType.toUpperCase()) {
            case "PILOT":
                available = planPilotAssignmentsService.checkPilotAvailable(resourceId, startTime, endTime);
                break;
            case "VEHICLE":
                available = planVehicleAssignmentsService.checkVehicleAvailable(resourceId, startTime, endTime);
                break;
            case "DRIVER":
                available = planDriverAssignmentsService.checkDriverAvailable(resourceId, startTime, endTime);
                break;
            case "PILOT_BOAT":
                available = planPilotBoatAssignmentsService.checkPilotBoatAvailable(resourceId, startTime, endTime);
                break;
            case "TUGBOAT":
                available = planTugboatAssignmentsService.checkTugboatAvailable(resourceId, startTime, endTime);
                break;
            default:
                return Result.error("不支持的资源类型");
        }

        return Result.success(available);
    }

    @GetMapping("/plan/{planId}/allAssignments")
    @Operation(summary = "获取计划的所有分配信息")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Object> getAllAssignments(@PathVariable Long planId) {
        log.info("获取计划的所有分配信息: planId={}", planId);

        // 实现略，可以根据需要实现

        return Result.success(null);
    }
}