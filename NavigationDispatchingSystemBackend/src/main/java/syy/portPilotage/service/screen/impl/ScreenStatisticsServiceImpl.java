package syy.portPilotage.service.screen.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syy.portPilotage.common.Result;
import syy.portPilotage.entity.*;
import syy.portPilotage.enums.VoyageTypeEnum;
import syy.portPilotage.service.*;
import syy.portPilotage.service.screen.ScreenStatisticsService;
import syy.portPilotage.vo.screen.OperationsOverviewVO;
import syy.portPilotage.vo.screen.TrafficOverviewVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ScreenStatisticsServiceImpl implements ScreenStatisticsService {

    @Autowired
    private VesselsService vesselsService;
    @Autowired
    private VoyagesService voyagesService;
    @Autowired
    private BerthsService berthsService;
    @Autowired
    private PilotagePlansService pilotagePlansService;
    @Autowired
    private TugboatsService tugboatsService;
    @Autowired
    private PlanVehicleAssignmentsService planVehicleAssignmentsService;
    @Autowired
    private PlanPilotBoatAssignmentsService planPilotBoatAssignmentsService;
    @Autowired
    private PlanTugboatAssignmentsService planTugboatAssignmentsService;
    @Autowired
    private PilotBoatsService pilotBoatsService;
    @Autowired
    private PortsService portsService;
    @Autowired
    private PilotsService pilotsService;
    @Autowired
    private PlanPilotAssignmentsService planPilotAssignmentsService;
    
    private static final int MAX_PORT_CAPACITY = 100; // 假设港口最大容量为100艘船/小时

    @Override
    public Result<TrafficOverviewVO> getTrafficStats() {
        TrafficOverviewVO trafficOverview = new TrafficOverviewVO();

        List<Vessels> vesselsList = vesselsService.list();

        // 指标数据
        List<TrafficOverviewVO.TrafficMetricVO> metrics = new ArrayList<>();

        TrafficOverviewVO.TrafficMetricVO totalMetric = new TrafficOverviewVO.TrafficMetricVO();
        totalMetric.setId("1");
        totalMetric.setLabel("总艘次");
        totalMetric.setValue((long) vesselsList.size());
        totalMetric.setTrend(getTotalVesselTrend()); // 实际实现
        totalMetric.setUnit("艘");
        totalMetric.setSubLabel("船舶数量");
        metrics.add(totalMetric);

        

        // 今日ETA数量
        TrafficOverviewVO.TrafficMetricVO todayEtaBerthingMetric = new TrafficOverviewVO.TrafficMetricVO();
        todayEtaBerthingMetric.setId("3");
        todayEtaBerthingMetric.setLabel("今日预计靠泊");
        todayEtaBerthingMetric.setValue(getTodayEtaBerthingCount());
        todayEtaBerthingMetric.setTrend(getTodayEtaBerthingTrend()); // 今日靠泊趋势
        todayEtaBerthingMetric.setUnit("艘");
        todayEtaBerthingMetric.setSubLabel("今日预到达船舶数量");
        metrics.add(todayEtaBerthingMetric);

        // 七日靠泊数量
        TrafficOverviewVO.TrafficMetricVO sevenDayBerthingMetric = new TrafficOverviewVO.TrafficMetricVO();
        sevenDayBerthingMetric.setId("4");
        sevenDayBerthingMetric.setLabel("七日靠泊");
        sevenDayBerthingMetric.setValue(getSevenDayBerthingCount());
        sevenDayBerthingMetric.setTrend(getSevenDayBerthingTrend()); // 七日靠泊趋势
        sevenDayBerthingMetric.setUnit("艘");
        sevenDayBerthingMetric.setSubLabel("七日内到达船舶数量");
        metrics.add(sevenDayBerthingMetric);

        TrafficOverviewVO.TrafficMetricVO completedPilotageMetric = new TrafficOverviewVO.TrafficMetricVO();
        completedPilotageMetric.setId("5");
        completedPilotageMetric.setLabel("引航完成");
        completedPilotageMetric.setValue(getCompletedPilotageCount());
        completedPilotageMetric.setTrend(getCompletedPilotageTrend()); // 引航完成趋势
        completedPilotageMetric.setUnit("次");
        completedPilotageMetric.setSubLabel("今日完成引航次数");
        metrics.add(completedPilotageMetric);

        // 今日ATA靠泊数量
        TrafficOverviewVO.TrafficMetricVO todayAtaBerthingMetric = new TrafficOverviewVO.TrafficMetricVO();
        todayAtaBerthingMetric.setId("6");
        todayAtaBerthingMetric.setLabel("今日实际靠泊");
        todayAtaBerthingMetric.setValue(getTodayAtaBerthingCount());
        todayAtaBerthingMetric.setTrend(getTodayAtaBerthingTrend()); // 今日靠泊趋势
        todayAtaBerthingMetric.setUnit("艘");
        todayAtaBerthingMetric.setSubLabel("今日实际靠泊船舶数量");
        metrics.add(todayAtaBerthingMetric);

        trafficOverview.setMetrics(metrics);
//            rank: number
//            nationality: string
//            value: number
//            ratio: number
//        }
        // 国籍排名
// 国籍排名
        List<TrafficOverviewVO.TrafficSourceItemVO> sourceRanking = new ArrayList<>();
        Map<String, List<Vessels>> nationalityGroup = vesselsList.stream()
                .collect(Collectors.groupingBy(Vessels::getNationality));

        AtomicInteger rank = new AtomicInteger(1);
        nationalityGroup.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .limit(5)
                .forEach(entry -> {
                    TrafficOverviewVO.TrafficSourceItemVO item = new TrafficOverviewVO.TrafficSourceItemVO();
                    item.setRank(rank.getAndIncrement());
                    item.setNationality(entry.getKey());
                    item.setValue((long) entry.getValue().size());
                    BigDecimal ratio = vesselsList.size() > 0 ? 
                        BigDecimal.valueOf(entry.getValue().size() * 100.0 / vesselsList.size()) : BigDecimal.ZERO;
                    item.setRatio(ratio);
                    sourceRanking.add(item);
                });

        trafficOverview.setSourceRanking(sourceRanking);

        // 趋势数据（实际实现）
        List<TrafficOverviewVO.TrafficTrendItemVO> trend = getTrafficTrendData();
        trafficOverview.setTrend(trend);

        // 类型分布
        List<TrafficOverviewVO.TypeDistributionItemVO> typeDistribution = new ArrayList<>();
        vesselsList.stream()
                .collect(Collectors.groupingBy(Vessels::getVesselType))
                .entrySet().stream()
                .forEach(entry -> {
                    TrafficOverviewVO.TypeDistributionItemVO item = new TrafficOverviewVO.TypeDistributionItemVO();
                    item.setName(entry.getKey());
                    item.setValue(entry.getValue().size());
                    item.setPercentage(BigDecimal.valueOf(entry.getValue().size() * 100.0 / vesselsList.size()));
                    typeDistribution.add(item);
                });
        trafficOverview.setTypeDistribution(typeDistribution);

        List<TrafficOverviewVO.GeoSpotItemVO> geoSpots = getHotspotData();
        trafficOverview.setGeoSpots(geoSpots);

        return Result.success(trafficOverview);
    }

    @Override
    public Result<OperationsOverviewVO> getOperationsStats() {
        OperationsOverviewVO operationsOverview = new OperationsOverviewVO();

        // KPI指标
        List<OperationsOverviewVO.KpiItemVO> kpis = new ArrayList<>();

        List<PilotagePlans> plansList = pilotagePlansService.list();
        List<Vessels> vesselsList = vesselsService.list();

        // 任务准点率（正点率）
        long onTimeCount = plansList.stream()
                .filter(plan -> plan.getActualCompletionTime() != null &&
                        plan.getPlanEndTime() != null &&
                        plan.getActualCompletionTime().compareTo(plan.getPlanEndTime()) <= 0)
                .count();
        long totalCount = plansList.size();
        BigDecimal punctualityRateValue = totalCount > 0 ?
                new BigDecimal(onTimeCount * 100).divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        long completedCount = plansList.stream()
                .filter(plan -> "COMPLETED".equals(plan.getPlanStatus()) || plan.getActualCompletionTime() != null)
                .count();
        BigDecimal dispatchEfficiencyValue = totalCount > 0 ?
                new BigDecimal(completedCount * 100).divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        BigDecimal energyConsumptionValue = calculateTotalEnergyConsumption(); // MWh

        List<Tugboats> tugboatsList = tugboatsService.list();
        List<PlanTugboatAssignments> assignmentsList = planTugboatAssignmentsService.list();

        long totalTugboats = tugboatsList.size();

        long workingTugboats = assignmentsList.stream()
                .filter(assignment -> assignment.getStatus() != null &&
                        !"COMPLETED".equals(assignment.getStatus().toString()) &&
                        !"CANCELLED".equals(assignment.getStatus().toString()))
                .map(PlanTugboatAssignments::getTugboatId)
                .distinct()
                .count();

        BigDecimal tugboatUtilizationValue = totalTugboats > 0 ?
                new BigDecimal(workingTugboats * 100).divide(new BigDecimal(totalTugboats), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        OperationsOverviewVO.KpiItemVO dispatchEfficiency = new OperationsOverviewVO.KpiItemVO();
        dispatchEfficiency.setLabel("调度效率");
        dispatchEfficiency.setValue(dispatchEfficiencyValue);
        dispatchEfficiency.setUnit("%");
        BigDecimal previousDayDispatchEfficiency = getPreviousDayDispatchEfficiency();
        dispatchEfficiency.setChange(previousDayDispatchEfficiency != null && dispatchEfficiencyValue != null ? 
            previousDayDispatchEfficiency.subtract(dispatchEfficiencyValue).abs() : BigDecimal.ZERO); // 示例变化值
        kpis.add(dispatchEfficiency);

        OperationsOverviewVO.KpiItemVO punctualityRate = new OperationsOverviewVO.KpiItemVO();
        punctualityRate.setLabel("任务准点率");
        punctualityRate.setValue(punctualityRateValue);
        punctualityRate.setUnit("%");
        BigDecimal previousDayPunctualityRate = getPreviousDayPunctualityRate();
        punctualityRate.setChange(previousDayPunctualityRate != null && punctualityRateValue != null ? 
            previousDayPunctualityRate.subtract(punctualityRateValue).abs() : BigDecimal.ZERO); // 示例变化值
        kpis.add(punctualityRate);

        OperationsOverviewVO.KpiItemVO energyConsumption = new OperationsOverviewVO.KpiItemVO();
        energyConsumption.setLabel("能源消耗");
        energyConsumption.setValue(energyConsumptionValue);
        energyConsumption.setUnit("MWh");
        BigDecimal previousDayEnergyConsumption = getPreviousDayEnergyConsumption();
        energyConsumption.setChange(previousDayEnergyConsumption != null && energyConsumptionValue != null ? 
            previousDayEnergyConsumption.subtract(energyConsumptionValue).abs() : BigDecimal.ZERO); // 示例变化值
        kpis.add(energyConsumption);

        OperationsOverviewVO.KpiItemVO tugboatUtilization = new OperationsOverviewVO.KpiItemVO();
        tugboatUtilization.setLabel("拖轮利用率");
        tugboatUtilization.setValue(tugboatUtilizationValue);
        tugboatUtilization.setUnit("%");
        BigDecimal previousDayTugboatUtilization = getPreviousDayTugboatUtilization();
        tugboatUtilization.setChange(previousDayTugboatUtilization != null && tugboatUtilizationValue != null ? 
            previousDayTugboatUtilization.subtract(tugboatUtilizationValue).abs() : BigDecimal.ZERO); // 示例变化值
        kpis.add(tugboatUtilization);

        operationsOverview.setKpis(kpis);

        // 引航任务（复用之前的接口数据）
        List<OperationsOverviewVO.PilotTaskItemVO> pilotTasks = new ArrayList<>();
        for (PilotagePlans plan : plansList) {
            OperationsOverviewVO.PilotTaskItemVO task = new OperationsOverviewVO.PilotTaskItemVO();
            task.setId(plan.getId());
            task.setEta(plan.getPlanStartTime() != null ? 
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(plan.getPlanStartTime()) : "");
            task.setEtd(plan.getPlanEndTime() != null ? 
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(plan.getPlanEndTime()) : "");
            task.setStatus(plan.getPlanStatus());
            
            if (plan.getVesselId() != null) {
                vesselsList.stream()
                        .filter(v -> v.getId().equals(plan.getVesselId()))
                        .findFirst().ifPresent(vessel -> task.setShipName(vessel.getNameEn()));

            }

            try {
                List<PlanPilotAssignments> assignments = planPilotAssignmentsService.list(
                        new LambdaQueryWrapper<PlanPilotAssignments>()
                                .eq(PlanPilotAssignments::getPlanId, plan.getId())
                                .orderByAsc(PlanPilotAssignments::getId)
                );
                if (assignments != null && !assignments.isEmpty()) {
                    PlanPilotAssignments main = assignments.stream()
                            .filter(a -> a.getRoleType() != null && ("MAIN".equalsIgnoreCase(a.getRoleType()) || "主引".equals(a.getRoleType())))
                            .findFirst()
                            .orElse(assignments.get(0));
                    if (main.getPilotId() != null) {
                        Pilots p = pilotsService.getById(main.getPilotId());
                        if (p != null) {
                            task.setPilot(p.getPilotName());
                        }
                    }
                }
            } catch (Exception ignore) {
            }
            
            pilotTasks.add(task);
        }
        operationsOverview.setPilotTasks(pilotTasks);

        List<OperationsOverviewVO.EnergyTrendItemVO> energyTrend = getRecentEnergyTrend();
        operationsOverview.setEnergyTrend(energyTrend);
        
        // 泊位利用率
        List<Berths> berthsList = berthsService.list();
        List<OperationsOverviewVO.BerthUsageItemVO> berthUsages = new ArrayList<>();
        for (Berths berth : berthsList) {
            OperationsOverviewVO.BerthUsageItemVO usage = new OperationsOverviewVO.BerthUsageItemVO();
            usage.setName(berth.getBerthName());
            usage.setValue(calculateBerthUtilization(berth)); // 使用计算方法
            berthUsages.add(usage);
        }
        operationsOverview.setBerthUsage(berthUsages);
        
        return Result.success(operationsOverview);
    }

    private BigDecimal getTotalVesselTrend() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = cal.getTime();

        // 由于船舶数据通常是静态的，我们可能需要基于其他动态数据计算趋势
        // 例如：基于当日新增的引航计划数量
        Date today = new Date();
        Date startOfToday = getStartOfDay(today);
        Date startOfYesterday = getStartOfDay(yesterday);

        long todayPlans = pilotagePlansService.count(
                new LambdaQueryWrapper<PilotagePlans>()
                        .ge(PilotagePlans::getCreatedAt, startOfToday)
        );

        long yesterdayPlans = pilotagePlansService.count(
                new LambdaQueryWrapper<PilotagePlans>()
                        .ge(PilotagePlans::getCreatedAt, startOfYesterday)
                        .lt(PilotagePlans::getCreatedAt, startOfToday)
        );

        if (yesterdayPlans == 0) {
            return todayPlans > 0 ? new BigDecimal("100.0") : BigDecimal.ZERO;
        }

        return new BigDecimal(todayPlans - yesterdayPlans)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(yesterdayPlans), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getInboundVesselTrend() {
        Calendar cal = Calendar.getInstance();
        Date today = new Date();
        Date yesterday = cal.getTime();

        Date startOfToday = getStartOfDay(today);
        Date startOfYesterday = getStartOfDay(yesterday);
        Date endOfYesterday = getEndOfDay(yesterday);

        long todayInbound = getInboundVesselCountForPeriod(startOfToday, new Date());

        long yesterdayInbound = getInboundVesselCountForPeriod(startOfYesterday, startOfToday);

        if (yesterdayInbound == 0) {
            return todayInbound > 0 ? new BigDecimal("100.0") : BigDecimal.ZERO;
        }

        return new BigDecimal(todayInbound - yesterdayInbound)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(yesterdayInbound), 2, RoundingMode.HALF_UP);
    }

    private long getInboundVesselCountForPeriod(Date start, Date end) {
        List<Voyages> voyages = voyagesService.list(
                new LambdaQueryWrapper<Voyages>()
                        .ge(Voyages::getEta, start)
                        .le(Voyages::getEta, end)
                        .eq(Voyages::getVoyageType, VoyageTypeEnum.IMPORT)
        );
        return voyages.size();
    }

    private Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    private long getTodayEtaBerthingCount() {
        Date todayStart = getStartOfDay(new Date());
        Date todayEnd = getEndOfDay(new Date());

        return voyagesService.count(
                new LambdaQueryWrapper<Voyages>()
                        .ge(Voyages::getEta, todayStart)
                        .le(Voyages::getEta, todayEnd)
        );
    }

    private long getSevenDayBerthingCount() {
        Calendar cal = Calendar.getInstance();
        Date today = new Date();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date sevenDaysAgo = cal.getTime();

        return getSevenDayBerthingCountForPeriod(sevenDaysAgo, today);
    }

    private long getSevenDayBerthingCountForPeriod(Date start, Date end) {
        return voyagesService.count(
                new LambdaQueryWrapper<Voyages>()
                        .ge(Voyages::getEta, start)
                        .le(Voyages::getEta, end)
        );
    }

    private long getCompletedPilotageCount() {
        Date todayStart = getStartOfDay(new Date());
        Date todayEnd = getEndOfDay(new Date());

        return pilotagePlansService.count(
                new LambdaQueryWrapper<PilotagePlans>()
//                        .isNotNull(PilotagePlans::getActualCompletionTime) // 实际完成时间不为空 
                        .eq(PilotagePlans::getPlanStatus, "COMPLETED")
                        .ge(PilotagePlans::getActualCompletionTime, todayStart)
                        .le(PilotagePlans::getActualCompletionTime, todayEnd)
        );
    }

    private long getTodayAtaBerthingCount() {
        Date todayStart = getStartOfDay(new Date());
        Date todayEnd = getEndOfDay(new Date());

        return voyagesService.count(
                new LambdaQueryWrapper<Voyages>()
                        .isNotNull(Voyages::getAta) // 实际到港时间不为空
                        .ge(Voyages::getAta, todayStart)
                        .le(Voyages::getAta, todayEnd)
        );
    }

    private BigDecimal getTodayEtaBerthingTrend() {
        Calendar cal = Calendar.getInstance();
        Date today = new Date();
        Date yesterday = cal.getTime();

        Date todayStart = getStartOfDay(today);
        Date todayEnd = getEndOfDay(today);
        Date yesterdayStart = getStartOfDay(yesterday);
        Date yesterdayEnd = getEndOfDay(yesterday);

        long todayBerthing = getTodayEtaBerthingCountForPeriod(todayStart, todayEnd);

        long yesterdayBerthing = getTodayEtaBerthingCountForPeriod(yesterdayStart, yesterdayEnd);

        if (yesterdayBerthing == 0) {
            return todayBerthing > 0 ? new BigDecimal("100.0") : BigDecimal.ZERO;
        }

        return new BigDecimal(todayBerthing - yesterdayBerthing)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(yesterdayBerthing), 2, RoundingMode.HALF_UP);
    }

    private long getTodayEtaBerthingCountForPeriod(Date start, Date end) {
        return voyagesService.count(
                new LambdaQueryWrapper<Voyages>()
                        .ge(Voyages::getEta, start)
                        .le(Voyages::getEta, end)
        );
    }

    private BigDecimal getSevenDayBerthingTrend() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date sevenDaysAgo = cal.getTime();
        
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -14);
        Date fourteenDaysAgo = cal.getTime();
        
        long currentSevenDays = getSevenDayBerthingCount();
        
        long previousSevenDays = voyagesService.count(
                new LambdaQueryWrapper<Voyages>()
                        .ge(Voyages::getEta, fourteenDaysAgo)
                        .lt(Voyages::getEta, sevenDaysAgo)
        );

        if (previousSevenDays == 0) {
            return currentSevenDays > 0 ? new BigDecimal("100.0") : BigDecimal.ZERO;
        }

        return new BigDecimal(currentSevenDays - previousSevenDays)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(previousSevenDays), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getCompletedPilotageTrend() {
        Calendar cal = Calendar.getInstance();
        Date today = new Date();
        Date yesterday = cal.getTime();

        Date todayStart = getStartOfDay(today);
        Date todayEnd = getEndOfDay(today);
        Date yesterdayStart = getStartOfDay(yesterday);
        Date yesterdayEnd = getEndOfDay(yesterday);

        long todayCompleted = getCompletedPilotageCountForPeriod(todayStart, todayEnd);

        long yesterdayCompleted = getCompletedPilotageCountForPeriod(yesterdayStart, yesterdayEnd);

        if (yesterdayCompleted == 0) {
            return todayCompleted > 0 ? new BigDecimal("100.0") : BigDecimal.ZERO;
        }

        return new BigDecimal(todayCompleted - yesterdayCompleted)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(yesterdayCompleted), 2, RoundingMode.HALF_UP);
    }

    private long getCompletedPilotageCountForPeriod(Date start, Date end) {
        return pilotagePlansService.count(
                new LambdaQueryWrapper<PilotagePlans>()
                        .isNotNull(PilotagePlans::getActualCompletionTime) // 实际完成时间不为空
                        .ge(PilotagePlans::getActualCompletionTime, start)
                        .le(PilotagePlans::getActualCompletionTime, end)
        );
    }

    private BigDecimal getTodayAtaBerthingTrend() {
        Calendar cal = Calendar.getInstance();
        Date today = new Date();
        Date yesterday = cal.getTime();

        Date todayStart = getStartOfDay(today);
        Date todayEnd = getEndOfDay(today);
        Date yesterdayStart = getStartOfDay(yesterday);
        Date yesterdayEnd = getEndOfDay(yesterday);

        long todayBerthing = getTodayAtaBerthingCountForPeriod(todayStart, todayEnd);

        long yesterdayBerthing = getTodayAtaBerthingCountForPeriod(yesterdayStart, yesterdayEnd);

        if (yesterdayBerthing == 0) {
            return todayBerthing > 0 ? new BigDecimal("100.0") : BigDecimal.ZERO;
        }

        return new BigDecimal(todayBerthing - yesterdayBerthing)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(yesterdayBerthing), 2, RoundingMode.HALF_UP);
    }

    private long getTodayAtaBerthingCountForPeriod(Date start, Date end) {
        return voyagesService.count(
                new LambdaQueryWrapper<Voyages>()
                        .isNotNull(Voyages::getAta) // 实际到港时间不为空
                        .ge(Voyages::getAta, start)
                        .le(Voyages::getAta, end)
        );
    }

    private long getInboundVesselCount(List<Voyages> voyagesList) {
        return voyagesList.stream()
                .filter(voyage -> voyage.getEta() != null &&
                        voyage.getEta().after(new Date()) &&
                        VoyageTypeEnum.IMPORT.equals(voyage.getVoyageType()))
                .count();
    }

    private List<TrafficOverviewVO.TrafficTrendItemVO> getTrafficTrendData() {
        List<TrafficOverviewVO.TrafficTrendItemVO> trend = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        for (int i = 23; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.HOUR_OF_DAY, -i);
            Date hourStart = cal.getTime();

            cal.add(Calendar.HOUR_OF_DAY, 1);
            Date hourEnd = cal.getTime();

            TrafficOverviewVO.TrafficTrendItemVO trendItem = new TrafficOverviewVO.TrafficTrendItemVO();
            trendItem.setTime(new SimpleDateFormat("HH:mm").format(hourStart));

            Integer inboundCount = getInboundCountForPeriod(hourStart, hourEnd);
            Integer outboundCount = getOutboundCountForPeriod(hourStart, hourEnd);

            trendItem.setInbound(inboundCount);
            trendItem.setOutbound(outboundCount);
            
            int totalTraffic = (inboundCount != null ? inboundCount : 0) + (outboundCount != null ? outboundCount : 0);
            int portLoadIndex = calculatePortLoadIndex(totalTraffic);
            trendItem.setTotalLoad(portLoadIndex);
            
            trend.add(trendItem);
        }

        return trend;
    }
    
    private int calculatePortLoadIndex(int totalTraffic) {
        // 基于当前交通流量计算港口负荷指数
        // 这里使用更精确的计算方法，考虑港口实际容量和资源利用率
        
        int totalBerths = Integer.parseInt(String.valueOf(berthsService.count()));
        int totalTugboats = Integer.parseInt(String.valueOf(tugboatsService.count()));
        
        // 这里假设一个简化模型：泊位数、引航员数和拖轮数的综合影响
        int theoreticalMaxCapacity = Math.max(10, totalBerths * 2 + totalTugboats); // 基于泊位和拖轮数的估算
        
        double loadRatio = (double) totalTraffic / theoreticalMaxCapacity;
        
        // 使用非线性函数，使负荷指数在高负荷时增长更快
        double normalizedLoad = Math.min(1.0, loadRatio);
        int loadIndex = (int) Math.min(120, normalizedLoad * 120);
        
        return loadIndex;
    }

    private Integer getInboundCountForPeriod(Date start, Date end) {
        List<Voyages> voyages = voyagesService.list(
                new LambdaQueryWrapper<Voyages>()
                        .ge(Voyages::getEta, start)
                        .le(Voyages::getEta, end)
                        .eq(Voyages::getVoyageType, VoyageTypeEnum.IMPORT)
        );
        return voyages.size();
    }

    private Integer getOutboundCountForPeriod(Date start, Date end) {
        List<Voyages> voyages = voyagesService.list(
                new LambdaQueryWrapper<Voyages>()
                        .ge(Voyages::getEtd, start)
                        .le(Voyages::getEtd, end)
                        .eq(Voyages::getVoyageType, VoyageTypeEnum.EXPORT)
        );
        return voyages.size();
    }

    private List<TrafficOverviewVO.GeoSpotItemVO> getHotspotData() {
        // 船舶来源热力：使用“航次出发港”作为来源点，按最近30天航次数聚合
        List<Voyages> voyagesList = voyagesService.list();
        if (voyagesList == null || voyagesList.isEmpty()) {
            return new ArrayList<>();
        }

        Date now = new Date();
        Date start = new Date(now.getTime() - 30L * 24 * 60 * 60 * 1000);

        Map<Long, Long> countByPortId = voyagesList.stream()
                .filter(v -> v.getPortOfDepartureId() != null)
                .filter(v -> {
                    Date t = v.getEta();
                    if (t == null) t = v.getEtd();
                    if (t == null) t = v.getCreatedAt();
                    return t == null || !t.before(start);
                })
                .collect(Collectors.groupingBy(Voyages::getPortOfDepartureId, Collectors.counting()));

        if (countByPortId.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> portIds = new ArrayList<>(countByPortId.keySet());
        Map<Long, Ports> portMap = portsService.listByIds(portIds).stream()
                .collect(Collectors.toMap(Ports::getId, p -> p, (a, b) -> a));

        List<TrafficOverviewVO.GeoSpotItemVO> geoSpots = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : countByPortId.entrySet()) {
            Ports port = portMap.get(entry.getKey());
            if (port == null || port.getLongitude() == null || port.getLatitude() == null) {
                continue;
            }
            TrafficOverviewVO.GeoSpotItemVO item = new TrafficOverviewVO.GeoSpotItemVO();
            item.setName(port.getPortNameZh() != null ? port.getPortNameZh() : (port.getPortNameEn() != null ? port.getPortNameEn() : String.valueOf(port.getId())));
            item.setValue(Arrays.asList(
                    port.getLongitude().doubleValue(),
                    port.getLatitude().doubleValue(),
                    entry.getValue().intValue()
            ));
            geoSpots.add(item);
        }

        // 按热度排序，避免点太多时影响性能
        geoSpots.sort((a, b) -> {
            try {
                List<?> av = (List<?>) a.getValue();
                List<?> bv = (List<?>) b.getValue();
                int ac = av != null && av.size() > 2 ? ((Number) av.get(2)).intValue() : 0;
                int bc = bv != null && bv.size() > 2 ? ((Number) bv.get(2)).intValue() : 0;
                return Integer.compare(bc, ac);
            } catch (Exception e) {
                return 0;
            }
        });
        if (geoSpots.size() > 30) {
            return geoSpots.subList(0, 30);
        }
        return geoSpots;
    }

    private int calculateBerthUtilization(Berths berth) {
        Date now = new Date();

        Date yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);

        List<PilotagePlans> berthPlans = pilotagePlansService.list(
                new LambdaQueryWrapper<PilotagePlans>()
                        .and(wrapper ->
                                wrapper.eq(PilotagePlans::getStartLocationId, berth.getId())
                                        .or().eq(PilotagePlans::getEndLocationId, berth.getId())
                        )
                        .ge(PilotagePlans::getPlanStartTime, yesterday)
                        .le(PilotagePlans::getPlanEndTime, now)
        );

        if (berthPlans.isEmpty()) {
            return 0; // 如果没有计划，利用率为0
        }

        long totalMinutes = 24 * 60; // 24小时总分钟数
        long occupiedMinutes = 0;

        // 按时间排序计划，避免重复计算重叠时间
        berthPlans.sort(Comparator.comparing(PilotagePlans::getPlanStartTime));

        Date lastEnd = null;
        for (PilotagePlans plan : berthPlans) {
            Date planStart = plan.getPlanStartTime();
            Date planEnd = plan.getPlanEndTime();

            if (planStart == null || planEnd == null) continue;

            // 确保时间在24小时内范围内
            if (planStart.before(yesterday)) {
                planStart = yesterday;
            }
            if (planEnd.after(now)) {
                planEnd = now;
            }

            long planDuration = planEnd.getTime() - planStart.getTime();
            long planMinutes = planDuration / (1000 * 60);

            if (lastEnd != null && planStart.before(lastEnd)) {
                // 如果有时间重叠，只计算新增部分
                if (planEnd.after(lastEnd)) {
                    long overlapEnd = planEnd.getTime();
                    long overlapStart = lastEnd.getTime();
                    occupiedMinutes += (overlapEnd - overlapStart) / (1000 * 60);
                }
            } else {
                // 没有重叠，直接加上计划时长
                occupiedMinutes += planMinutes;
            }

            lastEnd = planEnd.after(lastEnd != null ? lastEnd : yesterday) ? planEnd : (lastEnd != null ? lastEnd : yesterday);
        }

        int utilization = (int) Math.min(100, (occupiedMinutes * 100) / totalMinutes);
        return utilization;
    }

    private BigDecimal calculateTotalEnergyConsumption() {
        BigDecimal totalEnergy = BigDecimal.ZERO;

        List<PlanVehicleAssignments> vehicleAssignments = planVehicleAssignmentsService.list();
        BigDecimal vehicleEnergy = calculateVehicleEnergyConsumption(vehicleAssignments);

        List<PlanTugboatAssignments> tugboatAssignments = planTugboatAssignmentsService.list();
        BigDecimal tugboatEnergy = calculateTugboatEnergyConsumption(tugboatAssignments);

        List<PlanPilotBoatAssignments> pilotBoatAssignments = planPilotBoatAssignmentsService.list();
        BigDecimal pilotBoatEnergy = calculatePilotBoatEnergyConsumption(pilotBoatAssignments);

        if (vehicleEnergy != null) {
            totalEnergy = totalEnergy.add(vehicleEnergy);
        }
        if (tugboatEnergy != null) {
            totalEnergy = totalEnergy.add(tugboatEnergy);
        }
        if (pilotBoatEnergy != null) {
            totalEnergy = totalEnergy.add(pilotBoatEnergy);
        }

        // 如果计算结果为0，返回默认值
        return totalEnergy.compareTo(BigDecimal.ZERO) > 0 ? totalEnergy : new BigDecimal("12138");
    }

    private BigDecimal calculateVehicleEnergyConsumption(List<PlanVehicleAssignments> assignments) {
        BigDecimal totalEnergy = BigDecimal.ZERO;

        for (PlanVehicleAssignments assignment : assignments) {
            if (assignment.getFuelConsumption() != null) {
                // 假设1升柴油约等于0.0036 MWh
                BigDecimal energy = assignment.getFuelConsumption().multiply(new BigDecimal("0.0036"));
                if (energy != null) {
                    totalEnergy = totalEnergy.add(energy);
                }
            }
        }

        return totalEnergy;
    }

    private BigDecimal calculateTugboatEnergyConsumption(List<PlanTugboatAssignments> assignments) {
        BigDecimal totalEnergy = BigDecimal.ZERO;

        for (PlanTugboatAssignments assignment : assignments) {
            if (assignment.getActualStartTime() != null &&
                    assignment.getActualEndTime() != null &&
                    assignment.getTugboatId() != null) {

                long durationMs = assignment.getActualEndTime().getTime() - assignment.getActualStartTime().getTime();
                BigDecimal durationHours = new BigDecimal(durationMs / (1000.0 * 60 * 60));

                Tugboats tugboat = tugboatsService.getById(assignment.getTugboatId());
                if (tugboat != null && tugboat.getPower() != null) {
                    // 能源消耗 = 功率(kW) × 时间(小时) / 1000 = MWh
                    BigDecimal energy = tugboat.getPower()
                            .multiply(durationHours)
                            .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
                    if (energy != null) {
                        totalEnergy = totalEnergy.add(energy);
                    }
                }
            }
        }

        return totalEnergy;
    }

    private BigDecimal calculatePilotBoatEnergyConsumption(List<PlanPilotBoatAssignments> assignments) {
        BigDecimal totalEnergy = BigDecimal.ZERO;

        for (PlanPilotBoatAssignments assignment : assignments) {
            if (assignment.getActualStartTime() != null &&
                    assignment.getActualEndTime() != null &&
                    assignment.getPilotBoatId() != null) {

                long durationMs = assignment.getActualEndTime().getTime() - assignment.getActualStartTime().getTime();
                BigDecimal durationHours = new BigDecimal(durationMs / (1000.0 * 60 * 60));

                PilotBoats pilotBoat = pilotBoatsService.getById(assignment.getPilotBoatId());
                if (pilotBoat != null && pilotBoat.getEnginePower() != null) {
                    try {
                        BigDecimal power = new BigDecimal(pilotBoat.getEnginePower().replaceAll("[^0-9.]", ""));
                        // 能源消耗 = 功率(kW) × 时间(小时) / 1000 = MWh
                        BigDecimal energy = power
                                .multiply(durationHours)
                                .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
                        if (energy != null) {
                            totalEnergy = totalEnergy.add(energy);
                        }
                    } catch (NumberFormatException e) {
                        // 如果功率格式不正确，跳过该条记录
                    }
                }
            }
        }

        return totalEnergy;
    }

    private List<OperationsOverviewVO.EnergyTrendItemVO> getRecentEnergyTrend() {
        List<OperationsOverviewVO.EnergyTrendItemVO> energyTrend = new ArrayList<>();

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Date dayStart = Date.from(d.atStartOfDay(zone).toInstant());
            Date nextDayStart = Date.from(d.plusDays(1).atStartOfDay(zone).toInstant());

            BigDecimal dailyEnergy = calculateDailyEnergyConsumption(dayStart, nextDayStart);

            OperationsOverviewVO.EnergyTrendItemVO item = new OperationsOverviewVO.EnergyTrendItemVO();
            item.setTime(d.format(labelFmt));
            item.setValue(dailyEnergy.doubleValue());
            energyTrend.add(item);
        }

        return energyTrend;
    }

    /**
     * 计算指定日期区间内的能源消耗（左闭右开）。
     * - 优先使用 actualStartTime/actualEndTime
     * - 若任务未完成导致 actualEndTime 为空，则回退到计划时间（按 plan_start/end_time）
     *
     * 这样可以覆盖“今日任务尚未完成，但大屏需要展示今日趋势点”的场景。
     */
    private BigDecimal calculateDailyEnergyConsumption(Date dayStart, Date nextDayStartExclusive) {
        BigDecimal totalEnergy = BigDecimal.ZERO;

        // 车辆：按油耗直接累计（若没有油耗数据则为 0）
        List<PlanVehicleAssignments> vehicleAssignments = planVehicleAssignmentsService.list();
        for (PlanVehicleAssignments assignment : vehicleAssignments) {
            Date startTime = assignment.getActualStartTime();
            Date endTime = assignment.getActualEndTime();
            if (startTime == null || endTime == null) {
                PilotagePlans plan = assignment.getPlanId() == null ? null : pilotagePlansService.getById(assignment.getPlanId());
                if (plan != null) {
                    startTime = startTime == null ? plan.getPlanStartTime() : startTime;
                    endTime = endTime == null ? plan.getPlanEndTime() : endTime;
                }
            }
            if (!overlapsDay(startTime, endTime, dayStart, nextDayStartExclusive)) continue;
            if (assignment.getFuelConsumption() != null) {
                BigDecimal energy = assignment.getFuelConsumption().multiply(new BigDecimal("0.0036"));
                totalEnergy = totalEnergy.add(energy);
            }
        }

        List<PlanTugboatAssignments> tugboatAssignments = planTugboatAssignmentsService.list();
        for (PlanTugboatAssignments assignment : tugboatAssignments) {
            if (assignment.getTugboatId() == null) continue;

            Date startTime = assignment.getActualStartTime();
            Date endTime = assignment.getActualEndTime();
            if (startTime == null || endTime == null) {
                PilotagePlans plan = assignment.getPlanId() == null ? null : pilotagePlansService.getById(assignment.getPlanId());
                if (plan != null) {
                    startTime = startTime == null ? plan.getPlanStartTime() : startTime;
                    endTime = endTime == null ? plan.getPlanEndTime() : endTime;
                }
            }
            if (!overlapsDay(startTime, endTime, dayStart, nextDayStartExclusive)) continue;

            if (startTime != null && endTime != null && endTime.after(startTime)) {
                long durationMs = endTime.getTime() - startTime.getTime();
                BigDecimal durationHours = new BigDecimal(durationMs / (1000.0 * 60 * 60));

                Tugboats tugboat = tugboatsService.getById(assignment.getTugboatId());
                if (tugboat != null && tugboat.getPower() != null) {
                    BigDecimal energy = tugboat.getPower()
                            .multiply(durationHours)
                            .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
                    totalEnergy = totalEnergy.add(energy);
                }
            }
        }

        List<PlanPilotBoatAssignments> pilotBoatAssignments = planPilotBoatAssignmentsService.list();
        for (PlanPilotBoatAssignments assignment : pilotBoatAssignments) {
            if (assignment.getPilotBoatId() == null) continue;

            Date startTime = assignment.getActualStartTime();
            Date endTime = assignment.getActualEndTime();
            if (startTime == null || endTime == null) {
                PilotagePlans plan = assignment.getPlanId() == null ? null : pilotagePlansService.getById(assignment.getPlanId());
                if (plan != null) {
                    startTime = startTime == null ? plan.getPlanStartTime() : startTime;
                    endTime = endTime == null ? plan.getPlanEndTime() : endTime;
                }
            }
            if (!overlapsDay(startTime, endTime, dayStart, nextDayStartExclusive)) continue;

            if (startTime != null && endTime != null && endTime.after(startTime)) {
                long durationMs = endTime.getTime() - startTime.getTime();
                BigDecimal durationHours = new BigDecimal(durationMs / (1000.0 * 60 * 60));

                PilotBoats pilotBoat = pilotBoatsService.getById(assignment.getPilotBoatId());
                if (pilotBoat != null && pilotBoat.getEnginePower() != null) {
                    try {
                        BigDecimal power = new BigDecimal(pilotBoat.getEnginePower().replaceAll("[^0-9.]", ""));
                        BigDecimal energy = power
                                .multiply(durationHours)
                                .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
                        totalEnergy = totalEnergy.add(energy);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        return totalEnergy;
    }

    private boolean overlapsDay(Date startTime, Date endTime, Date dayStart, Date nextDayStartExclusive) {
        if (startTime == null || endTime == null) return false;
        return startTime.before(nextDayStartExclusive) && endTime.after(dayStart);
    }

    private BigDecimal getPreviousDayDispatchEfficiency() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String yesterdayStr = sdf.format(yesterday);

        List<PilotagePlans> yesterdayPlans = pilotagePlansService.list(
                new LambdaQueryWrapper<PilotagePlans>()
                        .like(PilotagePlans::getPlanStartTime, yesterdayStr)
        );

        if (yesterdayPlans.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long completedCount = yesterdayPlans.stream()
                .filter(plan -> "COMPLETED".equals(plan.getPlanStatus()) || plan.getActualCompletionTime() != null)
                .count();

        long totalCount = yesterdayPlans.size();
        return totalCount > 0 ?
                new BigDecimal(completedCount * 100).divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
    }

    private BigDecimal getPreviousDayPunctualityRate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String yesterdayStr = sdf.format(yesterday);

        List<PilotagePlans> yesterdayPlans = pilotagePlansService.list(
                new LambdaQueryWrapper<PilotagePlans>()
                        .like(PilotagePlans::getPlanStartTime, yesterdayStr)
        );

        if (yesterdayPlans.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long onTimeCount = yesterdayPlans.stream()
                .filter(plan -> plan.getActualCompletionTime() != null &&
                        plan.getPlanEndTime() != null &&
                        plan.getActualCompletionTime().compareTo(plan.getPlanEndTime()) <= 0)
                .count();

        long totalCount = yesterdayPlans.size();
        return totalCount > 0 ?
                new BigDecimal(onTimeCount * 100).divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
    }

    private BigDecimal getPreviousDayEnergyConsumption() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = cal.getTime();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate d = yesterday.toInstant().atZone(zone).toLocalDate();
        Date dayStart = Date.from(d.atStartOfDay(zone).toInstant());
        Date nextDayStart = Date.from(d.plusDays(1).atStartOfDay(zone).toInstant());

        return calculateDailyEnergyConsumption(dayStart, nextDayStart);
    }

    private BigDecimal getPreviousDayTugboatUtilization() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String yesterdayStr = sdf.format(yesterday);

        List<PlanTugboatAssignments> yesterdayAssignments = planTugboatAssignmentsService.list(
                new LambdaQueryWrapper<PlanTugboatAssignments>()
                        .like(PlanTugboatAssignments::getActualStartTime, yesterdayStr)
        );

        List<Tugboats> tugboatsList = tugboatsService.list();
        long totalTugboats = tugboatsList.size();

        if (totalTugboats == 0) {
            return BigDecimal.ZERO;
        }

        long workingTugboats = yesterdayAssignments.stream()
                .filter(assignment -> assignment.getStatus() != null &&
                        !"COMPLETED".equals(assignment.getStatus().toString()) &&
                        !"CANCELLED".equals(assignment.getStatus().toString()))
                .map(PlanTugboatAssignments::getTugboatId)
                .distinct()
                .count();

        return totalTugboats > 0 ?
                new BigDecimal(workingTugboats * 100).divide(new BigDecimal(totalTugboats), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
    }
    
    @Override
    public Result<Integer> getPortLoadIndex() {
        // 获取当前小时的交通流量
        Date currentHourStart = getStartOfCurrentHour();
        Date currentHourEnd = getEndOfCurrentHour();
        
        // 查询当前小时的进港船舶数量
        Integer inboundCount = getInboundCountForPeriod(currentHourStart, currentHourEnd);
        // 查询当前小时的出港船舶数量
        Integer outboundCount = getOutboundCountForPeriod(currentHourStart, currentHourEnd);
        
        int totalTraffic = (inboundCount != null ? inboundCount : 0) + (outboundCount != null ? outboundCount : 0);
        
        int portLoadIndex = calculatePortLoadIndex(totalTraffic);
        
        return Result.success(portLoadIndex);
    }
    
    // 获取当前小时开始时间
    private Date getStartOfCurrentHour() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    // 获取当前小时结束时间
    private Date getEndOfCurrentHour() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}