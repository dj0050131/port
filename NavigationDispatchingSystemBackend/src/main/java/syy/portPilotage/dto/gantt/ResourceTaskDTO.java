package syy.portPilotage.dto.gantt;

import lombok.Data;

import java.util.Date;

@Data
public class ResourceTaskDTO {
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 计划ID
     */
    private Long planId;
    
        private Date startTime;
    
        private Date endTime;
    
    /**
     * 任务状态
     */
    private String status;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 任务类型
     */
    private String taskType;
}