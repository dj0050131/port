package syy.portPilotage.vo.pilotage;

import lombok.Data;

import java.util.Date;

@Data
public class PilotagePlanVO {
    
    private Long id;
    private String planCode;
    private Long requestId;
    private String requestCode;
    private Long vesselId;
    private String vesselName;
    
    private Long pilotId;
    private String pilotName;
    
    private Date plannedStartTime;
    private Date plannedEndTime;
    private Date actualStartTime;
    private Date actualEndTime;
    
    private Long pilotBoatId;
    private String pilotBoatName;
    
    private Long vehicleId;
    private String vehiclePlateNumber;
    
    private Long driverId;
    private String driverName;
    
    private Long startBerthId;
    private String startBerthName;
    
    private Long endBerthId;
    private String endBerthName;
    
    private String planStatus;
    private String planNotes;
    private String specialInstructions;
    
    private Boolean notificationSent;
    private Date notificationSentAt;
    
    private Boolean confirmed;
    private Date confirmedAt;
    
    private Date createdAt;
    private Date updatedAt;
}