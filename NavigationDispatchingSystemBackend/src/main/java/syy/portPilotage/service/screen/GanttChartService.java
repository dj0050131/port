package syy.portPilotage.service.screen;

import syy.portPilotage.dto.gantt.*;
import syy.portPilotage.dto.gantt.ConflictCheckRequestDTO;
import syy.portPilotage.dto.gantt.ResourceConflictDTO;

import java.util.Date;
import java.util.List;

public interface GanttChartService {
    
    List<ResourceTimelineDTO> getResourceTimeline(Date startDate, Date endDate, String resourceType);

    
    GanttChartDataDTO getGanttData(Date startDate, Date endDate);

    
    Boolean updatePlanTime(Long planId, Date newStartTime, Date newEndTime);

    
    List<ResourceConflictDTO> checkConflicts(ConflictCheckRequestDTO request);

    
    Object getResourceOverview(Date startDate, Date endDate);
}