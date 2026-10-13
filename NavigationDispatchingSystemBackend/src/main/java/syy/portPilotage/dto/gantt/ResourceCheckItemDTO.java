package syy.portPilotage.dto.gantt;

import lombok.Data;

import java.util.Date;

@Data
public class ResourceCheckItemDTO {
    /**
     * 资源ID
     */
    private Long resourceId;
    
    /**
     * 资源类型
     */
    private String resourceType;
    
        private Date startTime;
    
        private Date endTime;
}