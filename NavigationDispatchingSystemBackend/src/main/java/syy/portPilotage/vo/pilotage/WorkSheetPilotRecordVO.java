package syy.portPilotage.vo.pilotage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WorkSheetPilotRecordVO {
    @Schema(description = "计划ID")
    private Long planId;

    @Schema(description = "计划编号")
    private String planCode;

    @Schema(description = "中文船名")
    private String shipName;

    @Schema(description = "英文船名")
    private String englishName;

    @Schema(description = "引航员姓名")
    private String pilotName;

    @Schema(description = "时间/日期（优先实际开始时间，其次计划开始时间）")
    private String time;

    @Schema(description = "泊位/地点（优先结束位置名称，其次起始位置名称）")
    private String berth;

    @Schema(description = "动态（进港/出港/移泊等）")
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
