package syy.portPilotage.service;

import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.plan.PlanConfirmationDTO;
import syy.portPilotage.dto.plan.PlanConfirmationQueryDTO;
import syy.portPilotage.dto.plan.ResourceConfirmationResultDTO;
import syy.portPilotage.entity.PlanConfirmations;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface PlanConfirmationsService extends IService<PlanConfirmations> {

        boolean createConfirmation(PlanConfirmationDTO dto);

        boolean batchConfirm(List<PlanConfirmationDTO> dtos);

        Map<String, Object> getAssignmentsResourceList(PlanConfirmationQueryDTO query);

        List<Map<String, Object>> getPlanConfirmationStatsByType(Long planId);

        Map<String, Object> getConfirmationList(PlanConfirmationQueryDTO query);

        Map<String, Object> getPlanConfirmationStats(Long planId);

        Map<String, Object> getPlanFullConfirmationStatus(Long planId);

        boolean cancelConfirmation(PlanConfirmationDTO dto, String reason);

        boolean batchCancel(List<PlanConfirmationDTO> dtos, String reason);

    /**
     * 获取我的任务列表
     * @param userId 用户ID
     * @param status 任务状态（可选）
     * @param page 页码
     * @param pageSize 页大小
     * @return 任务列表
     */
    PageResult<ResourceConfirmationResultDTO> getMyTasks(Long userId, String status, Integer page, Integer pageSize);

    /**
     * 获取任务详情
     * @param userId 用户ID
     * @param assignmentId 分配ID
     * @return 任务详情
     */
    ResourceConfirmationResultDTO getTaskDetail(Long userId, Long assignmentId);

}