package syy.portPilotage.dto.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "引航计划确认结果DTO")
public class PlanConfirmationResultDTO {

    @Schema(description = "确认记录ID")
    private Long id;

    @Schema(description = "引航计划ID")
    private Long planId;

    @Schema(description = "计划编号")
    private String planCode;

    @Schema(description = "资源类型")
    private String resourceType;

    @Schema(description = "资源类型名称")
    private String resourceTypeName;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "确认方式")
    private String confirmMethod;

    @Schema(description = "确认方式名称")
    private String confirmMethodName;

    @Schema(description = "确认人ID")
    private Long confirmedBy;

    @Schema(description = "确认人姓名")
    private String confirmedByName;

    @Schema(description = "确认时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date confirmedAt;

    @Schema(description = "确认状态")
    private String confirmationStatus;

    @Schema(description = "确认状态名称")
    private String confirmationStatusName;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
}