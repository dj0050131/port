package syy.portPilotage.dto.statistics;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PilotageMonthlyDTO {
    /**
     * 中文船名
     */
    private String shipName;
    
    /**
     * 英文船名
     */
    private String englishName;
    
    /**
     * 国籍
     */
    private String nationality;
    
    /**
     * 航线类型
     */
    private String routeType;
    
    /**
     * 动态
     */
    private String dynamic;
    
        private String startTime;
    
        private String endTime;
    
    /**
     * 总吨
     */
    private BigDecimal grossTonnage;
    
    /**
     * 净吨
     */
    private BigDecimal netTonnage;
    
    /**
     * 载重吨
     */
    private BigDecimal deadWeight;
    
    /**
     * 船长
     */
    private BigDecimal length;
    
    /**
     * 船宽
     */
    private BigDecimal width;
    
    /**
     * 吃水
     */
    private BigDecimal draft;
    
    /**
     * 泊位
     */
    private String berth;
    
    /**
     * 登离时间
     */
    private String boardingTime;
    
    /**
     * 登离地点
     */
    private String boardingPlace;
    
    /**
     * 拖轮
     */
    private String tug;
    
    /**
     * 主引
     */
    private List<String> pilotName;

    
    /**
     * 编号
     */
    private String no;
    
    /**
     * 代理
     */
    private String agent;
    
    /**
     * 接送时间
     */
    private String pickDropTime;
    
    /**
     * 净高
     */
    private BigDecimal height;
    
    /**
     * 外贸费
     */
    private BigDecimal foreignFee;
    
    /**
     * 备注
     */
    private String remarks;

}