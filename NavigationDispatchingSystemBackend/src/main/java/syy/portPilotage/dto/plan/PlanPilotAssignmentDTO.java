package syy.portPilotage.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "引航员分配数据传输对象")
public class PlanPilotAssignmentDTO {
    @Schema(description = "分配记录ID（详情返回）")
    private Long id;

    @Schema(description = "引航员ID", required = true)
    @NotNull(message = "引航员ID不能为空")
    private Long pilotId;

    @Schema(description = "引航员姓名（详情返回）")
    private String pilotName;

    @Schema(description = "角色类型", allowableValues = {"MAIN", "ASSISTANT", "TRAINEE", "RESERVE"})
    private String roleType;

    @Schema(description = "分配说明")
    private String assignmentNotes;

    @Schema(description = "预计开始时间")
    private Date estimatedStartTime;

    @Schema(description = "预计结束时间")
    private Date estimatedEndTime;

    @Schema(description = "是否参与考核", defaultValue = "true")
    private Boolean assessmentFlag = true;

    @Schema(description = "分配状态", allowableValues = {"ASSIGNED", "CONFIRMED", "CANCELLED", "COMPLETED"})
    private String status;
}