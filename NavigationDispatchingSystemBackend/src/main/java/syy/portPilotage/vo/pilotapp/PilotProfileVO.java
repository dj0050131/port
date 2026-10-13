package syy.portPilotage.vo.pilotapp;

import lombok.Data;

@Data
public class PilotProfileVO {
    private Long pilotId;
    private String pilotCode;
    private String pilotName;
    private String onDutyStatus;
}
