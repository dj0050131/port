package syy.portPilotage.dto.gantt;

import lombok.Data;

import java.util.List;

@Data
public class GanttChartDataDTO {
        private List<GanttTaskDTO> tasks;
    
        private List<GanttResourceDTO> resources;
    
    /**
     * 时间轴信息
     */
    private GanttTimelineDTO timeline;
}