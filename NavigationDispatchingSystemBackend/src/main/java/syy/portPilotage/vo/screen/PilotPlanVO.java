package syy.portPilotage.vo.screen;

import lombok.Data;
import java.util.List;
import java.util.Date;

@Data
public class PilotPlanVO {
    private String id;
    private String shipId;
    private String shipName;
    private String pilotName;
    private String schedule;
    private String status;
    private List<GeoCoordinateVO> route;
    private String remarks;
}