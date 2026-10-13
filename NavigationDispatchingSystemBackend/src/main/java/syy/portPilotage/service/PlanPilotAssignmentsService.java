package syy.portPilotage.service;

import syy.portPilotage.dto.plan.PlanPilotAssignmentDTO;
import syy.portPilotage.entity.PlanPilotAssignments;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

public interface PlanPilotAssignmentsService extends IService<PlanPilotAssignments> {
        boolean updateAssignmentStatus(Long planId, Long pilotId, String status);

        boolean batchUpdateAssignmentStatus(Long planId, String status);

        boolean updateActualTimes(Long planId, Long pilotId, Date actualStartTime, Date actualEndTime);

        boolean checkPilotAvailable(Long pilotId, Date startTime, Date endTime);

        boolean createBatchAssignments(Long planId, List<PlanPilotAssignmentDTO> assignments);

        boolean updateAssessmentInfo(Long assignmentId, Integer score, String notes);
}