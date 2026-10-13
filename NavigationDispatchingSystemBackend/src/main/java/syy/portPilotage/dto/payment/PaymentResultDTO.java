package syy.portPilotage.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "付款记录响应结果")
public class PaymentResultDTO {

    @Schema(description = "付款记录ID")
    private Long id;

    @Schema(description = "计费凭证ID")
    private Long billingVoucherId;

    @Schema(description = "付款金额")
    private BigDecimal paymentAmount;

    @Schema(description = "付款日期")
    private Date paymentDate;

    @Schema(description = "付款方式")
    private String paymentMethod;

    @Schema(description = "付款状态")
    private String paymentStatus;

    @Schema(description = "银行流水号")
    private String bankReferenceNumber;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "消息")
    private String message;
}