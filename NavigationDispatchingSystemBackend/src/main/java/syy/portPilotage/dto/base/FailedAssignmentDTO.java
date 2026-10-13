package syy.portPilotage.dto.base;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
@Data
@Schema(description = "失败的分配项DTO")
public class FailedAssignmentDTO {
    @Schema(description = "失败索引")
    private Integer index;

    @Schema(description = "失败原因")
    private String reason;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "资源类型")
    private String resourceType;
}