package syy.portPilotage.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "车辆分配数据传输对象")
public class PlanVehicleAssignmentDTO {
    @Schema(description = "分配记录ID（详情返回）")
    private Long id;

    @Schema(description = "车辆ID", required = true)
    @NotNull(message = "车辆ID不能为空")
    private Long vehicleId;

    @Schema(description = "车牌号（详情返回）")
    private String licensePlate;

    @Schema(description = "用途", allowableValues = {"接送引航员", "物资运输", "应急响应", "其他"})
    private String purpose;

    @Schema(description = "用车需求说明")
    private String requirements;

    @Schema(description = "预计开始时间")
    private Date estimatedStartTime;

    @Schema(description = "预计结束时间")
    private Date estimatedEndTime;

    @Schema(description = "分配状态", allowableValues = {"ASSIGNED", "CONFIRMED", "CANCELLED", "COMPLETED"})
    private String status;
}