package syy.portPilotage.dto.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Schema(description = "引航计划确认查询DTO")
public class PlanConfirmationQueryDTO {

    @Schema(description = "引航计划ID")
    private Long planId;

    @Schema(description = "计划编号")
    private String planCode;

    @Schema(description = "资源类型")
    private String resourceType;

    @Schema(description = "确认表ID")
    private Long confirmationId;

    @Schema(description = "确认方式")
    private String confirmMethod;

    @Schema(description = "确认状态")
    private String confirmationStatus;

    @Schema(description = "确认人ID")
    private Long confirmedBy;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startDate;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endDate;

    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "偏移量")
    private Integer offset;

}