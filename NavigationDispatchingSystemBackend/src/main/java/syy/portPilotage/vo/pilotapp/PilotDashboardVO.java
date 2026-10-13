package syy.portPilotage.vo.pilotapp;

import lombok.Data;
import syy.portPilotage.vo.common.TrendPointVO;

import java.util.List;

@Data
public class PilotDashboardVO {
    /**
     * 全港：今日计划数
     */
    private Long todayPlansTotal;

    /**
     * 我的：今日计划数
     */
    private Long myTodayPlans;

    /**
     * 我的：未来24小时计划数
     */
    private Long myUpcoming24hPlans;

        private Long myTotalPlans;

    /**
     * 全港：近7天任务趋势（复用大屏趋势）
     */
    private List<TrendPointVO> pilotageTrends7d;
}
