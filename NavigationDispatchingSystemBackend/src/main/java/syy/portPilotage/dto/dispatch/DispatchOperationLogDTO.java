package syy.portPilotage.dto.dispatch;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "调度操作日志DTO")
class DispatchOperationLogDTO {

    @Schema(description = "操作类型", allowableValues = {"PUBLISH", "START", "COMPLETE", "CANCEL", "UPDATE"})
    private String operationType;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;

    @Schema(description = "操作说明")
    private String description;

    @Schema(description = "操作前状态")
    private String beforeStatus;

    @Schema(description = "操作后状态")
    private String afterStatus;
}