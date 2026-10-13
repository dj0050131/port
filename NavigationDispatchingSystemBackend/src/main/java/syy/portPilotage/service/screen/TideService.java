package syy.portPilotage.service.screen;

import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.TideInfo;

public interface TideService {

    /**
     * 获取当前潮汐信息
     * @return 潮汐信息实体
     */
    Result<TideInfo> getCurrent();

    /**
     * 获取历史潮汐信息
     * @param query 分页查询参数
     * @return 分页结果
     */
    Result<PageResult<TideInfo>> history(PageQueryDTO query);
}