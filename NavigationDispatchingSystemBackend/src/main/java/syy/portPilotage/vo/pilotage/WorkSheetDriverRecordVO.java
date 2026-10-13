package syy.portPilotage.vo.pilotage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WorkSheetDriverRecordVO {
    @Schema(description = "计划ID")
    private Long planId;
    @Schema(description = "计划编号")
    private String planCode;

    @Schema(description = "中文船名")
    private String shipName;
    @Schema(description = "英文船名")
    private String englishName;

    @Schema(description = "驾驶员姓名")
    private String driverName;
    @Schema(description = "车牌号")
    private String licensePlate;

    @Schema(description = "时间/日期")
    private String time;
    @Schema(description = "泊位/地点")
    private String berth;
    @Schema(description = "动态")
    private String dynamic;

    @Schema(description = "总吨")
    private String grossTonnage;
    @Schema(description = "净吨")
    private String netTonnage;
    @Schema(description = "载重吨")
    private String deadWeight;
    @Schema(description = "船长")
    private String length;
    @Schema(description = "船宽")
    private String width;
    @Schema(description = "吃水")
    private String draft;
    @Schema(description = "国籍")
    private String nationality;
    @Schema(description = "呼号")
    private String callSign;
    @Schema(description = "IMO")
    private String imoNumber;
}
