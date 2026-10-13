package syy.portPilotage.controller.pilot;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.entity.PilotagePlans;
import syy.portPilotage.entity.PlanDriverAssignments;
import syy.portPilotage.entity.PlanPilotAssignments;
import syy.portPilotage.entity.Drivers;
import syy.portPilotage.entity.Pilots;
import syy.portPilotage.entity.Users;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.PilotagePlansService;
import syy.portPilotage.service.PlanConfirmationsService;
import syy.portPilotage.service.PlanDriverAssignmentsService;
import syy.portPilotage.service.PlanPilotAssignmentsService;
import syy.portPilotage.service.DriversService;
import syy.portPilotage.service.PilotsService;
import syy.portPilotage.service.UsersService;
import syy.portPilotage.service.screen.DashboardService;
import syy.portPilotage.util.SecurityUtil;
import syy.portPilotage.vo.pilotapp.AppProfileVO;
import syy.portPilotage.vo.pilotapp.PilotDashboardVO;
import syy.portPilotage.vo.pilotapp.PilotMyPlanItemVO;
import syy.portPilotage.vo.pilotage.PilotageStatVO;
import syy.portPilotage.dto.plan.PlanConfirmationDTO;
import syy.portPilotage.entity.PlanConfirmations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/pilot/app")
@RequiredArgsConstructor
@Tag(name = "引航员App", description = "引航员端移动App接口（uni-app）")
public class PilotAppController {

    private final UsersService usersService;
    private final PilotsService pilotsService;
    private final DriversService driversService;
    private final PilotagePlansService pilotagePlansService;
    private final PlanPilotAssignmentsService planPilotAssignmentsService;
    private final PlanDriverAssignmentsService planDriverAssignmentsService;
    private final PlanConfirmationsService planConfirmationsService;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "引航员App-首页看板")
    public Result<PilotDashboardVO> dashboard() {
        AppResource res = resolveAppResourceForCurrentUser();
        List<Long> planIdsByAssignment = resolvePlanIdsByAssignments(res);

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.atTime(LocalTime.MAX);

        Date start = Date.from(dayStart.atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(dayEnd.atZone(ZoneId.systemDefault()).toInstant());

        long todayTotal = pilotagePlansService.count(
                new LambdaQueryWrapper<PilotagePlans>()
                        .ge(PilotagePlans::getPlanStartTime, start)
                        .le(PilotagePlans::getPlanStartTime, end)
        );

        long myToday = pilotagePlansService.count(
                buildMyPlansWrapper(res, planIdsByAssignment)
                        .ge(PilotagePlans::getPlanStartTime, start)
                        .le(PilotagePlans::getPlanStartTime, end)
        );

        Date now = new Date();
        Date next24h = Date.from(LocalDateTime.now().plusHours(24).atZone(ZoneId.systemDefault()).toInstant());
        long myUpcoming = pilotagePlansService.count(
                buildMyPlansWrapper(res, planIdsByAssignment)
                        .ge(PilotagePlans::getPlanStartTime, now)
                        .le(PilotagePlans::getPlanStartTime, next24h)
        );

        long myTotal = pilotagePlansService.count(
                buildMyPlansWrapper(res, planIdsByAssignment)
                        .ne(PilotagePlans::getPlanStatus, syy.portPilotage.enums.PlanStatusEnum.CANCELLED.getCode())
        );

        PilotageStatVO pilotageStat = dashboardService.getPilotageStat().getData();

        PilotDashboardVO vo = new PilotDashboardVO();
        vo.setTodayPlansTotal(todayTotal);
        vo.setMyTodayPlans(myToday);
        vo.setMyUpcoming24hPlans(myUpcoming);
        vo.setMyTotalPlans(myTotal);
        vo.setPilotageTrends7d(pilotageStat != null ? pilotageStat.getTrends() : null);
        return Result.success(vo);
    }

        private LambdaQueryWrapper<PilotagePlans> buildMyPlansWrapper(AppResource res, List<Long> planIdsByAssignment) {
        LambdaQueryWrapper<PilotagePlans> wrapper = new LambdaQueryWrapper<>();
        if (res == null || res.resourceId == null || res.resourceType == null) return wrapper.eq(PilotagePlans::getId, -1L);

        if ("PILOT".equals(res.resourceType)) {
            if (planIdsByAssignment != null && !planIdsByAssignment.isEmpty()) {
                wrapper.and(w -> w.eq(PilotagePlans::getAssignedPilotId, res.resourceId)
                        .or()
                        .in(PilotagePlans::getId, planIdsByAssignment));
            } else {
                wrapper.eq(PilotagePlans::getAssignedPilotId, res.resourceId);
            }
        } else if ("DRIVER".equals(res.resourceType)) {
            if (planIdsByAssignment != null && !planIdsByAssignment.isEmpty()) {
                wrapper.and(w -> w.eq(PilotagePlans::getAssignedDriverId, res.resourceId)
                        .or()
                        .in(PilotagePlans::getId, planIdsByAssignment));
            } else {
                wrapper.eq(PilotagePlans::getAssignedDriverId, res.resourceId);
            }
        } else {
            wrapper.eq(PilotagePlans::getId, -1L);
        }
        return wrapper;
    }

    @GetMapping("/myPlans")
    @Operation(summary = "引航员App-我的计划列表（按当前登录用户映射到pilotId）")
    public Result<IPage<PilotMyPlanItemVO>> myPlans(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String planStatus
    ) {
        AppResource res = resolveAppResourceForCurrentUser();
        List<Long> planIdsByAssignment = resolvePlanIdsByAssignments(res);
        Page<PilotagePlans> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<PilotagePlans> wrapper = buildMyPlansWrapper(res, planIdsByAssignment)
                .orderByAsc(PilotagePlans::getPlanStartTime);

        Date start = parseDateStart(startDate);
        Date end = parseDateEnd(endDate);
        if (start != null) wrapper.ge(PilotagePlans::getPlanStartTime, start);
        if (end != null) wrapper.le(PilotagePlans::getPlanStartTime, end);
        if (planStatus != null && !planStatus.trim().isEmpty()) wrapper.eq(PilotagePlans::getPlanStatus, planStatus.trim());

        IPage<PilotagePlans> result = pilotagePlansService.page(mpPage, wrapper);

        Map<Long, String> confirmStatusByPlanId = new HashMap<>();
        List<PilotagePlans> planRecords = result.getRecords();
        if (planRecords != null && !planRecords.isEmpty()) {
            List<Long> planIds = planRecords.stream()
                    .map(PilotagePlans::getId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!planIds.isEmpty()) {
                List<PlanConfirmations> confirmations = planConfirmationsService.list(
                        new LambdaQueryWrapper<PlanConfirmations>()
                                .in(PlanConfirmations::getPlanId, planIds)
                                .eq(PlanConfirmations::getResourceType, res.resourceType)
                                .eq(PlanConfirmations::getResourceId, res.resourceId)
                );
                if (confirmations != null) {
                    for (PlanConfirmations pc : confirmations) {
                        if (pc != null && pc.getPlanId() != null) {
                            confirmStatusByPlanId.put(pc.getPlanId(), String.valueOf(pc.getConfirmationStatus()));
                        }
                    }
                }
            }
        }

        List<PilotMyPlanItemVO> records = result.getRecords().stream()
                .filter(Objects::nonNull)
                .map(p -> toMyPlanItem(p, confirmStatusByPlanId))
                .collect(Collectors.toList());

        Page<PilotMyPlanItemVO> pageVo = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        pageVo.setRecords(records);
        return Result.success(pageVo);
    }

        private List<Long> resolvePlanIdsByAssignments(AppResource res) {
        if (res == null || res.resourceId == null || res.resourceType == null) return List.of();
        if ("PILOT".equals(res.resourceType)) {
            List<PlanPilotAssignments> assigns = planPilotAssignmentsService.list(
                    new LambdaQueryWrapper<PlanPilotAssignments>()
                            .eq(PlanPilotAssignments::getPilotId, res.resourceId)
                            .notIn(PlanPilotAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
            );
            if (assigns == null || assigns.isEmpty()) return List.of();
            return assigns.stream()
                    .map(PlanPilotAssignments::getPlanId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
        if ("DRIVER".equals(res.resourceType)) {
            List<PlanDriverAssignments> assigns = planDriverAssignmentsService.list(
                    new LambdaQueryWrapper<PlanDriverAssignments>()
                            .eq(PlanDriverAssignments::getDriverId, res.resourceId)
                            .notIn(PlanDriverAssignments::getStatus, "CANCELLED", "SYSTEM_CANCELLATION")
            );
            if (assigns == null || assigns.isEmpty()) return List.of();
            return assigns.stream()
                    .map(PlanDriverAssignments::getPlanId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private PilotMyPlanItemVO toMyPlanItem(PilotagePlans plan, Map<Long, String> confirmStatusByPlanId) {
        PilotMyPlanItemVO vo = new PilotMyPlanItemVO();
        vo.setId(plan.getId());
        vo.setPlanCode(plan.getPlanCode());
        vo.setPlanStartTime(plan.getPlanStartTime());
        vo.setPlanEndTime(plan.getPlanEndTime());
        vo.setStartLocationName(plan.getStartLocationName());
        vo.setEndLocationName(plan.getEndLocationName());
        vo.setPlanStatus(plan.getPlanStatus());
        String cs = confirmStatusByPlanId != null ? confirmStatusByPlanId.get(plan.getId()) : null;
        if (cs == null || cs.trim().isEmpty()) cs = "PENDING";
        // 兼容旧字段：移动端沿用 pilotConfirmationStatus 字段展示“当前资源的确认状态”
        vo.setPilotConfirmationStatus(cs);
        return vo;
    }

    @GetMapping({"/profile", "/meProfile"})
    @Operation(summary = "App-我的信息（引航员/驾驶员通用，含在岗状态）")
    public Result<AppProfileVO> profile() {
        AppResource res = resolveAppResourceForCurrentUser();
        AppProfileVO vo = new AppProfileVO();
        vo.setResourceType(res.resourceType);
        vo.setResourceId(res.resourceId);
        vo.setResourceCode(res.resourceCode);
        vo.setResourceName(res.resourceName);
        vo.setOnDutyStatus(res.onDutyStatus);
        return Result.success(vo);
    }

    @GetMapping("/plans/{planId}/confirmStatus")
    @Operation(summary = "App-获取我对该计划的确认状态（引航员/驾驶员通用）")
    public Result<String> myPlanConfirmStatus(@PathVariable Long planId) {
        AppResource res = resolveAppResourceForCurrentUser();
        PlanConfirmations pc = planConfirmationsService.lambdaQuery()
                .eq(PlanConfirmations::getPlanId, planId)
                .eq(PlanConfirmations::getResourceType, res.resourceType)
                .eq(PlanConfirmations::getResourceId, res.resourceId)
                .one();
        if (pc == null || pc.getConfirmationStatus() == null) return Result.success("PENDING");
        return Result.success(String.valueOf(pc.getConfirmationStatus()));
    }

    @PostMapping("/plans/{planId}/confirm")
    @Operation(summary = "App-确认计划（确认我的资源：引航员/驾驶员）")
    public Result<Boolean> confirmPlan(@PathVariable Long planId) {
        AppResource res = resolveAppResourceForCurrentUser();
        PlanConfirmationDTO dto = new PlanConfirmationDTO();
        dto.setPlanId(planId);
        dto.setResourceType(res.resourceType);
        dto.setResourceId(res.resourceId);
        dto.setConfirmMethod("MANUAL"); // DB enum 仅允许 WEB/SMS/PHONE/MANUAL
        dto.setNotes("App确认");
        dto.setConfirmedAt(new Date());
        boolean ok = planConfirmationsService.createConfirmation(dto);
        return ok ? Result.success(true) : Result.error("确认失败");
    }

    @PutMapping("/plans/{planId}/cancelConfirm")
    @Operation(summary = "App-取消计划确认（取消我的资源确认：引航员/驾驶员）")
    public Result<Boolean> cancelPlanConfirm(
            @PathVariable Long planId,
            @RequestParam(required = false, defaultValue = "App取消确认") String reason
    ) {
        AppResource res = resolveAppResourceForCurrentUser();
        PlanConfirmationDTO dto = new PlanConfirmationDTO();
        dto.setPlanId(planId);
        dto.setResourceType(res.resourceType);
        dto.setResourceId(res.resourceId);
        dto.setConfirmMethod("MANUAL");
        dto.setNotes("App取消确认");
        dto.setConfirmedAt(new Date());
        boolean ok = planConfirmationsService.batchCancel(List.of(dto), reason);
        return ok ? Result.success(true) : Result.error("取消失败");
    }

    /**
     * App 端关键：把当前登录用户映射到 pilots.id / drivers.id
     * 优先按 pilots.user_id / drivers.user_id 绑定关系。
     */
    private AppResource resolveAppResourceForCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Users user = usersService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        String prefer = SecurityUtil.getCurrentUserType();
        prefer = prefer != null ? prefer.trim().toUpperCase() : "";

        if ("DRIVER".equals(prefer)) {
            Drivers driverByUserId = driversService.lambdaQuery().eq(Drivers::getUserId, userId).one();
            if (driverByUserId != null && driverByUserId.getId() != null) {
                return AppResource.fromDriver(driverByUserId);
            }
            Pilots pilotByUserId = pilotsService.lambdaQuery().eq(Pilots::getUserId, userId).one();
            if (pilotByUserId != null && pilotByUserId.getId() != null) {
                return AppResource.fromPilot(pilotByUserId);
            }
        } else {
            Pilots pilotByUserId = pilotsService.lambdaQuery().eq(Pilots::getUserId, userId).one();
            if (pilotByUserId != null && pilotByUserId.getId() != null) {
                return AppResource.fromPilot(pilotByUserId);
            }
            Drivers driverByUserId = driversService.lambdaQuery().eq(Drivers::getUserId, userId).one();
            if (driverByUserId != null && driverByUserId.getId() != null) {
                return AppResource.fromDriver(driverByUserId);
            }
        }

        // - 如果用户类型是 DRIVER，则优先尝试 drivers 表
        // - 如果用户类型是 PILOT，则优先尝试 pilots 表
        Drivers driver = null;
        // 当 user_type=DRIVER 时优先查 driver；即便 user_type 不准确，也在 pilot 尝试失败后兜底查 driver
        if ("DRIVER".equals(prefer)) {
            driver = tryResolveDriverByUserFields(user);
            if (driver != null && driver.getId() != null) return AppResource.fromDriver(driver);
        }

        Pilots pilot = null;
        if (user.getUserCode() != null && !user.getUserCode().trim().isEmpty()) {
            pilot = pilotsService.getOne(new LambdaQueryWrapper<Pilots>().eq(Pilots::getPilotCode, user.getUserCode().trim()), false);
        }
        if (pilot == null && user.getAccount() != null && !user.getAccount().trim().isEmpty()) {
            pilot = pilotsService.getOne(new LambdaQueryWrapper<Pilots>().eq(Pilots::getPilotCode, user.getAccount().trim()), false);
        }
        if (pilot != null && pilot.getId() != null) {
            return AppResource.fromPilot(pilot);
        }

        // pilot 未找到时兜底查 driver（解决“user_type 配错导致驾驶员被误判”）
        driver = tryResolveDriverByUserFields(user);
        if (driver != null && driver.getId() != null) {
            return AppResource.fromDriver(driver);
        }

        log.warn("无法将当前用户映射为引航员/驾驶员: userId={}, account={}, userCode={}, username={}",
                user.getId(), user.getAccount(), user.getUserCode(), user.getUsername());
        throw new BusinessException(ErrorCode.NOT_FOUND, "当前账号未绑定引航员/驾驶员档案（请在后台完成绑定）");
    }

    private Drivers tryResolveDriverByUserFields(Users user) {
        Drivers driver = null;
        if (user.getUserCode() != null && !user.getUserCode().trim().isEmpty()) {
            driver = driversService.getOne(new LambdaQueryWrapper<Drivers>().eq(Drivers::getDriverCode, user.getUserCode().trim()), false);
        }
        if (driver == null && user.getAccount() != null && !user.getAccount().trim().isEmpty()) {
            driver = driversService.getOne(new LambdaQueryWrapper<Drivers>().eq(Drivers::getDriverCode, user.getAccount().trim()), false);
        }
        if (driver == null && user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            // drivers 里 phone/mobile 两个字段，优先 phone，其次 mobile
            driver = driversService.getOne(new LambdaQueryWrapper<Drivers>().eq(Drivers::getPhone, user.getPhone().trim()), false);
            if (driver == null) {
                driver = driversService.getOne(new LambdaQueryWrapper<Drivers>().eq(Drivers::getMobile, user.getPhone().trim()), false);
            }
        }
        if (driver == null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            driver = driversService.getOne(new LambdaQueryWrapper<Drivers>().eq(Drivers::getEmail, user.getEmail().trim()), false);
        }
        if (driver == null && user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            driver = driversService.getOne(new LambdaQueryWrapper<Drivers>().eq(Drivers::getName, user.getUsername().trim()), false);
        }
        return driver;
    }

    private static class AppResource {
        private String resourceType;
        private Long resourceId;
        private String resourceCode;
        private String resourceName;
        private String onDutyStatus;

        static AppResource fromPilot(Pilots p) {
            AppResource r = new AppResource();
            r.resourceType = "PILOT";
            r.resourceId = p.getId();
            r.resourceCode = p.getPilotCode();
            r.resourceName = p.getPilotName();
            r.onDutyStatus = p.getOnDutyStatus() != null ? String.valueOf(p.getOnDutyStatus()) : null;
            return r;
        }

        static AppResource fromDriver(Drivers d) {
            AppResource r = new AppResource();
            r.resourceType = "DRIVER";
            r.resourceId = d.getId();
            r.resourceCode = d.getDriverCode();
            r.resourceName = d.getName();
            r.onDutyStatus = d.getOnDutyStatus() != null ? String.valueOf(d.getOnDutyStatus()) : null;
            return r;
        }
    }

    private Date parseDateStart(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            LocalDate date = LocalDate.parse(dateStr.trim());
            return Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Date parseDateEnd(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            LocalDate date = LocalDate.parse(dateStr.trim());
            return Date.from(date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception ignored) {
            return null;
        }
    }
}
