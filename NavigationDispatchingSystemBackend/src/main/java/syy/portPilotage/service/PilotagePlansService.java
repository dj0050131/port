package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import syy.portPilotage.dto.plan.PilotBoatAssignmentDTO;
import syy.portPilotage.dto.base.PlanDateDTO;
import syy.portPilotage.dto.base.SaveAssignmentsResultDTO;
import syy.portPilotage.dto.base.TugboatAssignmentDTO;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.dto.pilotage.*;
import syy.portPilotage.dto.statistics.PlanChangeDTO;
import syy.portPilotage.entity.PilotagePlans;

import java.time.LocalDate;
import java.util.List;
import java.util.Date;

public interface PilotagePlansService extends IService<PilotagePlans> {

//    /**
//     */

        BatchCreateResultDTO createPlansWithResult(List<PilotagePlanDTO> dtos);

        boolean updatePlan(Long id, PilotagePlanDTO dto);

    
    boolean replanPlan(Long id, ReplanPlanDTO dto);

        void releasePlanResources(Long planId);

        boolean deletePlan(Long id);

        boolean deletePlansBatch(List<Long> ids);

        PilotagePlanDTO getPlanDetail(Long id);

    IPage<PilotagePlanDTO> getPageWithJoin(PageQueryDTO query);

    
    boolean publishPlan(Long id);

        BatchPublishResultDTO publishPlansBatch(List<Long> planIds);

        boolean startPlan(Long id);

        boolean startPlan(Long id, Date actualStartTime);

        boolean completePlan(Long id);

        boolean completePlan(Long id, Date actualEndTime);

        boolean cancelPlan(Long id, String reason);

    
    boolean assignTugboat(Long planId, TugboatAssignmentDTO dto);

    
    boolean assignPilotBoat(Long planId, PilotBoatAssignmentDTO dto);

        boolean removeTugboatAssignment(Long planId, Long assignmentId);

        boolean removePilotBoatAssignment(Long planId, Long assignmentId);

        ResourceAvailabilityResultDTO checkResourceAvailability(ResourceCheckRequestDTO request);

        List<ResourceAvailabilityResultDTO> batchCheckResourceAvailability(List<ResourceCheckRequestDTO> requests);

        PlanStatisticsDTO getPlanStatistics(String startDate, String endDate, Long pilotId, String vesselType);

        byte[] exportPlans(String startDate, String endDate, String format);

    /**
     * 设置计划日期时间
     * @param id 计划ID
     * @param dto 日期时间信息
     * @return 是否成功
     */
    boolean setPlanDate(Long id, PlanDateDTO dto);

    /**
     * 保存拖轮分配（批量更新）
     * @param planId 计划ID
     * @param assignments 拖轮分配列表
     * @return 保存结果
     */
    SaveAssignmentsResultDTO saveTugboatAssignments(Long planId, List<TugboatAssignmentDTO> assignments);

    /**
     * 保存引航艇分配（批量更新）
     * @param planId 计划ID
     * @param assignments 引航艇分配列表
     * @return 保存结果
     */
    SaveAssignmentsResultDTO savePilotBoatAssignments(Long planId, List<PilotBoatAssignmentDTO> assignments);

        List<PlanChangeDTO> getPlanChangeReport(LocalDate startDate, LocalDate endDate, String status);
}
