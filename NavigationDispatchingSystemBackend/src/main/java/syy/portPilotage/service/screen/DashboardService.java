package syy.portPilotage.service.screen;

import syy.portPilotage.common.Result;
import syy.portPilotage.vo.billing.FinancialStatVO;
import syy.portPilotage.vo.pilotage.PilotageStatVO;

import java.util.Map;

public interface DashboardService {

    /**
     * 获取引航统计数据
     * @return 引航统计视图对象
     */
    Result<PilotageStatVO> getPilotageStat();

    /**
     * 获取财务统计数据
     * @return 财务统计视图对象
     */
    Result<FinancialStatVO> getFinancialStat();

    /**
     * 获取吞吐量统计数据
     * @return 吞吐量统计数据映射
     */
    Result<Map<String, Object>> getThroughputStat();

    /**
     * 获取资源利用率统计数据
     * @return 资源利用率统计数据映射
     */
    Result<Map<String, Object>> getResourceUsageStat();

    /**
     * 获取告警统计数据
     * @return 告警统计数据映射
     */
    Result<Map<String, Object>> getAlertStat();

    /**
     * 获取综合概览
     * @return 综合概览数据映射
     */
    Result<Map<String, Object>> getOverview();
}