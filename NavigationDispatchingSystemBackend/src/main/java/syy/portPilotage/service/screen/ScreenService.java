package syy.portPilotage.service.screen;

import syy.portPilotage.common.Result;
import syy.portPilotage.vo.screen.*;

import java.util.List;

public interface ScreenService {

    /**
     * 获取港口地图全量数据
     * @return 港口地图数据
     */
    Result<PortMapPayloadVO> getPortMapPayload();

    /**
     * 获取船舶动态列表
     * @return 船舶动态列表
     */
    Result<List<ShipDynamicVO>> getShipDynamics();

    /**
     * 获取引航任务列表
     * @return 引航任务列表
     */
    Result<List<PilotPlanVO>> getPilotTasks();

    /**
     * 获取财务营收统计（计费中心）
     * @return 财务统计数据
     */
    Result<FeeStatisticsVO> getFinancialStats();

    /**
     * 获取实时告警列表
     * @return 告警列表
     */
    Result<List<AlertItemVO>> getAlerts();

    /**
     * 获取系统资源状态
     * @return 系统资源状态
     */
    Result<List<SystemStatusItemVO>> getSystemStatus();

    /**
     * 获取泊位状态列表
     * @return 泊位状态列表
     */
    Result<List<BerthStatusVO>> getBerthStatus();
    
    /**
     * 获取系统性能指标和汇总统计
     * @return 系统性能数据
     */
    Result<SystemPerformanceDataVO> getSystemPerformanceData();
}