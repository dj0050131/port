package syy.portPilotage.dto.dispatch;

import lombok.Data;

import java.util.Date;

@Data
public class DispatchStatisticsQueryDTO {
    private Date startDate;
    private Date endDate;
    private Long pilotId;
    private String vesselType;
}