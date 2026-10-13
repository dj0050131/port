package syy.portPilotage.vo.screen;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

@Data
public class OperationsOverviewVO {
    private List<KpiItemVO> kpis;
    private List<PilotTaskItemVO> pilotTasks;
    private List<EnergyTrendItemVO> energyTrend;
    private List<BerthUsageItemVO> berthUsage;

    @Data
    public static class KpiItemVO {
        private String label;
        private BigDecimal value;
        private String unit;
        private BigDecimal change;
    }
    
    @Data
    public static class PilotTaskItemVO {
        private Long id;
        private String shipName;//船名
        private String pilot;//引航员
        private String eta;//预计到达时间
        private String etd;//预计离开时间
        private String status;
//        private BigDecimal duration; // 持续时间（小时）
//        private String delayReason; // 延迟原因
    }

    @Data
    public static class EnergyTrendItemVO {
        private String time;
        private Double value;
//        private String unit; // 单位
    }
    
    @Data
    public static class BerthUsageItemVO {
        private String name;
        private Integer value;
//        private Integer usedCapacity; // 已使用容量
    }

}