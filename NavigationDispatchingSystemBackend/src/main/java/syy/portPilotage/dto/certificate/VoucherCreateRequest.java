package syy.portPilotage.dto.certificate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建计费凭证请求包装类")
public class VoucherCreateRequest {

    @Schema(description = "签证单ID")
    @NotNull(message = "签证单ID不能为空")
    private Long certificateId;

    @Valid
    @Schema(description = "计费凭证数据")
    @NotNull(message = "计费凭证数据不能为空")
    private VoucherCreateDTO voucherData;
}