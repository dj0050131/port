package syy.portPilotage.dto.statistics;

import syy.portPilotage.dto.statistics.MonthlyStatisticsDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AnnualReportDTO {
    /**
     * 年份
     */
    private Integer year;
    
        private LocalDate startDate;
    
        private LocalDate endDate;
    
        private List<MonthlyStatisticsDTO> monthlyStatistics;
    
    /**
     * 年度应收总额
     */
    private BigDecimal annualTotalAmount;
    
    /**
     * 年度实收总额
     */
    private BigDecimal annualActualReceivedAmount;
    
    /**
     * 年度欠款总额
     */
    private BigDecimal annualArrears;

    // Getters and Setters
    
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<MonthlyStatisticsDTO> getMonthlyStatistics() {
        return monthlyStatistics;
    }

    public void setMonthlyStatistics(List<MonthlyStatisticsDTO> monthlyStatistics) {
        this.monthlyStatistics = monthlyStatistics;
    }

    public BigDecimal getAnnualTotalAmount() {
        return annualTotalAmount;
    }

    public void setAnnualTotalAmount(BigDecimal annualTotalAmount) {
        this.annualTotalAmount = annualTotalAmount;
    }

    public BigDecimal getAnnualActualReceivedAmount() {
        return annualActualReceivedAmount;
    }

    public void setAnnualActualReceivedAmount(BigDecimal annualActualReceivedAmount) {
        this.annualActualReceivedAmount = annualActualReceivedAmount;
    }

    public BigDecimal getAnnualArrears() {
        return annualArrears;
    }

    public void setAnnualArrears(BigDecimal annualArrears) {
        this.annualArrears = annualArrears;
    }
}