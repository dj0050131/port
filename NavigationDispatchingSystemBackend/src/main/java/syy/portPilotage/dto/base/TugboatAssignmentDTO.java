package syy.portPilotage.dto.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import syy.portPilotage.util.jackson.MultiFormatDateDeserializer;

import java.util.Date;

@Data
@Schema(description = "拖轮分配数据传输对象")
public class TugboatAssignmentDTO {
    @Schema(description = "分配记录ID（详情返回）")
    private Long id;

    @Schema(description = "拖轮ID", required = true, example = "2")
    @NotNull(message = "拖轮ID不能为空")
    private Long tugboatId;

    //名字
    @Schema(description = "拖轮名字", example = "拖轮1")
    private String tugboatName;

    @Schema(description = "分配类型", allowableValues = {"MAIN", "BACKUP"}, example = "MAIN")
    private String assignmentType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预计开始时间", example = "2025-12-04 13:30:00")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date estimatedStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "预计结束时间", example = "2025-12-04 16:30:00")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date estimatedEndTime;

    @Schema(description = "拖力(吨)", example = "10")
    private Double bollardPull;

    @Schema(description = "分配状态", allowableValues = {"ASSIGNED", "CONFIRMED", "CANCELLED", "COMPLETED"})
    private String status;
}