package syy.portPilotage.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "会计年度管理表")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FiscalYears implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会计年度（如 2026）")
    private String year;

    @Schema(description = "年度指标(吨)")
    private BigDecimal targetTonnage;

    @Schema(description = "年度力争指标(元)")
    private BigDecimal targetRevenue;

    @Schema(description = "预估收入(元)")
    private BigDecimal estimatedRevenue;

    @Schema(description = "年度已完成吨位(自动汇总)")
    private BigDecimal actualTonnage;

    @Schema(description = "年度已完成收入(自动汇总)")
    private BigDecimal actualRevenue;

    @Schema(description = "年度收入预测(自动汇总)")
    private BigDecimal forecastRevenue;

    @Schema(description = "最后计算时间(自动)")
    private Date lastCalculatedAt;

    @Schema(description = "启用状态(1启用/0停用)")
    private Boolean status;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "更新时间")
    private Date updatedAt;
}
