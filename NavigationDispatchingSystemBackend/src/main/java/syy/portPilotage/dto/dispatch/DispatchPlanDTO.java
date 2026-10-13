package syy.portPilotage.dto.dispatch;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "引航调度计划DTO")
public class DispatchPlanDTO {

    @Schema(description = "计划ID")
    private Long planId;

    @Schema(description = "计划编号")
    private String planCode;

    @Schema(description = "计划开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date planStartTime;

    @Schema(description = "计划结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date planEndTime;

    @Schema(description = "计划状态")
    private String planStatus;

    @Schema(description = "调度状态名称")
    private String dispatchStatus;

    @Schema(description = "船舶中文名称")
    private String vesselNameZh;

    @Schema(description = "船舶英文名称")
    private String vesselNameEn;

    @Schema(description = "船舶呼号")
    private String vesselCallSign;

    @Schema(description = "国籍")
    private String nationality;

    @Schema(description = "船舶长度")
    private BigDecimal vesselLength;

    @Schema(description = "船舶宽度")
    private BigDecimal vesselWidth;

    @Schema(description = "总吨位")
    private BigDecimal grossTonnage;

    @Schema(description = "净吨位")
    private BigDecimal netTonnage;

    @Schema(description = "载重吨")
    private BigDecimal deadWeight;

    @Schema(description = "航次编号")
    private String voyageNumber;

    @Schema(description = "航次类型")
    private String voyageType;

    @Schema(description = "预计到港时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eta;

    @Schema(description = "预计离港时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date etd;

    @Schema(description = "代理公司名称")
    private String agentName;

    @Schema(description = "代理联系人")
    private String agentContact;

    @Schema(description = "代理联系电话")
    private String agentPhone;

    @Schema(description = "主引航员姓名")
    private String mainPilot;

    @Schema(description = "引航员联系电话")
    private String pilotPhone;

    @Schema(description = "引航员短号")
    private String pilotShortNum;

    @Schema(description = "驾驶员姓名")
    private String driverName;

    @Schema(description = "驾驶员联系电话")
    private String driverPhone;

    @Schema(description = "车辆牌照")
    private String licensePlate;

    @Schema(description = "车辆型号")
    private String vehicleModel;

    @Schema(description = "车辆状态")
    private String vehicleStatus;

    @Schema(description = "拖轮列表（逗号分隔）")
    private String tugboats;

    @Schema(description = "引航艇列表（逗号分隔）")
    private String pilotBoats;

    @Schema(description = "已确认资源数量")
    private Integer confirmedCount;

    @Schema(description = "总资源数量")
    private Integer totalResourcesCount;

    @Schema(description = "确认进度")
    private Double confirmationProgress;

    @Schema(description = "起始位置名称")
    private String startLocationName;

    @Schema(description = "结束位置名称")
    private String endLocationName;

    @Schema(description = "登轮时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date boardingTime;

    @Schema(description = "离轮时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date leavingTime;

    @Schema(description = "备注")
    private String notes;
}