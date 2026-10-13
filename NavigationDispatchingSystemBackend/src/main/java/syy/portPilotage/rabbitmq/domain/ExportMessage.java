package syy.portPilotage.rabbitmq.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String exportType; // PLAN, CERTIFICATE, BILLING, STATISTICS
    private String format; // EXCEL, PDF
    private Map<String, Object> queryParams;
    private String userId; // 用户ID
    private String callbackUrl; // 回调URL（可选）
}
