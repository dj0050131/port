package syy.portPilotage.dto.pilotage;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class PlanStatisticsDTO {
    private int totalCount;
    private int planningCount;
    private int publishedCount;
    private int ongoingCount;
    private int completedCount;
    private int cancelledCount;
    private BigDecimal totalRevenue;
    private Map<String, Integer> vesselTypeDistribution;
    private Map<String, Integer> pilotDistribution;
}