package syy.portPilotage.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "拖轮信息DTO")
public class TugboatDTO {
    @Schema(description = "拖轮ID")
    private Long id;

    @Schema(description = "拖轮名称")
    private String tugboatName;

    @Schema(description = "呼号")
    private String callSign;

    @Schema(description = "系柱拉力(吨)")
    private Integer bollardPull;

    @Schema(description = "功率(马力)")
    private Integer power;

    @Schema(description = "当前位置")
    private String currentLocation;

    @Schema(description = "所属公司")
    private String ownerCompany;

    @Schema(description = "状态")
    private String status;
}