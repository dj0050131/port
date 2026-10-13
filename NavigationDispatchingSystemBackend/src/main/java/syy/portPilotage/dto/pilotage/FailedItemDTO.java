package syy.portPilotage.dto.pilotage;

import lombok.Data;

@Data
public class FailedItemDTO {
    private int index;
    private String reason;
}