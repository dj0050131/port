package syy.portPilotage.service;

import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.plan.PilotBoatAssignmentDTO;
import syy.portPilotage.entity.PlanPilotBoatAssignments;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

public interface PlanPilotBoatAssignmentsService extends IService<PlanPilotBoatAssignments> {
        boolean updateAssignmentStatus(Long planId, Long pilotBoatId, String status);

        boolean batchUpdateAssignmentStatus(Long planId, String status);

        boolean updateActualTimes(Long planId, Long pilotBoatId, Date actualStartTime, Date actualEndTime);

        boolean checkPilotBoatAvailable(Long pilotBoatId, Date startTime, Date endTime);

        boolean createBatchAssignments(Long planId, List<PilotBoatAssignmentDTO> assignments);

        List<PlanPilotBoatAssignments> getAssignmentsByPlanId(Long planId);

        List<PlanPilotBoatAssignments> getAssignmentsByPilotBoatId(Long pilotBoatId, Date startTime, Date endTime);
}