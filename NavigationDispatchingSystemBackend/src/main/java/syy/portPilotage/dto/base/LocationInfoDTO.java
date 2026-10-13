package syy.portPilotage.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "位置信息数据传输对象")
public class LocationInfoDTO {

    @Schema(description = "位置类型", required = true, allowableValues = {"PORT", "PORT_AREA", "WHARF", "BERTH", "ANCHORAGE"})
    @NotBlank(message = "位置类型不能为空")
    private String locationType;

    @Schema(description = "位置ID", required = true, example = "3")
    @NotNull(message = "位置ID不能为空")
    private Long locationId;

    @Schema(description = "位置名称", required = true, example = "1号锚地")
    @NotBlank(message = "位置名称不能为空")
    private String locationName;

    @Schema(description = "所属港口ID", example = "1")
    private Long portId;

    @Schema(description = "所属港区ID", example = "2")
    private Long portAreaId;

    @Schema(description = "所属码头ID", example = "5")
    private Long wharfId;
}