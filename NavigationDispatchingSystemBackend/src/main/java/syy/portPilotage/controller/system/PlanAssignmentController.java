package syy.portPilotage.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.service.PlanPilotBoatAssignmentsService;
import syy.portPilotage.service.PlanTugboatAssignmentsService;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/pilotage/assignments")
@RequiredArgsConstructor
@Tag(name = "资源分配管理", description = "引航艇、拖轮分配管理相关接口")
public class PlanAssignmentController {

    private final PlanPilotBoatAssignmentsService planPilotBoatAssignmentsService;
    private final PlanTugboatAssignmentsService planTugboatAssignmentsService;

    @GetMapping("/pilot-boat/{pilotBoatId}/availability")
    @Operation(summary = "检查引航艇可用性")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Boolean> checkPilotBoatAvailability(
            @PathVariable Long pilotBoatId,
            @RequestParam Date startTime,
            @RequestParam Date endTime) {
        log.info("检查引航艇可用性: pilotBoatId={}, startTime={}, endTime={}",
                pilotBoatId, startTime, endTime);

        boolean available = true;

        return Result.success(available);
    }

    @GetMapping("/tugboat/{tugboatId}/availability")
    @Operation(summary = "检查拖轮可用性")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Boolean> checkTugboatAvailability(
            @PathVariable Long tugboatId,
            @RequestParam Date startTime,
            @RequestParam Date endTime) {
        log.info("检查拖轮可用性: tugboatId={}, startTime={}, endTime={}",
                tugboatId, startTime, endTime);

        boolean available = true;
        return Result.success(available);
    }

    @PutMapping("/pilot-boat/{planId}/{pilotBoatId}/status")
    @Operation(summary = "更新引航艇分配状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER', 'PILOT')")
    public Result<Boolean> updatePilotBoatAssignmentStatus(
            @PathVariable Long planId,
            @PathVariable Long pilotBoatId,
            @RequestParam String status) {
        log.info("更新引航艇分配状态: planId={}, pilotBoatId={}, status={}",
                planId, pilotBoatId, status);

        boolean result = planPilotBoatAssignmentsService.updateAssignmentStatus(planId, pilotBoatId, status);

        return result ? Result.success() :
                Result.error("状态更新失败");
    }

    @PutMapping("/tugboat/{planId}/{tugboatId}/status")
    @Operation(summary = "更新拖轮分配状态")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Boolean> updateTugboatAssignmentStatus(
            @PathVariable Long planId,
            @PathVariable Long tugboatId,
            @RequestParam String status) {
        log.info("更新拖轮分配状态: planId={}, tugboatId={}, status={}",
                planId, tugboatId, status);

        boolean result = planTugboatAssignmentsService.updateAssignmentStatus(planId, tugboatId, status);

        return result ? Result.success() :
                Result.error("状态更新失败");
    }
}