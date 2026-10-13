package syy.portPilotage.dto.pilotage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WorkSheetRecordQueryDTO {
    @Schema(description = "关键词（船名/计划编号/人员名称）")
    private String keyword;

    @Schema(description = "开始日期时间（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）")
    private String startDate;

    @Schema(description = "结束日期时间（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）")
    private String endDate;
}
