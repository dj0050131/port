package syy.portPilotage.dto.base;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
@Data
@Schema(description = "计划日期设置DTO")
public class PlanDateDTO {
    @Schema(description = "计划开始时间")
    @NotNull(message = "计划开始时间不能为空")
    private Date planStartTime;

    @Schema(description = "计划结束时间")
    @NotNull(message = "计划结束时间不能为空")
    private Date planEndTime;

    @Schema(description = "登船时间")
    private Date boardingTime;

    @Schema(description = "离船时间")
    private Date leavingTime;
}