package syy.portPilotage.dto.billing;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentNoticeRequest {
        private List<Long> ids;

        private BigDecimal amount;
}
