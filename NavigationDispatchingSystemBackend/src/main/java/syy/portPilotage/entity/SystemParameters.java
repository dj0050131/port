package syy.portPilotage.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "系统参数表")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

        @Schema(description = "参数组")
    private String paramGroup;

        @Schema(description = "参数键")
    private String paramKey;

        @Schema(description = "参数值")
    private String paramValue;

        @Schema(description = "参数名称")
    private String paramName;

        @Schema(description = "是否系统参数")
    private Boolean isSystem;

        @Schema(description = "排序")
    private Integer sortOrder;

        @Schema(description = "创建时间")
    private Date createdAt;

        @Schema(description = "更新时间")
    private Date updatedAt;
}