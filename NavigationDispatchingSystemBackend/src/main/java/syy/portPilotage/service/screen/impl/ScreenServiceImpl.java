package syy.portPilotage.service.screen.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import oshi.hardware.NetworkIF;
import syy.portPilotage.common.Result;
import syy.portPilotage.entity.*;
import syy.portPilotage.service.*;
import syy.portPilotage.service.screen.ScreenService;
import syy.portPilotage.util.GeoCoordinateUtil;
import syy.portPilotage.vo.screen.*;
import syy.portPilotage.redis.RedisService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

@Service
public class ScreenServiceImpl implements ScreenService {

    @Autowired
    private VesselsService vesselsService;
    @Autowired
    private VoyagesService voyagesService;
    @Autowired
    private BerthsService berthsService;
    @Autowired
    private PilotagePlansService pilotagePlansService;
    @Autowired
    private WeatherInfoService weatherInfoService;
    @Autowired
    private TideInfoService tideInfoService;
    @Autowired
    private BillingVouchersService billingVouchersService;
    @Autowired
    private AnchoragesService anchoragesService;
    @Autowired
    private PilotsService pilotsService;
    @Autowired
    private DriversService driversService;
    @Autowired
    private VehiclesService vehiclesService;
    @Autowired
    private TugboatsService tugboatsService;
    @Autowired
    private PilotBoatsService pilotBoatsService;
    @Autowired
    private AgentInvoiceInfoService agentInvoiceInfoService;
    @Autowired
    private AgentsService agentsService;
    @Autowired
    private RedisService redisService;

    // 需要创建一个缓存来存储上一次的网络统计信息
    private static final Map<String, Long> lastRxBytes = new HashMap<>();
    private static final Map<String, Long> lastTxBytes = new HashMap<>();
    private static final Map<String, Long> lastTimestamp = new HashMap<>();
    
    // Redis缓存键
    private static final String SYSTEM_PERFORMANCE_KEY = "system:performance:current";
    private static final String SYSTEM_PERFORMANCE_HISTORY_KEY = "system:performance:history";

    @Override
    public Result<PortMapPayloadVO> getPortMapPayload() {
        PortMapPayloadVO payload = new PortMapPayloadVO();
        
        List<Vessels> vesselsList = vesselsService.list();
        List<Voyages> voyagesList = voyagesService.list();
        
        List<ShipDynamicVO> ships = new ArrayList<>();
        for (Vessels vessel : vesselsList) {
            ShipDynamicVO ship = new ShipDynamicVO();
            ship.setId(vessel.getId());
            ship.setName(vessel.getNameEn());
            ship.setImo(vessel.getImoNumber());
            ship.setType(vessel.getVesselType());
            
            Voyages voyage = voyagesList.stream()
                    .filter(v -> v.getVesselId() != null && v.getVesselId().equals(vessel.getId()))
                    .findFirst()
                    .orElse(null);
            
            if (voyage != null) {
                ship.setStatus(voyage.getVoyageStatus() != null ? voyage.getVoyageStatus().toString() : "UNKNOWN");
                ship.setDestination(voyage.getNextPort());
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (voyage.getEta() != null) {
                    ship.setEta(sdf.format(voyage.getEta()));
                }
                if (voyage.getEtd() != null) {
                    ship.setEtd(sdf.format(voyage.getEtd()));
                }
            }
            
            ship.setSpeed(vessel.getSpeed());
            ship.setHeading(0); // 数据库无此字段，默认为0
            ship.setPilotRequired(true); // 默认需要引航
            
            List<GeoCoordinateVO> geoCoordinateVOS = GeoCoordinateUtil.generateWenzhouCoastRandomCoordinates(1);
            ship.setPosition(geoCoordinateVOS.get(0));
            
            ships.add(ship);
        }
        payload.setShips(ships);
        
        List<Berths> berthsList = berthsService.list();
        List<BerthStatusVO> berths = new ArrayList<>();
        for (Berths berth : berthsList) {
            BerthStatusVO berthStatus = new BerthStatusVO();
            berthStatus.setId(String.valueOf(berth.getId()));
            berthStatus.setName(berth.getBerthName());
            berthStatus.setStatus(berth.getBerthStatus() != null ? berth.getBerthStatus().toString() : "UNKNOWN");
            
            GeoCoordinateVO position = new GeoCoordinateVO();
            position.setLng(berth.getLongitude());
            position.setLat(berth.getLatitude());
            berthStatus.setPosition(position);
            
            berthStatus.setLongitude(berth.getLongitude());
            berthStatus.setLatitude(berth.getLatitude());
            berthStatus.setStartLatitude(berth.getStartLatitude());
            berthStatus.setStartLongitude(berth.getStartLongitude());
            berthStatus.setEndLatitude(berth.getEndLatitude());
            berthStatus.setEndLongitude(berth.getEndLongitude());
            berthStatus.setBerthCode(berth.getBerthCode());
            berthStatus.setPortArea(berth.getPortArea());
            berthStatus.setBerthType(berth.getBerthType());
            berthStatus.setWharfDirection(berth.getWharfDirection());
            berthStatus.setBerthDirection(berth.getBerthDirection());
            berthStatus.setMaxDraft(berth.getMaxDraft());
            berthStatus.setBerthLength(berth.getBerthLength());
            berthStatus.setBerthDepth(berth.getBerthDepth());
            berthStatus.setContactPhone(berth.getContactPhone());
            berthStatus.setPortAreaId(berth.getPortAreaId());
            berthStatus.setWharfId(berth.getWharfId());
            
            berths.add(berthStatus);
        }
        payload.setBerths(berths);
        
        // 航线信息暂时为空，因为数据库中没有相关表
        List<PilotRouteSegmentVO> routes = new ArrayList<>();
        payload.setRoutes(routes);
        
        return Result.success(payload);
    }

    @Override
    public Result<List<ShipDynamicVO>> getShipDynamics() {
        List<Vessels> vesselsList = vesselsService.list();
        List<Voyages> voyagesList = voyagesService.list();

        List<ShipDynamicVO> ships = new ArrayList<>();
        for (Vessels vessel : vesselsList) {
            ShipDynamicVO ship = new ShipDynamicVO();
            ship.setId(vessel.getId());
            ship.setName(vessel.getNameEn());
            ship.setImo(vessel.getImoNumber());
            ship.setType(vessel.getVesselType());
            ship.setStatus("ARRIVED");
            ship.setSpeed(vessel.getSpeed());
            Voyages voyage = voyagesList.stream()
                    .filter(v -> v.getVesselId() != null && v.getVesselId().equals(vessel.getId()))
                    .findFirst()
                    .orElse(null);
            
            if (voyage != null) {
                ship.setDestination(voyage.getNextPort());
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (voyage.getEta() != null) {
                    ship.setEta(sdf.format(voyage.getEta()));
                }
                if (voyage.getEtd() != null) {
                    ship.setEtd(sdf.format(voyage.getEtd()));
                }
            }
            ship.setHeading((int) (Math.random() * 360));
            ship.setPilotRequired(true); // 默认需要引航
            
            List<GeoCoordinateVO> geoCoordinateVOS = GeoCoordinateUtil.generateWenzhouCoastRandomCoordinates(1);
            ship.setPosition(geoCoordinateVOS.get(0));
            
            ships.add(ship);
        }
        
        return Result.success(ships);
    }

    @Override
    public Result<List<PilotPlanVO>> getPilotTasks() {
        // 大屏综合主屏“今日引航计划/任务列表”口径：只取“今天”的计划（按 plan_start_time）
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        ZonedDateTime dayStart = today.atStartOfDay(zone);
        ZonedDateTime nextDayStart = today.plusDays(1).atStartOfDay(zone);
        Date start = Date.from(dayStart.toInstant());
        Date endExclusive = Date.from(nextDayStart.toInstant());

        List<PilotagePlans> plansList = pilotagePlansService.list(
                new LambdaQueryWrapper<PilotagePlans>()
                        .isNotNull(PilotagePlans::getPlanStartTime)
                        .ge(PilotagePlans::getPlanStartTime, start)
                        .lt(PilotagePlans::getPlanStartTime, endExclusive)
                        .orderByAsc(PilotagePlans::getPlanStartTime)
                        .last("LIMIT 200")
        );

        Set<Long> vesselIds = plansList.stream()
                .map(PilotagePlans::getVesselId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Vessels> vesselMap = vesselIds.isEmpty()
                ? Collections.emptyMap()
                : vesselsService.listByIds(vesselIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Vessels::getId, Function.identity(), (a, b) -> a));

        Set<Long> pilotIds = plansList.stream()
                .map(PilotagePlans::getAssignedPilotId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Pilots> pilotMap = pilotIds.isEmpty()
                ? Collections.emptyMap()
                : pilotsService.listByIds(pilotIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Pilots::getId, Function.identity(), (a, b) -> a));
        
        List<PilotPlanVO> pilotPlans = new ArrayList<>();
        for (PilotagePlans plan : plansList) {
            PilotPlanVO pilotPlan = new PilotPlanVO();
            pilotPlan.setId(String.valueOf(plan.getId()));
            pilotPlan.setSchedule(plan.getPlanStartTime() != null ? 
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(plan.getPlanStartTime()) : "");
            pilotPlan.setStatus(plan.getPlanStatus());

            List<GeoCoordinateVO> routeList = GeoCoordinateUtil.generateWenzhouCoastRandomCoordinates(20);

            pilotPlan.setRoute(routeList);
            pilotPlan.setRemarks(plan.getNotes());
            
            if (plan.getVesselId() != null) {
                Vessels vessel = vesselMap.get(plan.getVesselId());
                if (vessel != null) {
                    pilotPlan.setShipId(String.valueOf(vessel.getId()));
                    pilotPlan.setShipName(vessel.getNameEn());
                }
            }

            if (plan.getAssignedPilotId() != null) {
                Pilots pilot = pilotMap.get(plan.getAssignedPilotId());
                if (pilot != null) {
                    pilotPlan.setPilotName(pilot.getPilotName()); // 假设Pilots实体有name字段
                } else {
                    pilotPlan.setPilotName("未分配");
                }
            } else {
                pilotPlan.setPilotName("未分配");
            }
            pilotPlans.add(pilotPlan);
        }
        
        return Result.success(pilotPlans);
    }

    @Override
    public Result<FeeStatisticsVO> getFinancialStats() {
        FeeStatisticsVO feeStats = new FeeStatisticsVO();
        
        List<BillingVouchers> vouchersList = billingVouchersService.list();
        
        BigDecimal totalAmount = vouchersList.stream()
                .map(BillingVouchers::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        feeStats.setTotal(totalAmount);
        feeStats.setCurrency("CNY");
        feeStats.setTransactions((long) vouchersList.size());
        
        BigDecimal growth = calculateGrowthRate();
        feeStats.setGrowth(growth);
        
        // 费用分类统计
        List<FeeStatisticsVO.FeeBreakdownItemVO> breakdownList = getFeeBreakdownFromDatabase();
        feeStats.setBreakdown(breakdownList);
        
        // 趋势数据
        List<FeeStatisticsVO.FeeTrendItemVO> trendList = getFeeTrendFromDatabase(vouchersList);
        feeStats.setTrend(trendList);
        
        List<FeeStatisticsVO.FeeCategoryItemVO> categoryList = getCategoryStatsFromDatabase();
        feeStats.setCategory(categoryList);
        
        return Result.success(feeStats);
    }

        private List<FeeStatisticsVO.FeeBreakdownItemVO> getFeeBreakdownFromDatabase() {
        List<Map<String, Object>> breakdownData = billingVouchersService.getFeeBreakdownByType();
        
        List<FeeStatisticsVO.FeeBreakdownItemVO> breakdownList = new ArrayList<>();
        for (Map<String, Object> item : breakdownData) {
            FeeStatisticsVO.FeeBreakdownItemVO breakdownItem = new FeeStatisticsVO.FeeBreakdownItemVO();
            breakdownItem.setLabel((String) item.get("chargeType"));
            breakdownItem.setValue((BigDecimal) item.get("totalAmount"));
            breakdownList.add(breakdownItem);
        }
        return breakdownList;
    }

        private List<FeeStatisticsVO.FeeTrendItemVO> getFeeTrendFromDatabase(List<BillingVouchers> vouchersList) {
        // 大屏“收入热力图/计费趋势”需要日粒度，并且必须包含“今天”的数据点（即使为 0）。
        // 这里固定输出最近 35 天（5 周）的日趋势，按时间正序返回，供前端热力图按索引映射星期/周次。
        final int days = 35;
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        LocalDate startDay = today.minusDays(days - 1L);
        // 用全日期，便于前端准确计算星期/周序号（热力图不能再用 idx%7 推断）
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<LocalDate, BigDecimal> sums = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = startDay.plusDays(i);
            sums.put(d, BigDecimal.ZERO);
        }

        if (vouchersList != null) {
            for (BillingVouchers v : vouchersList) {
                if (v == null) continue;
                Date createdAt = v.getCreatedAt();
                BigDecimal amount = v.getTotalAmount();
                if (createdAt == null || amount == null) continue;

                LocalDate d = createdAt.toInstant().atZone(zone).toLocalDate();
                if (d.isBefore(startDay) || d.isAfter(today)) continue;
                sums.put(d, sums.getOrDefault(d, BigDecimal.ZERO).add(amount));
            }
        }

        List<FeeStatisticsVO.FeeTrendItemVO> trendList = new ArrayList<>(days);
        for (Map.Entry<LocalDate, BigDecimal> e : sums.entrySet()) {
            FeeStatisticsVO.FeeTrendItemVO item = new FeeStatisticsVO.FeeTrendItemVO();
            item.setTime(e.getKey().format(labelFmt));
            item.setValue(e.getValue());
            trendList.add(item);
        }
        return trendList;
    }

        private BigDecimal calculateGrowthRate() {
        BigDecimal currentAmount = billingVouchersService.getCurrentPeriodTotal();
        BigDecimal previousAmount = billingVouchersService.getPreviousPeriodTotal();

        // SUM 在无数据时可能返回 null，这里统一按 0 处理，避免 NPE
        currentAmount = currentAmount == null ? BigDecimal.ZERO : currentAmount;
        previousAmount = previousAmount == null ? BigDecimal.ZERO : previousAmount;

        if (previousAmount.compareTo(BigDecimal.ZERO) > 0) {
            return currentAmount.subtract(previousAmount)
                    .divide(previousAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }
    
        private List<FeeStatisticsVO.FeeCategoryItemVO> getCategoryStatsFromDatabase() {
        List<Map<String, Object>> categoryData = billingVouchersService.getCategoryStats();
        
        List<FeeStatisticsVO.FeeCategoryItemVO> categoryList = new ArrayList<>();
        for (Map<String, Object> item : categoryData) {
            FeeStatisticsVO.FeeCategoryItemVO categoryItem = new FeeStatisticsVO.FeeCategoryItemVO();
            categoryItem.setName((String) item.get("name"));
            categoryItem.setValue((BigDecimal) item.get("value"));
            categoryList.add(categoryItem);
        }
        return categoryList;
    }

    @Override
    public Result<List<AlertItemVO>> getAlerts() {
        List<AlertItemVO> alerts = new ArrayList<>();

        List<BillingVouchers> vouchersList = billingVouchersService.list();
        Date now = new Date();

        int overdueCount = 0;
        for (BillingVouchers voucher : vouchersList) {
            if (voucher.getPaymentStatus() != null &&
                    "UNPAID".equals(voucher.getPaymentStatus().toString()) &&
                    voucher.getDueDate() != null &&
                    voucher.getDueDate().before(now)) {

                AlertItemVO alert = new AlertItemVO();
                alert.setId(String.valueOf(voucher.getId()));
                alert.setLevel("warning");
                alert.setAlertType("BILL_OVERDUE"); // 账单逾期告警类型
                alert.setAlertTypeName(mapAlertTypeName(alert.getAlertType()));
                alert.setMessage("账单 " + (voucher.getVoucherCode() != null ? voucher.getVoucherCode() : "V" + voucher.getId()) + " 已逾期");

                // 关联代理商信息
                String agentName = getAgentNameByVoucherId(voucher.getId());
                alert.setDetail("代理: " + agentName);

                alert.setTimestamp(voucher.getDueDate() != null ?
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(voucher.getDueDate()) : "");
                alert.setAcknowledged(null);

                alerts.add(alert);
                overdueCount++;
                if (overdueCount >= 3) {
                    break;
                }
            }
        }

        List<PilotagePlans> plansList = pilotagePlansService.list();

        for (PilotagePlans plan : plansList) {
            if ("DELAYED".equals(plan.getPlanStatus())) {
                AlertItemVO alert = new AlertItemVO();
                alert.setId(String.valueOf(plan.getId()));
                alert.setLevel("warning");
                alert.setAlertType("PILOTAGE_DELAYED"); // 引航计划延迟告警类型
                alert.setAlertTypeName(mapAlertTypeName(alert.getAlertType()));
                alert.setMessage("引航计划 " + (plan.getPlanCode() != null ? plan.getPlanCode() : "P" + plan.getId()) + " 已延迟");

                // 关联船舶信息
                String vesselName = getVesselNameByPlanId(plan.getId());
                alert.setDetail("船舶: " + vesselName);

                alert.setTimestamp(plan.getActualCompletionTime() != null ?
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(plan.getActualCompletionTime()) : "");
                alert.setAcknowledged(null);

                alerts.add(alert);
            }
        }

        // 引航员不在岗
        List<Pilots> offDutyPilots = pilotsService.list(
                new LambdaQueryWrapper<Pilots>()
                        .and(w -> w.isNull(Pilots::getOnDutyStatus).or().ne(Pilots::getOnDutyStatus, "ON_DUTY"))
        );
        for (Pilots pilot : offDutyPilots) {
            AlertItemVO alert = new AlertItemVO();
            alert.setId("PILOT-" + pilot.getId());
            alert.setLevel("warning");
            alert.setAlertType("PILOT_OFF_DUTY");
            alert.setAlertTypeName(mapAlertTypeName(alert.getAlertType()));
            alert.setMessage("引航员 " + pilot.getPilotName() + " 不在岗");
            alert.setDetail("在岗状态: " + mapOnDutyStatusName(pilot.getOnDutyStatus()));
            alert.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            alert.setAcknowledged(null);
            alerts.add(alert);
        }

        // 驾驶员不在岗
        List<Drivers> offDutyDrivers = driversService.list(
                new LambdaQueryWrapper<Drivers>()
                        .and(w -> w.isNull(Drivers::getOnDutyStatus).or().ne(Drivers::getOnDutyStatus, "ON_DUTY"))
        );
        for (Drivers driver : offDutyDrivers) {
            AlertItemVO alert = new AlertItemVO();
            alert.setId("DRIVER-" + driver.getId());
            alert.setLevel("warning");
            alert.setAlertType("DRIVER_OFF_DUTY");
            alert.setAlertTypeName(mapAlertTypeName(alert.getAlertType()));
            alert.setMessage("驾驶员 " + driver.getName() + " 不在岗");
            alert.setDetail("在岗状态: " + mapOnDutyStatusName(driver.getOnDutyStatus()));
            alert.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            alert.setAcknowledged(null);
            alerts.add(alert);
        }

        // 新增：车辆状态异常告警
        List<Vehicles> problematicVehicles = vehiclesService.list(
                new LambdaQueryWrapper<Vehicles>()
                        .eq(Vehicles::getVehicleStatus, "MAINTENANCE")
        );
        for (Vehicles vehicle : problematicVehicles) {
            AlertItemVO alert = new AlertItemVO();
            alert.setId(String.valueOf(vehicle.getId()));
            alert.setLevel("warning");
            alert.setAlertType("VEHICLE_MAINTENANCE"); // 车辆维护告警类型
            alert.setAlertTypeName(mapAlertTypeName(alert.getAlertType()));
            alert.setMessage("车辆 " + vehicle.getVehicleCode() + " 需要维护");
            alert.setDetail("车辆状态: " + mapVehicleStatusName(vehicle.getVehicleStatus()));
            alert.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            alert.setAcknowledged(null);
            alerts.add(alert);
        }

        // 新增：拖轮状态异常告警
        List<Tugboats> problematicTugboats = tugboatsService.list(
                new LambdaQueryWrapper<Tugboats>()
                        .eq(Tugboats::getStatus, "MAINTENANCE")
        );
        for (Tugboats tugboat : problematicTugboats) {
            AlertItemVO alert = new AlertItemVO();
            alert.setId(String.valueOf(tugboat.getId()));
            alert.setLevel("warning");
            alert.setAlertType("TUGBOAT_MAINTENANCE"); // 拖轮维护告警类型
            alert.setAlertTypeName(mapAlertTypeName(alert.getAlertType()));
            alert.setMessage("拖轮 " + tugboat.getTugboatName() + " 需要维护");
            alert.setDetail("拖轮状态: " + mapTugboatStatusName(tugboat.getStatus()));
            alert.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            alert.setAcknowledged(null);
            alerts.add(alert);
        }

        // 新增：泊位不可用告警
        List<Berths> unavailableBerths = berthsService.list(
                new LambdaQueryWrapper<Berths>()
                        .eq(Berths::getBerthStatus, "MAINTENANCE")
        );
        for (Berths berth : unavailableBerths) {
            AlertItemVO alert = new AlertItemVO();
            alert.setId(String.valueOf(berth.getId()));
            alert.setLevel("warning");
            alert.setAlertType("BERTH_UNAVAILABLE"); // 泊位不可用告警类型
            alert.setAlertTypeName(mapAlertTypeName(alert.getAlertType()));
            alert.setMessage("泊位 " + berth.getBerthName() + " 不可用");
            alert.setDetail("泊位状态: " + mapBerthStatusName(berth.getBerthStatus()));
            alert.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            alert.setAcknowledged(null);
            alerts.add(alert);
        }

        return Result.success(alerts);
    }

    private String mapAlertTypeName(String type) {
        if (type == null || type.isEmpty()) {
            return "告警事件";
        }
        switch (type) {
            case "BILL_OVERDUE":
                return "账单逾期告警";
            case "PILOTAGE_DELAYED":
                return "引航计划延迟告警";
            case "PILOT_OFF_DUTY":
                return "引航员不在岗告警";
            case "DRIVER_OFF_DUTY":
                return "驾驶员不在岗告警";
            case "VEHICLE_MAINTENANCE":
                return "车辆维护告警";
            case "TUGBOAT_MAINTENANCE":
                return "拖轮维护告警";
            case "BERTH_UNAVAILABLE":
                return "泊位不可用告警";
            default:
                return type.replace("_", " ");
        }
    }

    private String mapOnDutyStatusName(Object status) {
        if (status == null) {
            return "未知";
        }
        String value = status.toString();
        switch (value) {
            case "ON_DUTY":
                return "在岗";
            case "RESTING":
                return "休息";
            case "LEAVE":
                return "请假";
            case "ASSIGNED":
                return "已分配";
            case "OPERATING":
                return "执行中";
            default:
                return value;
        }
    }

    private String mapVehicleStatusName(Object status) {
        if (status == null) {
            return "未知";
        }
        String value = status.toString();
        switch (value) {
            case "AVAILABLE":
                return "可用";
            case "IN_USE":
                return "使用中";
            case "MAINTENANCE":
                return "维护中";
            case "SCRAPPED":
                return "报废";
            default:
                return value;
        }
    }

    private String mapTugboatStatusName(Object status) {
        if (status == null) {
            return "未知";
        }
        String value = status.toString();
        switch (value) {
            case "AVAILABLE":
                return "可用";
            case "OPERATING":
                return "作业中";
            case "MAINTENANCE":
                return "维护中";
            default:
                return value;
        }
    }

    private String mapBerthStatusName(Object status) {
        if (status == null) {
            return "未知";
        }
        String value = status.toString();
        switch (value) {
            case "AVAILABLE":
                return "可用";
            case "OCCUPIED":
                return "占用中";
            case "MAINTENANCE":
                return "维护中";
            case "UNAVAILABLE":
                return "不可用";
            default:
                return value;
        }
    }

    @Override
    public Result<List<SystemStatusItemVO>> getSystemStatus() {
        List<SystemStatusItemVO> systemStatuses = new ArrayList<>();

        long totalPilotCount = pilotsService.count();
        long healthyPilotCount = pilotsService.count(new LambdaQueryWrapper<Pilots>()
                .eq(Pilots::getOnDutyStatus, "ON_DUTY"));
        long totalDriverCount = driversService.count();
        long healthyDriverCount = driversService.count(new LambdaQueryWrapper<Drivers>()
                .eq(Drivers::getOnDutyStatus, "ON_DUTY"));
        long totalVehicleCount = vehiclesService.count();
        long healthyVehicleCount = vehiclesService.count(new LambdaQueryWrapper<Vehicles>()
                .in(Vehicles::getVehicleStatus, "AVAILABLE", "IN_USE"));
        long totalTugboatCount = tugboatsService.count();
        long healthyTugboatCount = tugboatsService.count(new LambdaQueryWrapper<Tugboats>()
                .in(Tugboats::getStatus, "AVAILABLE", "OPERATING"));
        long totalPilotBoatCount = pilotBoatsService.count();
        long healthyPilotBoatCount = pilotBoatsService.count(new LambdaQueryWrapper<PilotBoats>()
                .in(PilotBoats::getCurrentStatus, "AVAILABLE", "OPERATING"));

        BigDecimal pilotUptime = totalPilotCount > 0 ?
                new BigDecimal(healthyPilotCount)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(totalPilotCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 引航员状态
        SystemStatusItemVO pilotStatus = new SystemStatusItemVO();
        pilotStatus.setModuleName("PILOTS");
        pilotStatus.setCount(healthyPilotCount); // 健康数量
        pilotStatus.setTotal(totalPilotCount);   // 总数
        pilotStatus.setStatus(healthyPilotCount > 0 ? "HEALTHY" : "DEGRADED");
        pilotStatus.setLastUpdate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        pilotStatus.setUptime(pilotUptime);
        systemStatuses.add(pilotStatus);

        BigDecimal driverUptime = totalDriverCount > 0 ?
                new BigDecimal(healthyDriverCount)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(totalDriverCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 驾驶员状态
        SystemStatusItemVO driverStatus = new SystemStatusItemVO();
        driverStatus.setModuleName("DRIVERS");
        driverStatus.setCount(healthyDriverCount); // 健康数量
        driverStatus.setTotal(totalDriverCount);   // 总数
        driverStatus.setStatus(healthyDriverCount > 0 ? "HEALTHY" : "DEGRADED");
        driverStatus.setLastUpdate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        driverStatus.setUptime(driverUptime);
        systemStatuses.add(driverStatus);

        BigDecimal vehicleUptime = totalVehicleCount > 0 ?
                new BigDecimal(healthyVehicleCount)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(totalVehicleCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 车辆状态
        SystemStatusItemVO vehicleStatus = new SystemStatusItemVO();
        vehicleStatus.setModuleName("VEHICLES");
        vehicleStatus.setCount(healthyVehicleCount); // 健康数量
        vehicleStatus.setTotal(totalVehicleCount);   // 总数
        vehicleStatus.setStatus(healthyVehicleCount > 0 ? "HEALTHY" : "DEGRADED");
        vehicleStatus.setLastUpdate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        vehicleStatus.setUptime(vehicleUptime);
        systemStatuses.add(vehicleStatus);

        BigDecimal tugboatUptime = totalTugboatCount > 0 ?
                new BigDecimal(healthyTugboatCount)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(totalTugboatCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 拖轮状态
        SystemStatusItemVO tugboatStatus = new SystemStatusItemVO();
        tugboatStatus.setModuleName("TUGBOATS");
        tugboatStatus.setCount(healthyTugboatCount); // 健康数量
        tugboatStatus.setTotal(totalTugboatCount);   // 总数
        tugboatStatus.setStatus(healthyTugboatCount > 0 ? "HEALTHY" : "DEGRADED");
        tugboatStatus.setLastUpdate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        tugboatStatus.setUptime(tugboatUptime);
        systemStatuses.add(tugboatStatus);

        BigDecimal pilotBoatUptime = totalPilotBoatCount > 0 ?
                new BigDecimal(healthyPilotBoatCount)
                        .multiply(new BigDecimal(100))
                        .divide(new BigDecimal(totalPilotBoatCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 引航艇状态
        SystemStatusItemVO pilotBoatStatus = new SystemStatusItemVO();
        pilotBoatStatus.setModuleName("PILOT_BOATS");
        pilotBoatStatus.setCount(healthyPilotBoatCount); // 健康数量
        pilotBoatStatus.setTotal(totalPilotBoatCount);   // 总数
        pilotBoatStatus.setStatus(healthyPilotBoatCount > 0 ? "HEALTHY" : "DEGRADED");
        pilotBoatStatus.setLastUpdate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        pilotBoatStatus.setUptime(pilotBoatUptime);
        systemStatuses.add(pilotBoatStatus);

        return Result.success(systemStatuses);
    }

    @Override
    public Result<List<BerthStatusVO>> getBerthStatus() {
        List<Berths> berthsList = berthsService.list();
        List<BerthStatusVO> berths = new ArrayList<>();
        
        for (Berths berth : berthsList) {
            BerthStatusVO berthStatus = new BerthStatusVO();
            berthStatus.setId(String.valueOf(berth.getId()));
            berthStatus.setName(berth.getBerthName());
            berthStatus.setStatus(berth.getBerthStatus() != null ? berth.getBerthStatus().toString() : "UNKNOWN");
            
            GeoCoordinateVO position = new GeoCoordinateVO();
            position.setLng(berth.getLongitude());
            position.setLat(berth.getLatitude());
            berthStatus.setPosition(position);
            
            berthStatus.setLongitude(berth.getLongitude());
            berthStatus.setLatitude(berth.getLatitude());
            berthStatus.setStartLatitude(berth.getStartLatitude());
            berthStatus.setStartLongitude(berth.getStartLongitude());
            berthStatus.setEndLatitude(berth.getEndLatitude());
            berthStatus.setEndLongitude(berth.getEndLongitude());
            berthStatus.setBerthCode(berth.getBerthCode());
            berthStatus.setPortArea(berth.getPortArea());
            berthStatus.setBerthType(berth.getBerthType());
            berthStatus.setWharfDirection(berth.getWharfDirection());
            berthStatus.setBerthDirection(berth.getBerthDirection());
            berthStatus.setMaxDraft(berth.getMaxDraft());
            berthStatus.setBerthLength(berth.getBerthLength());
            berthStatus.setBerthDepth(berth.getBerthDepth());
            berthStatus.setContactPhone(berth.getContactPhone());
            berthStatus.setPortAreaId(berth.getPortAreaId());
            berthStatus.setWharfId(berth.getWharfId());
            
            berths.add(berthStatus);
        }
        
        return Result.success(berths);
    }

    @Override
    public Result<SystemPerformanceDataVO> getSystemPerformanceData() {
        // 尝试从Redis获取缓存的系统性能数据
        SystemPerformanceDataVO performanceData = redisService.get(SYSTEM_PERFORMANCE_KEY, SystemPerformanceDataVO.class);
        
        if (performanceData == null) {
            // 如果Redis中没有缓存数据，采集真实系统性能数据
            performanceData = collectRealSystemPerformanceData();
            
            // 将数据缓存到Redis，设置30秒过期时间
            redisService.set(SYSTEM_PERFORMANCE_KEY, performanceData, 30, java.util.concurrent.TimeUnit.SECONDS);
        }
        
        return Result.success(performanceData);
    }
    
    
    @Scheduled(fixedRate = 30000) // 30秒执行一次
    public void scheduledCollectSystemPerformance() {
        SystemPerformanceDataVO performanceData = collectRealSystemPerformanceData();
        
        // 将数据缓存到Redis，设置60秒过期时间
        redisService.set(SYSTEM_PERFORMANCE_KEY, performanceData, 60, java.util.concurrent.TimeUnit.SECONDS);
        
        // 同时将当前指标添加到历史记录中
        if (!performanceData.getMetrics().isEmpty()) {
            SystemPerformanceDataVO.SystemMetricsItemVO currentMetric = performanceData.getMetrics().get(0);
            redisService.leftPush(SYSTEM_PERFORMANCE_HISTORY_KEY, currentMetric);
            // 限制历史记录数量，保留最近288条记录（24小时，每5分钟一条）
            redisService.trim(SYSTEM_PERFORMANCE_HISTORY_KEY, 0, 287);
        }
    }
    
    /**
     * 采集真实的系统性能数据
     */
    private SystemPerformanceDataVO collectRealSystemPerformanceData() {
        SystemPerformanceDataVO performanceData = new SystemPerformanceDataVO();

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hardware = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        CentralProcessor processor = hardware.getProcessor();
        GlobalMemory memory = hardware.getMemory();

        // 生成系统性能指标数据 - 获取当前真实系统数据
        List<SystemPerformanceDataVO.SystemMetricsItemVO> metrics = new ArrayList<>();

        // 获取当前系统指标
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);

        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        double memoryUsage = (double) (totalMemory - availableMemory) / totalMemory * 100.0;

        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fileStoreList = fileSystem.getFileStores();
        long totalDisk = 0;
        long usedDisk = 0;
        for (OSFileStore fs : fileStoreList) {
            if (fs.getTotalSpace() > 0) {
                totalDisk += fs.getTotalSpace();
                usedDisk += fs.getTotalSpace() - fs.getFreeSpace();
            }
        }
        double diskUsage = totalDisk > 0 ? (double) usedDisk / totalDisk * 100.0 : 0.0;

        List<NetworkIF> networkIFList = hardware.getNetworkIFs();
        long totalRxBytes = 0;
        long totalTxBytes = 0;

        for (NetworkIF net : networkIFList) {
            if (!net.getName().startsWith("lo") && net.getSpeed() > 0) {
                totalRxBytes += net.getBytesRecv();
                totalTxBytes += net.getBytesSent();
            }
        }

        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastTimestamp.getOrDefault("global", currentTime);
        BigDecimal networkIn = BigDecimal.ZERO;
        BigDecimal networkOut = BigDecimal.ZERO;

        if (timeDiff > 0) {
            Long lastRx = lastRxBytes.get("global");
            Long lastTx = lastTxBytes.get("global");

            if (lastRx != null && lastTx != null) {
                long rxRate = (totalRxBytes - lastRx) * 1000 / timeDiff;
                long txRate = (totalTxBytes - lastTx) * 1000 / timeDiff;

                networkIn = new BigDecimal(rxRate / (1024.0 * 1024.0)).setScale(1, RoundingMode.HALF_UP);
                networkOut = new BigDecimal(txRate / (1024.0 * 1024.0)).setScale(1, RoundingMode.HALF_UP);
            }
        }

        lastRxBytes.put("global", totalRxBytes);
        lastTxBytes.put("global", totalTxBytes);
        lastTimestamp.put("global", currentTime);

        // 当前时间点的指标
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SystemPerformanceDataVO.SystemMetricsItemVO currentMetric = new SystemPerformanceDataVO.SystemMetricsItemVO();
        currentMetric.setTimestamp(sdf.format(new Date()));
        currentMetric.setCpuUsage(new BigDecimal(String.format("%.1f", cpuLoad * 100)));
        currentMetric.setMemoryUsage(new BigDecimal(String.format("%.1f", memoryUsage)));
        currentMetric.setDiskUsage(new BigDecimal(String.format("%.1f", diskUsage)));
        currentMetric.setNetworkIn(networkIn);
        currentMetric.setNetworkOut(networkOut);
        currentMetric.setLoadAverage(new BigDecimal(String.format("%.2f", getSystemLoadAverage(processor))));
        currentMetric.setResponseTime(100); // 响应时间可根据实际API响应时间调整

        metrics.add(currentMetric);

        // 从Redis获取历史性能数据
        List<?> historyList = redisService.range(SYSTEM_PERFORMANCE_HISTORY_KEY, 0, 10);
        if (historyList != null) {
            for (Object obj : historyList) {
                if (obj instanceof SystemPerformanceDataVO.SystemMetricsItemVO) {
                    metrics.add((SystemPerformanceDataVO.SystemMetricsItemVO) obj);
                }
            }
        }

        List<BigDecimal> cpuUsages = metrics.stream()
                .map(SystemPerformanceDataVO.SystemMetricsItemVO::getCpuUsage)
                .toList();
        List<BigDecimal> memoryUsages = metrics.stream()
                .map(SystemPerformanceDataVO.SystemMetricsItemVO::getMemoryUsage)
                .toList();
        List<BigDecimal> loadAverages = metrics.stream()
                .map(SystemPerformanceDataVO.SystemMetricsItemVO::getLoadAverage)
                .toList();
        List<Integer> responseTimes = metrics.stream()
                .map(SystemPerformanceDataVO.SystemMetricsItemVO::getResponseTime)
                .toList();

        SystemPerformanceDataVO.SystemPerformanceSummaryVO summary = new SystemPerformanceDataVO.SystemPerformanceSummaryVO();
        summary.setAvgCpuUsage(cpuUsages.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(cpuUsages.size()), 2, RoundingMode.HALF_UP));
        summary.setAvgMemoryUsage(memoryUsages.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(memoryUsages.size()), 2, RoundingMode.HALF_UP));
        summary.setPeakLoad(loadAverages.stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO));
        summary.setAvgResponseTime(responseTimes.stream()
                .mapToInt(Integer::intValue)
                .sum() / responseTimes.size());

        performanceData.setMetrics(metrics);
        performanceData.setSummary(summary);

        return performanceData;
    }

    /**
     * 获取系统平均负载
     * @param processor 处理器实例
     * @return 系统平均负载
     */
    private double getSystemLoadAverage(CentralProcessor processor) {
        double[] loadAverage = processor.getSystemLoadAverage(1);
        return loadAverage[0] < 0 ? 0.5 : loadAverage[0]; // 如果获取不到负载信息，返回默认值0.5
    }

    /**
     * 根据凭证ID获取代理商名称
     * @param voucherId 凭证ID
     * @return 代理商名称
     */
    private String getAgentNameByVoucherId(Long voucherId) {
        List<AgentInvoiceInfo> agentInvoiceInfos = agentInvoiceInfoService.list(
                new LambdaQueryWrapper<AgentInvoiceInfo>()
                        .eq(AgentInvoiceInfo::getBillingVoucherId, voucherId)
        );

        if (!agentInvoiceInfos.isEmpty()) {
            AgentInvoiceInfo agentInfo = agentInvoiceInfos.get(0);
            String agentName = agentInfo.getAgentName();
            if (agentName != null && !agentName.trim().isEmpty()) {
                return agentName;
            }
            String payingUnitName = agentInfo.getPayingUnitName();
            if (payingUnitName != null && !payingUnitName.trim().isEmpty()) {
                return payingUnitName;
            }
            if (agentInfo.getAgentId() != null) {
                Agents agent = agentsService.getById(agentInfo.getAgentId());
                if (agent != null) {
                    String companyName = agent.getCompanyName();
                    if (companyName != null && !companyName.trim().isEmpty()) {
                        return companyName;
                    }
                    String shortName = agent.getShortName();
                    if (shortName != null && !shortName.trim().isEmpty()) {
                        return shortName;
                    }
                }
            }
        }
        
        BillingVouchers voucher = billingVouchersService.getById(voucherId);
        if (voucher != null && voucher.getAgentId() != null) {
            Agents agent = agentsService.getById(voucher.getAgentId());
            if (agent != null) {
                String companyName = agent.getCompanyName();
                if (companyName != null && !companyName.trim().isEmpty()) {
                    return companyName;
                }
                String shortName = agent.getShortName();
                if (shortName != null && !shortName.trim().isEmpty()) {
                    return shortName;
                }
            }
        }

        // 如果没有找到对应的代理商信息，返回默认值
        return "未知代理";
    }

    /**
     * 根据计划ID获取船舶名称
     * @param planId 计划ID
     * @return 船舶名称
     */
    private String getVesselNameByPlanId(Long planId) {
        PilotagePlans plan = pilotagePlansService.getById(planId);
        if (plan != null && plan.getVesselId() != null) {
            Vessels vessel = vesselsService.getById(plan.getVesselId());
            if (vessel != null) {
                return vessel.getNameEn();
            }
        }
        return "未知船舶";
    }
}