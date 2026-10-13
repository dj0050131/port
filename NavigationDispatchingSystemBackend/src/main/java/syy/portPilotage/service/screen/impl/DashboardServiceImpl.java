package syy.portPilotage.service.screen.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syy.portPilotage.common.Result;
import syy.portPilotage.entity.*;
import syy.portPilotage.service.*;
import syy.portPilotage.service.screen.DashboardService;
import syy.portPilotage.vo.billing.FinancialStatVO;
import syy.portPilotage.vo.common.TrendPointVO;
import syy.portPilotage.vo.pilotage.PilotageStatVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private PilotageRequestsService pilotageRequestsService;
    
    @Autowired
    private PilotagePlansService pilotagePlansService;
    
    @Autowired
    private PilotsService pilotsService;

    @Autowired
    private DriversService driversService;
    
    @Autowired
    private BillingVouchersService billingVouchersService;
    
    @Autowired
    private VesselsService vesselsService;
    
    @Autowired
    private VoyagesService voyagesService;
    
    @Autowired
    private PilotBoatsService pilotBoatsService;
    
    @Autowired
    private TugboatsService tugboatsService;
    
    @Autowired
    private VehiclesService vehiclesService;
    
    @Autowired
    private BerthsService berthsService;

    @Override
    public Result<PilotageStatVO> getPilotageStat() {
        PilotageStatVO stat = new PilotageStatVO();
        
        long totalRequests = pilotageRequestsService.count();
        stat.setTotalRequests(totalRequests);
        
        long pendingRequests = pilotageRequestsService.count(
            new LambdaQueryWrapper<PilotageRequests>()
                .eq(PilotageRequests::getRequestStatus, "PENDING")
        );
        stat.setPendingRequests(pendingRequests);
        
        long approvedRequests = pilotageRequestsService.count(
            new LambdaQueryWrapper<PilotageRequests>()
                .eq(PilotageRequests::getRequestStatus, "APPROVED")
        );
        stat.setApprovedRequests(approvedRequests);
        
        long completedPlans = pilotagePlansService.count(
            new LambdaQueryWrapper<PilotagePlans>()
                .eq(PilotagePlans::getPlanStatus, "COMPLETED")
        );
        stat.setCompletedPlans(completedPlans);
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        long todayPlans = pilotagePlansService.count(
            new LambdaQueryWrapper<PilotagePlans>()
                .ge(PilotagePlans::getPlanStartTime, startOfDay)
                .le(PilotagePlans::getPlanStartTime, endOfDay)
        );
        stat.setTodayPlans(todayPlans);
        
        long activePilots = pilotsService.count(
            new LambdaQueryWrapper<Pilots>()
                .eq(Pilots::getOnDutyStatus, "ON_DUTY")
        );
        stat.setActivePilots(activePilots);

        List<TrendPointVO> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            long dailyPlans = pilotagePlansService.count(
                new LambdaQueryWrapper<PilotagePlans>()
                    .ge(PilotagePlans::getPlanStartTime, dayStart)
                    .le(PilotagePlans::getPlanStartTime, dayEnd)
            );
            trends.add(new TrendPointVO(date.toString(), dailyPlans));
        }
        stat.setTrends(trends);
        
        return Result.success(stat);
    }

    @Override
    public Result<FinancialStatVO> getFinancialStat() {
        FinancialStatVO stat = new FinancialStatVO();
        
        BigDecimal totalRevenue = billingVouchersService.list().stream()
            .map(BillingVouchers::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stat.setTotalRevenue(totalRevenue);
        
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusSeconds(1);
        
        BigDecimal monthRevenue = billingVouchersService.list(
            new LambdaQueryWrapper<BillingVouchers>()
                .ge(BillingVouchers::getCreatedAt, startOfMonth)
                .le(BillingVouchers::getCreatedAt, endOfMonth)
        ).stream()
            .map(BillingVouchers::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stat.setMonthRevenue(monthRevenue);
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        
        BigDecimal todayRevenue = billingVouchersService.list(
            new LambdaQueryWrapper<BillingVouchers>()
                .ge(BillingVouchers::getCreatedAt, startOfDay)
                .le(BillingVouchers::getCreatedAt, endOfDay)
        ).stream()
            .map(BillingVouchers::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stat.setTodayRevenue(todayRevenue);
        
        long totalInvoices = billingVouchersService.count();
        stat.setTotalInvoices(totalInvoices);
        
        long unpaidInvoices = billingVouchersService.count(
            new LambdaQueryWrapper<BillingVouchers>()
                .eq(BillingVouchers::getPaymentStatus, "UNPAID")
        );
        stat.setUnpaidInvoices(unpaidInvoices);
        
        BigDecimal unpaidAmount = billingVouchersService.list(
            new LambdaQueryWrapper<BillingVouchers>()
                .eq(BillingVouchers::getPaymentStatus, "UNPAID")
        ).stream()
            .map(BillingVouchers::getTotalAmount)
             .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stat.setUnpaidAmount(unpaidAmount);
        
        Date now = new Date();
        BigDecimal overdueAmount = billingVouchersService.list(
            new LambdaQueryWrapper<BillingVouchers>()
                .eq(BillingVouchers::getPaymentStatus, "UNPAID")
                .lt(BillingVouchers::getDueDate, now)
        ).stream()
            .map(BillingVouchers::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stat.setOverdueAmount(overdueAmount);

        List<TrendPointVO> trends = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            BigDecimal dailyAmount = billingVouchersService.list(
                new LambdaQueryWrapper<BillingVouchers>()
                    .ge(BillingVouchers::getCreatedAt, dayStart)
                    .le(BillingVouchers::getCreatedAt, dayEnd)
            ).stream()
                .map(BillingVouchers::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            trends.add(new TrendPointVO(date.toString(), dailyAmount));
        }
        stat.setTrends(trends);
        
        return Result.success(stat);
    }

    @Override
    public Result<Map<String, Object>> getThroughputStat() {
        Map<String, Object> stat = new HashMap<>();
        
        long inboundShips = voyagesService.count(
            new LambdaQueryWrapper<Voyages>()
                // DB枚举为 IMPORT/EXPORT/TRANSIT，这里用 IMPORT 代表进港
                .eq(Voyages::getVoyageType, "IMPORT")
        );
        stat.put("inboundShips", inboundShips);

        long inPortShips = voyagesService.count(
            new LambdaQueryWrapper<Voyages>()
                .in(Voyages::getVoyageStatus, "IN_PORT", "在港")
        );
        stat.put("inPortShips", inPortShips);
        
        long outboundShips = voyagesService.count(
            new LambdaQueryWrapper<Voyages>()
                // DB枚举为 IMPORT/EXPORT/TRANSIT，这里用 EXPORT 代表出港
                .eq(Voyages::getVoyageType, "EXPORT")
        );
        stat.put("outboundShips", outboundShips);
        
        BigDecimal cargoWeight = vesselsService.list().stream()
            .map(Vessels::getDeadweight)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stat.put("cargoWeight", cargoWeight);
        
        stat.put("updateTime", new Date());
        
        List<TrendPoint> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            // 获取当天的船舶数量
            long dailyCount = voyagesService.count(
                new LambdaQueryWrapper<Voyages>()
                    .ge(Voyages::getCreatedAt, startOfDay)
                    .le(Voyages::getCreatedAt, endOfDay)
            );
            
            trends.add(new TrendPoint(date.toString(), dailyCount));
        }
        stat.put("trends", trends);
        
        return Result.success(stat);
    }

    @Override
    public Result<Map<String, Object>> getResourceUsageStat() {
        Map<String, Object> stat = new HashMap<>();
        
        long pilotBoats = pilotBoatsService.count();
        
        long tugboats = tugboatsService.count();
        
        long pilots = pilotsService.count();
        
        long vehicles = vehiclesService.count();
        
        stat.put("pilotBoats", pilotBoats);
        stat.put("tugboats", tugboats);
        stat.put("pilots", pilots);
        stat.put("vehicles", vehicles);
        stat.put("updateTime", new Date());
        
        List<TrendPoint> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            
            // 获取当天活跃的引航计划数量作为资源使用情况
            long dailyActivePlans = pilotagePlansService.count(
                new LambdaQueryWrapper<PilotagePlans>()
                    .ge(PilotagePlans::getPlanStartTime, startOfDay)
                    .le(PilotagePlans::getPlanStartTime, endOfDay)
            );
            
            trends.add(new TrendPoint(date.toString(), dailyActivePlans));
        }
        stat.put("trends", trends);
        
        return Result.success(stat);
    }

    @Override
    public Result<Map<String, Object>> getAlertStat() {
        Map<String, Object> stat = new HashMap<>();
        Date nowDate = new Date();
        
        long overdueCount = billingVouchersService.count(
            new LambdaQueryWrapper<BillingVouchers>()
                .eq(BillingVouchers::getPaymentStatus, "UNPAID")
                .isNotNull(BillingVouchers::getDueDate)
                .le(BillingVouchers::getDueDate, nowDate)
        );
        
        long delayedPlanCount = pilotagePlansService.count(
            new LambdaQueryWrapper<PilotagePlans>()
                .eq(PilotagePlans::getPlanStatus, "DELAYED")
        );
        
        long offDutyPilotCount = pilotsService.count(
            new LambdaQueryWrapper<Pilots>()
                .and(w -> w.isNull(Pilots::getOnDutyStatus).or().ne(Pilots::getOnDutyStatus, "ON_DUTY"))
        );
        long offDutyDriverCount = driversService.count(
            new LambdaQueryWrapper<Drivers>()
                .and(w -> w.isNull(Drivers::getOnDutyStatus).or().ne(Drivers::getOnDutyStatus, "ON_DUTY"))
        );
        
        long vehicleMaintenanceCount = vehiclesService.count(
            new LambdaQueryWrapper<Vehicles>()
                .eq(Vehicles::getVehicleStatus, "MAINTENANCE")
        );
        
        long tugboatMaintenanceCount = tugboatsService.count(
            new LambdaQueryWrapper<Tugboats>()
                .eq(Tugboats::getStatus, "MAINTENANCE")
        );
        
        long berthUnavailableCount = berthsService.count(
            new LambdaQueryWrapper<Berths>()
                .eq(Berths::getBerthStatus, "MAINTENANCE")
        );
        
        long criticalCount = 0;
        long warningCount = overdueCount + delayedPlanCount + vehicleMaintenanceCount + tugboatMaintenanceCount + berthUnavailableCount
            + offDutyPilotCount + offDutyDriverCount;
        long infoCount = 0;
        long totalCount = criticalCount + warningCount + infoCount;
        
        stat.put("totalCount", totalCount);
        stat.put("criticalCount", criticalCount);
        stat.put("warningCount", warningCount);
        stat.put("infoCount", infoCount);
        stat.put("updateTime", new Date());
        
        List<TrendPoint> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            Date startDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
            
            long dailyOverdueCount = billingVouchersService.count(
                new LambdaQueryWrapper<BillingVouchers>()
                    .eq(BillingVouchers::getPaymentStatus, "UNPAID")
                    .isNotNull(BillingVouchers::getDueDate)
                    .ge(BillingVouchers::getDueDate, startDate)
                    .le(BillingVouchers::getDueDate, endDate)
                    .le(BillingVouchers::getDueDate, now)
            );
            
            long dailyDelayedPlans = pilotagePlansService.count(
                new LambdaQueryWrapper<PilotagePlans>()
                    .eq(PilotagePlans::getPlanStatus, "DELAYED")
                    .ge(PilotagePlans::getUpdatedAt, startDate)
                    .le(PilotagePlans::getUpdatedAt, endDate)
            );
            
            long dailyOffDutyPilots = pilotsService.count(
                new LambdaQueryWrapper<Pilots>()
                    .and(w -> w.isNull(Pilots::getOnDutyStatus).or().ne(Pilots::getOnDutyStatus, "ON_DUTY"))
                    .ge(Pilots::getUpdatedAt, startDate)
                    .le(Pilots::getUpdatedAt, endDate)
            );
            long dailyOffDutyDrivers = driversService.count(
                new LambdaQueryWrapper<Drivers>()
                    .and(w -> w.isNull(Drivers::getOnDutyStatus).or().ne(Drivers::getOnDutyStatus, "ON_DUTY"))
                    .ge(Drivers::getUpdatedAt, startDate)
                    .le(Drivers::getUpdatedAt, endDate)
            );
            
            long dailyVehicleMaintenance = vehiclesService.count(
                new LambdaQueryWrapper<Vehicles>()
                    .eq(Vehicles::getVehicleStatus, "MAINTENANCE")
                    .ge(Vehicles::getUpdatedAt, startDate)
                    .le(Vehicles::getUpdatedAt, endDate)
            );
            
            long dailyTugboatMaintenance = tugboatsService.count(
                new LambdaQueryWrapper<Tugboats>()
                    .eq(Tugboats::getStatus, "MAINTENANCE")
                    .ge(Tugboats::getUpdatedAt, startDate)
                    .le(Tugboats::getUpdatedAt, endDate)
            );
            
            long dailyBerthUnavailable = berthsService.count(
                new LambdaQueryWrapper<Berths>()
                    .eq(Berths::getBerthStatus, "MAINTENANCE")
                    .ge(Berths::getUpdatedAt, startDate)
                    .le(Berths::getUpdatedAt, endDate)
            );
            
            long dailyTotal = dailyOverdueCount + dailyDelayedPlans + dailyOffDutyPilots + dailyOffDutyDrivers
                + dailyVehicleMaintenance + dailyTugboatMaintenance + dailyBerthUnavailable;
            trends.add(new TrendPoint(date.toString(), dailyTotal));
        }
        stat.put("trends", trends);
        
        return Result.success(stat);
    }

    @Override
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("pilotage", getPilotageStat().getData());
        overview.put("financial", getFinancialStat().getData());
        
        return Result.success(overview);
    }

    /**
     * 趋势数据点内部类
     */
    private static class TrendPoint {
        private String time;
        private Number value;
        
        public TrendPoint() {}
        
        public TrendPoint(String time, Number value) {
            this.time = time;
            this.value = value;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public Number getValue() {
            return value;
        }

        public void setValue(Number value) {
            this.value = value;
        }
    }
}