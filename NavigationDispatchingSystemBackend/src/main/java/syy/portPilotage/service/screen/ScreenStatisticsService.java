package syy.portPilotage.service.screen;

import syy.portPilotage.common.Result;
import syy.portPilotage.vo.screen.OperationsOverviewVO;
import syy.portPilotage.vo.screen.TrafficOverviewVO;

public interface ScreenStatisticsService {

    /**
     * 获取交通流/通航统计数据
     * @return 交通流统计视图对象
     */
    Result<TrafficOverviewVO> getTrafficStats();

    /**
     * 获取运营综合作业统计数据
     * @return 运营统计视图对象
     */
    Result<OperationsOverviewVO> getOperationsStats();
    
    /**
     * 获取港口负荷指数
     * @return 港口负荷指数
     */
    Result<Integer> getPortLoadIndex();
}