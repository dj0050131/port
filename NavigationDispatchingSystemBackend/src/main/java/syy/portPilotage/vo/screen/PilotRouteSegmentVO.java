package syy.portPilotage.vo.screen;

import lombok.Data;

@Data
public class PilotRouteSegmentVO {
    private GeoCoordinateVO from;
    private GeoCoordinateVO to;
    private String shipId;
}