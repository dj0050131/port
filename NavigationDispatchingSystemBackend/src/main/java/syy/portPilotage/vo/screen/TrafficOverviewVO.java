package syy.portPilotage.vo.screen;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

@Data
public class TrafficOverviewVO {
    private List<TrafficMetricVO> metrics;
    private List<TrafficSourceItemVO> sourceRanking;
    private List<DeviceStatusItemVO> deviceStatuses;
    private List<TrafficTrendItemVO> trend;
    private List<TypeDistributionItemVO> typeDistribution;
    private List<GeoSpotItemVO> geoSpots;
    
    @Data
    public static class TrafficMetricVO {
        private String id;
        private String label;
        private Long value;
        private String unit;
        private BigDecimal trend;
        private String subLabel;
    }
    
    @Data
    public static class TrafficSourceItemVO {
        private Integer rank;
        private String nationality;
        private Long value;
        private BigDecimal ratio;
    }
    
    @Data
    public static class DeviceStatusItemVO {
        private String name;
        private String category;
        private BigDecimal onlineRate;
        private Integer alarms;
        private String status; // 设备状态
        private Date lastCheckTime;
        private String location; // 位置
        private String maintenanceCycle; // 维护周期
    }
    
    @Data
    public static class TrafficTrendItemVO {
        private String time;
        private Integer inbound;
        private Integer outbound;
        private Integer totalLoad; // 港口负荷指数
    }
    
    @Data
    public static class TypeDistributionItemVO {
        private String name;
        private Integer value;
        private BigDecimal percentage; // 百分比
        private Integer lastPeriodValue; // 上期值
        private String trend; // 趋势
        private String color; // 显示颜色
    }
    
    @Data
    public static class GeoSpotItemVO {
        private String name;
        private Object value; // [number, number, number] array
    }
    
//
//    @Data
//    }
//
//    @Data
//        private BigDecimal tonnage; // 吨位
//    }
//
//    @Data
//        private List<HourlyData> hourlyData; // 小时数据
//
//        @Data
//            private String hour; // 小时
//        }
//    }
}