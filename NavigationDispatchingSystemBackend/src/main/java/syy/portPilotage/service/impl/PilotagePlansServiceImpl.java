package syy.portPilotage.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.base.*;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.dto.pilotage.*;
import syy.portPilotage.dto.plan.PilotBoatAssignmentDTO;
import syy.portPilotage.dto.plan.PlanConfirmationDTO;
import syy.portPilotage.dto.statistics.PlanChangeDTO;
import syy.portPilotage.entity.*;
import syy.portPilotage.enums.LocationTypeEnum;
import syy.portPilotage.enums.PlanStatusEnum;
import syy.portPilotage.enums.PriorityEnum;
import syy.portPilotage.enums.RequestStatusEnum;
import syy.portPilotage.event.PlanCreatedEvent;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.mapper.PilotagePlansMapper;
import syy.portPilotage.mapper.PlanConfirmationsMapper;
import syy.portPilotage.service.*;
import syy.portPilotage.util.BeanCopyUtil;
import syy.portPilotage.util.IdGenerator;
import syy.portPilotage.rabbitmq.service.MessageService;
import syy.portPilotage.dto.certificate.CertificateResultDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PilotagePlansServiceImpl extends ServiceImpl<PilotagePlansMapper, PilotagePlans>
        implements PilotagePlansService {

    private final PlanTugboatAssignmentsService planTugboatAssignmentsService;
    private final PlanPilotBoatAssignmentsService planPilotBoatAssignmentsService;
    private final PlanVehicleAssignmentsService planVehicleAssignmentsService;
    private final PlanDriverAssignmentsService planDriverAssignmentsService;
    private final PlanPilotAssignmentsService planPilotAssignmentsService;
    private final PilotsService pilotsService;
    private final DriversService driversService;
    private final VehiclesService vehiclesService;
    private final TugboatsService tugboatsService;
    private final PilotBoatsService pilotBoatsService;
    private final PilotagePlansMapper pilotagePlansMapper;
    private final PlanConfirmationsMapper planConfirmationsMapper;
    private final AgentsService agentsService;
    private final VesselsService vesselsService;
    private final PilotageRequestsService pilotageRequestsService;
    private final MessageService messageService;
    private final PilotageCertificatesService pilotageCertificatesService;

    private final ApplicationEventPublisher eventPublisher;

    private PilotagePlans convertToEntity(PilotagePlanDTO dto) {
        PilotagePlans plan = BeanCopyUtil.copy(dto, PilotagePlans.class);
        plan.setStartLocationType(LocationTypeEnum.valueOf(dto.getStartLocationType()));
        plan.setEndLocationType(LocationTypeEnum.valueOf(dto.getEndLocationType()));
        PriorityEnum p = PriorityEnum.fromJson(dto.getPriority());
        plan.setPriority(p == null ? PriorityEnum.MEDIUM : p);
        if (plan.getStartLocationId() != null) {
            plan.setStartLocationId(dto.getStartLocationId());
        }
        if (plan.getEndLocationId() != null) {
            plan.setEndLocationId(dto.getEndLocationId());
        }

        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchCreateResultDTO createPlansWithResult(List<PilotagePlanDTO> dtos) {
        log.info("批量创建引航计划并返回详细结果，数量: {}", dtos.size());

        BatchCreateResultDTO result = new BatchCreateResultDTO();
        List<Long> planIds = new ArrayList<>();
        List<FailedItemDTO> failedItems = new ArrayList<>();
        Date now = new Date();

        for (int i = 0; i < dtos.size(); i++) {
            PilotagePlanDTO dto = dtos.get(i);
            try {
                if (dto.getVoyageId() != null) {
                    Long cnt = pilotagePlansMapper.selectCount(new LambdaQueryWrapper<PilotagePlans>()
                            .eq(PilotagePlans::getVoyageId, dto.getVoyageId())
                            .ne(PilotagePlans::getPlanStatus, PlanStatusEnum.CANCELLED.getCode()));
                    if (cnt != null && cnt > 0) {
                        throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该航次已存在计划，请到【引航计划】中重排/修改");
                    }
                }

                PilotagePlans plan = convertToEntity(dto);

                plan.setPlanCode(IdGenerator.generatePlanCode());
                if (plan.getPlanStatus() == null || plan.getPlanStatus().trim().isEmpty()
                        || PlanStatusEnum.PLANNING.getCode().equals(plan.getPlanStatus())) {
                    plan.setPlanStatus(PlanStatusEnum.PLANNED.getCode());
                }
                plan.setCreatedAt(now);
                plan.setUpdatedAt(now);

                validateResources(dto);

                this.save(plan);
                Long planId = plan.getId();
                planIds.add(planId);

                if (dto.getTugboatAssignments() != null && !dto.getTugboatAssignments().isEmpty()) {
                    saveTugboatAssignments(planId, dto.getTugboatAssignments());
                }

                if (dto.getPilotBoatAssignments() != null && !dto.getPilotBoatAssignments().isEmpty()) {
                    savePilotBoatAssignments(planId, dto.getPilotBoatAssignments());
                }

                if (dto.getAssignedPilotId() != null) {
                    savePilotAssignment(planId, dto.getAssignedPilotId(), now, dto.getPlanStartTime(), dto.getPlanEndTime());
                }

                if (dto.getAssignedDriverId() != null) {
                    saveDriverAssignment(planId, dto.getAssignedDriverId(), now, dto.getPlanStartTime(), dto.getPlanEndTime());
                }

                if (dto.getAssignedVehicleId() != null) {
                    saveVehicleAssignment(planId, dto.getAssignedVehicleId(), now, dto.getPlanStartTime(), dto.getPlanEndTime());
                }

                if (dto.getRequestId() != null) {
                    updateRequestStatus(dto.getRequestId(), RequestStatusEnum.PLANNED);
                }

                updateResourceStatus(dto, planId, true);

            } catch (Exception e) {
                log.error("创建引航计划失败[索引={}]: {}", i, e.getMessage());
                FailedItemDTO failedItem = new FailedItemDTO();
                failedItem.setIndex(i);
                failedItem.setReason(e.getMessage());
                failedItems.add(failedItem);
            }
        }

        result.setSuccessCount(planIds.size());
        result.setFailedCount(failedItems.size());
        result.setFailedItems(failedItems);
        result.setPlanIds(planIds);

        return result;
    }

        private void publishPlanCreatedEvent(Long planId, PilotagePlanDTO dto, Date now) {
        List<Long> tugboatIds = dto.getTugboatAssignments() != null ?
                dto.getTugboatAssignments().stream()
                        .map(TugboatAssignmentDTO::getTugboatId)
                        .collect(Collectors.toList()) : new ArrayList<>();

        List<Long> pilotBoatIds = dto.getPilotBoatAssignments() != null ?
                dto.getPilotBoatAssignments().stream()
                        .map(PilotBoatAssignmentDTO::getPilotBoatId)
                        .collect(Collectors.toList()) : new ArrayList<>();

        // 发布事件
        PlanCreatedEvent event = new PlanCreatedEvent(
                this,
                planId,
                dto.getAssignedPilotId(),
                dto.getAssignedDriverId(),
                tugboatIds,
                pilotBoatIds,
                now
        );

        eventPublisher.publishEvent(event);
        log.info("已发布计划创建事件: planId={}", planId);
    }

        private void savePilotAssignment(Long planId, Long pilotId, Date now, Date planStartTime,Date planEndTime) {
        PlanPilotAssignments assignment = new PlanPilotAssignments();
        assignment.setPlanId(planId);
        assignment.setPilotId(pilotId);
        assignment.setRoleType("MAIN"); // 默认主引
        assignment.setStatus("ASSIGNED");
        assignment.setEstimatedStartTime(planStartTime);
        assignment.setEstimatedEndTime(planEndTime);
        assignment.setCreatedAt(now);
        assignment.setUpdatedAt(now);

        planPilotAssignmentsService.save(assignment);
    }

        private void saveDriverAssignment(Long planId, Long driverId, Date now,Date planStartTime,Date planEndTime) {
        PlanDriverAssignments assignment = new PlanDriverAssignments();
        assignment.setPlanId(planId);
        assignment.setDriverId(driverId);
        assignment.setTaskType("DRIVING"); // 默认驾驶任务
        assignment.setStatus("ASSIGNED");
        assignment.setEstimatedStartTime(planStartTime);
        assignment.setEstimatedEndTime(planEndTime);
        assignment.setCreatedAt(now);
        assignment.setUpdatedAt(now);

        planDriverAssignmentsService.save(assignment);
    }

        private void saveVehicleAssignment(Long planId, Long vehicleId, Date now,Date planStartTime,Date planEndTime) {
        PlanVehicleAssignments assignment = new PlanVehicleAssignments();
        assignment.setPlanId(planId);
        assignment.setVehicleId(vehicleId);
        assignment.setPurpose("PILOT_TRANSPORT"); // 默认接送引航员
        assignment.setStatus("ASSIGNED");
        assignment.setEstimatedStartTime(planStartTime);
        assignment.setEstimatedEndTime(planEndTime);
        assignment.setCreatedAt(now);
        assignment.setUpdatedAt(now);

        planVehicleAssignmentsService.save(assignment);
    }

    private void validateResources(PilotagePlanDTO dto) {
        if (dto.getAssignedPilotId() != null) {
            Pilots pilot = pilotsService.getById(dto.getAssignedPilotId());
            if (pilot == null) {
                throw new BusinessException(ErrorCode.PILOT_NOT_FOUND);
            }
            if (!"ON_DUTY".equals(String.valueOf(pilot.getOnDutyStatus()))) {
                throw new BusinessException(ErrorCode.PILOT_NOT_AVAILABLE);
            }
        }

        if (dto.getAssignedDriverId() != null) {
            Drivers driver = driversService.getById(dto.getAssignedDriverId());
            if (driver == null) {
                throw new BusinessException(ErrorCode.DRIVER_NOT_FOUND);
            }
            if (!"ON_DUTY".equals(String.valueOf(driver.getOnDutyStatus()))) {
                throw new BusinessException(ErrorCode.DRIVER_NOT_AVAILABLE);
            }
        }

        if (dto.getAssignedVehicleId() != null) {
            Vehicles vehicle = vehiclesService.getById(dto.getAssignedVehicleId());
            if (vehicle == null) {
                throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND);
            }
            if (!"AVAILABLE".equals(vehicle.getVehicleStatus())) {
                throw new BusinessException(ErrorCode.VEHICLE_NOT_AVAILABLE);
            }
        }
        if (dto.getTugboatAssignments() != null) {
            for (TugboatAssignmentDTO tugboatAssignment : dto.getTugboatAssignments()) {
                Tugboats tugboat = tugboatsService.getById(tugboatAssignment.getTugboatId());
                if (tugboat == null) {
                    throw new BusinessException(ErrorCode.TUGBOAT_NOT_FOUND);
                }
                if (!"AVAILABLE".equals(tugboat.getStatus())) {
                    throw new BusinessException(ErrorCode.TUGBOAT_NOT_AVAILABLE,
                            "拖轮 " + tugboat.getTugboatName() + " 当前状态为: " + tugboat.getStatus());
                }
            }
        }

        if (dto.getPilotBoatAssignments() != null) {
            for (PilotBoatAssignmentDTO pilotBoatAssignment : dto.getPilotBoatAssignments()) {
                PilotBoats pilotBoat = pilotBoatsService.getById(pilotBoatAssignment.getPilotBoatId());
                if (pilotBoat == null) {
                    throw new BusinessException(ErrorCode.PILOT_BOAT_NOT_FOUND);
                }
                if (!"AVAILABLE".equals(pilotBoat.getCurrentStatus())) {
                    throw new BusinessException(ErrorCode.PILOT_BOAT_NOT_AVAILABLE,
                            "引航艇 " + pilotBoat.getBoatName() + " 当前状态为: " + pilotBoat.getCurrentStatus());
                }
            }
        }
    }

    private void updateRequestStatus(Long requestId, RequestStatusEnum status) {
        if (requestId == null || status == null) return;
        Date now = new Date();
        pilotageRequestsService.update(new LambdaUpdateWrapper<PilotageRequests>()
                .eq(PilotageRequests::getId, requestId)
                .set(PilotageRequests::getRequestStatus, status)
                .set(PilotageRequests::getUpdatedAt, now));
    }

    private void updateResourceStatus(PilotagePlanDTO dto, Long planId, boolean occupy) {
        Date now = new Date();

        if (dto.getAssignedPilotId() != null) {
            Pilots pilot = pilotsService.getById(dto.getAssignedPilotId());
            if (pilot != null) {
                pilot.setOnDutyStatus(occupy ? "ASSIGNED" : "ON_DUTY");
                pilot.setUpdatedAt(now);
                pilotsService.updateById(pilot);
            }
        }

        if (dto.getAssignedDriverId() != null) {
            Drivers driver = driversService.getById(dto.getAssignedDriverId());
            if (driver != null) {
                driver.setOnDutyStatus(occupy ? "ASSIGNED" : "ON_DUTY");
                driver.setUpdatedAt(now);
                driversService.updateById(driver);
            }
        }

        if (dto.getAssignedVehicleId() != null) {
            Vehicles vehicle = vehiclesService.getById(dto.getAssignedVehicleId());
            if (vehicle != null) {
                vehicle.setVehicleStatus(occupy ? "ASSIGNED" : "AVAILABLE");
                vehicle.setUpdatedAt(now);
                vehiclesService.updateById(vehicle);
            }
        }

        if (dto.getTugboatAssignments() != null) {
            for (TugboatAssignmentDTO tugboatAssignment : dto.getTugboatAssignments()) {
                // 只更新 status/updated_at，避免 updateById 触发 build_year 等字段类型问题
                tugboatsService.update(new LambdaUpdateWrapper<Tugboats>()
                        .eq(Tugboats::getId, tugboatAssignment.getTugboatId())
                        .set(Tugboats::getStatus, occupy ? "ASSIGNED" : "AVAILABLE")
                        .set(Tugboats::getUpdatedAt, now));
            }
        }

        if (dto.getPilotBoatAssignments() != null) {
            for (PilotBoatAssignmentDTO pilotBoatAssignment : dto.getPilotBoatAssignments()) {
                PilotBoats pilotBoat = pilotBoatsService.getById(pilotBoatAssignment.getPilotBoatId());
                if (pilotBoat != null) {
                    pilotBoat.setCurrentStatus(occupy ? "ASSIGNED" : "AVAILABLE");
                    pilotBoat.setUpdatedAt(now);
                    pilotBoatsService.updateById(pilotBoat);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePlan(Long id, PilotagePlanDTO dto) {
        PilotagePlans plan = this.getById(id);
        if (plan == null) {
            return false;
        }

        if (!PlanStatusEnum.PLANNED.getCode().equals(plan.getPlanStatus())
                && !PlanStatusEnum.REPLANNED.getCode().equals(plan.getPlanStatus())) {
            if (!PlanStatusEnum.REPLAN_REQUIRED.getCode().equals(plan.getPlanStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID, "当前状态不允许重排: " + plan.getPlanStatus());
            }
        }

        // 如果是“资源取消导致待重排”，禁止再次分配同一个触发资源（避免再次安排到不可执行资源）
        if (PlanStatusEnum.REPLAN_REQUIRED.getCode().equals(plan.getPlanStatus())
                && plan.getReplanRequiredResourceType() != null
                && plan.getReplanRequiredResourceId() != null) {
            String t = plan.getReplanRequiredResourceType();
            Long rid = plan.getReplanRequiredResourceId();
            if ("PILOT".equals(t) && rid.equals(dto.getAssignedPilotId())) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该引航员曾取消导致计划待重排，请选择其他引航员");
            }
            if ("DRIVER".equals(t) && rid.equals(dto.getAssignedDriverId())) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该驾驶员曾取消导致计划待重排，请选择其他驾驶员");
            }
            if ("VEHICLE".equals(t) && rid.equals(dto.getAssignedVehicleId())) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该车辆曾取消导致计划待重排，请选择其他车辆");
            }
            if ("TUGBOAT".equals(t) && dto.getTugboatAssignments() != null
                    && dto.getTugboatAssignments().stream().anyMatch(x -> rid.equals(x.getTugboatId()))) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该拖轮曾取消导致计划待重排，请选择其他拖轮");
            }
            if ("PILOT_BOAT".equals(t) && dto.getPilotBoatAssignments() != null
                    && dto.getPilotBoatAssignments().stream().anyMatch(x -> rid.equals(x.getPilotBoatId()))) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该引航艇曾取消导致计划待重排，请选择其他引航艇");
            }
        }

        releaseResources(plan);

        Long planId = plan.getId();
        planPilotAssignmentsService.remove(new LambdaQueryWrapper<PlanPilotAssignments>()
                .eq(PlanPilotAssignments::getPlanId, planId));
        planDriverAssignmentsService.remove(new LambdaQueryWrapper<PlanDriverAssignments>()
                .eq(PlanDriverAssignments::getPlanId, planId));
        planVehicleAssignmentsService.remove(new LambdaQueryWrapper<PlanVehicleAssignments>()
                .eq(PlanVehicleAssignments::getPlanId, planId));
        planTugboatAssignmentsService.remove(new LambdaQueryWrapper<PlanTugboatAssignments>()
                .eq(PlanTugboatAssignments::getPlanId, planId));
        planPilotBoatAssignmentsService.remove(new LambdaQueryWrapper<PlanPilotBoatAssignments>()
                .eq(PlanPilotBoatAssignments::getPlanId, planId));

        validateResources(dto);

        BeanCopyUtil.copyPropertiesIgnoreNull(dto, plan);
        if (dto.getPriority() != null && !dto.getPriority().trim().isEmpty()) {
            PriorityEnum p = PriorityEnum.fromJson(dto.getPriority());
            plan.setPriority(p == null ? PriorityEnum.MEDIUM : p);
        }
        if (dto.getStartLocationId() != null) {
            plan.setStartLocationId(dto.getStartLocationId());
        }
        if (dto.getEndLocationId() != null) {
            plan.setEndLocationId(dto.getEndLocationId());
        }
        plan.setPlanStatus(PlanStatusEnum.REPLANNED.getCode());
        // 重排成功后清空“待重排原因”
        plan.setReplanRequiredAt(null);
        plan.setReplanRequiredResourceType(null);
        plan.setReplanRequiredResourceId(null);
        plan.setReplanRequiredReason(null);
        Date now = new Date();
        plan.setUpdatedAt(now);
        boolean ok = this.updateById(plan);
        if (!ok) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新计划失败");
        }

        if (dto.getAssignedPilotId() != null) {
            savePilotAssignment(planId, dto.getAssignedPilotId(), now, dto.getPlanStartTime(), dto.getPlanEndTime());
        }
        if (dto.getAssignedDriverId() != null) {
            saveDriverAssignment(planId, dto.getAssignedDriverId(), now, dto.getPlanStartTime(), dto.getPlanEndTime());
        }
        if (dto.getAssignedVehicleId() != null) {
            saveVehicleAssignment(planId, dto.getAssignedVehicleId(), now, dto.getPlanStartTime(), dto.getPlanEndTime());
        }
        if (dto.getTugboatAssignments() != null) {
            saveTugboatAssignments(planId, dto.getTugboatAssignments());
        }
        if (dto.getPilotBoatAssignments() != null) {
            savePilotBoatAssignments(planId, dto.getPilotBoatAssignments());
        }

        resetConfirmationsToPendingForCurrentAssignments(planId, dto, now);

        updateResourceStatus(dto, planId, true);
        return true;
    }

    private void resetConfirmationsToPendingForCurrentAssignments(Long planId, PilotagePlanDTO dto, Date now) {
        if (planId == null) return;
        // 单资源
        markConfirmationPending(planId, "PILOT", dto.getAssignedPilotId(), now);
        markConfirmationPending(planId, "DRIVER", dto.getAssignedDriverId(), now);
        markConfirmationPending(planId, "VEHICLE", dto.getAssignedVehicleId(), now);
        // 多资源
        if (dto.getTugboatAssignments() != null) {
            dto.getTugboatAssignments().forEach(a -> markConfirmationPending(planId, "TUGBOAT", a.getTugboatId(), now));
        }
        if (dto.getPilotBoatAssignments() != null) {
            dto.getPilotBoatAssignments().forEach(a -> markConfirmationPending(planId, "PILOT_BOAT", a.getPilotBoatId(), now));
        }
    }

    private void markConfirmationPending(Long planId, String resourceType, Long resourceId, Date now) {
        if (resourceId == null) return;
        LambdaQueryWrapper<PlanConfirmations> q = new LambdaQueryWrapper<PlanConfirmations>()
                .eq(PlanConfirmations::getPlanId, planId)
                .eq(PlanConfirmations::getResourceType, resourceType)
                .eq(PlanConfirmations::getResourceId, resourceId);
        PlanConfirmations existing = planConfirmationsMapper.selectOne(q);
        String note = "待确认（重排后重新发布）";
        if (existing == null) {
            PlanConfirmations pc = new PlanConfirmations();
            pc.setPlanId(planId);
            pc.setResourceType(resourceType);
            pc.setResourceId(resourceId);
            pc.setConfirmMethod("MANUAL");
            pc.setConfirmedBy(null);
            pc.setConfirmedAt(now);
            pc.setConfirmationStatus("PENDING");
            pc.setNotes(note);
            planConfirmationsMapper.insert(pc);
        } else {
            String merged = (existing.getNotes() == null || existing.getNotes().trim().isEmpty())
                    ? note
                    : existing.getNotes() + "；" + note;
            existing.setConfirmMethod("MANUAL");
            existing.setConfirmedBy(null);
            existing.setConfirmedAt(now);
            existing.setConfirmationStatus("PENDING");
            existing.setNotes(merged);
            planConfirmationsMapper.updateById(existing);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replanPlan(Long id, ReplanPlanDTO dto) {
        // 将重排 DTO 转为内部统一处理的 PilotagePlanDTO（服务层不触发 Jakarta Bean Validation）
        PilotagePlanDTO p = new PilotagePlanDTO();
        p.setPlanStartTime(dto.getPlanStartTime());
        p.setPlanEndTime(dto.getPlanEndTime());
        p.setAssignedPilotId(dto.getAssignedPilotId());
        p.setAssignedDriverId(dto.getAssignedDriverId());
        p.setAssignedVehicleId(dto.getAssignedVehicleId());
        p.setTugboatAssignments(dto.getTugboatAssignments());
        p.setPilotBoatAssignments(dto.getPilotBoatAssignments());
        p.setPriority(dto.getPriority());
        p.setWeatherImpact(dto.getWeatherImpact());
        p.setNotes(dto.getNotes());

        return updatePlan(id, p);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePlan(Long id) {
        LambdaQueryWrapper<PlanConfirmations> confirmationsWrapper = new LambdaQueryWrapper<>();
        confirmationsWrapper.eq(PlanConfirmations::getPlanId, id);
        planConfirmationsMapper.delete(confirmationsWrapper);

        LambdaQueryWrapper<PlanTugboatAssignments> tugboatWrapper = new LambdaQueryWrapper<>();
        tugboatWrapper.eq(PlanTugboatAssignments::getPlanId, id);
        planTugboatAssignmentsService.remove(tugboatWrapper);

        LambdaQueryWrapper<PlanPilotBoatAssignments> pilotBoatWrapper = new LambdaQueryWrapper<>();
        pilotBoatWrapper.eq(PlanPilotBoatAssignments::getPlanId, id);
        planPilotBoatAssignmentsService.remove(pilotBoatWrapper);

        LambdaQueryWrapper<PlanDriverAssignments> driverWrapper = new LambdaQueryWrapper<>();
        driverWrapper.eq(PlanDriverAssignments::getPlanId, id);
        planDriverAssignmentsService.remove(driverWrapper);

        LambdaQueryWrapper<PlanPilotAssignments> pilotWrapper = new LambdaQueryWrapper<>();
        pilotWrapper.eq(PlanPilotAssignments::getPlanId, id);
        planPilotAssignmentsService.remove(pilotWrapper);

        LambdaQueryWrapper<PlanVehicleAssignments> vehicleWrapper = new LambdaQueryWrapper<>();
        vehicleWrapper.eq(PlanVehicleAssignments::getPlanId, id);
        planVehicleAssignmentsService.remove(vehicleWrapper);

        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePlansBatch(List<Long> ids) {
        LambdaQueryWrapper<PlanConfirmations> confirmationsWrapper = new LambdaQueryWrapper<>();
        confirmationsWrapper.in(PlanConfirmations::getPlanId, ids);
        planConfirmationsMapper.delete(confirmationsWrapper);

        LambdaQueryWrapper<PlanTugboatAssignments> tugboatWrapper = new LambdaQueryWrapper<>();
        tugboatWrapper.in(PlanTugboatAssignments::getPlanId, ids);
        planTugboatAssignmentsService.remove(tugboatWrapper);

        LambdaQueryWrapper<PlanPilotBoatAssignments> pilotBoatWrapper = new LambdaQueryWrapper<>();
        pilotBoatWrapper.in(PlanPilotBoatAssignments::getPlanId, ids);
        planPilotBoatAssignmentsService.remove(pilotBoatWrapper);

        LambdaQueryWrapper<PlanDriverAssignments> driverWrapper = new LambdaQueryWrapper<>();
        driverWrapper.in(PlanDriverAssignments::getPlanId, ids);
        planDriverAssignmentsService.remove(driverWrapper);

        LambdaQueryWrapper<PlanPilotAssignments> pilotWrapper = new LambdaQueryWrapper<>();
        pilotWrapper.in(PlanPilotAssignments::getPlanId, ids);
        planPilotAssignmentsService.remove(pilotWrapper);

        LambdaQueryWrapper<PlanVehicleAssignments> vehicleWrapper = new LambdaQueryWrapper<>();
        vehicleWrapper.in(PlanVehicleAssignments::getPlanId, ids);
        planVehicleAssignmentsService.remove(vehicleWrapper);

        return this.removeByIds(ids);
    }

    @Override
    public PilotagePlanDTO getPlanDetail(Long id) {
        PilotagePlans plan = this.getById(id);
        if (plan == null) {
            return null;
        }

        PilotagePlanDTO dto = BeanCopyUtil.copy(plan, PilotagePlanDTO.class);
        if (plan.getPriority() != null) {
            dto.setPriority(plan.getPriority().name());
        }

        if (plan.getReplanRequiredResourceType() != null && plan.getReplanRequiredResourceId() != null) {
            dto.setReplanRequiredResourceName(resolveReplanResourceName(plan.getReplanRequiredResourceType(), plan.getReplanRequiredResourceId()));
        }

        if (plan.getStartLocationId() != null) {
            try {
                dto.setStartLocationId(plan.getStartLocationId());
            } catch (NumberFormatException e) {
                log.warn("startLocationId格式错误: {}", plan.getStartLocationId());
            }
        }
        if (plan.getEndLocationId() != null) {
            try {
                dto.setEndLocationId(plan.getEndLocationId());
            } catch (NumberFormatException e) {
                log.warn("endLocationId格式错误: {}", plan.getEndLocationId());
            }
        }

        LambdaQueryWrapper<PlanTugboatAssignments> tugboatWrapper = new LambdaQueryWrapper<>();
        tugboatWrapper.eq(PlanTugboatAssignments::getPlanId, id);
        List<PlanTugboatAssignments> tugboatAssignments = planTugboatAssignmentsService.list(tugboatWrapper);

        List<TugboatAssignmentDTO> tugboatDTOs = tugboatAssignments.stream()
                .map(assignment -> {
                    TugboatAssignmentDTO tugboatDTO = new TugboatAssignmentDTO();
                    tugboatDTO.setTugboatId(assignment.getTugboatId());
                    tugboatDTO.setAssignmentType(assignment.getAssignmentType());
                    tugboatDTO.setEstimatedStartTime(assignment.getEstimatedStartTime());
                    tugboatDTO.setEstimatedEndTime(assignment.getEstimatedEndTime());
                    return tugboatDTO;
                })
                .collect(Collectors.toList());

        dto.setTugboatAssignments(tugboatDTOs);

        LambdaQueryWrapper<PlanPilotBoatAssignments> pilotBoatWrapper = new LambdaQueryWrapper<>();
        pilotBoatWrapper.eq(PlanPilotBoatAssignments::getPlanId, id);
        List<PlanPilotBoatAssignments> pilotBoatAssignments = planPilotBoatAssignmentsService.list(pilotBoatWrapper);

        List<PilotBoatAssignmentDTO> pilotBoatDTOs = pilotBoatAssignments.stream()
                .map(assignment -> {
                    PilotBoatAssignmentDTO pilotBoatDTO = new PilotBoatAssignmentDTO();
                    pilotBoatDTO.setPilotBoatId(assignment.getPilotBoatId());
                    pilotBoatDTO.setPurpose(assignment.getPurpose());
                    pilotBoatDTO.setEstimatedStartTime(assignment.getEstimatedStartTime());
                    pilotBoatDTO.setEstimatedEndTime(assignment.getEstimatedEndTime());
                    return pilotBoatDTO;
                })
                .collect(Collectors.toList());

        dto.setPilotBoatAssignments(pilotBoatDTOs);

        return dto;
    }

    private String resolveReplanResourceName(String resourceType, Long resourceId) {
        try {
            if ("PILOT".equals(resourceType)) {
                Pilots p = pilotsService.getById(resourceId);
                return p != null ? p.getPilotName() : null;
            }
            if ("DRIVER".equals(resourceType)) {
                Drivers d = driversService.getById(resourceId);
                return d != null ? d.getName() : null;
            }
            if ("VEHICLE".equals(resourceType)) {
                Vehicles v = vehiclesService.getById(resourceId);
                return v != null ? v.getLicensePlate() : null;
            }
            if ("TUGBOAT".equals(resourceType)) {
                Tugboats t = tugboatsService.getById(resourceId);
                return t != null ? t.getTugboatName() : null;
            }
            if ("PILOT_BOAT".equals(resourceType)) {
                PilotBoats b = pilotBoatsService.getById(resourceId);
                return b != null ? b.getBoatName() : null;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public IPage<PilotagePlanDTO> getPageWithJoin(PageQueryDTO query) {
        MPJLambdaWrapper<PilotagePlans> wrapper = buildPageQueryWrapper(query);
        Page<PilotagePlanDTO> page = new Page<>(query.getCurrent(), query.getSize());
        return pilotagePlansMapper.selectJoinPage(page, PilotagePlanDTO.class, wrapper);
    }

    private PilotagePlanDTO convertToDTO(PilotagePlans plan) {
        PilotagePlanDTO dto = BeanCopyUtil.copy(plan, PilotagePlanDTO.class);

        if (plan.getStartLocationId() != null) {
            try {
                dto.setStartLocationId(plan.getStartLocationId());
            } catch (NumberFormatException e) {
                log.warn("startLocationId格式错误: {}", plan.getStartLocationId());
            }
        }
        if (plan.getEndLocationId() != null) {
            try {
                dto.setEndLocationId(plan.getEndLocationId());
            } catch (NumberFormatException e) {
                log.warn("endLocationId格式错误: {}", plan.getEndLocationId());
            }
        }

        return dto;
    }
    private MPJLambdaWrapper<PilotagePlans> buildPageQueryWrapper(PageQueryDTO query) {
        MPJLambdaWrapper<PilotagePlans> wrapper = new MPJLambdaWrapper<PilotagePlans>()
                // PilotagePlans 字段映射
                .selectAs(PilotagePlans::getId, PilotagePlanDTO::getId)
                .selectAs(PilotagePlans::getPlanCode, PilotagePlanDTO::getPlanCode)
                .selectAs(PilotagePlans::getPlanStartTime, PilotagePlanDTO::getPlanStartTime)
                .selectAs(PilotagePlans::getPlanEndTime, PilotagePlanDTO::getPlanEndTime)
                .selectAs(PilotagePlans::getStartLocationType, PilotagePlanDTO::getStartLocationType)
                .selectAs(PilotagePlans::getStartLocationId, PilotagePlanDTO::getStartLocationId)
                .selectAs(PilotagePlans::getStartLocationName, PilotagePlanDTO::getStartLocationName)
                .selectAs(PilotagePlans::getEndLocationType, PilotagePlanDTO::getEndLocationType)
                .selectAs(PilotagePlans::getEndLocationId, PilotagePlanDTO::getEndLocationId)
                .selectAs(PilotagePlans::getEndLocationName, PilotagePlanDTO::getEndLocationName)
                .selectAs(PilotagePlans::getAssignedPilotId, PilotagePlanDTO::getAssignedPilotId)
                .selectAs(PilotagePlans::getAssignedDriverId, PilotagePlanDTO::getAssignedDriverId)
                .selectAs(PilotagePlans::getAssignedVehicleId, PilotagePlanDTO::getAssignedVehicleId)
                .selectAs(PilotagePlans::getVesselId, PilotagePlanDTO::getVesselId)
                .selectAs(PilotagePlans::getVoyageId, PilotagePlanDTO::getVoyageId)
                .selectAs(PilotagePlans::getOperationTypeFlag, PilotagePlanDTO::getOperationTypeFlag)
                .selectAs(PilotagePlans::getPriority, PilotagePlanDTO::getPriority)
                .selectAs(PilotagePlans::getPlanStatus, PilotagePlanDTO::getPlanStatus)
                .selectAs(PilotagePlans::getBoardingTime, PilotagePlanDTO::getBoardingTime)
                .selectAs(PilotagePlans::getLeavingTime, PilotagePlanDTO::getLeavingTime)
                .selectAs(PilotagePlans::getWeatherImpact, PilotagePlanDTO::getWeatherImpact)
                .selectAs(PilotagePlans::getNotes, PilotagePlanDTO::getNotes)
                .selectAs(PilotagePlans::getCreatedBy, PilotagePlanDTO::getCreatedBy)

                // Vessels 字段映射
                .selectAs(Vessels::getNameZh, PilotagePlanDTO::getNameZh)
                .selectAs(Vessels::getNameEn, PilotagePlanDTO::getNameEn)
                .selectAs(Vessels::getNationality, PilotagePlanDTO::getCountry)
                .selectAs(Vessels::getVesselLength, PilotagePlanDTO::getVesselLength)
                .selectAs(Vessels::getVesselWidth, PilotagePlanDTO::getVesselWidth)
                .selectAs(Vessels::getGrossTonnage, PilotagePlanDTO::getGrossTonnage)
                .selectAs(Vessels::getNetTonnage, PilotagePlanDTO::getNetTonnage)
                .selectAs(Vessels::getDeadweight, PilotagePlanDTO::getDeadWeight)
                .selectAs(Vessels::getRouteType, PilotagePlanDTO::getRouteType)

                // PilotageRequests 字段映射
                .selectAs(PilotageRequests::getId, PilotagePlanDTO::getRequestId)
                .selectAs(PilotageRequests::getExpectedBerthId, PilotagePlanDTO::getExpectedBerthId)
                // 注意：计划页的“优先级”应展示计划优先级（pilotage_plans.priority），申请优先级保留在申请表中

                // Voyages 字段映射
                .selectAs(Voyages::getVoyageType, PilotagePlanDTO::getVoyageType)

                // Pilots 字段映射
                .selectAs(Pilots::getPilotName, PilotagePlanDTO::getMainPilot)

                // Berths 字段映射
                .selectAs(Berths::getBerthName, PilotagePlanDTO::getBerth)

                .selectCount(PlanTugboatAssignments::getId, PilotagePlanDTO::getTugboatCount)

                // 关联关系
                .leftJoin(Vessels.class, Vessels::getId, PilotagePlans::getVesselId)
                .leftJoin(Voyages.class, Voyages::getId, PilotagePlans::getVoyageId)
                .leftJoin(Pilots.class, Pilots::getId, PilotagePlans::getAssignedPilotId)
                .leftJoin(Berths.class, Berths::getId, Voyages::getPlannedStartId)
                .leftJoin(PilotageRequests.class, PilotageRequests::getId, Voyages::getPilotageRequestsId)
                .leftJoin(PlanTugboatAssignments.class, PlanTugboatAssignments::getPlanId, PilotagePlans::getId)
                .groupBy(PilotagePlans::getId)
                .orderByDesc(PilotagePlans::getCreatedAt);

        if (query.getParams() != null) {
            Map<String, Object> params = query.getParams();

            if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
                String keyword = query.getKeyword().trim();
                wrapper.and(w -> w.like(PilotagePlans::getPlanCode, keyword)
                        .or()
                        .like(Vessels::getNameZh, keyword)
                        .or()
                        .like(Vessels::getNameEn, keyword)
                        .or()
                        .like(Voyages::getVoyageNumber, keyword));
            }

            if (params.containsKey("vesselName")) {
                wrapper.like(Vessels::getNameZh, params.get("vesselName"))
                        .or()
                        .like(Vessels::getNameEn, params.get("vesselName"));
            }

            if (params.containsKey("planStatus")) {
                wrapper.eq(PilotagePlans::getPlanStatus, params.get("planStatus"));
            }
            
            if (params.containsKey("voyageType")) {
                wrapper.eq(Voyages::getVoyageType, params.get("voyageType"));
            }
            
            if (params.containsKey("priority")) {
                wrapper.eq(PilotagePlans::getPriority, params.get("priority"));
            }

            // 按时间范围查询
            if (params.containsKey("startDate") && params.containsKey("endDate")
                    && params.get("startDate") != null && params.get("endDate") != null) {
                wrapper.between(PilotagePlans::getPlanStartTime,
                        params.get("startDate"), params.get("endDate"));
            } else if (params.containsKey("startDate") && params.get("startDate") != null) {
                wrapper.ge(PilotagePlans::getPlanStartTime, params.get("startDate"));
            } else if (params.containsKey("endDate") && params.get("endDate") != null) {
                wrapper.le(PilotagePlans::getPlanStartTime, params.get("endDate"));
            }

            if (params.containsKey("pilotName")) {
                wrapper.like(Pilots::getPilotName, params.get("pilotName"));
            }
        }
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishPlan(Long id) {
        PilotagePlans plan = this.getById(id);
        if (plan == null) {
            return false;
        }

        if (!PlanStatusEnum.PLANNED.getCode().equals(plan.getPlanStatus())
                && !PlanStatusEnum.REPLANNED.getCode().equals(plan.getPlanStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID);
        }

        plan.setPlanStatus(PlanStatusEnum.PUBLISHED.getCode());
        plan.setPublishedAt(new Date());
        plan.setUpdatedAt(new Date());

        boolean result = this.updateById(plan);
        
        if (result) {
            // 发布后：为当前分配的所有资源创建待确认记录（PENDING），避免“只确认了引航员就把计划判定为已确认”
            try {
                createPendingConfirmationsForCurrentAssignments(plan.getId(), new Date());
            } catch (Exception e) {
                log.warn("发布后创建待确认记录失败: planId={}, err={}", plan.getId(), e.getMessage());
            }

            notifyPlanPublication(plan);
        }
        
        return result;
    }

    private void createPendingConfirmationsForCurrentAssignments(Long planId, Date now) {
        if (planId == null) return;
        // 单资源（仅当前有效分配）
        List<PlanPilotAssignments> pilots = planPilotAssignmentsService.list(
                new LambdaQueryWrapper<PlanPilotAssignments>()
                        .eq(PlanPilotAssignments::getPlanId, planId)
                        .in(PlanPilotAssignments::getStatus, "ASSIGNED", "CONFIRMED")
        );
        pilots.stream().map(PlanPilotAssignments::getPilotId).filter(Objects::nonNull).distinct()
                .forEach(pid -> markConfirmationPending(planId, "PILOT", pid, now));

        List<PlanDriverAssignments> drivers = planDriverAssignmentsService.list(
                new LambdaQueryWrapper<PlanDriverAssignments>()
                        .eq(PlanDriverAssignments::getPlanId, planId)
                        .in(PlanDriverAssignments::getStatus, "ASSIGNED", "CONFIRMED")
        );
        drivers.stream().map(PlanDriverAssignments::getDriverId).filter(Objects::nonNull).distinct()
                .forEach(did -> markConfirmationPending(planId, "DRIVER", did, now));

        List<PlanVehicleAssignments> vehicles = planVehicleAssignmentsService.list(
                new LambdaQueryWrapper<PlanVehicleAssignments>()
                        .eq(PlanVehicleAssignments::getPlanId, planId)
                        .in(PlanVehicleAssignments::getStatus, "ASSIGNED", "CONFIRMED")
        );
        vehicles.stream().map(PlanVehicleAssignments::getVehicleId).filter(Objects::nonNull).distinct()
                .forEach(vid -> markConfirmationPending(planId, "VEHICLE", vid, now));

        // 多资源
        List<PlanTugboatAssignments> tugboats = planTugboatAssignmentsService.list(
                new LambdaQueryWrapper<PlanTugboatAssignments>()
                        .eq(PlanTugboatAssignments::getPlanId, planId)
                        .in(PlanTugboatAssignments::getStatus, "ASSIGNED", "CONFIRMED")
        );
        tugboats.stream().map(PlanTugboatAssignments::getTugboatId).filter(Objects::nonNull).distinct()
                .forEach(tid -> markConfirmationPending(planId, "TUGBOAT", tid, now));

        List<PlanPilotBoatAssignments> pilotBoats = planPilotBoatAssignmentsService.list(
                new LambdaQueryWrapper<PlanPilotBoatAssignments>()
                        .eq(PlanPilotBoatAssignments::getPlanId, planId)
                        .in(PlanPilotBoatAssignments::getStatus, "ASSIGNED", "CONFIRMED")
        );
        pilotBoats.stream().map(PlanPilotBoatAssignments::getPilotBoatId).filter(Objects::nonNull).distinct()
                .forEach(pbid -> markConfirmationPending(planId, "PILOT_BOAT", pbid, now));
    }
    
        private void notifyPlanPublication(PilotagePlans plan) {
        try {
            List<PlanPilotAssignments> pilotAssignments = planPilotAssignmentsService.list(
                new LambdaQueryWrapper<PlanPilotAssignments>()
                    .eq(PlanPilotAssignments::getPlanId, plan.getId())
                    // 只通知当前有效分配（避免取消/系统取消的历史记录还被通知）
                    .in(PlanPilotAssignments::getStatus, "ASSIGNED", "CONFIRMED")
            );
            
            List<PlanDriverAssignments> driverAssignments = planDriverAssignmentsService.list(
                new LambdaQueryWrapper<PlanDriverAssignments>()
                    .eq(PlanDriverAssignments::getPlanId, plan.getId())
                    .in(PlanDriverAssignments::getStatus, "ASSIGNED", "CONFIRMED")
            );
            
            Vessels vessel = vesselsService.getById(plan.getVesselId());
            
            for (PlanPilotAssignments assignment : pilotAssignments) {
                Pilots pilot = pilotsService.getById(assignment.getPilotId());
                if (pilot != null && pilot.getEmail() != null && !pilot.getEmail().trim().isEmpty()) {
                    String subject = "引航计划发布通知 - 计划编号: " + plan.getPlanCode();
                    String content = buildPlanNotificationEmailContent(plan, vessel, "引航员", pilot.getPilotName());
                    messageService.sendEmail(pilot.getEmail(), subject, content, true);
                    log.info("已发送引航计划发布通知邮件给引航员: {} ({})", pilot.getPilotName(), pilot.getEmail());
                }
            }
            
            for (PlanDriverAssignments assignment : driverAssignments) {
                Drivers driver = driversService.getById(assignment.getDriverId());
                if (driver != null && driver.getEmail() != null && !driver.getEmail().trim().isEmpty()) {
                    String subject = "引航计划发布通知 - 计划编号: " + plan.getPlanCode();
                    String content = buildPlanNotificationEmailContent(plan, vessel, "驾驶员", driver.getName());
                    messageService.sendEmail(driver.getEmail(), subject, content, true);
                    log.info("已发送引航计划发布通知邮件给驾驶员: {} ({})", driver.getName(), driver.getEmail());
                }
            }
            
        } catch (Exception e) {
            log.error("发送计划发布通知邮件失败: {}", e.getMessage(), e);
        }
    }
    
        private String buildPlanNotificationEmailContent(PilotagePlans plan, Vessels vessel, String role, String roleName) {
        StringBuilder content = new StringBuilder();
        content.append("<h2>引航计划发布通知</h2>");
        content.append("<p>尊敬的" + role + " <strong>" + roleName + "</strong>：</p>");
        content.append("<p>您已被分配到以下引航计划，请注意相关安排：</p>");
        
        content.append("<div style='border: 1px solid #ccc; padding: 15px; margin: 10px 0; border-radius: 5px;'>");
        content.append("<h3>计划信息</h3>");
        content.append("<table style='width: 100%; border-collapse: collapse;'>");
        content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>计划编号</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>").append(plan.getPlanCode()).append("</td></tr>");
        if (vessel != null) {
            content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>船舶名称</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>")
                   .append(vessel.getNameZh() != null ? vessel.getNameZh() : vessel.getNameEn()).append("</td></tr>");
        }
        content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>计划开始时间</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>").append(formatDate(plan.getPlanStartTime())).append("</td></tr>");
        content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>计划结束时间</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>").append(formatDate(plan.getPlanEndTime())).append("</td></tr>");
        content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>起始地点</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>").append(plan.getStartLocationName()).append("</td></tr>");
        content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>终点地点</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>").append(plan.getEndLocationName()).append("</td></tr>");
        content.append("</table>");
        content.append("</div>");
        
        content.append("<p>请您提前做好相应准备，如有任何疑问，请及时联系相关部门。</p>");
        content.append("<p>谢谢！</p>");
        content.append("<p><em>港口引航与计费系统</em></p>");
        
        return content.toString();
    }
    
    private String formatDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        return DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchPublishResultDTO publishPlansBatch(List<Long> planIds) {
        BatchPublishResultDTO result = new BatchPublishResultDTO();
        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();

        for (Long planId : planIds) {
            try {
                boolean success = publishPlan(planId);
                if (success) {
                    successIds.add(planId);
                } else {
                    failedIds.add(planId);
                }
            } catch (Exception e) {
                log.error("发布引航计划失败: planId={}", planId, e);
                failedIds.add(planId);
            }
        }

        result.setSuccessCount(successIds.size());
        result.setFailedCount(failedIds.size());
        result.setSuccessIds(successIds);
        result.setFailedIds(failedIds);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startPlan(Long id) {
        return startPlan(id, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startPlan(Long id, Date actualStartTime) {
        PilotagePlans plan = this.getById(id);
        if (plan == null) {
            return false;
        }

        // 允许从 CONFIRMED 开始；部分场景下计划可能停留在 PUBLISHED（确认统计未及时推进），此时需确保“资源已全确认”
        String ps = plan.getPlanStatus();
        if (PlanStatusEnum.PUBLISHED.getCode().equals(ps)) {
            long total =
                    planPilotAssignmentsService.count(new LambdaQueryWrapper<PlanPilotAssignments>().eq(PlanPilotAssignments::getPlanId, id).ne(PlanPilotAssignments::getStatus, "CANCELLED"))
                            + planDriverAssignmentsService.count(new LambdaQueryWrapper<PlanDriverAssignments>().eq(PlanDriverAssignments::getPlanId, id).ne(PlanDriverAssignments::getStatus, "CANCELLED"))
                            + planTugboatAssignmentsService.count(new LambdaQueryWrapper<PlanTugboatAssignments>().eq(PlanTugboatAssignments::getPlanId, id).ne(PlanTugboatAssignments::getStatus, "CANCELLED"))
                            + planPilotBoatAssignmentsService.count(new LambdaQueryWrapper<PlanPilotBoatAssignments>().eq(PlanPilotBoatAssignments::getPlanId, id).ne(PlanPilotBoatAssignments::getStatus, "CANCELLED"))
                            + planVehicleAssignmentsService.count(new LambdaQueryWrapper<PlanVehicleAssignments>().eq(PlanVehicleAssignments::getPlanId, id).ne(PlanVehicleAssignments::getStatus, "CANCELLED"));
            long confirmed =
                    planPilotAssignmentsService.count(new LambdaQueryWrapper<PlanPilotAssignments>().eq(PlanPilotAssignments::getPlanId, id).eq(PlanPilotAssignments::getStatus, "CONFIRMED"))
                            + planDriverAssignmentsService.count(new LambdaQueryWrapper<PlanDriverAssignments>().eq(PlanDriverAssignments::getPlanId, id).eq(PlanDriverAssignments::getStatus, "CONFIRMED"))
                            + planTugboatAssignmentsService.count(new LambdaQueryWrapper<PlanTugboatAssignments>().eq(PlanTugboatAssignments::getPlanId, id).eq(PlanTugboatAssignments::getStatus, "CONFIRMED"))
                            + planPilotBoatAssignmentsService.count(new LambdaQueryWrapper<PlanPilotBoatAssignments>().eq(PlanPilotBoatAssignments::getPlanId, id).eq(PlanPilotBoatAssignments::getStatus, "CONFIRMED"))
                            + planVehicleAssignmentsService.count(new LambdaQueryWrapper<PlanVehicleAssignments>().eq(PlanVehicleAssignments::getPlanId, id).eq(PlanVehicleAssignments::getStatus, "CONFIRMED"));
            if (total <= 0 || confirmed < total) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_CONFIRMED, "资源未全部确认，不能开始引航");
            }
        } else if (!PlanStatusEnum.CONFIRMED.getCode().equals(ps)) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID, "当前状态不允许开始: " + ps);
        }

        Date startAt = actualStartTime != null ? actualStartTime : new Date();
        plan.setPlanStatus(PlanStatusEnum.ONGOING.getCode());
        plan.setActualStartTime(startAt);
        plan.setUpdatedAt(new Date());

        boolean success = this.updateById(plan);
        if (success) {
            syncAssignmentActualTimesOnStart(id, startAt);
            markResourcesOperating(plan);
        }
        return success;
    }

    
    private void syncAssignmentActualTimesOnStart(Long planId, Date startAt) {
        if (planId == null || startAt == null) return;
        Date now = new Date();

        // 引航员分配
        planPilotAssignmentsService.update(new LambdaUpdateWrapper<PlanPilotAssignments>()
                .eq(PlanPilotAssignments::getPlanId, planId)
                .notIn(PlanPilotAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
                .isNull(PlanPilotAssignments::getActualStartTime)
                .set(PlanPilotAssignments::getActualStartTime, startAt)
                .set(PlanPilotAssignments::getUpdatedAt, now));

        // 驾驶员分配
        planDriverAssignmentsService.update(new LambdaUpdateWrapper<PlanDriverAssignments>()
                .eq(PlanDriverAssignments::getPlanId, planId)
                .notIn(PlanDriverAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
                .isNull(PlanDriverAssignments::getActualStartTime)
                .set(PlanDriverAssignments::getActualStartTime, startAt)
                .set(PlanDriverAssignments::getUpdatedAt, now));

        // 车辆分配
        planVehicleAssignmentsService.update(new LambdaUpdateWrapper<PlanVehicleAssignments>()
                .eq(PlanVehicleAssignments::getPlanId, planId)
                .notIn(PlanVehicleAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
                .isNull(PlanVehicleAssignments::getActualStartTime)
                .set(PlanVehicleAssignments::getActualStartTime, startAt)
                .set(PlanVehicleAssignments::getUpdatedAt, now));

        // 拖轮分配（status 字段为 Object，这里仍按字符串状态值写入/过滤）
        planTugboatAssignmentsService.update(new LambdaUpdateWrapper<PlanTugboatAssignments>()
                .eq(PlanTugboatAssignments::getPlanId, planId)
                .notIn(PlanTugboatAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
                .isNull(PlanTugboatAssignments::getActualStartTime)
                .set(PlanTugboatAssignments::getActualStartTime, startAt)
                .set(PlanTugboatAssignments::getUpdatedAt, now));

        // 引航艇分配（status 字段为 Object，这里仍按字符串状态值写入/过滤）
        planPilotBoatAssignmentsService.update(new LambdaUpdateWrapper<PlanPilotBoatAssignments>()
                .eq(PlanPilotBoatAssignments::getPlanId, planId)
                .notIn(PlanPilotBoatAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
                .isNull(PlanPilotBoatAssignments::getActualStartTime)
                .set(PlanPilotBoatAssignments::getActualStartTime, startAt)
                .set(PlanPilotBoatAssignments::getUpdatedAt, now));
    }

    private void markResourcesOperating(PilotagePlans plan) {
        Date now = new Date();
        // 引航员/驾驶员：OPERATING
        if (plan.getAssignedPilotId() != null) {
            Pilots pilot = pilotsService.getById(plan.getAssignedPilotId());
            if (pilot != null) {
                pilot.setOnDutyStatus("OPERATING");
                pilot.setUpdatedAt(now);
                pilotsService.updateById(pilot);
            }
        }
        if (plan.getAssignedDriverId() != null) {
            Drivers driver = driversService.getById(plan.getAssignedDriverId());
            if (driver != null) {
                driver.setOnDutyStatus("OPERATING");
                driver.setUpdatedAt(now);
                driversService.updateById(driver);
            }
        }
        // 车辆：IN_USE
        if (plan.getAssignedVehicleId() != null) {
            Vehicles vehicle = vehiclesService.getById(plan.getAssignedVehicleId());
            if (vehicle != null) {
                vehicle.setVehicleStatus("IN_USE");
                vehicle.setUpdatedAt(now);
                vehiclesService.updateById(vehicle);
            }
        }
        // 拖轮：OPERATING
        LambdaQueryWrapper<PlanTugboatAssignments> tugboatWrapper = new LambdaQueryWrapper<>();
        tugboatWrapper.eq(PlanTugboatAssignments::getPlanId, plan.getId());
        List<PlanTugboatAssignments> tugboatAssignments = planTugboatAssignmentsService.list(tugboatWrapper);
        for (PlanTugboatAssignments a : tugboatAssignments) {
            tugboatsService.update(new LambdaUpdateWrapper<Tugboats>()
                    .eq(Tugboats::getId, a.getTugboatId())
                    .set(Tugboats::getStatus, "OPERATING")
                    .set(Tugboats::getUpdatedAt, now));
        }
        // 引航艇：OPERATING
        LambdaQueryWrapper<PlanPilotBoatAssignments> pilotBoatWrapper = new LambdaQueryWrapper<>();
        pilotBoatWrapper.eq(PlanPilotBoatAssignments::getPlanId, plan.getId());
        List<PlanPilotBoatAssignments> pilotBoatAssignments = planPilotBoatAssignmentsService.list(pilotBoatWrapper);
        for (PlanPilotBoatAssignments a : pilotBoatAssignments) {
            PilotBoats boat = pilotBoatsService.getById(a.getPilotBoatId());
            if (boat != null) {
                boat.setCurrentStatus("OPERATING");
                boat.setUpdatedAt(now);
                pilotBoatsService.updateById(boat);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completePlan(Long id) {
        return completePlan(id, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completePlan(Long id, Date actualEndTime) {
        PilotagePlans plan = this.getById(id);
        if (plan == null) {
            return false;
        }

        if (!PlanStatusEnum.ONGOING.getCode().equals(plan.getPlanStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID);
        }

        plan.setPlanStatus(PlanStatusEnum.COMPLETED.getCode());
        Date endAt = actualEndTime != null ? actualEndTime : new Date();
        plan.setActualEndTime(endAt);
        plan.setActualCompletionTime(endAt);
        plan.setUpdatedAt(new Date());

        // 回填“资源分配表”的实际结束时间（并推进分配状态为 COMPLETED）
        Date startAt = plan.getActualStartTime() != null ? plan.getActualStartTime() : endAt;
        syncAssignmentActualTimesOnComplete(id, startAt, endAt);

        releaseResources(plan);

        boolean ok = this.updateById(plan);
        if (!ok) return false;

        // 自动生成签证单并把计划推进到 CERTIFICATED（与 web 端一致）
        CertificateResultDTO cert = pilotageCertificatesService.createCertificateFromPlanAuto(id);
        if (cert == null || !Boolean.TRUE.equals(cert.getSuccess())) {
            throw new BusinessException(ErrorCode.CERTIFICATE_CREATE_FAILED, "自动生成签证单失败");
        }
        return true;
    }

    private void syncAssignmentActualTimesOnComplete(Long planId, Date startAt, Date endAt) {
        if (planId == null || endAt == null) return;
        Date now = new Date();

        planPilotAssignmentsService.update(new LambdaUpdateWrapper<PlanPilotAssignments>()
                .eq(PlanPilotAssignments::getPlanId, planId)
                .notIn(PlanPilotAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
                .set(PlanPilotAssignments::getActualStartTime, startAt)
                .set(PlanPilotAssignments::getActualEndTime, endAt)
                .set(PlanPilotAssignments::getStatus, "COMPLETED")
                .set(PlanPilotAssignments::getUpdatedAt, now));

        planDriverAssignmentsService.update(new LambdaUpdateWrapper<PlanDriverAssignments>()
                .eq(PlanDriverAssignments::getPlanId, planId)
                .notIn(PlanDriverAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
                .set(PlanDriverAssignments::getActualStartTime, startAt)
                .set(PlanDriverAssignments::getActualEndTime, endAt)
                .set(PlanDriverAssignments::getStatus, "COMPLETED")
                .set(PlanDriverAssignments::getUpdatedAt, now));

        planVehicleAssignmentsService.update(new LambdaUpdateWrapper<PlanVehicleAssignments>()
                .eq(PlanVehicleAssignments::getPlanId, planId)
                .notIn(PlanVehicleAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
                .set(PlanVehicleAssignments::getActualStartTime, startAt)
                .set(PlanVehicleAssignments::getActualEndTime, endAt)
                .set(PlanVehicleAssignments::getStatus, "COMPLETED")
                .set(PlanVehicleAssignments::getUpdatedAt, now));

        planTugboatAssignmentsService.update(new LambdaUpdateWrapper<PlanTugboatAssignments>()
                .eq(PlanTugboatAssignments::getPlanId, planId)
                .notIn(PlanTugboatAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
                .set(PlanTugboatAssignments::getActualStartTime, startAt)
                .set(PlanTugboatAssignments::getActualEndTime, endAt)
                .set(PlanTugboatAssignments::getStatus, "COMPLETED")
                .set(PlanTugboatAssignments::getUpdatedAt, now));

        planPilotBoatAssignmentsService.update(new LambdaUpdateWrapper<PlanPilotBoatAssignments>()
                .eq(PlanPilotBoatAssignments::getPlanId, planId)
                .notIn(PlanPilotBoatAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
                .set(PlanPilotBoatAssignments::getActualStartTime, startAt)
                .set(PlanPilotBoatAssignments::getActualEndTime, endAt)
                .set(PlanPilotBoatAssignments::getStatus, "COMPLETED")
                .set(PlanPilotBoatAssignments::getUpdatedAt, now));
    }

    private void releaseResources(PilotagePlans plan) {
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

        for (PlanTugboatAssignments assignment : tugboatAssignments) {
            tugboatsService.update(new LambdaUpdateWrapper<Tugboats>()
                    .eq(Tugboats::getId, assignment.getTugboatId())
                    .set(Tugboats::getStatus, "AVAILABLE")
                    .set(Tugboats::getUpdatedAt, now));
        }

        LambdaQueryWrapper<PlanPilotBoatAssignments> pilotBoatWrapper = new LambdaQueryWrapper<>();
        pilotBoatWrapper.eq(PlanPilotBoatAssignments::getPlanId, plan.getId());
        List<PlanPilotBoatAssignments> pilotBoatAssignments = planPilotBoatAssignmentsService.list(pilotBoatWrapper);

        for (PlanPilotBoatAssignments assignment : pilotBoatAssignments) {
            PilotBoats pilotBoat = pilotBoatsService.getById(assignment.getPilotBoatId());
            if (pilotBoat != null) {
                pilotBoat.setCurrentStatus("AVAILABLE");
                pilotBoat.setUpdatedAt(now);
                pilotBoatsService.updateById(pilotBoat);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releasePlanResources(Long planId) {
        if (planId == null) return;
        PilotagePlans plan = this.getById(planId);
        if (plan == null) return;
        releaseResources(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelPlan(Long id, String reason) {
        PilotagePlans plan = this.getById(id);
        if (plan == null) {
            return false;
        }

        if (!PlanStatusEnum.PLANNING.getCode().equals(plan.getPlanStatus()) &&
                !PlanStatusEnum.PUBLISHED.getCode().equals(plan.getPlanStatus()) &&
                !PlanStatusEnum.CONFIRMED.getCode().equals(plan.getPlanStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID);
        }

        plan.setPlanStatus(PlanStatusEnum.CANCELLED.getCode());
        plan.setCancelReason(reason);
        plan.setUpdatedAt(new Date());

        releaseResources(plan);

        return this.updateById(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignTugboat(Long planId, TugboatAssignmentDTO dto) {
        PilotagePlans plan = this.getById(planId);
        if (plan == null) {
            return false;
        }

        Tugboats tugboat = tugboatsService.getById(dto.getTugboatId());
        if (tugboat == null) {
            throw new BusinessException(ErrorCode.TUGBOAT_NOT_FOUND);
        }
        if (!"AVAILABLE".equals(tugboat.getStatus())) {
            throw new BusinessException(ErrorCode.TUGBOAT_NOT_AVAILABLE);
        }

        PlanTugboatAssignments assignment = new PlanTugboatAssignments();
        assignment.setPlanId(planId);
        assignment.setTugboatId(dto.getTugboatId());
        assignment.setAssignmentType(dto.getAssignmentType() != null ? dto.getAssignmentType() : "MAIN");
        assignment.setEstimatedStartTime(dto.getEstimatedStartTime() != null ? dto.getEstimatedStartTime() : plan.getPlanStartTime());
        assignment.setEstimatedEndTime(dto.getEstimatedEndTime() != null ? dto.getEstimatedEndTime() : plan.getPlanEndTime());
        assignment.setStatus("ASSIGNED");
        assignment.setCreatedAt(new Date());
        assignment.setUpdatedAt(new Date());

        return planTugboatAssignmentsService.save(assignment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPilotBoat(Long planId, PilotBoatAssignmentDTO dto) {
        PilotagePlans plan = this.getById(planId);
        if (plan == null) {
            return false;
        }

        PilotBoats pilotBoat = pilotBoatsService.getById(dto.getPilotBoatId());
        if (pilotBoat == null) {
            throw new BusinessException(ErrorCode.PILOT_BOAT_NOT_FOUND);
        }
        if (!"AVAILABLE".equals(pilotBoat.getCurrentStatus())) {
            throw new BusinessException(ErrorCode.PILOT_BOAT_NOT_AVAILABLE);
        }

        PlanPilotBoatAssignments assignment = new PlanPilotBoatAssignments();
        assignment.setPlanId(planId);
        assignment.setPilotBoatId(dto.getPilotBoatId());
        assignment.setPurpose(dto.getPurpose() != null ? dto.getPurpose() : "接送引航员");
        assignment.setEstimatedStartTime(dto.getEstimatedStartTime() != null ? dto.getEstimatedStartTime() : plan.getPlanStartTime());
        assignment.setEstimatedEndTime(dto.getEstimatedEndTime() != null ? dto.getEstimatedEndTime() : plan.getPlanEndTime());
        assignment.setStatus("ASSIGNED");
        assignment.setCreatedAt(new Date());
        assignment.setUpdatedAt(new Date());

        return planPilotBoatAssignmentsService.save(assignment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeTugboatAssignment(Long planId, Long assignmentId) {
        PlanTugboatAssignments assignment = planTugboatAssignmentsService.getById(assignmentId);
        if (assignment == null || !assignment.getPlanId().equals(planId)) {
            return false;
        }

        return planTugboatAssignmentsService.removeById(assignmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removePilotBoatAssignment(Long planId, Long assignmentId) {
        PlanPilotBoatAssignments assignment = planPilotBoatAssignmentsService.getById(assignmentId);
        if (assignment == null || !assignment.getPlanId().equals(planId)) {
            return false;
        }

        return planPilotBoatAssignmentsService.removeById(assignmentId);
    }

    @Override
    public ResourceAvailabilityResultDTO checkResourceAvailability(ResourceCheckRequestDTO request) {
        ResourceAvailabilityResultDTO result = new ResourceAvailabilityResultDTO();
        result.setResourceType(request.getResourceType());
        result.setResourceId(request.getResourceId());

        try {
            switch (request.getResourceType()) {
                case "PILOT":
                    result = checkPilotAvailability(request);
                    break;
                case "DRIVER":
                    result = checkDriverAvailability(request);
                    break;
                case "VEHICLE":
                    result = checkVehicleAvailability(request);
                    break;
                case "TUGBOAT":
                    result = checkTugboatAvailability(request);
                    break;
                case "PILOT_BOAT":
                    result = checkPilotBoatAvailability(request);
                    break;
                default:
                    result.setAvailable(false);
                    result.setMessage("不支持的资源类型: " + request.getResourceType());
            }
        } catch (Exception e) {
            log.error("检查资源可用性失败", e);
            result.setAvailable(false);
            result.setMessage("检查资源可用性失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<ResourceAvailabilityResultDTO> batchCheckResourceAvailability(List<ResourceCheckRequestDTO> requests) {
        return requests.stream()
                .map(this::checkResourceAvailability)
                .collect(Collectors.toList());
    }

    private ResourceAvailabilityResultDTO checkPilotAvailability(ResourceCheckRequestDTO request) {
        ResourceAvailabilityResultDTO result = new ResourceAvailabilityResultDTO();
        result.setResourceType("PILOT");
        result.setResourceId(request.getResourceId());

        Pilots pilot = pilotsService.getById(request.getResourceId());
        if (pilot == null) {
            result.setAvailable(false);
            result.setMessage("引航员不存在");
            return result;
        }

        if (!"ON_DUTY".equals(pilot.getOnDutyStatus())) {
            result.setAvailable(false);
            result.setMessage("引航员不在岗");
            return result;
        }

        LambdaQueryWrapper<PilotagePlans> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PilotagePlans::getAssignedPilotId, request.getResourceId())
                .ne(request.getExcludePlanId() != null, PilotagePlans::getId, request.getExcludePlanId())
                .and(w -> w.between(PilotagePlans::getPlanStartTime, request.getStartTime(), request.getEndTime())
                        .or()
                        .between(PilotagePlans::getPlanEndTime, request.getStartTime(), request.getEndTime())
                        .or(w2 -> w2.le(PilotagePlans::getPlanStartTime, request.getStartTime())
                        .ge(PilotagePlans::getPlanEndTime, request.getEndTime())))
                        .last("LIMIT 1");;

        long conflictCount = this.count(wrapper);
        if (conflictCount > 0) {
            result.setAvailable(false);
            result.setMessage("引航员在该时间段已有任务安排");

            List<PilotagePlans> conflictPlans = this.list(wrapper);
            if (!conflictPlans.isEmpty()) {
                PilotagePlans conflictPlan = conflictPlans.get(0);
                PlanConflictInfoDTO conflictInfo = new PlanConflictInfoDTO();
                conflictInfo.setConflictPlanId(conflictPlan.getId());
                conflictInfo.setConflictPlanCode(conflictPlan.getPlanCode());
                conflictInfo.setConflictTimeRange(
                        conflictPlan.getPlanStartTime() + " - " + conflictPlan.getPlanEndTime());
                conflictInfo.setConflictResourceType("PILOT");
                result.setConflictInfo(conflictInfo);
            }
            return result;
        }

        result.setAvailable(true);
        result.setMessage("引航员可用");
        return result;
    }

    private ResourceAvailabilityResultDTO checkDriverAvailability(ResourceCheckRequestDTO request) {
        // 类似checkPilotAvailability的实现
        ResourceAvailabilityResultDTO result = new ResourceAvailabilityResultDTO();
        result.setResourceType("DRIVER");
        result.setResourceId(request.getResourceId());

        return result;
    }

    private ResourceAvailabilityResultDTO checkVehicleAvailability(ResourceCheckRequestDTO request) {
        // 类似checkPilotAvailability的实现
        ResourceAvailabilityResultDTO result = new ResourceAvailabilityResultDTO();
        result.setResourceType("VEHICLE");
        result.setResourceId(request.getResourceId());

        return result;
    }

    private ResourceAvailabilityResultDTO checkTugboatAvailability(ResourceCheckRequestDTO request) {
        // 类似checkPilotAvailability的实现
        ResourceAvailabilityResultDTO result = new ResourceAvailabilityResultDTO();
        result.setResourceType("TUGBOAT");
        result.setResourceId(request.getResourceId());

        return result;
    }

    private ResourceAvailabilityResultDTO checkPilotBoatAvailability(ResourceCheckRequestDTO request) {
        // 类似checkPilotAvailability的实现
        ResourceAvailabilityResultDTO result = new ResourceAvailabilityResultDTO();
        result.setResourceType("PILOT_BOAT");
        result.setResourceId(request.getResourceId());

        return result;
    }

    @Override
    public PlanStatisticsDTO getPlanStatistics(String startDate, String endDate, Long pilotId, String vesselType) {
        PlanStatisticsDTO statistics = new PlanStatisticsDTO();

        LambdaQueryWrapper<PilotagePlans> wrapper = new LambdaQueryWrapper<>();

        if (startDate != null && endDate != null) {
            wrapper.between(PilotagePlans::getPlanStartTime, startDate, endDate);
        }

        if (pilotId != null) {
            wrapper.eq(PilotagePlans::getAssignedPilotId, pilotId);
        }

        List<PilotagePlans> plans = this.list(wrapper);

        statistics.setTotalCount(plans.size());
        statistics.setPlanningCount((int) plans.stream()
                .filter(p -> PlanStatusEnum.PLANNING.getCode().equals(p.getPlanStatus()))
                .count());
        statistics.setPublishedCount((int) plans.stream()
                .filter(p -> PlanStatusEnum.PUBLISHED.getCode().equals(p.getPlanStatus()))
                .count());
        statistics.setOngoingCount((int) plans.stream()
                .filter(p -> PlanStatusEnum.ONGOING.getCode().equals(p.getPlanStatus()))
                .count());
        statistics.setCompletedCount((int) plans.stream()
                .filter(p -> PlanStatusEnum.COMPLETED.getCode().equals(p.getPlanStatus()))
                .count());
        statistics.setCancelledCount((int) plans.stream()
                .filter(p -> PlanStatusEnum.CANCELLED.getCode().equals(p.getPlanStatus()))
                .count());

        statistics.setVesselTypeDistribution(new HashMap<>());
        statistics.setPilotDistribution(new HashMap<>());

        return statistics;
    }

    @Override
    public byte[] exportPlans(String startDate, String endDate, String format) {
        if (!"excel".equalsIgnoreCase(format)) {
            // 目前仅实现 Excel；其他格式返回空文件（避免前端报错）
            return new byte[0];
        }

        try {
            DateRange range = buildDateRange(startDate, endDate);

            LambdaQueryWrapper<PilotagePlans> wrapper = new LambdaQueryWrapper<>();
            if (range != null) {
                wrapper.ge(PilotagePlans::getPlanStartTime, range.start)
                        .le(PilotagePlans::getPlanStartTime, range.end);
            }
            wrapper.orderByDesc(PilotagePlans::getPlanStartTime);

            List<PilotagePlans> plans = this.list(wrapper);

            org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("引航计划");

            String[] headers = new String[] {
                    "计划编号", "计划状态", "船舶ID", "起始位置", "结束位置",
                    "计划开始时间", "计划结束时间", "引航员", "驾驶员", "车辆"
            };

            org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 18 * 256);
            }

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int i = 0; i < plans.size(); i++) {
                PilotagePlans p = plans.get(i);
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);

                String statusName = p.getPlanStatus();
                PlanStatusEnum st = PlanStatusEnum.fromCode(p.getPlanStatus());
                if (st != null) statusName = st.getName();

                String pilotName = "";
                if (p.getAssignedPilotId() != null) {
                    Pilots pilot = pilotsService.getById(p.getAssignedPilotId());
                    pilotName = pilot == null ? "" : (pilot.getPilotName() == null ? "" : pilot.getPilotName());
                }
                String driverName = "";
                if (p.getAssignedDriverId() != null) {
                    Drivers driver = driversService.getById(p.getAssignedDriverId());
                    driverName = driver == null ? "" : (driver.getName() == null ? "" : driver.getName());
                }
                String vehicleName = "";
                if (p.getAssignedVehicleId() != null) {
                    Vehicles vehicle = vehiclesService.getById(p.getAssignedVehicleId());
                    if (vehicle != null) {
                        String plate = vehicle.getLicensePlate() == null ? "" : vehicle.getLicensePlate();
                        String model = vehicle.getVehicleModel() == null ? "" : vehicle.getVehicleModel();
                        vehicleName = (plate + " " + model).trim();
                    }
                }

                row.createCell(0).setCellValue(p.getPlanCode() == null ? "" : p.getPlanCode());
                row.createCell(1).setCellValue(statusName == null ? "" : statusName);
                row.createCell(2).setCellValue(p.getVesselId() == null ? "" : String.valueOf(p.getVesselId()));
                row.createCell(3).setCellValue(p.getStartLocationName() == null ? "" : p.getStartLocationName());
                row.createCell(4).setCellValue(p.getEndLocationName() == null ? "" : p.getEndLocationName());
                row.createCell(5).setCellValue(p.getPlanStartTime() == null ? "" : sdf.format(p.getPlanStartTime()));
                row.createCell(6).setCellValue(p.getPlanEndTime() == null ? "" : sdf.format(p.getPlanEndTime()));
                row.createCell(7).setCellValue(pilotName);
                row.createCell(8).setCellValue(driverName);
                row.createCell(9).setCellValue(vehicleName);
            }

            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            wb.write(out);
            wb.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出引航计划失败: startDate={}, endDate={}", startDate, endDate, e);
            return new byte[0];
        }
    }

    private static class DateRange {
        final Date start;
        final Date end;
        DateRange(Date start, Date end) { this.start = start; this.end = end; }
    }

    private DateRange buildDateRange(String startDate, String endDate) {
        try {
            if ((startDate == null || startDate.trim().isEmpty()) && (endDate == null || endDate.trim().isEmpty())) {
                return null;
            }
            Date s = parseDateFlex(startDate);
            Date e = parseDateFlex(endDate);

            // 只有一个值时，当作“某一天”
            if (s != null && e == null) e = endOfDay(s);
            if (s == null && e != null) s = startOfDay(e);

            if (s != null && e != null && e.before(s)) {
                Date tmp = s;
                s = startOfDay(e);
                e = endOfDay(tmp);
            }
            if (s == null || e == null) return null;
            return new DateRange(s, e);
        } catch (Exception ex) {
            log.warn("解析导出时间范围失败: startDate={}, endDate={}", startDate, endDate, ex);
            return null;
        }
    }

    private Date parseDateFlex(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s);
        } catch (Exception ignore) {
            // ignore
        }
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(s);
        } catch (Exception ignore) {
            // ignore
        }
        try {
            return java.util.Date.from(java.time.Instant.parse(s));
        } catch (Exception ignore) {
            // ignore
        }
        try {
            return java.util.Date.from(java.time.OffsetDateTime.parse(s).toInstant());
        } catch (Exception ignore) {
            return null;
        }
    }

    private Date startOfDay(Date d) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(d);
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private Date endOfDay(Date d) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(d);
        c.set(java.util.Calendar.HOUR_OF_DAY, 23);
        c.set(java.util.Calendar.MINUTE, 59);
        c.set(java.util.Calendar.SECOND, 59);
        c.set(java.util.Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setPlanDate(Long id, PlanDateDTO dto) {
        log.info("设置计划日期时间: id={}, planStartTime={}, planEndTime={}",
                id, dto.getPlanStartTime(), dto.getPlanEndTime());

        PilotagePlans plan = this.getById(id);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        if (!PlanStatusEnum.PLANNING.getCode().equals(plan.getPlanStatus()) &&
                !PlanStatusEnum.PUBLISHED.getCode().equals(plan.getPlanStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID, "计划状态不允许修改日期");
        }

        if (dto.getPlanEndTime() != null && dto.getPlanStartTime() != null &&
                dto.getPlanEndTime().before(dto.getPlanStartTime())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "计划结束时间不能早于开始时间");
        }

        plan.setPlanStartTime(dto.getPlanStartTime());
        plan.setPlanEndTime(dto.getPlanEndTime());
        if (dto.getBoardingTime() != null) {
            plan.setBoardingTime(dto.getBoardingTime());
        }
        if (dto.getLeavingTime() != null) {
            plan.setLeavingTime(dto.getLeavingTime());
        }
        plan.setUpdatedAt(new Date());

        updateRelatedAssignmentsTime(id, dto);

        return this.updateById(plan);
    }

    private void updateRelatedAssignmentsTime(Long planId, PlanDateDTO dto) {
        Date now = new Date();

        LambdaQueryWrapper<PlanTugboatAssignments> tugboatWrapper = new LambdaQueryWrapper<>();
        tugboatWrapper.eq(PlanTugboatAssignments::getPlanId, planId)
                .eq(PlanTugboatAssignments::getStatus, "ASSIGNED");

        List<PlanTugboatAssignments> tugboatAssignments = planTugboatAssignmentsService.list(tugboatWrapper);
        if (!tugboatAssignments.isEmpty()) {
            for (PlanTugboatAssignments assignment : tugboatAssignments) {
                assignment.setEstimatedStartTime(dto.getPlanStartTime());
                assignment.setEstimatedEndTime(dto.getPlanEndTime());
                assignment.setUpdatedAt(now);
            }
            planTugboatAssignmentsService.updateBatchById(tugboatAssignments);
        }

        LambdaQueryWrapper<PlanPilotBoatAssignments> pilotBoatWrapper = new LambdaQueryWrapper<>();
        pilotBoatWrapper.eq(PlanPilotBoatAssignments::getPlanId, planId)
                .eq(PlanPilotBoatAssignments::getStatus, "ASSIGNED");

        List<PlanPilotBoatAssignments> pilotBoatAssignments = planPilotBoatAssignmentsService.list(pilotBoatWrapper);
        if (!pilotBoatAssignments.isEmpty()) {
            for (PlanPilotBoatAssignments assignment : pilotBoatAssignments) {
                assignment.setEstimatedStartTime(dto.getPlanStartTime());
                assignment.setEstimatedEndTime(dto.getPlanEndTime());
                assignment.setUpdatedAt(now);
            }
            planPilotBoatAssignmentsService.updateBatchById(pilotBoatAssignments);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SaveAssignmentsResultDTO saveTugboatAssignments(Long planId, List<TugboatAssignmentDTO> assignments) {
        log.info("保存拖轮分配: planId={}, assignmentsCount={}", planId, assignments.size());

        SaveAssignmentsResultDTO result = new SaveAssignmentsResultDTO();
        List<FailedAssignmentDTO> failedItems = new ArrayList<>();
        List<PlanTugboatAssignments> successAssignments = new ArrayList<>();

        PilotagePlans plan = this.getById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        LambdaQueryWrapper<PlanTugboatAssignments> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(PlanTugboatAssignments::getPlanId, planId);
        Map<Long, PlanTugboatAssignments> existingMap = planTugboatAssignmentsService.list(existingWrapper)
                .stream()
                .collect(Collectors.toMap(PlanTugboatAssignments::getTugboatId, Function.identity()));

        Date now = new Date();
        for (int i = 0; i < assignments.size(); i++) {
            TugboatAssignmentDTO dto = assignments.get(i);
            try {
                Tugboats tugboat = tugboatsService.getById(dto.getTugboatId());
                if (tugboat == null) {
                    throw new BusinessException(ErrorCode.TUGBOAT_NOT_FOUND, "拖轮不存在: " + dto.getTugboatId());
                }

                if (!"AVAILABLE".equals(tugboat.getStatus())) {
                    // 检查是否已经在当前计划中分配（允许更新已分配的资源）
                    PlanTugboatAssignments existing = existingMap.get(dto.getTugboatId());
                    if (existing == null || !"ASSIGNED".equals(existing.getStatus())) {
                        throw new BusinessException(ErrorCode.TUGBOAT_NOT_AVAILABLE,
                                "拖轮不可用: " + tugboat.getTugboatName() + "(" + tugboat.getStatus() + ")");
                    }
                }

                // 检查时间冲突（排除当前计划）
                if (dto.getEstimatedStartTime() != null && dto.getEstimatedEndTime() != null) {
                    checkTugboatTimeConflict(dto.getTugboatId(), dto.getEstimatedStartTime(),
                            dto.getEstimatedEndTime(), planId);
                }

                PlanTugboatAssignments assignment = existingMap.get(dto.getTugboatId());
                if (assignment == null) {
                    assignment = new PlanTugboatAssignments();
                    assignment.setPlanId(planId);
                    assignment.setTugboatId(dto.getTugboatId());
                    assignment.setCreatedAt(now);
                }

                assignment.setAssignmentType(dto.getAssignmentType() != null ? dto.getAssignmentType() : "MAIN");
                assignment.setEstimatedStartTime(dto.getEstimatedStartTime() != null ?
                        dto.getEstimatedStartTime() : plan.getPlanStartTime());
                assignment.setEstimatedEndTime(dto.getEstimatedEndTime() != null ?
                        dto.getEstimatedEndTime() : plan.getPlanEndTime());
                assignment.setStatus("ASSIGNED");
                assignment.setUpdatedAt(now);

                successAssignments.add(assignment);

            } catch (Exception e) {
                log.error("保存拖轮分配失败[索引={}, tugboatId={}]: {}", i, dto.getTugboatId(), e.getMessage());

                FailedAssignmentDTO failedItem = new FailedAssignmentDTO();
                failedItem.setIndex(i);
                failedItem.setReason(e.getMessage());
                failedItem.setResourceId(dto.getTugboatId());
                failedItem.setResourceType("TUGBOAT");
                failedItems.add(failedItem);
            }
        }

        if (!successAssignments.isEmpty()) {
            planTugboatAssignmentsService.saveOrUpdateBatch(successAssignments);
        }

        Set<Long> newTugboatIds = assignments.stream()
                .map(TugboatAssignmentDTO::getTugboatId)
                .collect(Collectors.toSet());

        List<Long> toRemoveIds = existingMap.values().stream()
                .filter(a -> !newTugboatIds.contains(a.getTugboatId()))
                .map(PlanTugboatAssignments::getId)
                .collect(Collectors.toList());

        if (!toRemoveIds.isEmpty()) {
            planTugboatAssignmentsService.removeByIds(toRemoveIds);
        }

        result.setSuccessCount(successAssignments.size());
        result.setFailedCount(failedItems.size());
        result.setFailedItems(failedItems);

        return result;
    }

    private void checkTugboatTimeConflict(Long tugboatId, Date startTime, Date endTime, Long excludePlanId) {
        MPJLambdaWrapper<PlanTugboatAssignments> wrapper = new MPJLambdaWrapper<PlanTugboatAssignments>()
                .select(PlanTugboatAssignments::getPlanId)
                .select(PilotagePlans::getPlanCode)
                .select(PlanTugboatAssignments::getEstimatedStartTime)
                .select(PlanTugboatAssignments::getEstimatedEndTime)
                .eq(PlanTugboatAssignments::getTugboatId, tugboatId)
                .eq(PlanTugboatAssignments::getStatus, "ASSIGNED")
                .ne(excludePlanId != null, PlanTugboatAssignments::getPlanId, excludePlanId)
                .leftJoin(PilotagePlans.class, PilotagePlans::getId, PlanTugboatAssignments::getPlanId)
                .and(w -> w
                        .between(PlanTugboatAssignments::getEstimatedStartTime, startTime, endTime)
                        .or()
                        .between(PlanTugboatAssignments::getEstimatedEndTime, startTime, endTime)
                        .or(w2 -> w2.le(PlanTugboatAssignments::getEstimatedStartTime, startTime)
                                .ge(PlanTugboatAssignments::getEstimatedEndTime, endTime)))
                .last("LIMIT 1");

        List<Map<String, Object>> conflicts = planTugboatAssignmentsService.listMaps(wrapper);
        if (!conflicts.isEmpty()) {
            Map<String, Object> conflict = conflicts.get(0);
            String planCode = conflict.get("planCode") != null ?
                    (String) conflict.get("planCode") : "未知计划";
            Date conflictStart = (Date) conflict.get("estimatedStartTime");
            Date conflictEnd = (Date) conflict.get("estimatedEndTime");

            String startTimeStr = conflictStart != null ?
                    DateFormatUtils.format(conflictStart, "yyyy-MM-dd HH:mm") : "未设置开始时间";
            String endTimeStr = conflictEnd != null ?
                    DateFormatUtils.format(conflictEnd, "yyyy-MM-dd HH:mm") : "未设置结束时间";

            throw new BusinessException(ErrorCode.RESOURCE_TIME_CONFLICT,
                    String.format("拖轮时间冲突，已在计划 %s 中分配 (%s - %s)",
                            planCode, startTimeStr, endTimeStr));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SaveAssignmentsResultDTO savePilotBoatAssignments(Long planId, List<PilotBoatAssignmentDTO> assignments) {
        log.info("保存引航艇分配: planId={}, assignmentsCount={}", planId, assignments.size());

        // 实现逻辑与 saveTugboatAssignments 类似
        SaveAssignmentsResultDTO result = new SaveAssignmentsResultDTO();
        List<FailedAssignmentDTO> failedItems = new ArrayList<>();
        List<PlanPilotBoatAssignments> successAssignments = new ArrayList<>();

        LambdaQueryWrapper<PlanPilotBoatAssignments> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(PlanPilotBoatAssignments::getPlanId, planId);
        Map<Long, PlanPilotBoatAssignments> existingMap = planPilotBoatAssignmentsService.list(existingWrapper)
                .stream()
                .collect(Collectors.toMap(PlanPilotBoatAssignments::getPilotBoatId, Function.identity()));

        Date now = new Date();
        for (int i = 0; i < assignments.size(); i++) {
            PilotBoatAssignmentDTO dto = assignments.get(i);
            try {
                PilotBoats pilotBoat = pilotBoatsService.getById(dto.getPilotBoatId());
                if (pilotBoat == null) {
                    throw new BusinessException(ErrorCode.PILOT_BOAT_NOT_FOUND,
                            "引航艇不存在: " + dto.getPilotBoatId());
                }

                PlanPilotBoatAssignments assignment = existingMap.get(dto.getPilotBoatId());
                if (assignment == null) {
                    assignment = new PlanPilotBoatAssignments();
                    assignment.setPlanId(planId);
                    assignment.setPilotBoatId(dto.getPilotBoatId());
                    assignment.setCreatedAt(now);
                }

                assignment.setPurpose(dto.getPurpose() != null ? dto.getPurpose() : "接送引航员");
                assignment.setEstimatedStartTime(dto.getEstimatedStartTime());
                assignment.setEstimatedEndTime(dto.getEstimatedEndTime());
                assignment.setStatus("ASSIGNED");
                assignment.setUpdatedAt(now);

                successAssignments.add(assignment);

            } catch (Exception e) {
                log.error("保存引航艇分配失败[索引={}, pilotBoatId={}]: {}", i, dto.getPilotBoatId(), e.getMessage());

                FailedAssignmentDTO failedItem = new FailedAssignmentDTO();
                failedItem.setIndex(i);
                failedItem.setReason(e.getMessage());
                failedItem.setResourceId(dto.getPilotBoatId());
                failedItem.setResourceType("PILOT_BOAT");
                failedItems.add(failedItem);
            }
        }

        if (!successAssignments.isEmpty()) {
            planPilotBoatAssignmentsService.saveOrUpdateBatch(successAssignments);
        }

        Set<Long> newPilotBoatIds = assignments.stream()
                .map(PilotBoatAssignmentDTO::getPilotBoatId)
                .collect(Collectors.toSet());

        List<Long> toRemoveIds = existingMap.values().stream()
                .filter(a -> !newPilotBoatIds.contains(a.getPilotBoatId()))
                .map(PlanPilotBoatAssignments::getId)
                .collect(Collectors.toList());

        if (!toRemoveIds.isEmpty()) {
            planPilotBoatAssignmentsService.removeByIds(toRemoveIds);
        }

        result.setSuccessCount(successAssignments.size());
        result.setFailedCount(failedItems.size());
        result.setFailedItems(failedItems);

        return result;
    }

    @Override
    public List<PlanChangeDTO> getPlanChangeReport(LocalDate startDate, LocalDate endDate, String status) {
        log.info("获取计划改变查询统计: startDate={}, endDate={}, status={}", startDate, endDate, status);

        LambdaQueryWrapper<PilotagePlans> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotagePlans::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PilotagePlans::getPlanStatus, status);
        }
        wrapper.orderByAsc(PilotagePlans::getCreatedAt);

        List<PilotagePlans> plans = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();
        List<Agents> agents = agentsService.list();
        List<Pilots> pilots = pilotsService.list();

        List<PlanChangeDTO> result = new ArrayList<>();
        for (PilotagePlans plan : plans) {
            PlanChangeDTO dto = new PlanChangeDTO();

            Vessels vessel = vessels.stream()
                    .filter(v -> v.getId().equals(plan.getVesselId()))
                    .findFirst()
                    .orElse(null);

            dto.setShipName(vessel.getNameZh());
            dto.setEnglishName(vessel.getNameEn());
            dto.setStatus(plan.getPlanStatus());
            dto.setReason(plan.getCancelReason() != null ? plan.getCancelReason() : "计划变更");
            dto.setDynamic(plan.getOperationTypeFlag());
            dto.setTimeDate(DateUtil.formatDateTime(plan.getCreatedAt()));

            if (vessel != null) {
                dto.setNationality(vessel.getNationality());
                dto.setGrossTonnage(vessel.getGrossTonnage());
            }

            if (plan.getAssignedPilotId() != null) {
                Pilots pilot = pilots.stream()
                        .filter(p -> p.getId().equals(plan.getAssignedPilotId()))
                        .findFirst()
                        .orElse(null);
                if (pilot != null) {
                    dto.setMainPilot(pilot.getPilotName());
                }
            }

            dto.setRemarks(plan.getNotes());

            result.add(dto);
        }

        return result;
    }

}
