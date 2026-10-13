package syy.portPilotage.dto.gantt;

import lombok.Data;

import java.util.Date;

@Data
public class ResourceConflictDTO {
    /**
     * 资源ID
     */
    private Long resourceId;
    
    /**
     * 资源名称
     */
    private String resourceName;
    
    /**
     * 资源类型
     */
    private String resourceType;
    
        private Date startTime;
    
        private Date endTime;
    
    /**
     * 冲突的任务ID
     */
    private Long conflictingTaskId;
    
    /**
     * 冲突的任务名称
     */
    private String conflictingTaskName;
    
    /**
     * 冲突描述
     */
    private String description;
    
    /**
     * 冲突严重程度
     */
    private String severity; // LOW, MEDIUM, HIGH
}