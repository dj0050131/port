package syy.portPilotage.dto.dispatch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Map;

@Data
@Schema(description = "引航调度查询DTO")
public class DispatchQueryDTO {

    @Schema(description = "当前页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer size = 20;

    @Schema(description = "计划编号")
    private String planCode;

    @Schema(description = "船舶名称")
    private String vesselName;

    @Schema(description = "计划状态", allowableValues = {"CONFIRMED", "PUBLISHED", "ONGOING", "COMPLETED", "CANCELLED"})
    private String planStatus;

    @Schema(description = "引航员姓名")
    private String pilotName;

    @Schema(description = "确认级别", allowableValues = {"ALL_CONFIRMED", "PARTIAL_CONFIRMED", "NOT_CONFIRMED"})
    private String confirmationLevel;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @Schema(description = "其他查询参数")
    private Map<String, Object> params;
}