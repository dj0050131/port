package syy.portPilotage.vo.screen;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SystemStatusItemVO {
    private String moduleName;
    private String status;
    private String lastUpdate;
    private BigDecimal uptime;
    private Long count;
    private Long total;
}