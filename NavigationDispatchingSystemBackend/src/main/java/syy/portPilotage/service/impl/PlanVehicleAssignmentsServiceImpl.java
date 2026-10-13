package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.dto.plan.PlanVehicleAssignmentDTO;
import syy.portPilotage.entity.PlanVehicleAssignments;
import syy.portPilotage.service.PlanVehicleAssignmentsService;
import syy.portPilotage.mapper.PlanVehicleAssignmentsMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.entity.PlanVehicleAssignments;
import syy.portPilotage.enums.AssignmentStatusEnum;
import syy.portPilotage.mapper.PlanVehicleAssignmentsMapper;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanVehicleAssignmentsServiceImpl extends ServiceImpl<PlanVehicleAssignmentsMapper, PlanVehicleAssignments> implements PlanVehicleAssignmentsService {

    private final PlanVehicleAssignmentsMapper planVehicleAssignmentsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAssignmentStatus(Long planId, Long vehicleId, String status) {
        log.info("更新车辆分配状态: planId={}, vehicleId={}, status={}", planId, vehicleId, status);

        try {
            LambdaUpdateWrapper<PlanVehicleAssignments> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PlanVehicleAssignments::getPlanId, planId)
                    .eq(PlanVehicleAssignments::getVehicleId, vehicleId)
                    .set(PlanVehicleAssignments::getStatus, status)
                    .set(PlanVehicleAssignments::getUpdatedAt, new Date());

            boolean result = update(wrapper);

            if (result) {
                log.info("车辆分配状态更新成功");

                // 如果状态是COMPLETED且还没有实际结束时间，设置实际结束时间
                if (AssignmentStatusEnum.COMPLETED.getCode().equals(status)) {
                    updateActualTimes(planId, vehicleId, null, new Date());
                }

                return true;
            } else {
                log.warn("车辆分配状态更新失败: 未找到匹配的记录");
                return false;
            }
        } catch (Exception e) {
            log.error("车辆分配状态更新异常", e);
            throw new RuntimeException("更新车辆分配状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateAssignmentStatus(Long planId, String status) {
        log.info("批量更新车辆分配状态: planId={}, status={}", planId, status);

        try {
            LambdaUpdateWrapper<PlanVehicleAssignments> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PlanVehicleAssignments::getPlanId, planId)
                    .set(PlanVehicleAssignments::getStatus, status)
                    .set(PlanVehicleAssignments::getUpdatedAt, new Date());

            return update(wrapper);
        } catch (Exception e) {
            log.error("批量更新车辆分配状态异常", e);
            throw new RuntimeException("批量更新车辆分配状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateActualTimes(Long planId, Long vehicleId, Date actualStartTime, Date actualEndTime) {
        log.info("更新车辆实际时间: planId={}, vehicleId={}, start={}, end={}",
                planId, vehicleId, actualStartTime, actualEndTime);

        try {
            LambdaQueryWrapper<PlanVehicleAssignments> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PlanVehicleAssignments::getPlanId, planId)
                    .eq(PlanVehicleAssignments::getVehicleId, vehicleId);

            PlanVehicleAssignments assignment = getOne(queryWrapper);
            if (assignment == null) {
                log.warn("车辆分配记录不存在: planId={}, vehicleId={}", planId, vehicleId);
                return false;
            }

            if (actualStartTime != null) {
                assignment.setActualStartTime(actualStartTime);
            }
            if (actualEndTime != null) {
                assignment.setActualEndTime(actualEndTime);
            }

            if (assignment.getActualStartTime() != null &&
                    assignment.getActualEndTime() != null &&
                    !AssignmentStatusEnum.COMPLETED.getCode().equals(assignment.getStatus())) {
                assignment.setStatus(AssignmentStatusEnum.COMPLETED.getCode());
            }

            return updateById(assignment);
        } catch (Exception e) {
            log.error("更新车辆实际时间异常", e);
            throw new RuntimeException("更新车辆实际时间失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean checkVehicleAvailable(Long vehicleId, Date startTime, Date endTime) {
        log.info("检查车辆可用性: vehicleId={}, startTime={}, endTime={}", vehicleId, startTime, endTime);

        try {
            LambdaQueryWrapper<PlanVehicleAssignments> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PlanVehicleAssignments::getVehicleId, vehicleId)
                    .in(PlanVehicleAssignments::getStatus,
                            AssignmentStatusEnum.ASSIGNED.getCode(),
                            AssignmentStatusEnum.CONFIRMED.getCode())
                    .and(w -> w
                            .and(q -> q
                                    .le(PlanVehicleAssignments::getEstimatedStartTime, endTime)
                                    .ge(PlanVehicleAssignments::getEstimatedEndTime, startTime))
                            .or(q -> q
                                    .le(PlanVehicleAssignments::getActualStartTime, endTime)
                                    .ge(PlanVehicleAssignments::getActualEndTime, startTime))
                    );

            long count = count(wrapper);

            boolean available = count == 0;
            log.info("车辆可用性检查结果: vehicleId={}, available={}, conflictingCount={}",
                    vehicleId, available, count);

            return available;
        } catch (Exception e) {
            log.error("检查车辆可用性异常", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createBatchAssignments(Long planId, List<PlanVehicleAssignmentDTO> assignments) {
        log.info("批量创建车辆分配: planId={}, 数量={}", planId, assignments.size());

        try {
            for (PlanVehicleAssignmentDTO dto : assignments) {
                PlanVehicleAssignments assignment = new PlanVehicleAssignments();
                assignment.setPlanId(planId);
                assignment.setVehicleId(dto.getVehicleId());
                assignment.setPurpose(dto.getPurpose());
                assignment.setRequirements(dto.getRequirements());
                assignment.setEstimatedStartTime(dto.getEstimatedStartTime());
                assignment.setEstimatedEndTime(dto.getEstimatedEndTime());
                assignment.setStatus(dto.getStatus() != null ? dto.getStatus() : AssignmentStatusEnum.ASSIGNED.getCode());

                save(assignment);
            }

            log.info("批量创建车辆分配成功");
            return true;
        } catch (Exception e) {
            log.error("批量创建车辆分配异常", e);
            throw new RuntimeException("批量创建车辆分配失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateVehicleUsage(Long assignmentId, Double mileage, Double fuelConsumption) {
        log.info("更新车辆使用信息: assignmentId={}, mileage={}, fuelConsumption={}",
                assignmentId, mileage, fuelConsumption);

        try {
            PlanVehicleAssignments assignment = getById(assignmentId);
            if (assignment == null) {
                log.warn("车辆分配记录不存在: assignmentId={}", assignmentId);
                return false;
            }

            if (mileage != null) {
                assignment.setActualMileage(BigDecimal.valueOf(mileage));
            }
            if (fuelConsumption != null) {
                assignment.setFuelConsumption(BigDecimal.valueOf(fuelConsumption));
            }

            return updateById(assignment);
        } catch (Exception e) {
            log.error("更新车辆使用信息异常", e);
            throw new RuntimeException("更新车辆使用信息失败: " + e.getMessage(), e);
        }
    }
}