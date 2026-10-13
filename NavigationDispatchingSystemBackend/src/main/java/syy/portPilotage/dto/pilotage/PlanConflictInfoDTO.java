package syy.portPilotage.dto.pilotage;

import lombok.Data;

@Data
public class PlanConflictInfoDTO {
    private Long conflictPlanId;
    private String conflictPlanCode;
    private String conflictTimeRange;
    private String conflictResourceType;
}