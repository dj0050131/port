package syy.portPilotage.dto.gantt;

import lombok.Data;

import java.util.Date;

@Data
public class GanttTaskDTO {
    /**
     * 任务ID
     */
    private Long id;
    
    /**
     * 任务名称
     */
    private String name;
    
        private Date startDate;
    
        private Date endDate;
    
    /**
     * 任务进度（0-100）
     */
    private Integer progress;
    
    /**
     * 资源ID
     */
    private Long resourceId;
    
    /**
     * 资源类型（PILOT, TUGBOAT, PILOT_BOAT, DRIVER, VEHICLE等）
     */
    private String resourceType;
    
    /**
     * 任务类型
     */
    private String taskType;
    
    /**
     * 计划ID
     */
    private Long planId;
    
    /**
     * 描述信息
     */
    private String description;
}