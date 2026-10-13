package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.base.TugboatAssignmentDTO;
import syy.portPilotage.entity.PlanTugboatAssignments;
import syy.portPilotage.enums.AssignmentStatusEnum;
import syy.portPilotage.service.PlanTugboatAssignmentsService;
import syy.portPilotage.mapper.PlanTugboatAssignmentsMapper;
import org.springframework.stereotype.Service;
import syy.portPilotage.service.TugboatsService;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanTugboatAssignmentsServiceImpl extends ServiceImpl<PlanTugboatAssignmentsMapper, PlanTugboatAssignments>
    implements PlanTugboatAssignmentsService{

    private final PlanTugboatAssignmentsMapper planTugboatAssignmentsMapper;
    private final TugboatsService tugboatsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAssignmentStatus(Long planId, Long tugboatId, String status) {
        log.info("更新拖轮分配状态: planId={}, tugboatId={}, status={}",
                planId, tugboatId, status);

        try {
            if (!AssignmentStatusEnum.isValid(status)) {
                throw new RuntimeException("无效的分配状态: " + status);
            }

            LambdaQueryWrapper<PlanTugboatAssignments> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PlanTugboatAssignments::getPlanId, planId)
                    .eq(PlanTugboatAssignments::getTugboatId, tugboatId);

            PlanTugboatAssignments assignment = getOne(queryWrapper);
            if (assignment == null) {
                log.warn("拖轮分配记录不存在: planId={}, tugboatId={}", planId, tugboatId);
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
                log.info("拖轮分配状态更新成功");
                return true;
            } else {
                log.warn("拖轮分配状态更新失败");
                return false;
            }
        } catch (Exception e) {
            log.error("拖轮分配状态更新异常", e);
            throw new RuntimeException("更新拖轮分配状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateAssignmentStatus(Long planId, String status) {
        log.info("批量更新拖轮分配状态: planId={}, status={}", planId, status);

        try {
            if (!AssignmentStatusEnum.isValid(status)) {
                throw new RuntimeException("无效的分配状态: " + status);
            }

            LambdaUpdateWrapper<PlanTugboatAssignments> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PlanTugboatAssignments::getPlanId, planId)
                    .set(PlanTugboatAssignments::getStatus, status)
                    .set(PlanTugboatAssignments::getUpdatedAt, new Date());

            // 如果状态是COMPLETED，设置实际结束时间
            if (AssignmentStatusEnum.COMPLETED.getCode().equals(status)) {
                wrapper.set(PlanTugboatAssignments::getActualEndTime, new Date());
            }

            boolean result = update(wrapper);

            if (result) {
                log.info("批量更新拖轮分配状态成功: planId={}", planId);
            } else {
                log.warn("批量更新拖轮分配状态失败: planId={}", planId);
            }

            return result;
        } catch (Exception e) {
            log.error("批量更新拖轮分配状态异常", e);
            throw new RuntimeException("批量更新拖轮分配状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateActualTimes(Long planId, Long tugboatId, Date actualStartTime, Date actualEndTime) {
        log.info("更新拖轮实际时间: planId={}, tugboatId={}, start={}, end={}",
                planId, tugboatId, actualStartTime, actualEndTime);

        try {
            LambdaQueryWrapper<PlanTugboatAssignments> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PlanTugboatAssignments::getPlanId, planId)
                    .eq(PlanTugboatAssignments::getTugboatId, tugboatId);

            PlanTugboatAssignments assignment = getOne(queryWrapper);
            if (assignment == null) {
                log.warn("拖轮分配记录不存在: planId={}, tugboatId={}", planId, tugboatId);
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
                log.info("拖轮实际时间更新成功");
            } else {
                log.warn("拖轮实际时间更新失败");
            }

            return result;
        } catch (Exception e) {
            log.error("更新拖轮实际时间异常", e);
            throw new RuntimeException("更新拖轮实际时间失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean checkTugboatAvailable(Long tugboatId, Date startTime, Date endTime) {
        log.info("检查拖轮可用性: tugboatId={}, startTime={}, endTime={}",
                tugboatId, startTime, endTime);

        try {
            boolean existsAndAvailable = tugboatsService.lambdaQuery()
                    .eq(syy.portPilotage.entity.Tugboats::getId, tugboatId)
                    .eq(syy.portPilotage.entity.Tugboats::getStatus, "AVAILABLE")
                    .exists();

            if (!existsAndAvailable) {
                log.info("拖轮不存在或不可用: tugboatId={}", tugboatId);
                return false;
            }

            LambdaQueryWrapper<PlanTugboatAssignments> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PlanTugboatAssignments::getTugboatId, tugboatId)
                    .in(PlanTugboatAssignments::getStatus,
                            AssignmentStatusEnum.ASSIGNED.getCode(),
                            AssignmentStatusEnum.CONFIRMED.getCode())
                    .and(w -> w
                            .and(q -> q
                                    .le(PlanTugboatAssignments::getEstimatedStartTime, endTime)
                                    .ge(PlanTugboatAssignments::getEstimatedEndTime, startTime))
                            .or(q -> q
                                    .le(PlanTugboatAssignments::getActualStartTime, endTime)
                                    .ge(PlanTugboatAssignments::getActualEndTime, startTime))
                    );

            long count = count(wrapper);

            boolean available = count == 0;
            log.info("拖轮可用性检查结果: tugboatId={}, available={}, conflictingCount={}",
                    tugboatId, available, count);

            return available;
        } catch (Exception e) {
            log.error("检查拖轮可用性异常", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createBatchAssignments(Long planId, List<TugboatAssignmentDTO> assignments) {
        log.info("批量创建拖轮分配: planId={}, 数量={}", planId, assignments.size());

        try {
            for (TugboatAssignmentDTO dto : assignments) {
                if (dto.getEstimatedStartTime() != null && dto.getEstimatedEndTime() != null) {
                    boolean available = checkTugboatAvailable(
                            dto.getTugboatId(),
                            dto.getEstimatedStartTime(),
                            dto.getEstimatedEndTime());

                    if (!available) {
                        log.warn("拖轮不可用: tugboatId={}, 时间范围: {} - {}",
                                dto.getTugboatId(),
                                dto.getEstimatedStartTime(),
                                dto.getEstimatedEndTime());
                        throw new RuntimeException("拖轮ID " + dto.getTugboatId() + " 在指定时间不可用");
                    }
                }

                PlanTugboatAssignments assignment = new PlanTugboatAssignments();
                assignment.setPlanId(planId);
                assignment.setTugboatId(dto.getTugboatId());
                assignment.setAssignmentType(dto.getAssignmentType());
                assignment.setEstimatedStartTime(dto.getEstimatedStartTime());
                assignment.setEstimatedEndTime(dto.getEstimatedEndTime());
                assignment.setStatus(dto.getStatus() != null ? dto.getStatus() : AssignmentStatusEnum.ASSIGNED.getCode());

                save(assignment);
            }

            log.info("批量创建拖轮分配成功: planId={}", planId);
            return true;
        } catch (Exception e) {
            log.error("批量创建拖轮分配异常", e);
            throw new RuntimeException("批量创建拖轮分配失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PlanTugboatAssignments> getAssignmentsByPlanId(Long planId) {
        log.info("获取计划的拖轮分配列表: planId={}", planId);

        LambdaQueryWrapper<PlanTugboatAssignments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanTugboatAssignments::getPlanId, planId)
                .orderByAsc(PlanTugboatAssignments::getEstimatedStartTime);

        return list(wrapper);
    }

    @Override
    public List<PlanTugboatAssignments> getAssignmentsByTugboatId(Long tugboatId, Date startTime, Date endTime) {
        log.info("根据拖轮ID获取分配记录: tugboatId={}, startTime={}, endTime={}",
                tugboatId, startTime, endTime);

        LambdaQueryWrapper<PlanTugboatAssignments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanTugboatAssignments::getTugboatId, tugboatId)
                .ge(startTime != null, PlanTugboatAssignments::getEstimatedStartTime, startTime)
                .le(endTime != null, PlanTugboatAssignments::getEstimatedEndTime, endTime)
                .orderByAsc(PlanTugboatAssignments::getEstimatedStartTime);

        return list(wrapper);
    }
}
