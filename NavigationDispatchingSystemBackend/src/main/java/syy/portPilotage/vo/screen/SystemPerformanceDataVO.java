package syy.portPilotage.vo.screen;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.math.BigDecimal;

@Data
public class SystemPerformanceDataVO implements Serializable {
    private List<SystemMetricsItemVO> metrics;        // 性能指标列表，用于趋势图
    private SystemPerformanceSummaryVO summary;       // 性能汇总统计，用于关键指标展示
    
    @Data
    public static class SystemMetricsItemVO implements Serializable {
        private static final long serialVersionUID = 1L;
        private String timestamp;        // 时间戳
        private BigDecimal cpuUsage;     // CPU使用率
        private BigDecimal memoryUsage;  // 内存使用率
        private BigDecimal diskUsage;    // 磁盘使用率
        private BigDecimal networkIn;    // 网络流入
        private BigDecimal networkOut;   // 网络流出
        private BigDecimal loadAverage;  // 系统负载
        private Integer responseTime;    // 响应时间
    }
    
    @Data
    public static class SystemPerformanceSummaryVO implements Serializable {
        private static final long serialVersionUID = 1L;
        private BigDecimal avgCpuUsage;      // 平均CPU使用率
        private BigDecimal avgMemoryUsage;   // 平均内存使用率
        private BigDecimal peakLoad;         // 峰值负载
        private Integer avgResponseTime;     // 平均响应时间
    }
}