package syy.portPilotage.dto.billing;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvoicePrintDTO {
    
    @NotNull(message = "计费凭证ID不能为空")
    private Long voucherId;
    
    private String taxpayerFullName;
    private String taxpayerId;
    private String buyerBank;
    private String buyerAddress;
}
