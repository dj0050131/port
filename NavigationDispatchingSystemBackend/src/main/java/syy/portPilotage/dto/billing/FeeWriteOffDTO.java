package syy.portPilotage.dto.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "创建费用核销单请求参数")
public class FeeWriteOffDTO {

    @Schema(description = "代理公司ID")
    @NotNull(message = "代理公司ID不能为空")
    private Long agentId;

    @Schema(description = "核销单编号（自动生成时可空）")
    private String writeOffCode;

    @Schema(description = "核销人")
    private String writeOffPerson;

    @Schema(description = "核销日期")
    private Date writeOffDate;

    @Schema(description = "核销单类型")
    private String writeOffType;

    @Schema(description = "票据属性")
    private String documentAttribute;

    @Schema(description = "核销金额")
    private BigDecimal writeOffAmount;

    @Schema(description = "核销方式")
    private String writeOffMethod;

    @Schema(description = "银行流水号")
    private String bankReferenceNumber;

    @Schema(description = "凭证号码")
    private String voucherNumber;

    @Schema(description = "财务确认人")
    private String financeConfirmer;

    @Schema(description = "财务确认时间")
    private Date financeConfirmTime;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "关联的计费凭证ID列表")
    private List<Long> billingVoucherIds;
}