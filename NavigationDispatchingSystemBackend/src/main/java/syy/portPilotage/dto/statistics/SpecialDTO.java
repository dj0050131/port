package syy.portPilotage.dto.statistics;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpecialDTO {
    /**
     * 中文船名
     */
    private String shipName;
    
    /**
     * 英文船名
     */
    private String englishName;
    
    /**
     * 特殊作业
     */
    private String specialOp;
    
    /**
     * 特殊说明
     */
    private String specialDesc;
    
    /**
     * 国籍
     */
    private String nationality;
    
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
     * 主引
     */
    private String pilotName;
    
    /**
     * 外贸费
     */
    private BigDecimal foreignFee;
    
    /**
     * 备注
     */
    private String remarks;

}