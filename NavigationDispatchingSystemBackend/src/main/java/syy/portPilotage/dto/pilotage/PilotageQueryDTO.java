package syy.portPilotage.dto.pilotage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import syy.portPilotage.dto.common.PageQueryDTO;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class PilotageQueryDTO extends PageQueryDTO {
    
    private String requestCode;
    private Long vesselId;
    private String requestStatus;
    private Date etaStart;
    private Date etaEnd;
    private String priority;
}
