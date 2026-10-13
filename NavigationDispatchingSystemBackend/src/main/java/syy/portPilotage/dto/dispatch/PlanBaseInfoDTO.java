package syy.portPilotage.dto.dispatch;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "计划基本信息DTO")
public class PlanBaseInfoDTO {

    @Schema(description = "计划ID")
    private Long id;

    @Schema(description = "计划编号")
    private String planCode;

    @Schema(description = "计划开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date planStartTime;

    @Schema(description = "计划结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date planEndTime;

    @Schema(description = "实际开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualStartTime;

    @Schema(description = "实际结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualEndTime;

    @Schema(description = "计划状态")
    private String planStatus;

    @Schema(description = "起始位置类型")
    private String startLocationType;

    @Schema(description = "起始位置名称")
    private String startLocationName;

    @Schema(description = "结束位置类型")
    private String endLocationType;

    @Schema(description = "结束位置名称")
    private String endLocationName;

    @Schema(description = "登轮时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date boardingTime;

    @Schema(description = "离轮时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date leavingTime;

    @Schema(description = "天气影响说明")
    private String weatherImpact;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "延误原因")
    private String delayReason;
}