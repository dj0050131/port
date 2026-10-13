package syy.portPilotage.dto.gantt;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ConflictCheckRequestDTO {
        private List<ResourceCheckItemDTO> resources;
    
        private Date startDate;
    
        private Date endDate;
    
        private Long planId;
}