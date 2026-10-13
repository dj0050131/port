package syy.portPilotage.vo.screen;

import lombok.Data;
import java.util.List;

@Data
public class PortMapPayloadVO {
    private List<ShipDynamicVO> ships;
    private List<BerthStatusVO> berths;
    private List<PilotRouteSegmentVO> routes;
}