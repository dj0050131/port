package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.plan.PilotBoatAssignmentDTO;
import syy.portPilotage.entity.PlanPilotBoatAssignments;
import syy.portPilotage.enums.AssignmentStatusEnum;
import syy.portPilotage.service.PilotBoatsService;
import syy.portPilotage.service.PlanPilotBoatAssignmentsService;
import syy.portPilotage.mapper.PlanPilotBoatAssignmentsMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanPilotBoatAssignmentsServiceImpl extends ServiceImpl<PlanPilotBoatAssignmentsMapper, PlanPilotBoatAssignments>
    implements PlanPilotBoatAssignmentsService{

    private final PlanPilotBoatAssignmentsMapper planPilotBoatAssignmentsMapper;
    private final PilotBoatsService pilotBoatsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAssignmentStatus(Long planId, Long pilotBoatId, String status) {
        log.info("更新引航艇分配状态: planId={}, pilotBoatId={}, status={}",
                planId, pilotBoatId, status);

        try {
            if (!AssignmentStatusEnum.isValid(status)) {
                throw new RuntimeException("无效的分配状态: " + status);
            }

            LambdaQueryWrapper<PlanPilotBoatAssignments> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PlanPilotBoatAssignments::getPlanId, planId)
                    .eq(PlanPilotBoatAssignments::getPilotBoatId, pilotBoatId);

            PlanPilotBoatAssignments assignment = getOne(queryWrapper);
            if (assignment == null) {
                log.warn("引航艇分配记录不存在: planId={}, pilotBoatId={}", planId, pilotBoatId);
                return false;
            }

            assignment.setStatus(status);
            assignment.setUpdatedAt(new Date());

            // 如果状态是COMPLETED且还没有实际结束时间，设置实际结束时间
            if (AssignmentStatusEnum.COMPLETED.getCode().equals(status) &&
                    assignment.getActualEndTime() == null) {
                assignment.setActualEndTime(new Date());
            }

            boolean result = updateById(assignment);

            if (result) {
                log.info("引航艇分配状态更新成功");
                return true;
            } else {
                log.warn("引航艇分配状态更新失败");
                return false;
            }
        } catch (Exception e) {
            log.error("引航艇分配状态更新异常", e);
            throw new RuntimeException("更新引航艇分配状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateAssignmentStatus(Long planId, String status) {
        log.info("批量更新引航艇分配状态: planId={}, status={}", planId, status);

        try {
            if (!AssignmentStatusEnum.isValid(status)) {
                throw new RuntimeException("无效的分配状态: " + status);
            }

            LambdaUpdateWrapper<PlanPilotBoatAssignments> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PlanPilotBoatAssignments::getPlanId, planId)
                    .set(PlanPilotBoatAssignments::getStatus, status)
                    .set(PlanPilotBoatAssignments::getUpdatedAt, new Date());

            // 如果状态是COMPLETED，设置实际结束时间
            if (AssignmentStatusEnum.COMPLETED.getCode().equals(status)) {
                wrapper.set(PlanPilotBoatAssignments::getActualEndTime, new Date());
            }

            boolean result = update(wrapper);

            if (result) {
                log.info("批量更新引航艇分配状态成功: planId={}", planId);
            } else {
                log.warn("批量更新引航艇分配状态失败: planId={}", planId);
            }

            return result;
        } catch (Exception e) {
            log.error("批量更新引航艇分配状态异常", e);
            throw new RuntimeException("批量更新引航艇分配状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateActualTimes(Long planId, Long pilotBoatId, Date actualStartTime, Date actualEndTime) {
        log.info("更新引航艇实际时间: planId={}, pilotBoatId={}, start={}, end={}",
                planId, pilotBoatId, actualStartTime, actualEndTime);

        try {
            LambdaQueryWrapper<PlanPilotBoatAssignments> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PlanPilotBoatAssignments::getPlanId, planId)
                    .eq(PlanPilotBoatAssignments::getPilotBoatId, pilotBoatId);

            PlanPilotBoatAssignments assignment = getOne(queryWrapper);
            if (assignment == null) {
                log.warn("引航艇分配记录不存在: planId={}, pilotBoatId={}", planId, pilotBoatId);
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

            assignment.setUpdatedAt(new Date());

            boolean result = updateById(assignment);

            if (result) {
                log.info("引航艇实际时间更新成功");
            } else {
                log.warn("引航艇实际时间更新失败");
            }

            return result;
        } catch (Exception e) {
            log.error("更新引航艇实际时间异常", e);
            throw new RuntimeException("更新引航艇实际时间失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean checkPilotBoatAvailable(Long pilotBoatId, Date startTime, Date endTime) {
        log.info("检查引航艇可用性: pilotBoatId={}, startTime={}, endTime={}",
                pilotBoatId, startTime, endTime);

        try {
            boolean existsAndAvailable = pilotBoatsService.lambdaQuery()
                    .eq(syy.portPilotage.entity.PilotBoats::getId, pilotBoatId)
                    .eq(syy.portPilotage.entity.PilotBoats::getCurrentStatus, "AVAILABLE")
                    .exists();

            if (!existsAndAvailable) {
                log.info("引航艇不存在或不可用: pilotBoatId={}", pilotBoatId);
                return false;
            }

            LambdaQueryWrapper<PlanPilotBoatAssignments> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PlanPilotBoatAssignments::getPilotBoatId, pilotBoatId)
                    .in(PlanPilotBoatAssignments::getStatus,
                            AssignmentStatusEnum.ASSIGNED.getCode(),
                            AssignmentStatusEnum.CONFIRMED.getCode())
                    .and(w -> w
                            .and(q -> q
                                    .le(PlanPilotBoatAssignments::getEstimatedStartTime, endTime)
                                    .ge(PlanPilotBoatAssignments::getEstimatedEndTime, startTime))
                            .or(q -> q
                                    .le(PlanPilotBoatAssignments::getActualStartTime, endTime)
                                    .ge(PlanPilotBoatAssignments::getActualEndTime, startTime))
                    );

            long count = count(wrapper);

            boolean available = count == 0;
            log.info("引航艇可用性检查结果: pilotBoatId={}, available={}, conflictingCount={}",
                    pilotBoatId, available, count);

            return available;
        } catch (Exception e) {
            log.error("检查引航艇可用性异常", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createBatchAssignments(Long planId, List<PilotBoatAssignmentDTO> assignments) {
        log.info("批量创建引航艇分配: planId={}, 数量={}", planId, assignments.size());

        try {
            for (PilotBoatAssignmentDTO dto : assignments) {
                if (dto.getEstimatedStartTime() != null && dto.getEstimatedEndTime() != null) {
                    boolean available = checkPilotBoatAvailable(
                            dto.getPilotBoatId(),
                            dto.getEstimatedStartTime(),
                            dto.getEstimatedEndTime());

                    if (!available) {
                        log.warn("引航艇不可用: pilotBoatId={}, 时间范围: {} - {}",
                                dto.getPilotBoatId(),
                                dto.getEstimatedStartTime(),
                                dto.getEstimatedEndTime());
                        throw new RuntimeException("引航艇ID " + dto.getPilotBoatId() + " 在指定时间不可用");
                    }
                }

                PlanPilotBoatAssignments assignment = new PlanPilotBoatAssignments();
                assignment.setPlanId(planId);
                assignment.setPilotBoatId(dto.getPilotBoatId());
                assignment.setPurpose(dto.getPurpose());
                assignment.setEstimatedStartTime(dto.getEstimatedStartTime());
                assignment.setEstimatedEndTime(dto.getEstimatedEndTime());
                assignment.setStatus(dto.getStatus() != null ? dto.getStatus() : AssignmentStatusEnum.ASSIGNED.getCode());

                save(assignment);
            }

            log.info("批量创建引航艇分配成功: planId={}", planId);
            return true;
        } catch (Exception e) {
            log.error("批量创建引航艇分配异常", e);
            throw new RuntimeException("批量创建引航艇分配失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PlanPilotBoatAssignments> getAssignmentsByPlanId(Long planId) {
        log.info("获取计划的引航艇分配列表: planId={}", planId);

        LambdaQueryWrapper<PlanPilotBoatAssignments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanPilotBoatAssignments::getPlanId, planId)
                .orderByAsc(PlanPilotBoatAssignments::getEstimatedStartTime);

        return list(wrapper);
    }

    @Override
    public List<PlanPilotBoatAssignments> getAssignmentsByPilotBoatId(Long pilotBoatId, Date startTime, Date endTime) {
        log.info("根据引航艇ID获取分配记录: pilotBoatId={}, startTime={}, endTime={}",
                pilotBoatId, startTime, endTime);

        LambdaQueryWrapper<PlanPilotBoatAssignments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanPilotBoatAssignments::getPilotBoatId, pilotBoatId)
                .ge(startTime != null, PlanPilotBoatAssignments::getEstimatedStartTime, startTime)
                .le(endTime != null, PlanPilotBoatAssignments::getEstimatedEndTime, endTime)
                .orderByAsc(PlanPilotBoatAssignments::getEstimatedStartTime);

        return list(wrapper);
    }
}
