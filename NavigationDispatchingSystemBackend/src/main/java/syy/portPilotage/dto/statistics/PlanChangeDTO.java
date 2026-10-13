package syy.portPilotage.dto.statistics;

import java.math.BigDecimal;

public class PlanChangeDTO {
    /**
     * 代理
     */
    private String agent;
    
    /**
     * 中文船名
     */
    private String shipName;
    
    /**
     * 英文船名
     */
    private String englishName;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 原因
     */
    private String reason;
    
    /**
     * 国籍
     */
    private String nationality;
    
    /**
     * 动态
     */
    private String dynamic;
    
    /**
     * 时间/日期
     */
    private String timeDate;
    
    /**
     * 总吨
     */
    private BigDecimal grossTonnage;
    
    /**
     * 主引
     */
    private String mainPilot;
    
    /**
     * 备注
     */
    private String remarks;

    // Getters and Setters
    
    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getDynamic() {
        return dynamic;
    }

    public void setDynamic(String dynamic) {
        this.dynamic = dynamic;
    }

    public String getTimeDate() {
        return timeDate;
    }

    public void setTimeDate(String timeDate) {
        this.timeDate = timeDate;
    }

    public BigDecimal getGrossTonnage() {
        return grossTonnage;
    }

    public void setGrossTonnage(BigDecimal grossTonnage) {
        this.grossTonnage = grossTonnage;
    }

    public String getMainPilot() {
        return mainPilot;
    }

    public void setMainPilot(String mainPilot) {
        this.mainPilot = mainPilot;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}