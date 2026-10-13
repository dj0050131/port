package syy.portPilotage.dto.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MonthlyReportDTO {
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 月份
     */
    private Integer month;
    
        private LocalDate startDate;
    
        private LocalDate endDate;
    
    /**
     * 计费凭证数
     */
    private Long billingVoucherCount;
    
    /**
     * 船舶数
     */
    private Long vesselCount;
    
    /**
     * 代理公司数
     */
    private Long agentCount;
    
    /**
     * 应收总额
     */
    private BigDecimal totalAmount;
    
    /**
     * 实收总额
     */
    private BigDecimal actualReceivedAmount;
    
    /**
     * 折扣总额
     */
    private BigDecimal discountAmount;
    
    /**
     * 滞纳金总额
     */
    private BigDecimal lateFee;
    
    /**
     * 未付款单数
     */
    private Long unpaidCount;
    
    /**
     * 部分付款单数
     */
    private Long partialPaidCount;
    
    /**
     * 已付款单数
     */
    private Long fullyPaidCount;
    
    /**
     * 国际航线金额
     */
    private BigDecimal internationalRouteAmount;
    
    /**
     * 国内航线金额
     */
    private BigDecimal domesticRouteAmount;

    // Getters and Setters
    
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
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

    public Long getBillingVoucherCount() {
        return billingVoucherCount;
    }

    public void setBillingVoucherCount(Long billingVoucherCount) {
        this.billingVoucherCount = billingVoucherCount;
    }

    public Long getVesselCount() {
        return vesselCount;
    }

    public void setVesselCount(Long vesselCount) {
        this.vesselCount = vesselCount;
    }

    public Long getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(Long agentCount) {
        this.agentCount = agentCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getActualReceivedAmount() {
        return actualReceivedAmount;
    }

    public void setActualReceivedAmount(BigDecimal actualReceivedAmount) {
        this.actualReceivedAmount = actualReceivedAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public void setLateFee(BigDecimal lateFee) {
        this.lateFee = lateFee;
    }

    public Long getUnpaidCount() {
        return unpaidCount;
    }

    public void setUnpaidCount(Long unpaidCount) {
        this.unpaidCount = unpaidCount;
    }

    public Long getPartialPaidCount() {
        return partialPaidCount;
    }

    public void setPartialPaidCount(Long partialPaidCount) {
        this.partialPaidCount = partialPaidCount;
    }

    public Long getFullyPaidCount() {
        return fullyPaidCount;
    }

    public void setFullyPaidCount(Long fullyPaidCount) {
        this.fullyPaidCount = fullyPaidCount;
    }

    public BigDecimal getInternationalRouteAmount() {
        return internationalRouteAmount;
    }

    public void setInternationalRouteAmount(BigDecimal internationalRouteAmount) {
        this.internationalRouteAmount = internationalRouteAmount;
    }

    public BigDecimal getDomesticRouteAmount() {
        return domesticRouteAmount;
    }

    public void setDomesticRouteAmount(BigDecimal domesticRouteAmount) {
        this.domesticRouteAmount = domesticRouteAmount;
    }
}