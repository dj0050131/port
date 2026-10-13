package syy.portPilotage.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "记录付款请求包装类")
public class PaymentCreateRequest {

    @Schema(description = "计费凭证ID")
    @NotNull(message = "计费凭证ID不能为空")
    private Long voucherId;

    @Valid
    @Schema(description = "付款数据")
    @NotNull(message = "付款数据不能为空")
    private PaymentCreateDTO paymentData;
}