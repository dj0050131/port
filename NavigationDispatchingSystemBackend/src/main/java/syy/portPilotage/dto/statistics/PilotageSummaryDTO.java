package syy.portPilotage.dto.statistics;

import java.math.BigDecimal;

public class PilotageSummaryDTO {
    /**
     * 月份
     */
    private Integer month;
    
    /**
     * 外轮
     */
    private Long foreign;
    
    /**
     * 国轮
     */
    private Long domestic;
    
    /**
     * >250m
     */
    private Long gt250;
    
    /**
     * 200m-250m
     */
    private Long gt200_250;
    
    /**
     * 净吨
     */
    private BigDecimal netTonnage;
    
    /**
     * 总吨
     */
    private BigDecimal grossTonnage;
    
    /**
     * 载重吨
     */
    private BigDecimal deadWeight;
    
    /**
     * 危险品船
     */
    private Long dangerous;
    
    /**
     * 集装箱船
     */
    private Long container;
    
    /**
     * 夜航
     */
    private Long night;
    
    /**
     * 休息日
     */
    private Long restDay;
    
    /**
     * 法定节假日
     */
    private Long holiday;
    
    /**
     * 引航距离
     */
    private BigDecimal distance;
    
    /**
     * 引航时间
     */
    private BigDecimal time;
    
    /**
     * 备注
     */
    private String remarks;

    // Getters and Setters
    
    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Long getForeign() {
        return foreign;
    }

    public void setForeign(Long foreign) {
        this.foreign = foreign;
    }

    public Long getDomestic() {
        return domestic;
    }

    public void setDomestic(Long domestic) {
        this.domestic = domestic;
    }

    public Long getGt250() {
        return gt250;
    }

    public void setGt250(Long gt250) {
        this.gt250 = gt250;
    }

    public Long getGt200_250() {
        return gt200_250;
    }

    public void setGt200_250(Long gt200_250) {
        this.gt200_250 = gt200_250;
    }

    public BigDecimal getNetTonnage() {
        return netTonnage;
    }

    public void setNetTonnage(BigDecimal netTonnage) {
        this.netTonnage = netTonnage;
    }

    public BigDecimal getGrossTonnage() {
        return grossTonnage;
    }

    public void setGrossTonnage(BigDecimal grossTonnage) {
        this.grossTonnage = grossTonnage;
    }

    public BigDecimal getDeadWeight() {
        return deadWeight;
    }

    public void setDeadWeight(BigDecimal deadWeight) {
        this.deadWeight = deadWeight;
    }

    public Long getDangerous() {
        return dangerous;
    }

    public void setDangerous(Long dangerous) {
        this.dangerous = dangerous;
    }

    public Long getContainer() {
        return container;
    }

    public void setContainer(Long container) {
        this.container = container;
    }

    public Long getNight() {
        return night;
    }

    public void setNight(Long night) {
        this.night = night;
    }

    public Long getRestDay() {
        return restDay;
    }

    public void setRestDay(Long restDay) {
        this.restDay = restDay;
    }

    public Long getHoliday() {
        return holiday;
    }

    public void setHoliday(Long holiday) {
        this.holiday = holiday;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public BigDecimal getTime() {
        return time;
    }

    public void setTime(BigDecimal time) {
        this.time = time;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}