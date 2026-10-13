package syy.portPilotage.dto.pilotage;

import lombok.Data;
import java.util.List;

@Data
public class BatchCreateResultDTO {
    private int successCount;
    private int failedCount;
    private List<FailedItemDTO> failedItems;
    private List<Long> planIds;
}