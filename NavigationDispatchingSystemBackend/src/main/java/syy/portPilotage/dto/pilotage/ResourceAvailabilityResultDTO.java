package syy.portPilotage.dto.pilotage;

import lombok.Data;

@Data
public class ResourceAvailabilityResultDTO {
    private boolean available;
    private String message;
    private String resourceType;
    private Long resourceId;
    private PlanConflictInfoDTO conflictInfo;
}