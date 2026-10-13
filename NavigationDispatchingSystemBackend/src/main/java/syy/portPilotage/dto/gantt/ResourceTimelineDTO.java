package syy.portPilotage.dto.gantt;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ResourceTimelineDTO {
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
    
        private List<ResourceTaskDTO> tasks;
    
        private Date startDate;
    
        private Date endDate;
    
    /**
     * 资源总体可用性百分比
     */
    private Double availabilityPercentage;
}