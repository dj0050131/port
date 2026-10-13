package syy.portPilotage.dto.certificate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "创建计费凭证请求参数")
public class VoucherCreateDTO {

    @Schema(description = "签证单ID")
    @NotNull(message = "签证单ID不能为空")
    private Long certificateId;

    @Schema(description = "凭证编号（自动生成时可空）")
    private String voucherCode;

    @Schema(description = "计费方式")
    @NotNull(message = "计费方式不能为空")
    private String billingMethod;

    @Schema(description = "航线类型")
    private String routeType;

    @Schema(description = "标准费率二")
    private BigDecimal standardRate2;

    @Schema(description = "标准费率三")
    private BigDecimal standardRate3;

    @Schema(description = "实际标准费率二")
    private BigDecimal actualStandardRate2;

    @Schema(description = "实际标准费率三")
    private BigDecimal actualStandardRate3;

    @Schema(description = "开始引航时间")
    private Date pilotageStartTime;

    @Schema(description = "结束引航时间")
    private Date pilotageEndTime;

    @Schema(description = "免收标记")
    private String freeChargeFlag;

    @Schema(description = "四舍五入方式")
    private String roundingMethod;

    @Schema(description = "参考政策折扣")
    private BigDecimal policyDiscountReference;

    @Schema(description = "税务开票标记")
    private String taxInvoicingFlag;

    @Schema(description = "折扣金额")
    private BigDecimal discountAmount;

    @Schema(description = "滞纳金")
    private BigDecimal lateFee;

    @Schema(description = "备注")
    private String notes;
}