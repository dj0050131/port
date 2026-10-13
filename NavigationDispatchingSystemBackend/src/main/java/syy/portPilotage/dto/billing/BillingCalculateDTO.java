package syy.portPilotage.dto.billing;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillingCalculateDTO {
    
    @NotNull(message = "签证单ID不能为空")
    private Long certificateId;
    
    private Boolean autoCalculate = true;
    private Double discountRate;
    private String notes;
}