package syy.portPilotage.dto.statistics;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecordBreakDTO {
    /**
     * 说明
     */
    private String desc;

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
     * 船长
     */
    private BigDecimal length;

    /**
     * 吃水
     */
    private BigDecimal draft;

    /**
     * 泊位
     */
    private String berth;

    /**
     * 主引
     */
    private String pilotName;

}