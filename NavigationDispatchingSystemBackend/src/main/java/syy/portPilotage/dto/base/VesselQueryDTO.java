package syy.portPilotage.dto.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import syy.portPilotage.dto.common.PageQueryDTO;

@Data
@EqualsAndHashCode(callSuper = true)
public class VesselQueryDTO extends PageQueryDTO {
    
    private String vesselName;
    private String vesselType;
    private String nationality;
    private String callSign;
    private String imoNumber;
}