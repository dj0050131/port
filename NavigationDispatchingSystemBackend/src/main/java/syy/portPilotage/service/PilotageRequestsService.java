package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.dto.pilotage.PilotageRequestDTO;
import syy.portPilotage.entity.PilotageRequests;
import com.baomidou.mybatisplus.extension.service.IService;
import syy.portPilotage.enums.RequestStatusEnum;

import java.util.Date;
import java.util.List;

public interface PilotageRequestsService extends IService<PilotageRequests> {
    /**
     * 根据申请ID获取详细的引航申请信息，包括船舶相关信息
     * @param id 申请ID
     * @return 完整的引航申请信息
     */
    PilotageRequests getDetailedRequest(Long id);

    @Transactional
    PilotageRequests savePilotAgeRequestData(PilotageRequestDTO pilotageRequestDTO);

    PilotageRequestDTO getPilotAgeRequestById(Long pilotageId);

    @Transactional
    PilotageRequests updatePilotAgeData(PilotageRequestDTO pilotageRequestDTO);

    @Transactional
    boolean deletePilotAgeData(Long vesselId);

    <DTO> IPage<DTO> selectJoinPage(Page<DTO> page, Class<DTO> clazz, MPJLambdaWrapper<PilotageRequests> wrapper);

    IPage<PilotageRequestDTO> getPageWithJoin(PageQueryDTO query);

        List<PilotageRequestDTO> getListWithJoinByIds(List<Long> ids);

    boolean deleteBatchPilotAgeData(List<Long> requestIds);

    boolean batchReviewRequests(List<Long> requestIds, RequestStatusEnum status, Long reviewerId,
                                Date reviewedAt, String rejectReason);
    
    /**
     * 更新引航申请状态
     * @param requestIds 申请ID列表
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updatePilotageRequestStatus(List<Long> requestIds, RequestStatusEnum status);
}
