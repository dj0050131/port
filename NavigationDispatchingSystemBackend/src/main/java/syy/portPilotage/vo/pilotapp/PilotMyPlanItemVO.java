package syy.portPilotage.vo.pilotapp;

import lombok.Data;

import java.util.Date;

@Data
public class PilotMyPlanItemVO {
    private Long id;
    private String planCode;
    private Date planStartTime;
    private Date planEndTime;
    private String startLocationName;
    private String endLocationName;
    private String planStatus;

        private String pilotConfirmationStatus;
}
