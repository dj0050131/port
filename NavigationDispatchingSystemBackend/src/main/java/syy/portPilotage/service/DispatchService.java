package syy.portPilotage.service;

import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.dispatch.DispatchQueryDTO;
import syy.portPilotage.dto.dispatch.DispatchDetailDTO;
import syy.portPilotage.dto.dispatch.DispatchStatisticsQueryDTO;
import syy.portPilotage.dto.dispatch.DispatchTimeUpdateDTO;

import java.util.Map;

public interface DispatchService {

        Map<String, Object> getDispatchList(DispatchQueryDTO query);

        DispatchDetailDTO getDispatchDetail(Long planId);

        boolean publishDispatch(Long planId);

    /**
     * 开始调度（带时间参数）
     * @param planId 计划ID
     * @param dto 时间参数
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    boolean startDispatch(Long planId, DispatchTimeUpdateDTO dto);

    /**
     * 完成调度（带时间参数）
     * @param planId 计划ID
     * @param dto 时间参数
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    boolean completeDispatch(Long planId, DispatchTimeUpdateDTO dto);

        boolean cancelDispatch(Long planId, String reason);

        boolean updateDispatchTime(Long planId, DispatchTimeUpdateDTO dto);

        Map<String, Object> getDispatchStatistics(DispatchStatisticsQueryDTO query);

    /**
     * 检查并确认调度
     * 当所有资源都已确认时，将计划状态更新为CONFIRMED
     * @param planId 计划ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    boolean checkAndConfirmDispatch(Long planId);
}