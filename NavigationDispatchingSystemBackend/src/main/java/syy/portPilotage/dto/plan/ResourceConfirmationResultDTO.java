package syy.portPilotage.dto.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "资源确认情况结果DTO")
public class ResourceConfirmationResultDTO {

    @Schema(description = "计划ID")
    private Long planId;

    @Schema(description = "计划编号")
    private String planCode;

    @Schema(description = "计划状态")
    private String planStatus;

    @Schema(description = "分配记录ID（各资源分配表的主键ID，用于任务详情查询）")
    private Long assignmentId;

    @Schema(description = "资源类型")
    private String resourceType;

    @Schema(description = "资源类型名称")
    private String resourceTypeName;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "资源名称")
    private String resourceName;

    @Schema(description = "分配状态")
    private String assignmentStatus;

    @Schema(description = "分配状态名称")
    private String assignmentStatusName;

    @Schema(description = "确认状态")
    private String confirmationStatus;

    @Schema(description = "确认状态名称")
    private String confirmationStatusName;

    @Schema(description = "确认表id")
    private Long confirmationId;

    @Schema(description = "确认方式")
    private String confirmMethod;

    @Schema(description = "确认方式名称")
    private String confirmMethodName;

    @Schema(description = "确认人ID")
    private Long confirmedBy;

    @Schema(description = "确认人姓名")
    private String confirmedByName;

    @Schema(description = "确认时间")
    private Date confirmedAt;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "船舶名称")
    private String vesselName;

    @Schema(description = "中文船名")
    private String vesselNameZh;

    @Schema(description = "引航员/驾驶员/车辆/拖轮/引航艇的详细信息")
    private Object resourceDetail;

    @Schema(description = "预计开始时间")
    private Date estimatedStartTime;

    @Schema(description = "预计结束时间")
    private Date estimatedEndTime;

    @Schema(description = "创建时间")
    private Date createdAt;

    //英文船名
    @Schema(description = "英文船名")
    private String vesselNameEn;

        @Schema(description = "中文船名(兼容旧字段)")
    private String vesselNameZn;
}