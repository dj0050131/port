package syy.portPilotage.dto.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "引航计划确认DTO")
public class PlanConfirmationDTO {

    @NotNull(message = "计划ID不能为空")
    @Schema(description = "引航计划ID")
    private Long planId;

    @NotNull(message = "资源类型不能为空")
    @Schema(description = "资源类型", allowableValues = {"PILOT", "DRIVER", "PILOT_BOAT", "TUGBOAT", "OVERALL"})
    private String resourceType;

    @Schema(description = "资源ID（引航员/驾驶员/引航艇/拖轮ID）")
    private Long resourceId;

    @NotNull(message = "确认方式不能为空")
    @Schema(description = "确认方式", allowableValues = {"WEB", "SMS", "PHONE", "MANUAL"})
    private String confirmMethod;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "确认时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date confirmedAt;
}