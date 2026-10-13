package syy.portPilotage.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "驾驶员分配数据传输对象")
public class PlanDriverAssignmentDTO {
    @Schema(description = "分配记录ID（详情返回）")
    private Long id;

    @Schema(description = "驾驶员ID", required = true)
    @NotNull(message = "驾驶员ID不能为空")
    private Long driverId;

    @Schema(description = "驾驶员姓名（详情返回）")
    private String driverName;

    @Schema(description = "车辆分配ID（可选）")
    private Long vehicleAssignmentId;

    @Schema(description = "任务类型", allowableValues = {"DRIVING", "STANDBY", "EMERGENCY"})
    private String taskType;

    @Schema(description = "预计开始时间")
    private Date estimatedStartTime;

    @Schema(description = "预计结束时间")
    private Date estimatedEndTime;

    @Schema(description = "分配状态", allowableValues = {"ASSIGNED", "CONFIRMED", "CANCELLED", "COMPLETED"})
    private String status;
}