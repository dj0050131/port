package syy.portPilotage.dto.certificate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建签证单请求包装类")
public class CertificateCreateRequest {

    @Schema(description = "计划ID")
    @NotNull(message = "计划ID不能为空")
    private Long planId;

    @Valid
    @Schema(description = "签证单数据")
    @NotNull(message = "签证单数据不能为空")
    private CertificateCreateDTO certificateData;
}