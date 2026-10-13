package syy.portPilotage.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "发票操作记录表")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceOperationLogs implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "")
    private Long id;

    /**
     * 计费凭证ID
     */
    @Schema(description = "计费凭证ID")
    private Long voucherId;

    /**
     * 操作类型
     */
    @Schema(description = "操作类型")
    private Object operationType;

    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID")
    private Long operationBy;

    /**
     * 操作时间
     */
    @Schema(description = "操作时间")
    private Date operationTime;

    /**
     * 发票号码
     */
    @Schema(description = "发票号码")
    private String invoiceNo;

        @Schema(description = "操作详情")
    private String operationDetails;

    /**
     * 操作结果
     */
    @Schema(description = "操作结果")
    private Object result;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String notes;

        @Schema(description = "创建时间")
    private Date createdAt;
}