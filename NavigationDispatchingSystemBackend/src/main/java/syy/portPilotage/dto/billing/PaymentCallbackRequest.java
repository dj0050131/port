package syy.portPilotage.dto.billing;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PaymentCallbackRequest {
    /**
     * 计费凭证ID
     */
    private Long voucherId;

    /**
     * 实收金额（可空；为空则按计费凭证 totalAmount 作为实收）
     */
    private BigDecimal receivedAmount;

    /**
     * 付款时间（可空；为空则取当前时间）
     */
    private Date paidAt;

    /**
     * 外部支付流水号（可选）
     */
    private String externalPaymentNo;
}
