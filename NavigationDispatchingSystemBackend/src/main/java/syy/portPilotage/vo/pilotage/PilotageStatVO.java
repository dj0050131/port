package syy.portPilotage.vo.pilotage;

import lombok.Data;
import syy.portPilotage.vo.common.TrendPointVO;

import java.util.List;

@Data
public class PilotageStatVO {
    
    private Long totalRequests;
    private Long pendingRequests;
    private Long approvedRequests;
    private Long completedPlans;
    private Long todayPlans;
    private Long activePilots;
    private List<TrendPointVO> trends;
}
