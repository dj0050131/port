package syy.portPilotage.dto.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import syy.portPilotage.util.jackson.MultiFormatDateDeserializer;

import java.util.Date;

@Data
@Schema(description = "引航艇分配数据传输对象")
public class PilotBoatAssignmentDTO {
    @Schema(description = "分配记录ID（详情返回）")
    private Long id;

    @Schema(description = "引航艇ID", required = true, example = "3")
    @NotNull(message = "引航艇ID不能为空")
    private Long pilotBoatId;

    @Schema(description = "引航艇名称（详情返回）", example = "引航艇1")
    private String pilotBoatName;

    @Schema(description = "用途", allowableValues = {"接送引航员", "应急", "其他"}, example = "接送引航员")
    private String purpose;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预计开始时间", example = "2025-12-04 13:00:00")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date estimatedStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预计结束时间", example = "2025-12-04 14:30:00")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date estimatedEndTime;

    @Schema(description = "分配状态", allowableValues = {"ASSIGNED", "CONFIRMED", "CANCELLED", "COMPLETED"})
    private String status;
}