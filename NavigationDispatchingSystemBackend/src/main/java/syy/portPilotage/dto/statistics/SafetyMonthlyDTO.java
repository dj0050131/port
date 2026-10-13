package syy.portPilotage.dto.statistics;

public class SafetyMonthlyDTO {
    /**
     * 船名
     */
    private String shipName;
    
    /**
     * 日期
     */
    private String date;
    
    /**
     * 动态时间
     */
    private String dynamicTime;
    
    /**
     * 开航时间
     */
    private String sailTime;
    
    /**
     * 停靠时间
     */
    private String berthTime;
    
    /**
     * 主引
     */
    private String pilot;
    
    /**
     * 被引船舶
     */
    private String towedShip;
    
    /**
     * 动态
     */
    private String dynamic;
    
    /**
     * 等待时间
     */
    private String waitTime;

    // Getters and Setters
    
    public String getShipName() {
        return shipName;
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDynamicTime() {
        return dynamicTime;
    }

    public void setDynamicTime(String dynamicTime) {
        this.dynamicTime = dynamicTime;
    }

    public String getSailTime() {
        return sailTime;
    }

    public void setSailTime(String sailTime) {
        this.sailTime = sailTime;
    }

    public String getBerthTime() {
        return berthTime;
    }

    public void setBerthTime(String berthTime) {
        this.berthTime = berthTime;
    }

    public String getPilot() {
        return pilot;
    }

    public void setPilot(String pilot) {
        this.pilot = pilot;
    }

    public String getTowedShip() {
        return towedShip;
    }

    public void setTowedShip(String towedShip) {
        this.towedShip = towedShip;
    }

    public String getDynamic() {
        return dynamic;
    }

    public void setDynamic(String dynamic) {
        this.dynamic = dynamic;
    }

    public String getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(String waitTime) {
        this.waitTime = waitTime;
    }
}