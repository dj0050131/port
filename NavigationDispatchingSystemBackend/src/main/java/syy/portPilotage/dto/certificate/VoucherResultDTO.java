package syy.portPilotage.dto.certificate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "计费凭证创建响应结果")
public class VoucherResultDTO {

    @Schema(description = "计费凭证ID")
    private Long id;

    @Schema(description = "凭证编号")
    private String voucherCode;

    @Schema(description = "签证单ID")
    private Long certificateId;

    @Schema(description = "航次ID")
    private Long voyageId;

    @Schema(description = "船舶ID")
    private Long vesselId;

    @Schema(description = "审核状态")
    private String auditStatus;

    @Schema(description = "付款状态")
    private String paymentStatus;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "到期日")
    private Date dueDate;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "消息")
    private String message;
}