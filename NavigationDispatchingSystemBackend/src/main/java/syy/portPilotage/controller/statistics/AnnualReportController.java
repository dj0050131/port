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
import syy.portPilotage.service.BillingVouchersService;
import syy.portPilotage.dto.statistics.AnnualReportDTO;
import syy.portPilotage.dto.statistics.MonthlyStatisticsDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/statistics/annualReport")
@RequiredArgsConstructor
@Tag(name = "费收年报表统计", description = "费收年报表统计相关接口")
public class AnnualReportController {

    @Autowired
    private BillingVouchersService billingVouchersService;

    @GetMapping
    @Operation(summary = "获取费收年报表统计数据")
    public Result<AnnualReportDTO> getAnnualReport(
            @RequestParam int year) {
        log.info("获取费收年报表统计数据: year={}", year);
        
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        LocalDate endExclusive = endDate.plusDays(1);
        
        List<BillingVouchers> vouchers = billingVouchersService.listApprovedVouchersByDateRange(
                startDate.atStartOfDay(), 
                endExclusive.atStartOfDay());
        
        // 按月份分组
        Map<Integer, List<BillingVouchers>> vouchersByMonth = vouchers.stream()
                .filter(v -> v.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        bv -> bv.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue()
                ));
        
        List<MonthlyStatisticsDTO> monthlyStats = new ArrayList<>();
        BigDecimal annualTotalAmount = BigDecimal.ZERO;
        BigDecimal annualActualReceivedAmount = BigDecimal.ZERO;
        BigDecimal annualArrears = BigDecimal.ZERO;
        
        for (int month = 1; month <= 12; month++) {
            List<BillingVouchers> monthVouchers = vouchersByMonth.getOrDefault(month, new ArrayList<>());
            
            MonthlyStatisticsDTO monthlyStat = new MonthlyStatisticsDTO();
            monthlyStat.setMonth(month);
            
            monthlyStat.setBillingVoucherCount((long) monthVouchers.size());
            
            BigDecimal monthTotalAmount = monthVouchers.stream()
                    .map(BillingVouchers::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal monthActualReceivedAmount = monthVouchers.stream()
                    .map(BillingVouchers::getActualReceivedAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            monthlyStat.setTotalAmount(monthTotalAmount);
            monthlyStat.setActualReceivedAmount(monthActualReceivedAmount);
            
            annualTotalAmount = annualTotalAmount.add(monthTotalAmount);
            annualActualReceivedAmount = annualActualReceivedAmount.add(monthActualReceivedAmount);
            
            monthlyStat.setCumulativeTotalAmount(annualTotalAmount);
            monthlyStat.setCumulativeActualReceivedAmount(annualActualReceivedAmount);
            
            BigDecimal monthArrears = monthVouchers.stream()
                    .map(voucher -> {
                        if (voucher.getTotalAmount() != null && voucher.getActualReceivedAmount() != null) {
                            return voucher.getTotalAmount().subtract(voucher.getActualReceivedAmount());
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            annualArrears = annualArrears.add(monthArrears);
            monthlyStat.setArrears(monthArrears);
            monthlyStat.setCumulativeArrears(annualArrears);
            
            monthlyStats.add(monthlyStat);
        }
        
        AnnualReportDTO report = new AnnualReportDTO();
        report.setYear(year);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setMonthlyStatistics(monthlyStats);
        
        // 年度总计
        report.setAnnualTotalAmount(annualTotalAmount);
        report.setAnnualActualReceivedAmount(annualActualReceivedAmount);
        report.setAnnualArrears(annualArrears);
        
        log.info("费收年报表统计数据获取成功: year={}", year);
        return Result.success(report);
    }
}