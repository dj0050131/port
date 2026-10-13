package syy.portPilotage.dto.gantt;

import lombok.Data;

@Data
public class GanttResourceDTO {
    /**
     * 资源ID
     */
    private Long id;
    
    /**
     * 资源名称
     */
    private String name;
    
    /**
     * 资源类型（PILOT, TUGBOAT, PILOT_BOAT, DRIVER, VEHICLE等）
     */
    private String type;
    
    /**
     * 资源状态（AVAILABLE, BUSY, MAINTENANCE等）
     */
    private String status;
    
    /**
     * 描述信息
     */
    private String description;
    
    /**
     * 资源级别或分类
     */
    private String level;
}