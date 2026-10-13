package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.base.TugboatAssignmentDTO;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.dto.dispatch.*;
import syy.portPilotage.dto.plan.PilotBoatAssignmentDTO;
import syy.portPilotage.dto.plan.PlanDriverAssignmentDTO;
import syy.portPilotage.dto.plan.PlanPilotAssignmentDTO;
import syy.portPilotage.dto.plan.PlanVehicleAssignmentDTO;
import syy.portPilotage.entity.*;
import syy.portPilotage.enums.PlanStatusEnum;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.event.DispatchCompletedEvent;
import syy.portPilotage.mapper.DispatchMapper;
import syy.portPilotage.service.*;
import syy.portPilotage.util.BeanCopyUtil;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchServiceImpl extends ServiceImpl<DispatchMapper, PilotagePlans> implements DispatchService {

    private final DispatchMapper dispatchMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PilotagePlansService pilotagePlansService;
    private final PlanPilotAssignmentsService planPilotAssignmentsService;
    private final PlanDriverAssignmentsService planDriverAssignmentsService;
    private final PlanTugboatAssignmentsService planTugboatAssignmentsService;
    private final PlanPilotBoatAssignmentsService planPilotBoatAssignmentsService;
    private final PlanVehicleAssignmentsService planVehicleAssignmentsService;
    private final VesselsService vesselsService;
    private final VoyagesService voyagesService;
    private final AgentsService agentsService;
    private final PilotsService pilotsService;
    private final DriversService driversService;
    private final VehiclesService vehiclesService;
    private final TugboatsService tugboatsService;
    private final PilotBoatsService pilotBoatsService;

    @Override
    public Map<String, Object> getDispatchList(DispatchQueryDTO query) {
        log.info("获取调度列表: query={}", query);

        PageQueryDTO pageQuery = new PageQueryDTO();
        pageQuery.setCurrent(query.getPage());
        pageQuery.setSize(query.getSize());

        Map<String, Object> params = new HashMap<>();
        params.put("planStatus", query.getPlanStatus());
        params.put("startDate", query.getStartDate());
        params.put("endDate", query.getEndDate());
        params.put("vesselName", query.getVesselName());
        params.put("planCode", query.getPlanCode());
        params.put("pilotName", query.getPilotName());
        params.put("confirmationLevel", query.getConfirmationLevel());
        pageQuery.setParams(params);

        IPage<DispatchPlanDTO> pageResult = this.getPageWithJoin(pageQuery);

        pageResult.getRecords().forEach(dto -> {
            if (dto.getTotalResourcesCount() != null && dto.getTotalResourcesCount() > 0) {
                double progress = (dto.getConfirmedCount() * 100.0) / dto.getTotalResourcesCount();
                dto.setConfirmationProgress(Math.round(progress * 100.0) / 100.0);
            }

            dto.setDispatchStatus(getDispatchStatusName(dto.getPlanStatus()));
        });

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("pages", pageResult.getPages());

        log.info("调度列表查询完成: 找到{}条记录, 共{}页", pageResult.getRecords().size(), pageResult.getPages());

        return result;
    }

        private IPage<DispatchPlanDTO> getPageWithJoin(PageQueryDTO query) {
        Page<DispatchPlanDTO> page = new Page<>(query.getCurrent(), query.getSize());

        Map<String, Object> paramMap = new HashMap<>();
        if (query.getParams() != null) {
            paramMap.putAll(query.getParams());
        }
        if (!paramMap.containsKey("planStatusFilter")) {
            paramMap.put("planStatusFilter", Arrays.asList("CONFIRMED", "PUBLISHED", "ONGOING","COMPLETED","CANCELLED","CERTIFICATED","BILLED","PAID","REPLANNED"));
        }

        return dispatchMapper.selectDispatchPage(page, paramMap);
    }

    @Override
    public DispatchDetailDTO getDispatchDetail(Long planId) {
        log.info("获取调度详情: planId={}", planId);

        PilotagePlans plan = pilotagePlansService.getById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        DispatchDetailDTO detail = new DispatchDetailDTO();

        PlanBaseInfoDTO planInfo = BeanCopyUtil.copy(plan, PlanBaseInfoDTO.class);
        detail.setPlanInfo(planInfo);

        if (plan.getVesselId() != null) {
            Vessels vessel = vesselsService.getById(plan.getVesselId());
            detail.setVessel(vessel);
        }

        if (plan.getVoyageId() != null) {
            Voyages voyage = voyagesService.getById(plan.getVoyageId());
            detail.setVoyage(voyage);

            if (voyage != null && voyage.getAgentId() != null) {
                Agents agent = agentsService.getById(voyage.getAgentId());
                detail.setAgent(agent);
            }
        }

        LambdaQueryWrapper<PlanPilotAssignments> pilotWrapper = new LambdaQueryWrapper<>();
        pilotWrapper.eq(PlanPilotAssignments::getPlanId, planId)
                .ne(PlanPilotAssignments::getStatus, "CANCELLED");
        List<PlanPilotAssignments> pilotAssignments = planPilotAssignmentsService.list(pilotWrapper);
        detail.setPilotAssignments(pilotAssignments.stream()
                .map(this::convertToPilotAssignmentDTO)
                .collect(Collectors.toList()));

        LambdaQueryWrapper<PlanDriverAssignments> driverWrapper = new LambdaQueryWrapper<>();
        driverWrapper.eq(PlanDriverAssignments::getPlanId, planId)
                .ne(PlanDriverAssignments::getStatus, "CANCELLED");
        List<PlanDriverAssignments> driverAssignments = planDriverAssignmentsService.list(driverWrapper);
        detail.setDriverAssignments(driverAssignments.stream()
                .map(this::convertToDriverAssignmentDTO)
                .collect(Collectors.toList()));

        LambdaQueryWrapper<PlanTugboatAssignments> tugboatWrapper = new LambdaQueryWrapper<>();
        tugboatWrapper.eq(PlanTugboatAssignments::getPlanId, planId)
                .ne(PlanTugboatAssignments::getStatus, "CANCELLED");
        List<PlanTugboatAssignments> tugboatAssignments = planTugboatAssignmentsService.list(tugboatWrapper);
        detail.setTugboatAssignments(tugboatAssignments.stream()
                .map(this::convertToTugboatAssignmentDTO)
                .collect(Collectors.toList()));

        LambdaQueryWrapper<PlanPilotBoatAssignments> pilotBoatWrapper = new LambdaQueryWrapper<>();
        pilotBoatWrapper.eq(PlanPilotBoatAssignments::getPlanId, planId)
                .ne(PlanPilotBoatAssignments::getStatus, "CANCELLED");
        List<PlanPilotBoatAssignments> pilotBoatAssignments = planPilotBoatAssignmentsService.list(pilotBoatWrapper);
        detail.setPilotBoatAssignments(pilotBoatAssignments.stream()
                .map(this::convertToPilotBoatAssignmentDTO)
                .collect(Collectors.toList()));

        LambdaQueryWrapper<PlanVehicleAssignments> vehicleWrapper = new LambdaQueryWrapper<>();
        vehicleWrapper.eq(PlanVehicleAssignments::getPlanId, planId)
                .ne(PlanVehicleAssignments::getStatus, "CANCELLED");
        List<PlanVehicleAssignments> vehicleAssignments = planVehicleAssignmentsService.list(vehicleWrapper);
        detail.setVehicleAssignments(vehicleAssignments.stream()
                .map(this::convertToVehicleAssignmentDTO)
                .collect(Collectors.toList()));

        Map<String, Object> stats = dispatchMapper.selectPlanConfirmationStats(planId);
        if (stats != null && stats.get("total_count") != null && stats.get("confirmed_count") != null) {
            Number totalCount = (Number) stats.get("total_count");
            Number confirmedCount = (Number) stats.get("confirmed_count");
            if (totalCount.intValue() > 0) {
                double progress = (confirmedCount.doubleValue() * 100.0) / totalCount.doubleValue();
                detail.setConfirmationProgress(Math.round(progress * 100.0) / 100.0);
            }
        }

        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishDispatch(Long planId) {
        log.info("发布调度: planId={}", planId);

        PilotagePlans plan = pilotagePlansService.getById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        if (!PlanStatusEnum.CONFIRMED.getCode().equals(plan.getPlanStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID,
                    "只有已确认的计划才能发布调度");
        }

        Map<String, Object> stats = dispatchMapper.selectPlanConfirmationStats(planId);
        Number totalCount = (Number) stats.get("total_count");
        Number confirmedCount = (Number) stats.get("confirmed_count");

        if (totalCount != null && confirmedCount != null &&
                confirmedCount.intValue() < totalCount.intValue()) {
            // 如果还有未确认的资源，可以记录日志或给出警告
            log.warn("计划{}还有未确认的资源: {}/{}", planId, confirmedCount, totalCount);
            // 可以根据业务需求决定是否允许发布
        }

        plan.setPlanStatus(PlanStatusEnum.PUBLISHED.getCode());
        plan.setPublishedAt(new Date());
        plan.setUpdatedAt(new Date());

        boolean success = pilotagePlansService.updateById(plan);

        if (success) {
            log.info("调度发布成功: planId={}, planCode={}", planId, plan.getPlanCode());

            recordDispatchOperation(planId, "PUBLISH",
                    PlanStatusEnum.CONFIRMED.getCode(), PlanStatusEnum.PUBLISHED.getCode(),
                    "调度发布");
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startDispatch(Long planId, DispatchTimeUpdateDTO dto) {
        log.info("开始调度: planId={}, dto={}", planId, dto);

        PilotagePlans plan = pilotagePlansService.getById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        if (!PlanStatusEnum.CONFIRMED.getCode().equals(plan.getPlanStatus())
                && !PlanStatusEnum.PUBLISHED.getCode().equals(plan.getPlanStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID,
                    "只有已确认或已发布的计划才能开始调度");
        }

        if (dto.getActualStartTime() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "开始时间不能为空");
        }

        plan.setPlanStatus(PlanStatusEnum.ONGOING.getCode());
        plan.setActualStartTime(dto.getActualStartTime());

        // 如果结束时间也提供了，一并设置（可能用于直接完成的情况）
        if (dto.getActualEndTime() != null) {
            plan.setActualEndTime(dto.getActualEndTime());
        }

        plan.setUpdatedAt(new Date());

        boolean success = pilotagePlansService.updateById(plan);

        if (success) {
            log.info("调度开始成功: planId={}, planCode={}, startTime={}",
                    planId, plan.getPlanCode(), dto.getActualStartTime());

            recordDispatchOperation(planId, "START",
                    plan.getPlanStatus(), PlanStatusEnum.ONGOING.getCode(),
                    String.format("调度开始，开始时间: %s", dto.getActualStartTime()));
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeDispatch(Long planId, DispatchTimeUpdateDTO dto) {
        log.info("完成调度: planId={}, dto={}", planId, dto);

        PilotagePlans plan = pilotagePlansService.getById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        if (!PlanStatusEnum.ONGOING.getCode().equals(plan.getPlanStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID,
                    "只有进行中的计划才能完成调度");
        }

        if (dto.getActualEndTime() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "结束时间不能为空");
        }

        plan.setPlanStatus(PlanStatusEnum.COMPLETED.getCode());
        plan.setActualEndTime(dto.getActualEndTime());

        // 如果开始时间也需要更新，也一并设置
        if (dto.getActualStartTime() != null) {
            plan.setActualStartTime(dto.getActualStartTime());
        }

        plan.setUpdatedAt(new Date());

        boolean success = pilotagePlansService.updateById(plan);

        if (success) {
            log.info("调度完成成功: planId={}, planCode={}, endTime={}",
                    planId, plan.getPlanCode(), dto.getActualEndTime());

            recordDispatchOperation(planId, "COMPLETE",
                    PlanStatusEnum.ONGOING.getCode(), PlanStatusEnum.COMPLETED.getCode(),
                    String.format("调度完成，结束时间: %s", dto.getActualEndTime()));

            // 调度完成意味着资源实际已执行，兜底把 ASSIGNED/空状态的分配记录补齐为 CONFIRMED，确保数据闭环一致。
            try {
                autoConfirmAssignmentsAfterDispatchComplete(planId);
            } catch (Exception e) {
                log.error("调度完成后自动确认资源失败: planId={}, error={}", planId, e.getMessage(), e);
            }

            try {
                eventPublisher.publishEvent(new DispatchCompletedEvent(planId));
            } catch (Exception e) {
                log.error("发布调度完成事件失败: planId={}, error={}", planId, e.getMessage(), e);
            }

            try {
                releaseResourcesAfterDispatchComplete(plan);
            } catch (Exception e) {
                log.error("调度完成后释放资源失败: planId={}, error={}", planId, e.getMessage(), e);
            }
        }

        return success;
    }

    private void autoConfirmAssignmentsAfterDispatchComplete(Long planId) {
        if (planId == null) return;
        Date now = new Date();

        // 只确认 ASSIGNED/空状态，避免覆盖 CANCELLED/COMPLETED 等状态
        planPilotAssignmentsService.update(new LambdaUpdateWrapper<PlanPilotAssignments>()
                .eq(PlanPilotAssignments::getPlanId, planId)
                .and(w -> w.isNull(PlanPilotAssignments::getStatus)
                        .or()
                        .eq(PlanPilotAssignments::getStatus, "ASSIGNED")
                        .or()
                        .eq(PlanPilotAssignments::getStatus, ""))
                .set(PlanPilotAssignments::getStatus, "CONFIRMED")
                .set(PlanPilotAssignments::getUpdatedAt, now));

        planDriverAssignmentsService.update(new LambdaUpdateWrapper<PlanDriverAssignments>()
                .eq(PlanDriverAssignments::getPlanId, planId)
                .and(w -> w.isNull(PlanDriverAssignments::getStatus)
                        .or()
                        .eq(PlanDriverAssignments::getStatus, "ASSIGNED")
                        .or()
                        .eq(PlanDriverAssignments::getStatus, ""))
                .set(PlanDriverAssignments::getStatus, "CONFIRMED")
                .set(PlanDriverAssignments::getUpdatedAt, now));

        planVehicleAssignmentsService.update(new LambdaUpdateWrapper<PlanVehicleAssignments>()
                .eq(PlanVehicleAssignments::getPlanId, planId)
                .and(w -> w.isNull(PlanVehicleAssignments::getStatus)
                        .or()
                        .eq(PlanVehicleAssignments::getStatus, "ASSIGNED")
                        .or()
                        .eq(PlanVehicleAssignments::getStatus, ""))
                .set(PlanVehicleAssignments::getStatus, "CONFIRMED")
                .set(PlanVehicleAssignments::getUpdatedAt, now));

        planTugboatAssignmentsService.update(new LambdaUpdateWrapper<PlanTugboatAssignments>()
                .eq(PlanTugboatAssignments::getPlanId, planId)
                .and(w -> w.isNull(PlanTugboatAssignments::getStatus)
                        .or()
                        .eq(PlanTugboatAssignments::getStatus, "ASSIGNED")
                        .or()
                        .eq(PlanTugboatAssignments::getStatus, ""))
                .set(PlanTugboatAssignments::getStatus, "CONFIRMED")
                .set(PlanTugboatAssignments::getUpdatedAt, now));

        planPilotBoatAssignmentsService.update(new LambdaUpdateWrapper<PlanPilotBoatAssignments>()
                .eq(PlanPilotBoatAssignments::getPlanId, planId)
                .and(w -> w.isNull(PlanPilotBoatAssignments::getStatus)
                        .or()
                        .eq(PlanPilotBoatAssignments::getStatus, "ASSIGNED")
                        .or()
                        .eq(PlanPilotBoatAssignments::getStatus, ""))
                .set(PlanPilotBoatAssignments::getStatus, "CONFIRMED")
                .set(PlanPilotBoatAssignments::getUpdatedAt, now));
    }

    private void releaseResourcesAfterDispatchComplete(PilotagePlans plan) {
        if (plan == null) return;
        Date now = new Date();

        if (plan.getAssignedPilotId() != null) {
            Pilots pilot = pilotsService.getById(plan.getAssignedPilotId());
            if (pilot != null) {
                pilot.setOnDutyStatus("ON_DUTY");
                pilot.setUpdatedAt(now);
                pilotsService.updateById(pilot);
            }
        }
        if (plan.getAssignedDriverId() != null) {
            Drivers driver = driversService.getById(plan.getAssignedDriverId());
            if (driver != null) {
                driver.setOnDutyStatus("ON_DUTY");
                driver.setUpdatedAt(now);
                driversService.updateById(driver);
            }
        }

        // 车辆
        if (plan.getAssignedVehicleId() != null) {
            Vehicles vehicle = vehiclesService.getById(plan.getAssignedVehicleId());
            if (vehicle != null) {
                vehicle.setVehicleStatus("AVAILABLE");
                vehicle.setUpdatedAt(now);
                vehiclesService.updateById(vehicle);
            }
        }

        LambdaQueryWrapper<PlanTugboatAssignments> tugboatWrapper = new LambdaQueryWrapper<>();
        tugboatWrapper.eq(PlanTugboatAssignments::getPlanId, plan.getId());
        List<PlanTugboatAssignments> tugboatAssignments = planTugboatAssignmentsService.list(tugboatWrapper);
        for (PlanTugboatAssignments a : tugboatAssignments) {
            // 只更新状态字段，避免 updateById 写入 build_year 等字段导致截断
            tugboatsService.update(new LambdaUpdateWrapper<Tugboats>()
                    .eq(Tugboats::getId, a.getTugboatId())
                    .set(Tugboats::getStatus, "AVAILABLE")
                    .set(Tugboats::getUpdatedAt, now));
        }

        LambdaQueryWrapper<PlanPilotBoatAssignments> pilotBoatWrapper = new LambdaQueryWrapper<>();
        pilotBoatWrapper.eq(PlanPilotBoatAssignments::getPlanId, plan.getId());
        List<PlanPilotBoatAssignments> pilotBoatAssignments = planPilotBoatAssignmentsService.list(pilotBoatWrapper);
        for (PlanPilotBoatAssignments a : pilotBoatAssignments) {
            PilotBoats boat = pilotBoatsService.getById(a.getPilotBoatId());
            if (boat != null) {
                boat.setCurrentStatus("AVAILABLE");
                boat.setUpdatedAt(now);
                pilotBoatsService.updateById(boat);
            }
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelDispatch(Long planId, String reason) {
        log.info("取消调度: planId={}, reason={}", planId, reason);

        // 这里调用原有的取消计划方法
        return pilotagePlansService.cancelPlan(planId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDispatchTime(Long planId, DispatchTimeUpdateDTO dto) {
        log.info("更新调度时间: planId={}, dto={}", planId, dto);

        PilotagePlans plan = pilotagePlansService.getById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        if (dto.getBoardingTime() != null) {
            plan.setBoardingTime(dto.getBoardingTime());
        }
        if (dto.getLeavingTime() != null) {
            plan.setLeavingTime(dto.getLeavingTime());
        }
        if (dto.getActualStartTime() != null) {
            plan.setActualStartTime(dto.getActualStartTime());
        }
        if (dto.getActualEndTime() != null) {
            plan.setActualEndTime(dto.getActualEndTime());
        }

        plan.setUpdatedAt(new Date());

        return pilotagePlansService.updateById(plan);
    }

    @Override
    public Map<String, Object> getDispatchStatistics(DispatchStatisticsQueryDTO query) {
        log.info("获取调度统计信息: query={}", query);

        Map<String, Object> statistics = new HashMap<>();

        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndConfirmDispatch(Long planId) {
        log.info("检查并确认调度: planId={}", planId);

        PilotagePlans plan = pilotagePlansService.getById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        // 根据数据库注释，计划状态有: PLANNING, PLANNED, PUBLISHED, CONFIRMED, ONGOING, COMPLETED, CANCELLED
        // 假设在资源分配后状态为"PLANNED"（已计划）
        if (!"PLANNED".equals(plan.getPlanStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID,
                    "只有已计划的计划才能进行确认操作");
        }

        Map<String, Object> stats = dispatchMapper.selectPlanConfirmationStats(planId);
        if (stats == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "确认统计信息不存在");
        }

        Number totalCount = (Number) stats.get("total_count");
        Number confirmedCount = (Number) stats.get("confirmed_count");

        if (totalCount != null && totalCount.intValue() > 0 &&
                confirmedCount != null && totalCount.intValue() == confirmedCount.intValue()) {

            plan.setPlanStatus(PlanStatusEnum.CONFIRMED.getCode());
            plan.setUpdatedAt(new Date());

            boolean success = pilotagePlansService.updateById(plan);

            if (success) {
                log.info("调度确认成功: planId={}, planCode={}, 确认资源数={}/{}",
                        planId, plan.getPlanCode(), confirmedCount, totalCount);

                recordDispatchOperation(planId, "CONFIRM",
                        "PLANNED", PlanStatusEnum.CONFIRMED.getCode(),
                        "调度确认：所有资源已确认");
                return true;
            } else {
                log.error("调度确认失败: planId={}", planId);
                return false;
            }
        } else {
            int pendingCount = totalCount != null ? totalCount.intValue() - (confirmedCount != null ? confirmedCount.intValue() : 0) : 0;
            throw new BusinessException(ErrorCode.RESOURCE_NOT_CONFIRMED,
                    String.format("还有%d个资源未确认，无法确认调度", pendingCount));
        }
    }

        private String getDispatchStatusName(String planStatus) {
        switch (planStatus) {
            case "PLANNED":
                return "已计划";
            case "CONFIRMED":
                return "待调度";
            case "PUBLISHED":
                return "已调度";
            case "ONGOING":
                return "进行中";
            case "COMPLETED":
                return "已完成";
            case "CANCELLED":
                return "已取消";
            default:
                return planStatus;
        }
    }

        private void recordDispatchOperation(Long planId, String operationType,
                                         String beforeStatus, String afterStatus,
                                         String description) {
        log.info("调度操作记录: planId={}, operationType={}, beforeStatus={}, afterStatus={}, description={}",
                planId, operationType, beforeStatus, afterStatus, description);
    }

    private PlanPilotAssignmentDTO convertToPilotAssignmentDTO(PlanPilotAssignments entity) {
        PlanPilotAssignmentDTO dto = BeanCopyUtil.copy(entity, PlanPilotAssignmentDTO.class);
        if (dto != null) {
            dto.setId(entity.getId());
            if (entity.getPilotId() != null) {
                Pilots pilot = pilotsService.getById(entity.getPilotId());
                if (pilot != null) {
                    dto.setPilotName(pilot.getPilotName());
                }
            }
        }
        return dto;
    }

    private PlanDriverAssignmentDTO convertToDriverAssignmentDTO(PlanDriverAssignments entity) {
        PlanDriverAssignmentDTO dto = BeanCopyUtil.copy(entity, PlanDriverAssignmentDTO.class);
        if (dto != null) {
            dto.setId(entity.getId());
            if (entity.getDriverId() != null) {
                Drivers driver = driversService.getById(entity.getDriverId());
                if (driver != null) {
                    dto.setDriverName(driver.getName());
                }
            }
        }
        return dto;
    }

    private TugboatAssignmentDTO convertToTugboatAssignmentDTO(PlanTugboatAssignments entity) {
        TugboatAssignmentDTO dto = BeanCopyUtil.copy(entity, TugboatAssignmentDTO.class);
        if (dto != null) {
            dto.setId(entity.getId());
            if (entity.getTugboatId() != null) {
                Tugboats tugboat = tugboatsService.getById(entity.getTugboatId());
                if (tugboat != null) {
                    dto.setTugboatName(tugboat.getTugboatName());
                    dto.setBollardPull(tugboat.getBollardPull() != null ?
                            tugboat.getBollardPull().doubleValue() : null);
                }
            }
        }
        return dto;
    }

    private PilotBoatAssignmentDTO convertToPilotBoatAssignmentDTO(PlanPilotBoatAssignments entity) {
        PilotBoatAssignmentDTO dto = BeanCopyUtil.copy(entity, PilotBoatAssignmentDTO.class);
        if (dto != null) {
            dto.setId(entity.getId());
            if (entity.getPilotBoatId() != null) {
                PilotBoats boat = pilotBoatsService.getById(entity.getPilotBoatId());
                if (boat != null) {
                    dto.setPilotBoatName(boat.getBoatName());
                }
            }
        }
        return dto;
    }

    private PlanVehicleAssignmentDTO convertToVehicleAssignmentDTO(PlanVehicleAssignments entity) {
        PlanVehicleAssignmentDTO dto = BeanCopyUtil.copy(entity, PlanVehicleAssignmentDTO.class);
        if (dto != null) {
            dto.setId(entity.getId());
            if (entity.getVehicleId() != null) {
                Vehicles vehicle = vehiclesService.getById(entity.getVehicleId());
                if (vehicle != null) {
                    dto.setLicensePlate(vehicle.getLicensePlate());
                }
            }
        }
        return dto;
    }
}