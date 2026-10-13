package syy.portPilotage.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.plan.PlanPilotAssignmentDTO;
import syy.portPilotage.entity.PlanPilotAssignments;
import syy.portPilotage.enums.AssignmentStatusEnum;
import syy.portPilotage.mapper.PlanPilotAssignmentsMapper;
import syy.portPilotage.service.PlanPilotAssignmentsService;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanPilotAssignmentsServiceImpl extends ServiceImpl<PlanPilotAssignmentsMapper, PlanPilotAssignments> implements PlanPilotAssignmentsService {

    private final PlanPilotAssignmentsMapper planPilotAssignmentsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAssignmentStatus(Long planId, Long pilotId, String status) {
        log.info("更新引航员分配状态: planId={}, pilotId={}, status={}", planId, pilotId, status);

        try {
            LambdaUpdateWrapper<PlanPilotAssignments> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PlanPilotAssignments::getPlanId, planId)
                    .eq(PlanPilotAssignments::getPilotId, pilotId)
                    .set(PlanPilotAssignments::getStatus, status)
                    .set(PlanPilotAssignments::getUpdatedAt, new Date());

            boolean result = update(wrapper);

            if (result) {
                log.info("引航员分配状态更新成功");

                // 如果状态是COMPLETED且还没有实际结束时间，设置实际结束时间
                if (AssignmentStatusEnum.COMPLETED.getCode().equals(status)) {
                    updateActualTimes(planId, pilotId, null, new Date());
                }

                return true;
            } else {
                log.warn("引航员分配状态更新失败: 未找到匹配的记录");
                return false;
            }
        } catch (Exception e) {
            log.error("引航员分配状态更新异常", e);
            throw new RuntimeException("更新引航员分配状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateAssignmentStatus(Long planId, String status) {
        log.info("批量更新引航员分配状态: planId={}, status={}", planId, status);

        try {
            LambdaUpdateWrapper<PlanPilotAssignments> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PlanPilotAssignments::getPlanId, planId)
                    .set(PlanPilotAssignments::getStatus, status)
                    .set(PlanPilotAssignments::getUpdatedAt, new Date());

            return update(wrapper);
        } catch (Exception e) {
            log.error("批量更新引航员分配状态异常", e);
            throw new RuntimeException("批量更新引航员分配状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateActualTimes(Long planId, Long pilotId, Date actualStartTime, Date actualEndTime) {
        log.info("更新引航员实际时间: planId={}, pilotId={}, start={}, end={}",
                planId, pilotId, actualStartTime, actualEndTime);

        try {
            LambdaQueryWrapper<PlanPilotAssignments> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PlanPilotAssignments::getPlanId, planId)
                    .eq(PlanPilotAssignments::getPilotId, pilotId);

            PlanPilotAssignments assignment = getOne(queryWrapper);
            if (assignment == null) {
                log.warn("引航员分配记录不存在: planId={}, pilotId={}", planId, pilotId);
                return false;
            }

            if (actualStartTime != null) {
                assignment.setActualStartTime(actualStartTime);
            }
            if (actualEndTime != null) {
                assignment.setActualEndTime(actualEndTime);
            }

            // 如果实际开始和结束时间都已设置，且状态不是COMPLETED，则更新状态为COMPLETED
            if (assignment.getActualStartTime() != null &&
                    assignment.getActualEndTime() != null &&
                    !AssignmentStatusEnum.COMPLETED.getCode().equals(assignment.getStatus())) {
                assignment.setStatus(AssignmentStatusEnum.COMPLETED.getCode());
            }

            return updateById(assignment);
        } catch (Exception e) {
            log.error("更新引航员实际时间异常", e);
            throw new RuntimeException("更新引航员实际时间失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean checkPilotAvailable(Long pilotId, Date startTime, Date endTime) {
        log.info("检查引航员可用性: pilotId={}, startTime={}, endTime={}", pilotId, startTime, endTime);

        try {
            LambdaQueryWrapper<PlanPilotAssignments> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PlanPilotAssignments::getPilotId, pilotId)
                    .in(PlanPilotAssignments::getStatus,
                            AssignmentStatusEnum.ASSIGNED.getCode(),
                            AssignmentStatusEnum.CONFIRMED.getCode())
                    .and(w -> w
                            .and(q -> q
                                    .le(PlanPilotAssignments::getEstimatedStartTime, endTime)
                                    .ge(PlanPilotAssignments::getEstimatedEndTime, startTime))
                            .or(q -> q
                                    .le(PlanPilotAssignments::getActualStartTime, endTime)
                                    .ge(PlanPilotAssignments::getActualEndTime, startTime))
                    );

            long count = count(wrapper);

            boolean available = count == 0;
            log.info("引航员可用性检查结果: pilotId={}, available={}, conflictingCount={}",
                    pilotId, available, count);

            return available;
        } catch (Exception e) {
            log.error("检查引航员可用性异常", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createBatchAssignments(Long planId, List<PlanPilotAssignmentDTO> assignments) {
        log.info("批量创建引航员分配: planId={}, 数量={}", planId, assignments.size());

        try {
            for (PlanPilotAssignmentDTO dto : assignments) {
                PlanPilotAssignments assignment = new PlanPilotAssignments();
                assignment.setPlanId(planId);
                assignment.setPilotId(dto.getPilotId());
                assignment.setRoleType(dto.getRoleType());
                assignment.setAssignmentNotes(dto.getAssignmentNotes());
                assignment.setEstimatedStartTime(dto.getEstimatedStartTime());
                assignment.setEstimatedEndTime(dto.getEstimatedEndTime());
                assignment.setStatus(dto.getStatus() != null ? dto.getStatus() : AssignmentStatusEnum.ASSIGNED.getCode());
                assignment.setAssessmentFlag(dto.getAssessmentFlag() != null ? dto.getAssessmentFlag() : true);

                save(assignment);
            }

            log.info("批量创建引航员分配成功");
            return true;
        } catch (Exception e) {
            log.error("批量创建引航员分配异常", e);
            throw new RuntimeException("批量创建引航员分配失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAssessmentInfo(Long assignmentId, Integer score, String notes) {
        log.info("更新引航员考核信息: assignmentId={}, score={}", assignmentId, score);

        try {
            PlanPilotAssignments assignment = getById(assignmentId);
            if (assignment == null) {
                log.warn("引航员分配记录不存在: assignmentId={}", assignmentId);
                return false;
            }

            assignment.setAssessmentScore(score);
            assignment.setAssessmentNotes(notes);

            return updateById(assignment);
        } catch (Exception e) {
            log.error("更新引航员考核信息异常", e);
            throw new RuntimeException("更新引航员考核信息失败: " + e.getMessage(), e);
        }
    }
}