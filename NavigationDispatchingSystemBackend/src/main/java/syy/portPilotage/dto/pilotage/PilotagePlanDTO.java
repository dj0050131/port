package syy.portPilotage.dto.pilotage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import syy.portPilotage.dto.plan.PilotBoatAssignmentDTO;
import syy.portPilotage.dto.base.TugboatAssignmentDTO;
import syy.portPilotage.util.jackson.MultiFormatDateDeserializer;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "引航计划创建/更新数据传输对象")
public class PilotagePlanDTO {

    @Schema(description = "引航计划ID")
    private Long id;

    @Schema(description = "引航计划编号", example = "P20251204001")
    private String planCode;

    @Schema(description = "引航计划开始时间", required = true, example = "2025-12-04 14:00:00")
    @NotNull(message = "计划开始时间不能为空")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date planStartTime;

    @Schema(description = "引航计划结束时间", required = true, example = "2025-12-04 16:00:00")
    @NotNull(message = "计划结束时间不能为空")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date planEndTime;

    @Schema(description = "起始位置类型", required = true, allowableValues = {"PORT", "PORT_AREA", "WHARF", "BERTH", "ANCHORAGE"})
    @NotBlank(message = "起始位置类型不能为空")
    private String startLocationType;

    @Schema(description = "起始位置ID", required = true, example = "3")
    @NotNull(message = "起始位置ID不能为空")
    private Long startLocationId;

    @Schema(description = "起始位置名称", required = true, example = "1号锚地")
    @NotBlank(message = "起始位置名称不能为空")
    private String startLocationName;

    @Schema(description = "结束位置类型", required = true, allowableValues = {"PORT", "PORT_AREA", "WHARF", "BERTH", "ANCHORAGE"})
    @NotBlank(message = "结束位置类型不能为空")
    private String endLocationType;

    @Schema(description = "结束位置ID", required = true, example = "7")
    @NotNull(message = "结束位置ID不能为空")
    private Long endLocationId;

    @Schema(description = "结束位置名称", required = true, example = "3号泊位")
    @NotBlank(message = "结束位置名称不能为空")
    private String endLocationName;

    @Schema(description = "执行任务引航员ID", required = true, example = "8")
    @NotNull(message = "引航员不能为空")
    private Long assignedPilotId;

    @Schema(description = "执行任务车辆驾驶员ID", required = true, example = "3")
    @NotNull(message = "驾驶员不能为空")
    private Long assignedDriverId;

    @Schema(description = "执行任务车辆ID", required = true, example = "5")
    @NotNull(message = "车辆不能为空")
    private Long assignedVehicleId;

    @Schema(description = "拖轮分配列表")
    private List<TugboatAssignmentDTO> tugboatAssignments;

    @Schema(description = "引航艇分配列表")
    private List<PilotBoatAssignmentDTO> pilotBoatAssignments;

    @Schema(description = "船舶ID", required = true, example = "28")
    @NotNull(message = "船舶不能为空")
    private Long vesselId;

    @Schema(description = "航次ID", required = true, example = "15")
    @NotNull(message = "航次不能为空")
    private Long voyageId;

    @Schema(description = "引航申请ID", required = true, example = "12")
    @NotNull(message = "引航申请不能为空")
    private Long requestId;

    @Schema(description = "引航作业类型标志", allowableValues = {"INBOUND", "OUTBOUND", "SHIFT"})
    private String operationTypeFlag;

    @Schema(description = "优先级", allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT"})
    private String priority;

    @Schema(description = "计划状态", allowableValues = {"PLANNING", "PUBLISHED", "ONGOING", "COMPLETED", "CANCELLED"})
    private String planStatus;

    @Schema(description = "登轮时间", example = "2025-12-04 14:15:00")
    private Date boardingTime;

    @Schema(description = "离轮时间", example = "2025-12-04 15:45:00")
    private Date leavingTime;

    @Schema(description = "预计作业泊位ID", example = "7")
    private Long expectedBerthId;

    @Schema(description = "航线代码", example = "R001")
    private String routeCode;

    @Schema(description = "天气影响说明")
    private String weatherImpact;

    @Schema(description = "备注/特殊要求", example = "需要夜间引航，需准备照明设备")
    private String notes;

    @Schema(description = "待重排原因（资源取消/系统原因）")
    private String replanRequiredReason;

    @Schema(description = "触发待重排的资源类型")
    private String replanRequiredResourceType;

    @Schema(description = "触发待重排的资源ID")
    private Long replanRequiredResourceId;

    @Schema(description = "触发待重排时间")
    private Date replanRequiredAt;

    @Schema(description = "触发待重排的资源名称（用于前端展示）")
    private String replanRequiredResourceName;

    @Schema(description = "创建人ID", example = "56")
    private Long createdBy;

    //中文船名
    @Schema(description = "中文船名")
    private String nameZh;

    @Schema(description = "英文船名")
    private String nameEn;

    //国籍
    @Schema(description = "国籍")
    private String country;

    //航次类型
    @Schema(description = "航次类型")
    private String voyageType;

    //总吨
    @Schema(description = "总吨")
    private Double grossTonnage;

    //净吨
    @Schema(description = "净吨")
    private Double netTonnage;

    //载重吨
    @Schema(description = "载重吨")
    private Double deadWeight;

    //船长
    @Schema(description = "船长")
    private Double vesselLength;

    @Schema(description = "船宽")
    private Double vesselWidth;

    //泊位
    @Schema(description = "泊位")
    private String berth;

    //主引  
    @Schema(description = "主引")
    private String mainPilot;

    //副引    
    @Schema(description = "副引")
    private String backupPilot;

//    //编号 

    //接送拖轮数量 
    @Schema(description = "接送拖轮数量")
    private Integer tugboatCount;

    //净高 
    @Schema(description = "净高")
    private Double netHeight;

    //航线类型
    @Schema(description = "航线类型")
    private String routeType;

    //备注
    @Schema(description = "备注")
    private String remark;

}