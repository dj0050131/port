package syy.portPilotage.vo.billing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "付款提示提交外部系统失败记录")
public class PaymentNoticeFailureVO {

    @Schema(description = "计费凭证信息")
    private BillingResultVO voucher;

    @Schema(description = "最后一次提交时间")
    private Date lastAttemptAt;

    @Schema(description = "失败原因/错误信息")
    private String errorMessage;
}
