package syy.portPilotage.dto.pilotage;

import lombok.Data;

// DTO定义
@Data
public class ResourceCheckRequestDTO {
    private String resourceType; // PILOT, DRIVER, VEHICLE, TUGBOAT, PILOT_BOAT
    private Long resourceId;
    private String startTime;
    private String endTime;
    private Long excludePlanId;
}