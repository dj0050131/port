package syy.portPilotage.dto.pilotage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import syy.portPilotage.dto.base.TugboatAssignmentDTO;
import syy.portPilotage.dto.plan.PilotBoatAssignmentDTO;
import syy.portPilotage.util.jackson.MultiFormatDateDeserializer;

import java.util.Date;
import java.util.List;

/**
 * 重排专用 DTO：仅包含“重排资源/时间”需要的字段。
 * 不能复用 PilotagePlanDTO（它用于创建/完整更新，字段校验过于严格）。
 */
@Data
@Schema(description = "引航计划重排（重新排资源）请求")
public class ReplanPlanDTO {

    @Schema(description = "计划开始时间", required = true, example = "2026-01-15 08:00:00")
    @NotNull(message = "计划开始时间不能为空")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date planStartTime;

    @Schema(description = "计划结束时间", required = true, example = "2026-01-15 10:00:00")
    @NotNull(message = "计划结束时间不能为空")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date planEndTime;

    @Schema(description = "引航员ID", required = true)
    @NotNull(message = "引航员不能为空")
    private Long assignedPilotId;

    @Schema(description = "驾驶员ID", required = true)
    @NotNull(message = "驾驶员不能为空")
    private Long assignedDriverId;

    @Schema(description = "车辆ID", required = true)
    @NotNull(message = "车辆不能为空")
    private Long assignedVehicleId;

    @Schema(description = "拖轮分配列表（可多条）")
    private List<TugboatAssignmentDTO> tugboatAssignments;

    @Schema(description = "引航艇分配列表（可多条）")
    private List<PilotBoatAssignmentDTO> pilotBoatAssignments;

    @Schema(description = "优先级", allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"})
    private String priority;

    @Schema(description = "天气影响说明")
    private String weatherImpact;

    @Schema(description = "备注/特殊要求")
    private String notes;
}
