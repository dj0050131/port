package syy.portPilotage.dto.statistics;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class NewWharfDTO {
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
     * 代理
     */
    private String agent;

    /**
     * 备注
     */
    private String remarks;

}