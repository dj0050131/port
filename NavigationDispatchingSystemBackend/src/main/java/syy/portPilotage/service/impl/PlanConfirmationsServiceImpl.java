package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.plan.PlanConfirmationDTO;
import syy.portPilotage.dto.plan.PlanConfirmationQueryDTO;
import syy.portPilotage.dto.plan.PlanConfirmationResultDTO;
import syy.portPilotage.dto.plan.ResourceConfirmationResultDTO;
import syy.portPilotage.entity.*;
import syy.portPilotage.enums.ConfirmationStatusEnum;
import syy.portPilotage.enums.PlanStatusEnum;
import syy.portPilotage.service.*;
import syy.portPilotage.mapper.PlanConfirmationsMapper;
import org.springframework.stereotype.Service;
import syy.portPilotage.util.SecurityUtil;
import syy.portPilotage.rabbitmq.service.MessageService;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanConfirmationsServiceImpl extends ServiceImpl<PlanConfirmationsMapper, PlanConfirmations> implements PlanConfirmationsService {

    private final PlanConfirmationsMapper planConfirmationsMapper;
    private final MessageService messageService;
    private final PlanPilotBoatAssignmentsService planPilotBoatAssignmentsService;
    private final PlanTugboatAssignmentsService planTugboatAssignmentsService;
    private final UsersService usersService;

    private final PilotsService pilotsService;
    private final DriversService driversService;
    private final PilotBoatsService pilotBoatsService;
    private final TugboatsService tugboatsService;
    @Lazy
    private final PilotagePlansService pilotagePlansService;

    @Lazy
    private final PlanPilotAssignmentsService planPilotAssignmentsService;

    @Lazy
    private final PlanDriverAssignmentsService planDriverAssignmentsService;

    @Lazy
    private final PlanVehicleAssignmentsService planVehicleAssignmentsService;
    private final VesselsService vesselsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createConfirmation(PlanConfirmationDTO dto) {  //多处使用
        log.info("创建引航计划确认记录: planId={}, resourceType={}, resourceId={}",
                dto.getPlanId(), dto.getResourceType(), dto.getResourceId());

        PilotagePlans plan = pilotagePlansService.getById(dto.getPlanId());
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        // 真实业务：必须在计划发布后才允许资源确认
        // 计划可能已进入 CONFIRMED（例如部分场景下先确认引航员），此时仍允许对其余资源进行确认
        String ps = plan.getPlanStatus();
        if (!PlanStatusEnum.PUBLISHED.getCode().equals(ps) && !PlanStatusEnum.CONFIRMED.getCode().equals(ps)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "计划未发布，不能确认资源");
        }

        PlanConfirmations existing = this.lambdaQuery()
                .eq(PlanConfirmations::getPlanId, dto.getPlanId())
                .eq(PlanConfirmations::getResourceType, dto.getResourceType())
                .eq(PlanConfirmations::getResourceId, dto.getResourceId())
                .one();

        Long currentUserId = SecurityUtil.getCurrentUserId();
        Date now = new Date();

        if (existing != null) {
            if (ConfirmationStatusEnum.CONFIRMED.getCode().equals(existing.getConfirmationStatus())) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该资源已确认，请勿重复确认");
            }
            // PENDING / REJECTED -> CONFIRMED
            existing.setConfirmMethod(dto.getConfirmMethod());
            existing.setConfirmedBy(currentUserId);
            existing.setConfirmedAt(now);
            existing.setConfirmationStatus(ConfirmationStatusEnum.CONFIRMED.getCode());
            existing.setNotes(dto.getNotes());
            boolean updated = this.updateById(existing);
            if (!updated) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新确认记录失败");
            }
        } else {
            PlanConfirmations confirmation = new PlanConfirmations();
            confirmation.setPlanId(dto.getPlanId());
            confirmation.setResourceType(dto.getResourceType());
            confirmation.setResourceId(dto.getResourceId());
            confirmation.setConfirmMethod(dto.getConfirmMethod());
            confirmation.setConfirmedBy(currentUserId);
            confirmation.setConfirmedAt(now);
            confirmation.setConfirmationStatus(ConfirmationStatusEnum.CONFIRMED.getCode());
            confirmation.setNotes(dto.getNotes());

            boolean saved = save(confirmation);
            if (!saved) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "保存确认记录失败");
            }
        }

        updateResourceAssignmentStatus(dto.getPlanId(), dto.getResourceType(), dto.getResourceId());

        checkAndUpdatePlanConfirmationStatus(dto.getPlanId());

        try {
            notifyPilotDriverConfirmOrCancel(dto.getPlanId(), dto.getResourceType(), dto.getResourceId(), "CONFIRMED", null);
        } catch (Exception e) {
            log.warn("发送确认邮件失败: planId={}, resourceType={}, resourceId={}, err={}",
                    dto.getPlanId(), dto.getResourceType(), dto.getResourceId(), e.getMessage());
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchConfirm(List<PlanConfirmationDTO> dtos) {
        log.info("批量确认引航计划: 数量={}", dtos.size());

        for (PlanConfirmationDTO dto : dtos) {
            createConfirmation(dto);
        }

        return true;
    }

    @Override
    public Map<String, Object> getAssignmentsResourceList(PlanConfirmationQueryDTO query) {
        log.info("查询各项资源的确认情况: planCode={}, resourceType={}, confirmMethod={}, confirmationStatus={}, page={}, pageSize={}",
                query.getPlanCode(), query.getResourceType(), query.getConfirmMethod(),
                query.getConfirmationStatus(), query.getPage(), query.getPageSize());

        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(20);
        }
        if (query.getPageSize() > 100) {
            query.setPageSize(100);
        }

        int offset = (query.getPage() - 1) * query.getPageSize();
        query.setOffset(offset);

        List<Map<String, Object>> list = planConfirmationsMapper.selectResourceConfirmationList(query);

        Long total = planConfirmationsMapper.selectResourceConfirmationCount(query);

        List<ResourceConfirmationResultDTO> resultList = list.stream()
                .map(this::mapToResourceConfirmationResultDTO)
                .collect(Collectors.toList());

        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("list", resultList);
        pageResult.put("total", total);
        pageResult.put("page", query.getPage());
        pageResult.put("pageSize", query.getPageSize());
        pageResult.put("pages", (int) Math.ceil((double) total / query.getPageSize()));

        log.info("查询完成: 找到{}条记录, 共{}页", resultList.size(), pageResult.get("pages"));

        return pageResult;
    }

    @Override
    public List<Map<String, Object>> getPlanConfirmationStatsByType(Long planId) {
        log.info("获取计划的资源确认统计: planId={}", planId);

        PilotagePlans plan = pilotagePlansService.getById(planId);
        if (plan == null) {
            throw new RuntimeException("引航计划不存在");
        }

        List<Map<String, Object>> stats = planConfirmationsMapper.selectPlanConfirmationStats(planId);

        // 如果没有确认记录，返回空列表
        if (stats == null || stats.isEmpty()) {
            return new ArrayList<>();
        }

        return stats;
    }

        private ResourceConfirmationResultDTO mapToResourceConfirmationResultDTO(Map<String, Object> map) {
        ResourceConfirmationResultDTO dto = new ResourceConfirmationResultDTO();

        // 基本字段
        dto.setPlanId((Long) map.get("plan_id"));
        dto.setPlanCode((String) map.get("plan_code"));
        dto.setPlanStatus((String) map.get("plan_status"));
        dto.setResourceType((String) map.get("resource_type"));
        dto.setAssignmentId((Long) map.get("assignment_id"));
        // 船名字段：不同SQL有不同别名，统一兜底
        String vesselZh = (String) (map.get("vessel_name_zh") != null ? map.get("vessel_name_zh") : map.get("vessel_name"));
        dto.setVesselNameZh(vesselZh);
        dto.setVesselNameZn(vesselZh); // 兼容旧字段
        dto.setVesselNameEn((String) map.get("vessel_name_en"));
        dto.setResourceId((Long) map.get("resource_id"));
        Object createdAtObj = map.get("assignment_created_at");
        if (createdAtObj != null) {
            if (createdAtObj instanceof LocalDateTime) {
                dto.setCreatedAt(Date.from(((LocalDateTime) createdAtObj).atZone(ZoneId.systemDefault()).toInstant()));
            } else if (createdAtObj instanceof Date) {
                // 如果已经是 Date 类型，直接设置
                dto.setCreatedAt((Date) createdAtObj);
            }
        }
        dto.setResourceName((String) map.get("resource_name"));
        Object estStart = map.get("estimated_start_time");
        if (estStart instanceof LocalDateTime) {
            dto.setEstimatedStartTime(Date.from(((LocalDateTime) estStart).atZone(ZoneId.systemDefault()).toInstant()));
        } else if (estStart instanceof Date) {
            dto.setEstimatedStartTime((Date) estStart);
        }
        Object estEnd = map.get("estimated_end_time");
        if (estEnd instanceof LocalDateTime) {
            dto.setEstimatedEndTime(Date.from(((LocalDateTime) estEnd).atZone(ZoneId.systemDefault()).toInstant()));
        } else if (estEnd instanceof Date) {
            dto.setEstimatedEndTime((Date) estEnd);
        }
        String assignmentStatus = (String) map.get("assignment_status");
        dto.setAssignmentStatus(assignmentStatus);

        dto.setConfirmationId((Long) map.get("confirmation_id"));
        dto.setConfirmMethod((String) map.get("confirm_method"));
        dto.setConfirmedBy((Long) map.get("confirmed_by"));
        String confirmationStatus = (String) map.get("confirmation_status");

        // 关键：对外展示的“确认状态”优先使用分配表状态（当其不是 ASSIGNED 时），否则回退到确认表状态。
        // 这样 SYSTEM_CANCELLATION / CANCELLED 等会正确展示且可用于前端禁用操作。
        String effectiveStatus;
        if (assignmentStatus != null && !"ASSIGNED".equals(assignmentStatus)) {
            effectiveStatus = assignmentStatus;
        } else if (confirmationStatus != null && !confirmationStatus.trim().isEmpty()) {
            effectiveStatus = confirmationStatus;
        } else {
            effectiveStatus = ConfirmationStatusEnum.PENDING.getCode();
        }
        dto.setConfirmationStatus(effectiveStatus);
        dto.setConfirmedByName((String) map.get("confirmed_by_name"));
        Object confirmedAtObj = map.get("confirmed_at");
        if (confirmedAtObj != null) {
            if (confirmedAtObj instanceof LocalDateTime) {
                dto.setConfirmedAt(Date.from(((LocalDateTime) confirmedAtObj).atZone(ZoneId.systemDefault()).toInstant()));
            } else if (confirmedAtObj instanceof Date) {
                // 如果已经是 Date 类型，直接设置
                dto.setConfirmedAt((Date) confirmedAtObj);
            }
        }

        dto.setNotes((String) map.get("confirmation_notes"));

        // 船舶信息
        dto.setVesselName(vesselZh);

        dto.setResourceTypeName(syy.portPilotage.enums.ResourceTypeEnum.getNameByCode(dto.getResourceType()));

        if (dto.getConfirmMethod() != null) {
            dto.setConfirmMethodName(syy.portPilotage.enums.ConfirmMethodEnum.getNameByCode(dto.getConfirmMethod()));
        }

        if (dto.getConfirmationStatus() != null) {
            dto.setConfirmationStatusName(ConfirmationStatusEnum.getNameByCode(dto.getConfirmationStatus()));
        } else {
            // 如果没有确认记录，状态为"待确认"
            dto.setConfirmationStatus("PENDING");
            dto.setConfirmationStatusName("待确认");
        }

        if (dto.getAssignmentStatus() != null) {
            dto.setAssignmentStatusName(getAssignmentStatusName(dto.getAssignmentStatus()));
        }

        dto.setResourceDetail(getResourceDetailForAssignment(dto.getResourceType(), map));

        return dto;
    }

        private Map<String, Object> getResourceDetailForAssignment(String resourceType, Map<String, Object> map) {
        Map<String, Object> detail = new HashMap<>();

        switch (resourceType) {
            case "PILOT":
                detail.put("pilotCode", map.get("resource_code"));
                detail.put("pilotName", map.get("resource_name"));
                detail.put("phone", map.get("resource_phone"));
                detail.put("onDutyStatus", map.get("resource_status"));
                detail.put("roleType", map.get("role_type"));
                detail.put("estimatedStartTime", map.get("estimated_start_time"));
                detail.put("estimatedEndTime", map.get("estimated_end_time"));
                break;

            case "DRIVER":
                detail.put("driverCode", map.get("resource_code"));
                detail.put("name", map.get("resource_name"));
                detail.put("phone", map.get("resource_phone"));
                detail.put("onDutyStatus", map.get("resource_status"));
                detail.put("taskType", map.get("task_type"));
                detail.put("licensePlate", map.get("license_plate"));
                detail.put("vehicleModel", map.get("vehicle_model"));
                detail.put("estimatedStartTime", map.get("estimated_start_time"));
                detail.put("estimatedEndTime", map.get("estimated_end_time"));
                break;

            case "TUGBOAT":
                detail.put("tugboatCode", map.get("resource_code"));
                detail.put("tugboatName", map.get("resource_name"));
                detail.put("callSign", map.get("call_sign"));
                detail.put("status", map.get("resource_status"));
                detail.put("assignmentType", map.get("assignment_type"));
                detail.put("estimatedStartTime", map.get("estimated_start_time"));
                detail.put("estimatedEndTime", map.get("estimated_end_time"));
                break;

            case "PILOT_BOAT":
                detail.put("boatName", map.get("resource_name"));
                detail.put("vesselCallSign", map.get("resource_code"));
                detail.put("currentStatus", map.get("resource_status"));
                detail.put("purpose", map.get("purpose"));
                detail.put("estimatedStartTime", map.get("estimated_start_time"));
                detail.put("estimatedEndTime", map.get("estimated_end_time"));
                break;

            case "VEHICLE":
                detail.put("licensePlate", map.get("resource_name"));
                detail.put("vehicleCode", map.get("resource_code"));
                detail.put("vehicleStatus", map.get("resource_status"));
                detail.put("purpose", map.get("purpose"));
                detail.put("estimatedStartTime", map.get("estimated_start_time"));
                detail.put("estimatedEndTime", map.get("estimated_end_time"));
                break;
        }

        return detail;
    }
        private String getAssignmentStatusName(String statusCode) {
        switch (statusCode) {
            case "ASSIGNED":
                return "已分配";
            case "CONFIRMED":
                return "已确认";
            case "CANCELLED":
                return "已取消";
            case "COMPLETED":
                return "已完成";
            default:
                return statusCode;
        }
    }

        private Object getResourceDetail(String resourceType, Long resourceId) {
        if (resourceId == null) {
            return null;
        }

        try {
            switch (resourceType) {
                case "PILOT":
                    Pilots pilot = pilotsService.getById(resourceId);
                    return pilot != null ? getPilotDetail(pilot) : null;
                case "DRIVER":
                    Drivers driver = driversService.getById(resourceId);
                    return driver != null ? getDriverDetail(driver) : null;
                case "PILOT_BOAT":
                    PilotBoats pilotBoat = pilotBoatsService.getById(resourceId);
                    return pilotBoat != null ? getPilotBoatDetail(pilotBoat) : null;
                case "TUGBOAT":
                    Tugboats tugboat = tugboatsService.getById(resourceId);
                    return tugboat != null ? getTugboatDetail(tugboat) : null;
                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("获取资源详细信息失败: resourceType={}, resourceId={}", resourceType, resourceId, e);
            return null;
        }
    }

        private Map<String, Object> getPilotDetail(Pilots pilot) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("pilotCode", pilot.getPilotCode());
        detail.put("pilotName", pilot.getPilotName());
        detail.put("pilotLevel", pilot.getPilotLevel());
        detail.put("phone", pilot.getPhone());
        detail.put("onDutyStatus", pilot.getOnDutyStatus());
        detail.put("certificateNo", pilot.getCertificateNo());
        return detail;
    }

        private Map<String, Object> getDriverDetail(Drivers driver) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("driverCode", driver.getDriverCode());
        detail.put("name", driver.getName());
        detail.put("licenseNumber", driver.getLicenseNumber());
        detail.put("phone", driver.getPhone());
        detail.put("onDutyStatus", driver.getOnDutyStatus());
        detail.put("licenseType", driver.getLicenseType());
        return detail;
    }

        private Map<String, Object> getPilotBoatDetail(PilotBoats pilotBoat) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("boatName", pilotBoat.getBoatName());
        detail.put("vesselCallSign", pilotBoat.getVesselCallSign());
        detail.put("imoNumber", pilotBoat.getImoNumber());
        detail.put("currentStatus", pilotBoat.getCurrentStatus());
        detail.put("maxSpeed", pilotBoat.getMaxSpeed());
        detail.put("passengerCapacity", pilotBoat.getPassengerCapacity());
        return detail;
    }

        private Map<String, Object> getTugboatDetail(Tugboats tugboat) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("tugboatName", tugboat.getTugboatName());
        detail.put("tugboatCode", tugboat.getTugboatCode());
        detail.put("callSign", tugboat.getCallSign());
        detail.put("tugboatType", tugboat.getTugboatType());
        detail.put("bollardPull", tugboat.getBollardPull());
        detail.put("status", tugboat.getStatus());
        detail.put("ownerCompany", tugboat.getOwnerCompany());
        return detail;
    }

    @Override
    public Map<String, Object> getConfirmationList(PlanConfirmationQueryDTO query) {
        int offset = (query.getPage() - 1) * query.getPageSize();
        query.setPageSize(query.getPageSize());
        query.setOffset(offset);

        List<Map<String, Object>> list = planConfirmationsMapper.selectConfirmationList(query);

        Long total = planConfirmationsMapper.selectConfirmationCount(query);

        List<PlanConfirmationResultDTO> resultList = list.stream().map(this::mapToResultDTO).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultList);
        result.put("total", total);
        result.put("page", query.getPage());
        result.put("pages", (total + query.getPageSize() - 1) / query.getPageSize());

        return result;
    }

    @Override
    public Map<String, Object> getPlanConfirmationStats(Long planId) {
        Map<String, Object> stats = planConfirmationsMapper.getPlanConfirmationStats(planId);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("pilot_count", 0);
            stats.put("driver_count", 0);
            stats.put("pilot_boat_count", 0);
            stats.put("tugboat_count", 0);
            stats.put("vehicle_count", 0);
            stats.put("confirmed_count", 0);
            stats.put("total_count", 0);
        }
        return stats;
    }

    @Override
    public Map<String, Object> getPlanFullConfirmationStatus(Long planId) {
        Map<String, Object> result = new HashMap<>();

        PilotagePlans plan = pilotagePlansService.getById(planId);
        if (plan == null) {
            throw new RuntimeException("引航计划不存在");
        }

        result.put("planCode", plan.getPlanCode());
        result.put("planStatus", plan.getPlanStatus());
        result.put("planStartTime", plan.getPlanStartTime());

        Map<String, Object> stats = getPlanConfirmationStats(planId);
        result.putAll(stats);

        long pilotTotal = planPilotAssignmentsService.count(new LambdaQueryWrapper<PlanPilotAssignments>()
                .eq(PlanPilotAssignments::getPlanId, planId)
                .ne(PlanPilotAssignments::getStatus, "CANCELLED"));
        long pilotConfirmed = planPilotAssignmentsService.count(new LambdaQueryWrapper<PlanPilotAssignments>()
                .eq(PlanPilotAssignments::getPlanId, planId)
                .eq(PlanPilotAssignments::getStatus, "CONFIRMED"));

        long driverTotal = planDriverAssignmentsService.count(new LambdaQueryWrapper<PlanDriverAssignments>()
                .eq(PlanDriverAssignments::getPlanId, planId)
                .ne(PlanDriverAssignments::getStatus, "CANCELLED"));
        long driverConfirmed = planDriverAssignmentsService.count(new LambdaQueryWrapper<PlanDriverAssignments>()
                .eq(PlanDriverAssignments::getPlanId, planId)
                .eq(PlanDriverAssignments::getStatus, "CONFIRMED"));

        long tugboatTotal = planTugboatAssignmentsService.count(new LambdaQueryWrapper<PlanTugboatAssignments>()
                .eq(PlanTugboatAssignments::getPlanId, planId)
                .ne(PlanTugboatAssignments::getStatus, "CANCELLED"));
        long tugboatConfirmed = planTugboatAssignmentsService.count(new LambdaQueryWrapper<PlanTugboatAssignments>()
                .eq(PlanTugboatAssignments::getPlanId, planId)
                .eq(PlanTugboatAssignments::getStatus, "CONFIRMED"));

        long pilotBoatTotal = planPilotBoatAssignmentsService.count(new LambdaQueryWrapper<PlanPilotBoatAssignments>()
                .eq(PlanPilotBoatAssignments::getPlanId, planId)
                .ne(PlanPilotBoatAssignments::getStatus, "CANCELLED"));
        long pilotBoatConfirmed = planPilotBoatAssignmentsService.count(new LambdaQueryWrapper<PlanPilotBoatAssignments>()
                .eq(PlanPilotBoatAssignments::getPlanId, planId)
                .eq(PlanPilotBoatAssignments::getStatus, "CONFIRMED"));

        long vehicleTotal = planVehicleAssignmentsService.count(new LambdaQueryWrapper<PlanVehicleAssignments>()
                .eq(PlanVehicleAssignments::getPlanId, planId)
                .ne(PlanVehicleAssignments::getStatus, "CANCELLED"));
        long vehicleConfirmed = planVehicleAssignmentsService.count(new LambdaQueryWrapper<PlanVehicleAssignments>()
                .eq(PlanVehicleAssignments::getPlanId, planId)
                .eq(PlanVehicleAssignments::getStatus, "CONFIRMED"));

        long totalCount = pilotTotal + driverTotal + tugboatTotal + pilotBoatTotal + vehicleTotal;
        long confirmedCount = pilotConfirmed + driverConfirmed + tugboatConfirmed + pilotBoatConfirmed + vehicleConfirmed;

        result.put("pilot_total_count", pilotTotal);
        result.put("pilot_confirmed_count", pilotConfirmed);
        result.put("driver_total_count", driverTotal);
        result.put("driver_confirmed_count", driverConfirmed);
        result.put("tugboat_total_count", tugboatTotal);
        result.put("tugboat_confirmed_count", tugboatConfirmed);
        result.put("pilot_boat_total_count", pilotBoatTotal);
        result.put("pilot_boat_confirmed_count", pilotBoatConfirmed);
        result.put("vehicle_total_count", vehicleTotal);
        result.put("vehicle_confirmed_count", vehicleConfirmed);
        result.put("total_count", totalCount);
        result.put("confirmed_count", confirmedCount);

        List<PlanConfirmations> confirmations = list(new LambdaQueryWrapper<PlanConfirmations>()
                .eq(PlanConfirmations::getPlanId, planId)
                .orderByDesc(PlanConfirmations::getConfirmedAt));

        // 按资源类型分组

        Map<String, List<PlanConfirmations>> groupedConfirmations = confirmations.stream()
                .collect(Collectors.groupingBy(c -> (String) c.getResourceType()));

        result.put("confirmations", groupedConfirmations);

        double progress = 0.0;
        if (totalCount > 0) {
            progress = (confirmedCount * 100.0) / totalCount;
        }
        result.put("confirmationProgress", progress);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelConfirmation(PlanConfirmationDTO dto, String reason) {
        log.info("取消确认: planId={}, resourceType={}, resourceId={}, reason={}", 
                 dto.getPlanId(), dto.getResourceType(), dto.getResourceId(), reason);

        PilotagePlans currentPlan = pilotagePlansService.getById(dto.getPlanId());
        if (currentPlan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }
        String ps = currentPlan.getPlanStatus();
        if (!PlanStatusEnum.PUBLISHED.getCode().equals(ps) && !PlanStatusEnum.CONFIRMED.getCode().equals(ps)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "计划已开始或已结束，不能再取消资源");
        }

        PlanConfirmations confirmation = lambdaQuery()
                .eq(PlanConfirmations::getPlanId, dto.getPlanId())
                .eq(PlanConfirmations::getResourceType, dto.getResourceType())
                .eq(PlanConfirmations::getResourceId, dto.getResourceId())
                .one();

        String resourceType = dto.getResourceType();
        Long resourceId = dto.getResourceId();
        Long planId = dto.getPlanId();
        
        if (confirmation != null) {
            // 如果确认记录存在，根据其状态决定如何取消
            if ("CONFIRMED".equals(confirmation.getConfirmationStatus())) {
                cancelConfirmedRecord(confirmation, reason);
            } else {
                directCancelRecord(confirmation, reason);
            }
        } else {
            updateResourceAssignmentStatusToCancelled(
                    dto.getPlanId(),
                    dto.getResourceType(),
                    dto.getResourceId()
            );
        }
        
        // 无论确认记录是否存在：如果计划已发布/已确认，则该计划已无法继续，需要“待重排”
        // 同时将其他资源按“已取消(自动)”处理，并写入取消原因（与手动点击取消一致的效果）
        PilotagePlans plan = currentPlan;
        if (plan != null && (PlanStatusEnum.PUBLISHED.getCode().equals(plan.getPlanStatus()) || PlanStatusEnum.CONFIRMED.getCode().equals(plan.getPlanStatus()))) {
            Date now = new Date();
            plan.setPlanStatus(PlanStatusEnum.REPLAN_REQUIRED.getCode());
            plan.setReplanRequiredAt(now);
            plan.setReplanRequiredResourceType(resourceType);
            plan.setReplanRequiredResourceId(resourceId);
            String storedReason = (reason != null && !reason.trim().isEmpty()) ? reason.trim() : "资源取消";
            plan.setReplanRequiredReason(storedReason);
            plan.setUpdatedAt(now);
            pilotagePlansService.updateById(plan);
            
            // 自动取消其他资源（与“取消确认”同态），并写入统一原因
            String autoReason = "自动取消：因其他资源取消，计划需重新编排后重新发布";
            autoCancelOtherResources(planId, resourceType, autoReason);

            // 释放该计划当前占用的资源，避免资源长期被锁占导致“不可选择”
            try {
                pilotagePlansService.releasePlanResources(planId);
            } catch (Exception e) {
                log.warn("待重排释放资源失败: planId={}, err={}", planId, e.getMessage());
            }
            
            notifyCancellationOfPlanResources(planId, reason);
        }

        try {
            notifyPilotDriverConfirmOrCancel(planId, resourceType, resourceId, "CANCELLED", reason);
        } catch (Exception e) {
            log.warn("发送取消邮件失败: planId={}, resourceType={}, resourceId={}, err={}",
                    planId, resourceType, resourceId, e.getMessage());
        }
        
        return true;
    }

    /**
     * 自动取消同一计划中的其他资源：
     * - 分配表：status -> CANCELLED
     * - plan_confirmations：confirmation_status -> CANCELLED，notes 填入自动原因
     */
    private void autoCancelOtherResources(Long planId, String excludedResourceType, String autoReason) {
        Date now = new Date();
        Long operatorId = null;
        try { operatorId = SecurityUtil.getCurrentUserId(); } catch (Exception ignored) {}

        // PILOT
        if (!"PILOT".equals(excludedResourceType)) {
            planPilotAssignmentsService.batchUpdateAssignmentStatus(planId, ConfirmationStatusEnum.CANCELLED.getCode());
            List<PlanPilotAssignments> list = planPilotAssignmentsService.list(new LambdaQueryWrapper<PlanPilotAssignments>()
                    .eq(PlanPilotAssignments::getPlanId, planId));
            for (PlanPilotAssignments a : list) {
                upsertAutoCancelledConfirmation(planId, "PILOT", a.getPilotId(), operatorId, now, autoReason);
            }
        }
        // DRIVER
        if (!"DRIVER".equals(excludedResourceType)) {
            planDriverAssignmentsService.batchUpdateAssignmentStatus(planId, ConfirmationStatusEnum.CANCELLED.getCode());
            List<PlanDriverAssignments> list = planDriverAssignmentsService.list(new LambdaQueryWrapper<PlanDriverAssignments>()
                    .eq(PlanDriverAssignments::getPlanId, planId));
            for (PlanDriverAssignments a : list) {
                upsertAutoCancelledConfirmation(planId, "DRIVER", a.getDriverId(), operatorId, now, autoReason);
            }
        }
        // PILOT_BOAT
        if (!"PILOT_BOAT".equals(excludedResourceType)) {
            planPilotBoatAssignmentsService.batchUpdateAssignmentStatus(planId, ConfirmationStatusEnum.CANCELLED.getCode());
            List<PlanPilotBoatAssignments> list = planPilotBoatAssignmentsService.list(new LambdaQueryWrapper<PlanPilotBoatAssignments>()
                    .eq(PlanPilotBoatAssignments::getPlanId, planId));
            for (PlanPilotBoatAssignments a : list) {
                upsertAutoCancelledConfirmation(planId, "PILOT_BOAT", a.getPilotBoatId(), operatorId, now, autoReason);
            }
        }
        // TUGBOAT
        if (!"TUGBOAT".equals(excludedResourceType)) {
            planTugboatAssignmentsService.batchUpdateAssignmentStatus(planId, ConfirmationStatusEnum.CANCELLED.getCode());
            List<PlanTugboatAssignments> list = planTugboatAssignmentsService.list(new LambdaQueryWrapper<PlanTugboatAssignments>()
                    .eq(PlanTugboatAssignments::getPlanId, planId));
            for (PlanTugboatAssignments a : list) {
                upsertAutoCancelledConfirmation(planId, "TUGBOAT", a.getTugboatId(), operatorId, now, autoReason);
            }
        }
        // VEHICLE
        if (!"VEHICLE".equals(excludedResourceType)) {
            planVehicleAssignmentsService.batchUpdateAssignmentStatus(planId, ConfirmationStatusEnum.CANCELLED.getCode());
            List<PlanVehicleAssignments> list = planVehicleAssignmentsService.list(new LambdaQueryWrapper<PlanVehicleAssignments>()
                    .eq(PlanVehicleAssignments::getPlanId, planId));
            for (PlanVehicleAssignments a : list) {
                upsertAutoCancelledConfirmation(planId, "VEHICLE", a.getVehicleId(), operatorId, now, autoReason);
            }
        }
    }

    private void upsertAutoCancelledConfirmation(Long planId, String resourceType, Long resourceId, Long operatorId, Date now, String reason) {
        if (planId == null || resourceId == null) return;
        PlanConfirmations existing = this.lambdaQuery()
                .eq(PlanConfirmations::getPlanId, planId)
                .eq(PlanConfirmations::getResourceType, resourceType)
                .eq(PlanConfirmations::getResourceId, resourceId)
                .one();

        String notes = reason;
        if (existing != null && existing.getNotes() != null && !existing.getNotes().trim().isEmpty()) {
            notes = existing.getNotes() + "；" + reason;
        }

        if (existing == null) {
            PlanConfirmations pc = new PlanConfirmations();
            pc.setPlanId(planId);
            pc.setResourceType(resourceType);
            pc.setResourceId(resourceId);
            pc.setConfirmMethod("MANUAL");
            pc.setConfirmedBy(operatorId);
            pc.setConfirmedAt(now);
            pc.setConfirmationStatus(ConfirmationStatusEnum.CANCELLED.getCode());
            pc.setNotes(notes);
            save(pc);
        } else {
            existing.setConfirmMethod("MANUAL");
            existing.setConfirmedBy(operatorId);
            existing.setConfirmedAt(now);
            existing.setConfirmationStatus(ConfirmationStatusEnum.CANCELLED.getCode());
            existing.setNotes(notes);
            updateById(existing);
        }
    }

    private void notifyPilotDriverConfirmOrCancel(Long planId, String resourceType, Long resourceId, String action, String reason) {
        if (planId == null || resourceType == null || resourceId == null) return;
        if (!"PILOT".equals(resourceType) && !"DRIVER".equals(resourceType)) return;

        PilotagePlans plan = pilotagePlansService.getById(planId);
        if (plan == null) return;
        Vessels vessel = plan.getVesselId() != null ? vesselsService.getById(plan.getVesselId()) : null;

        String email = null;
        String name = null;
        String role = "PILOT".equals(resourceType) ? "引航员" : "驾驶员";

        if ("PILOT".equals(resourceType)) {
            Pilots pilot = pilotsService.getById(resourceId);
            if (pilot != null) {
                email = pilot.getEmail();
                name = pilot.getPilotName();
            }
        } else {
            Drivers driver = driversService.getById(resourceId);
            if (driver != null) {
                email = driver.getEmail();
                name = driver.getName();
            }
        }

        if (email == null || email.trim().isEmpty()) return;

        String subject;
        String content;
        if ("CONFIRMED".equals(action)) {
            subject = "引航任务确认成功 - 计划编号: " + plan.getPlanCode();
            content = buildConfirmationEmailContent(plan, vessel, role, name);
        } else {
            subject = "引航任务取消通知 - 计划编号: " + plan.getPlanCode();
            content = buildCancellationEmailContent(plan, vessel, role, name, reason != null ? reason : "取消");
        }
        messageService.sendEmail(email, subject, content, true);
    }

    private String buildConfirmationEmailContent(PilotagePlans plan, Vessels vessel, String role, String name) {
        String vesselName = vessel != null ? (vessel.getNameZh() != null ? vessel.getNameZh() : vessel.getNameEn()) : "-";
        String start = plan.getPlanStartTime() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(plan.getPlanStartTime()) : "-";
        String end = plan.getPlanEndTime() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(plan.getPlanEndTime()) : "-";
        return "<div style='font-family:Arial,sans-serif;font-size:14px;line-height:1.6'>" +
                "<p>您好，" + role + "【" + (name != null ? name : "") + "】：</p>" +
                "<p>您已成功确认引航任务。</p>" +
                "<p><b>计划编号：</b>" + plan.getPlanCode() + "</p>" +
                "<p><b>船舶：</b>" + vesselName + "</p>" +
                "<p><b>计划时间：</b>" + start + " - " + end + "</p>" +
                "<p style='color:#666'>此邮件由系统自动发送，请勿回复。</p>" +
                "</div>";
    }

        @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchCancel(List<PlanConfirmationDTO> dtos, String reason) {
        log.info("批量取消确认: 数量={}, reason={}", dtos.size(), reason);
        
        for (PlanConfirmationDTO dto : dtos) {
            cancelConfirmation(dto, reason);
        }
        
        return true;
    }

        @Transactional(rollbackFor = Exception.class)
    public boolean cancelConfirmedRecord(PlanConfirmations confirmation, String reason) {
        log.info("取消已确认记录: confirmationId={}, reason={}", confirmation.getId(), reason);

        Long planId = confirmation.getPlanId();
        String resourceType = String.valueOf(confirmation.getResourceType());
        Long resourceId = confirmation.getResourceId();

        confirmation.setConfirmationStatus(ConfirmationStatusEnum.CANCELLED.getCode());
        confirmation.setNotes(confirmation.getNotes() + " [取消原因: " + reason + "]");

        boolean updated = updateById(confirmation);

        if (updated) {
            updateResourceAssignmentStatusToCancelled(
                    planId,
                    resourceType,
                    resourceId
            );

            checkAndUpdatePlanConfirmationStatus(planId);
        }

        return updated;
    }

        @Transactional(rollbackFor = Exception.class)
    public boolean directCancelRecord(PlanConfirmations confirmation, String reason) {
        log.info("直接取消记录: confirmationId={}, reason={}", confirmation.getId(), reason);

        Long planId = confirmation.getPlanId();
        String resourceType = String.valueOf(confirmation.getResourceType());
        Long resourceId = confirmation.getResourceId();

        confirmation.setConfirmationStatus(ConfirmationStatusEnum.CANCELLED.getCode());
        confirmation.setNotes(confirmation.getNotes() + " [取消原因: " + reason + "]");

        boolean updated = updateById(confirmation);

        if (updated) {
            updateResourceAssignmentStatusToCancelled(
                    planId,
                    resourceType,
                    resourceId
            );

            checkAndUpdatePlanConfirmationStatus(planId);
        }

        return updated;
    }

        private void updateResourceAssignmentStatus(Long planId, String resourceType, Long resourceId) {
        switch (resourceType) {
            case "PILOT":
                planPilotAssignmentsService.updateAssignmentStatus(planId, resourceId, "CONFIRMED");
                break;
            case "DRIVER":
                planDriverAssignmentsService.updateAssignmentStatus(planId, resourceId, "CONFIRMED");
                break;
            case "PILOT_BOAT":
                planPilotBoatAssignmentsService.updateAssignmentStatus(planId, resourceId, "CONFIRMED");
                break;
            case "TUGBOAT":
                planTugboatAssignmentsService.updateAssignmentStatus(planId, resourceId, "CONFIRMED");
                break;
            case "VEHICLE":
                planVehicleAssignmentsService.updateAssignmentStatus(planId, resourceId, "CONFIRMED");
                break;
            default:
                log.warn("未知的资源类型: {}", resourceType);
        }
    }

        private void updateResourceAssignmentStatusToCancelled(Long planId, String resourceType, Long resourceId) {
        switch (resourceType) {
            case "PILOT":
                planPilotAssignmentsService.updateAssignmentStatus(planId, resourceId, "CANCELLED");
                break;
            case "DRIVER":
                planDriverAssignmentsService.updateAssignmentStatus(planId, resourceId, "CANCELLED");
                break;
            case "PILOT_BOAT":
                planPilotBoatAssignmentsService.updateAssignmentStatus(planId, resourceId, "CANCELLED");
                break;
            case "TUGBOAT":
                planTugboatAssignmentsService.updateAssignmentStatus(planId, resourceId, "CANCELLED");
                break;
            case "VEHICLE":
                planVehicleAssignmentsService.updateAssignmentStatus(planId, resourceId, "CANCELLED");
                break;
            default:
                break;
        }
    }

        private void checkAndUpdatePlanConfirmationStatus(Long planId) {
        Map<String, Object> stats = getPlanConfirmationStats(planId);
        // 使用 Number 类型来处理可能的不同数字类型
        Number totalCountObj = (Number) stats.get("total_count");
        Number confirmedCountObj = (Number) stats.get("confirmed_count");

        if (totalCountObj != null && totalCountObj.longValue() > 0
                && totalCountObj.longValue() == confirmedCountObj.longValue()) {
            PilotagePlans plan = pilotagePlansService.getById(planId);
            if (plan != null && !"COMPLETED".equals(plan.getPlanStatus())) {
                plan.setPlanStatus("CONFIRMED");
                pilotagePlansService.updateById(plan);
                log.info("计划所有资源确认完成: planId={}", planId);
            }
        }
    }
    
    // 旧逻辑 updateOtherResourcesToSystemCancellation 已由 autoCancelOtherResources 替代（同态“取消”并写入原因）
    
        private void notifyCancellationOfPlanResources(Long planId, String reason) {
        try {
            PilotagePlans plan = pilotagePlansService.getById(planId);
            if (plan == null) {
                log.warn("计划不存在: planId={}", planId);
                return;
            }
            
            List<PlanPilotAssignments> pilotAssignments = planPilotAssignmentsService.list(
                new LambdaQueryWrapper<PlanPilotAssignments>()
                    .eq(PlanPilotAssignments::getPlanId, planId)
            );
            
            List<PlanDriverAssignments> driverAssignments = planDriverAssignmentsService.list(
                new LambdaQueryWrapper<PlanDriverAssignments>()
                    .eq(PlanDriverAssignments::getPlanId, planId)
            );
            
            Vessels vessel = null;
            if (plan.getVesselId() != null) {
                vessel = vesselsService.getById(plan.getVesselId());
            }
            
            for (PlanPilotAssignments assignment : pilotAssignments) {
                Pilots pilot = pilotsService.getById(assignment.getPilotId());
                if (pilot != null && pilot.getEmail() != null && !pilot.getEmail().trim().isEmpty()) {
                    String subject = "引航计划取消通知 - 计划编号: " + plan.getPlanCode();
                    String content = buildCancellationEmailContent(plan, vessel, "引航员", pilot.getPilotName(), reason);
                    messageService.sendEmail(pilot.getEmail(), subject, content, true);
                    log.info("已发送引航计划取消通知邮件给引航员: {} ({})", pilot.getPilotName(), pilot.getEmail());
                }
            }
            
            for (PlanDriverAssignments assignment : driverAssignments) {
                Drivers driver = driversService.getById(assignment.getDriverId());
                if (driver != null && driver.getEmail() != null && !driver.getEmail().trim().isEmpty()) {
                    String subject = "引航计划取消通知 - 计划编号: " + plan.getPlanCode();
                    String content = buildCancellationEmailContent(plan, vessel, "驾驶员", driver.getName(), reason);
                    messageService.sendEmail(driver.getEmail(), subject, content, true);
                    log.info("已发送引航计划取消通知邮件给驾驶员: {} ({})", driver.getName(), driver.getEmail());
                }
            }
            
        } catch (Exception e) {
            log.error("发送计划取消通知邮件失败: {}", e.getMessage(), e);
        }
    }
    
        private String buildCancellationEmailContent(PilotagePlans plan, Vessels vessel, String role, String roleName, String reason) {
        StringBuilder content = new StringBuilder();
        content.append("<h2>引航计划取消通知</h2>");
        content.append("<p>尊敬的" + role + " <strong>" + roleName + "</strong>：</p>");
        content.append("<p>很抱歉通知您，以下引航计划已被取消：</p>");
        
        content.append("<div style='border: 1px solid #ccc; padding: 15px; margin: 10px 0; border-radius: 5px;'>");
        content.append("<h3>计划信息</h3>");
        content.append("<table style='width: 100%; border-collapse: collapse;'>");
        content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>计划编号</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>").append(plan.getPlanCode()).append("</td></tr>");
        if (vessel != null) {
            content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>船舶名称</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>")
                   .append(vessel.getNameZh() != null ? vessel.getNameZh() : vessel.getNameEn()).append("</td></tr>");
        }
        content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>计划开始时间</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>").append(org.apache.commons.lang3.time.DateFormatUtils.format(plan.getPlanStartTime(), "yyyy-MM-dd HH:mm:ss")).append("</td></tr>");
        content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>计划结束时间</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>").append(org.apache.commons.lang3.time.DateFormatUtils.format(plan.getPlanEndTime(), "yyyy-MM-dd HH:mm:ss")).append("</td></tr>");
        content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>起始地点</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>").append(plan.getStartLocationName()).append("</td></tr>");
        content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>终点地点</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>").append(plan.getEndLocationName()).append("</td></tr>");
        content.append("<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>取消原因</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>").append(reason).append("</td></tr>");
        content.append("</table>");
        content.append("</div>");
        
        content.append("<p>请您重新安排您的工作计划，如有任何疑问，请及时联系相关部门。</p>");
        content.append("<p>谢谢！</p>");
        content.append("<p><em>港口引航与计费系统</em></p>");
        
        return content.toString();
    }

    
    private PlanConfirmationResultDTO mapToResultDTO(Map<String, Object> map) {
        PlanConfirmationResultDTO dto = new PlanConfirmationResultDTO();
        dto.setId((Long) map.get("id"));
        dto.setPlanId((Long) map.get("plan_id"));
        dto.setPlanCode((String) map.get("plan_code"));
        dto.setResourceType((String) map.get("resource_type"));
        dto.setResourceId((Long) map.get("resource_id"));
        dto.setResourceName((String) map.get("resource_name"));
        dto.setConfirmMethod((String) map.get("confirm_method"));
        dto.setConfirmedBy((Long) map.get("confirmed_by"));
        dto.setConfirmedByName((String) map.get("confirmed_by_name"));
        dto.setConfirmedAt((Date) map.get("confirmed_at"));
        dto.setConfirmationStatus((String) map.get("confirmation_status"));
        dto.setNotes((String) map.get("notes"));
        dto.setCreatedAt((Date) map.get("created_at"));

        dto.setResourceTypeName(syy.portPilotage.enums.ResourceTypeEnum.getNameByCode(dto.getResourceType()));
        dto.setConfirmMethodName(syy.portPilotage.enums.ConfirmMethodEnum.getNameByCode(dto.getConfirmMethod()));
        dto.setConfirmationStatusName(ConfirmationStatusEnum.getNameByCode(dto.getConfirmationStatus()));

        return dto;
    }

    @Override
    public PageResult<ResourceConfirmationResultDTO> getMyTasks(Long userId, String status, Integer page, Integer pageSize) {
        log.info("获取我的任务列表: userId={}, status={}, page={}, pageSize={}", userId, status, page, pageSize);

        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        int offset = (page - 1) * pageSize;

        Pilots pilot = pilotsService.lambdaQuery()
                .eq(Pilots::getUserId, userId)
                .one();
        
        Drivers driver = driversService.lambdaQuery()
                .eq(Drivers::getUserId, userId)
                .one();

        List<Map<String, Object>> taskList = new ArrayList<>();
        Long total = 0L;

        if (pilot != null) {
            taskList = planConfirmationsMapper.selectPilotTasks(pilot.getId(), status, offset, pageSize);
            total = planConfirmationsMapper.selectPilotTasksCount(pilot.getId(), status);
        } else if (driver != null) {
            taskList = planConfirmationsMapper.selectDriverTasks(driver.getId(), status, offset, pageSize);
            total = planConfirmationsMapper.selectDriverTasksCount(driver.getId(), status);
        }

        List<ResourceConfirmationResultDTO> resultList = taskList.stream()
                .map(this::mapToResourceConfirmationResultDTO)
                .collect(Collectors.toList());

        log.info("查询完成: 找到{}条记录, 总计{}条", resultList.size(), total);

        return PageResult.of(total, page, pageSize, resultList);
    }

    @Override
    public ResourceConfirmationResultDTO getTaskDetail(Long userId, Long assignmentId) {
        log.info("获取任务详情: userId={}, assignmentId={}", userId, assignmentId);

        Pilots pilot = pilotsService.lambdaQuery()
                .eq(Pilots::getUserId, userId)
                .one();
        
        Drivers driver = driversService.lambdaQuery()
                .eq(Drivers::getUserId, userId)
                .one();

        ResourceConfirmationResultDTO taskDetail = null;

        if (pilot != null) {
            Map<String, Object> task = planConfirmationsMapper
                    .selectPilotTaskById(pilot.getId(), assignmentId);
            if (task != null) {
                taskDetail = mapToResourceConfirmationResultDTO(task);
            }
        } else if (driver != null) {
            Map<String, Object> task = planConfirmationsMapper
                    .selectDriverTaskById(driver.getId(), assignmentId);
            if (task != null) {
                taskDetail = mapToResourceConfirmationResultDTO(task);
            }
        }

        if (taskDetail == null) {
            throw new RuntimeException("未找到对应的任务或无权限查看");
        }

        return taskDetail;
    }
}