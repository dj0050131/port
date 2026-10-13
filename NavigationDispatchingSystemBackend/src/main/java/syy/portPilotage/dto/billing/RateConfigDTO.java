package syy.portPilotage.dto.billing;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RateConfigDTO {
    
    @NotNull(message = "费率类型不能为空")
    private String rateType;
    
    @NotNull(message = "费率值不能为空")
    private BigDecimal rateValue;
    
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String unit;
    private String description;
}
