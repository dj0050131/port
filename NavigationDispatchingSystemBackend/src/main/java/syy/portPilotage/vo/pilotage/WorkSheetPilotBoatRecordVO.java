package syy.portPilotage.vo.pilotage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WorkSheetPilotBoatRecordVO {
    @Schema(description = "计划ID")
    private Long planId;
    @Schema(description = "计划编号")
    private String planCode;

    @Schema(description = "中文船名")
    private String shipName;
    @Schema(description = "英文船名")
    private String englishName;

    @Schema(description = "引航艇艇名")
    private String boatName;
    @Schema(description = "引航艇呼号")
    private String boatCallSign;

    @Schema(description = "时间/日期")
    private String time;
    @Schema(description = "泊位/地点")
    private String berth;
    @Schema(description = "动态")
    private String dynamic;
}
