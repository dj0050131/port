package syy.portPilotage.dto.certificate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "签证单创建响应结果")
public class CertificateResultDTO {

    @Schema(description = "签证单ID")
    private Long id;

    @Schema(description = "签证单编号")
    private String certificateCode;

    @Schema(description = "计划ID")
    private Long planId;

    @Schema(description = "船舶ID")
    private Long vesselId;

    @Schema(description = "航次ID")
    private Long voyageId;

    @Schema(description = "提交状态")
    private String submitStatus;

    @Schema(description = "计费状态")
    private String billingStatus;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "消息")
    private String message;
}