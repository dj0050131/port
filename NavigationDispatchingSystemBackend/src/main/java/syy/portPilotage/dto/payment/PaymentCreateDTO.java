package syy.portPilotage.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "创建付款信息请求参数")
public class PaymentCreateDTO {

    @Schema(description = "计费凭证ID")
    @NotNull(message = "计费凭证ID不能为空")
    private Long billingVoucherId;

    @Schema(description = "收款单位")
    @NotNull(message = "收款单位不能为空")
    private String payeeUnit;

    @Schema(description = "收款账号")
    @NotNull(message = "收款账号不能为空")
    private String payeeBankAccount;

    @Schema(description = "收款开户行")
    @NotNull(message = "收款开户行不能为空")
    private String payeeBankName;

    @Schema(description = "付款金额")
    @NotNull(message = "付款金额不能为空")
    private BigDecimal paymentAmount;

    @Schema(description = "付款日期")
    private Date paymentDate;

    @Schema(description = "付款方式")
    private String paymentMethod;

    @Schema(description = "应付款日期")
    private Date dueDate;

    @Schema(description = "实际付款日期")
    private Date actualPaymentDate;

    @Schema(description = "付款备注")
    private String paymentRemark;

    @Schema(description = "银行流水号")
    private String bankReferenceNumber;
}