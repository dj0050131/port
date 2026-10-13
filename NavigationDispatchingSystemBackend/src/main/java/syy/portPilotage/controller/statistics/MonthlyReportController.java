package syy.portPilotage.controller.statistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.Result;
import syy.portPilotage.entity.BillingVouchers;
import syy.portPilotage.entity.Vessels;
import syy.portPilotage.service.BillingVouchersService;
import syy.portPilotage.service.VesselsService;
import syy.portPilotage.dto.statistics.MonthlyReportDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/statistics/monthlyReport")
@RequiredArgsConstructor
@Tag(name = "费收月报表统计", description = "费收月报表统计相关接口")
public class MonthlyReportController {

    @Autowired
    private BillingVouchersService billingVouchersService;
    
    @Autowired
    private VesselsService vesselsService;

    @GetMapping
    @Operation(summary = "获取费收月报表统计数据")
    public Result<MonthlyReportDTO> getMonthlyReport(
            @RequestParam String yearMonth) { // 格式: YYYY-MM
        log.info("获取费收月报表统计数据: yearMonth={}", yearMonth);
        
        LocalDate startDate = LocalDate.parse(yearMonth + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        // endExclusive：结束日的下一天 00:00:00，保证整个月都统计到
        LocalDate endExclusive = endDate.plusDays(1);
        
        List<BillingVouchers> vouchers = billingVouchersService.listApprovedVouchersByDateRange(
                startDate.atStartOfDay(), 
                endExclusive.atStartOfDay());
        
        MonthlyReportDTO report = new MonthlyReportDTO();
        report.setYear(startDate.getYear());
        report.setMonth(startDate.getMonthValue());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        
        report.setBillingVoucherCount((long) vouchers.size());
        report.setVesselCount(vouchers.stream().map(BillingVouchers::getVesselId).filter(Objects::nonNull).distinct().count());
        report.setAgentCount(vouchers.stream().map(BillingVouchers::getAgentId).filter(Objects::nonNull).distinct().count());
        
        report.setTotalAmount(vouchers.stream()
                .map(BillingVouchers::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        report.setActualReceivedAmount(vouchers.stream()
                .map(BillingVouchers::getActualReceivedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        report.setDiscountAmount(vouchers.stream()
                .map(BillingVouchers::getDiscountAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        report.setLateFee(vouchers.stream()
                .map(BillingVouchers::getLateFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        Map<String, Long> paymentStatusCount = vouchers.stream()
                .collect(Collectors.groupingBy(
                        bv -> bv.getPaymentStatus() != null ? bv.getPaymentStatus().toString() : "UNKNOWN",
                        Collectors.counting()
                ));
        
        report.setUnpaidCount(paymentStatusCount.getOrDefault("UNPAID", 0L));
        report.setPartialPaidCount(paymentStatusCount.getOrDefault("PARTIAL", 0L));
        report.setFullyPaidCount(paymentStatusCount.getOrDefault("FULLY_PAID", 0L));
        
        BigDecimal internationalAmount = BigDecimal.ZERO;
        BigDecimal domesticAmount = BigDecimal.ZERO;
        
        List<Long> vesselIds = vouchers.stream()
                .map(BillingVouchers::getVesselId)
                .distinct()
                .filter(id -> id != null)
                .collect(Collectors.toList());
        
        if (!vesselIds.isEmpty()) {
            List<Vessels> vesselsList = vesselsService.listByIds(vesselIds);
            Map<Long, Vessels> vesselMap = vesselsList.stream()
                    .collect(Collectors.toMap(Vessels::getId, v -> v));
            
            for (BillingVouchers voucher : vouchers) {
                Vessels vessel = vesselMap.get(voucher.getVesselId());
                if (vessel != null && voucher.getTotalAmount() != null) {
                    if ("INTERNATIONAL".equals(vessel.getRouteType())) {
                        internationalAmount = internationalAmount.add(voucher.getTotalAmount());
                    } else if ("DOMESTIC".equals(vessel.getRouteType())) {
                        domesticAmount = domesticAmount.add(voucher.getTotalAmount());
                    }
                }
            }
        }
        
        report.setInternationalRouteAmount(internationalAmount);
        report.setDomesticRouteAmount(domesticAmount);
        
        log.info("费收月报表统计数据获取成功: year={}, month={}", report.getYear(), report.getMonth());
        return Result.success(report);
    }
}