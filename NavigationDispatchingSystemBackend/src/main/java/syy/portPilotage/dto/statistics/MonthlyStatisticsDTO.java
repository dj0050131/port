package syy.portPilotage.dto.statistics;

import java.math.BigDecimal;

public class MonthlyStatisticsDTO {
    /**
     * 月份
     */
    private Integer month;
    
    /**
     * 计费凭证数
     */
    private Long billingVoucherCount;
    
    /**
     * 月度应收总额
     */
    private BigDecimal totalAmount;
    
    /**
     * 月度实收总额
     */
    private BigDecimal actualReceivedAmount;
    
    /**
     * 月度欠款额
     */
    private BigDecimal arrears;
    
    /**
     * 年度累计应收
     */
    private BigDecimal cumulativeTotalAmount;
    
    /**
     * 年度累计实收
     */
    private BigDecimal cumulativeActualReceivedAmount;
    
    /**
     * 年度累计欠款
     */
    private BigDecimal cumulativeArrears;

    // Getters and Setters
    
    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Long getBillingVoucherCount() {
        return billingVoucherCount;
    }

    public void setBillingVoucherCount(Long billingVoucherCount) {
        this.billingVoucherCount = billingVoucherCount;
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

    public BigDecimal getArrears() {
        return arrears;
    }

    public void setArrears(BigDecimal arrears) {
        this.arrears = arrears;
    }

    public BigDecimal getCumulativeTotalAmount() {
        return cumulativeTotalAmount;
    }

    public void setCumulativeTotalAmount(BigDecimal cumulativeTotalAmount) {
        this.cumulativeTotalAmount = cumulativeTotalAmount;
    }

    public BigDecimal getCumulativeActualReceivedAmount() {
        return cumulativeActualReceivedAmount;
    }

    public void setCumulativeActualReceivedAmount(BigDecimal cumulativeActualReceivedAmount) {
        this.cumulativeActualReceivedAmount = cumulativeActualReceivedAmount;
    }

    public BigDecimal getCumulativeArrears() {
        return cumulativeArrears;
    }

    public void setCumulativeArrears(BigDecimal cumulativeArrears) {
        this.cumulativeArrears = cumulativeArrears;
    }
}