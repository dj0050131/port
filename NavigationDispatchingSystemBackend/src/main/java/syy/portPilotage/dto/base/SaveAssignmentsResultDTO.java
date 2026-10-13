package syy.portPilotage.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分配保存结果DTO")
public class SaveAssignmentsResultDTO {
    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failedCount;

    @Schema(description = "失败项列表")
    private List<FailedAssignmentDTO> failedItems;
}