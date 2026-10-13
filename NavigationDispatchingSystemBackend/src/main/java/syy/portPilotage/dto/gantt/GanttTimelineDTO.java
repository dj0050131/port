package syy.portPilotage.dto.gantt;

import lombok.Data;

import java.util.Date;

@Data
public class GanttTimelineDTO {
        private Date startDate;
    
        private Date endDate;
    
    /**
     * 时间单位（DAY, HOUR, MINUTE等）
     */
    private String timeUnit;
    
    /**
     * 时间刻度间隔
     */
    private Integer timeInterval;
    
    
    private Boolean showWeekends;
    
    
    private Boolean showHolidays;
}