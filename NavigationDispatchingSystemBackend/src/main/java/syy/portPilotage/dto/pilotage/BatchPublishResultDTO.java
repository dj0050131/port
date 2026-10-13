package syy.portPilotage.dto.pilotage;

import lombok.Data;

import java.util.List;

@Data
public class BatchPublishResultDTO {
    private int successCount;
    private int failedCount;
    private List<Long> successIds;
    private List<Long> failedIds;
}