package syy.portPilotage.dto.pilotage;

import lombok.Data;
import java.util.List;

@Data
public class UpdateStatusDTO {
    private List<Long> ids;
    private String status;
}