package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.dto.plan.PlanDriverAssignmentDTO;
import syy.portPilotage.entity.PlanDriverAssignments;
import syy.portPilotage.service.PlanDriverAssignmentsService;
import syy.portPilotage.mapper.PlanDriverAssignmentsMapper;
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
import syy.portPilotage.entity.PlanDriverAssignments;
import syy.portPilotage.enums.AssignmentStatusEnum;
import syy.portPilotage.mapper.PlanDriverAssignmentsMapper;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanDriverAssignmentsServiceImpl extends ServiceImpl<PlanDriverAssignmentsMapper, PlanDriverAssignments> implements PlanDriverAssignmentsService {

    private final PlanDriverAssignmentsMapper planDriverAssignmentsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAssignmentStatus(Long planId, Long driverId, String status) {
        log.info("更新驾驶员分配状态: planId={}, driverId={}, status={}", planId, driverId, status);

        try {
            LambdaUpdateWrapper<PlanDriverAssignments> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PlanDriverAssignments::getPlanId, planId)
                    .eq(PlanDriverAssignments::getDriverId, driverId)
                    .set(PlanDriverAssignments::getStatus, status)
                    .set(PlanDriverAssignments::getUpdatedAt, new Date());

            boolean result = update(wrapper);

            if (result) {
                log.info("驾驶员分配状态更新成功");

                if (AssignmentStatusEnum.COMPLETED.getCode().equals(status)) {
                    updateActualTimes(planId, driverId, null, new Date());
                }

                return true;
            } else {
                log.warn("驾驶员分配状态更新失败: 未找到匹配的记录");
                return false;
            }
        } catch (Exception e) {
            log.error("驾驶员分配状态更新异常", e);
            throw new RuntimeException("更新驾驶员分配状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateAssignmentStatus(Long planId, String status) {
        log.info("批量更新驾驶员分配状态: planId={}, status={}", planId, status);

        try {
            LambdaUpdateWrapper<PlanDriverAssignments> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PlanDriverAssignments::getPlanId, planId)
                    .set(PlanDriverAssignments::getStatus, status)
                    .set(PlanDriverAssignments::getUpdatedAt, new Date());

            return update(wrapper);
        } catch (Exception e) {
            log.error("批量更新驾驶员分配状态异常", e);
            throw new RuntimeException("批量更新驾驶员分配状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateActualTimes(Long planId, Long driverId, Date actualStartTime, Date actualEndTime) {
        log.info("更新驾驶员实际时间: planId={}, driverId={}, start={}, end={}",
                planId, driverId, actualStartTime, actualEndTime);

        try {
            LambdaQueryWrapper<PlanDriverAssignments> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PlanDriverAssignments::getPlanId, planId)
                    .eq(PlanDriverAssignments::getDriverId, driverId);

            PlanDriverAssignments assignment = getOne(queryWrapper);
            if (assignment == null) {
                log.warn("驾驶员分配记录不存在: planId={}, driverId={}", planId, driverId);
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
            log.error("更新驾驶员实际时间异常", e);
            throw new RuntimeException("更新驾驶员实际时间失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean checkDriverAvailable(Long driverId, Date startTime, Date endTime) {
        log.info("检查驾驶员可用性: driverId={}, startTime={}, endTime={}", driverId, startTime, endTime);

        try {
            LambdaQueryWrapper<PlanDriverAssignments> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PlanDriverAssignments::getDriverId, driverId)
                    .in(PlanDriverAssignments::getStatus,
                            AssignmentStatusEnum.ASSIGNED.getCode(),
                            AssignmentStatusEnum.CONFIRMED.getCode())
                    .and(w -> w
                            .and(q -> q
                                    .le(PlanDriverAssignments::getEstimatedStartTime, endTime)
                                    .ge(PlanDriverAssignments::getEstimatedEndTime, startTime))
                            .or(q -> q
                                    .le(PlanDriverAssignments::getActualStartTime, endTime)
                                    .ge(PlanDriverAssignments::getActualEndTime, startTime))
                    );

            long count = count(wrapper);

            boolean available = count == 0;
            log.info("驾驶员可用性检查结果: driverId={}, available={}, conflictingCount={}",
                    driverId, available, count);

            return available;
        } catch (Exception e) {
            log.error("检查驾驶员可用性异常", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createBatchAssignments(Long planId, List<PlanDriverAssignmentDTO> assignments) {
        log.info("批量创建驾驶员分配: planId={}, 数量={}", planId, assignments.size());

        try {
            for (PlanDriverAssignmentDTO dto : assignments) {
                PlanDriverAssignments assignment = new PlanDriverAssignments();
                assignment.setPlanId(planId);
                assignment.setDriverId(dto.getDriverId());
                assignment.setVehicleAssignmentId(dto.getVehicleAssignmentId());
                assignment.setTaskType(dto.getTaskType());
                assignment.setEstimatedStartTime(dto.getEstimatedStartTime());
                assignment.setEstimatedEndTime(dto.getEstimatedEndTime());
                assignment.setStatus(dto.getStatus() != null ? dto.getStatus() : AssignmentStatusEnum.ASSIGNED.getCode());

                save(assignment);
            }

            log.info("批量创建驾驶员分配成功");
            return true;
        } catch (Exception e) {
            log.error("批量创建驾驶员分配异常", e);
            throw new RuntimeException("批量创建驾驶员分配失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDriverWorkingHours(Long assignmentId, Double workingHours) {
        log.info("更新驾驶员出勤信息: assignmentId={}, workingHours={}", assignmentId, workingHours);

        try {
            PlanDriverAssignments assignment = getById(assignmentId);
            if (assignment == null) {
                log.warn("驾驶员分配记录不存在: assignmentId={}", assignmentId);
                return false;
            }

            assignment.setWorkingHours(BigDecimal.valueOf(workingHours));

            return updateById(assignment);
        } catch (Exception e) {
            log.error("更新驾驶员出勤信息异常", e);
            throw new RuntimeException("更新驾驶员出勤信息失败: " + e.getMessage(), e);
        }
    }
}