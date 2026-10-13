package syy.portPilotage.service;

import syy.portPilotage.dto.plan.PlanDriverAssignmentDTO;
import syy.portPilotage.entity.PlanDriverAssignments;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

public interface PlanDriverAssignmentsService extends IService<PlanDriverAssignments> {
        boolean updateAssignmentStatus(Long planId, Long driverId, String status);

        boolean batchUpdateAssignmentStatus(Long planId, String status);

        boolean updateActualTimes(Long planId, Long driverId, Date actualStartTime, Date actualEndTime);

        boolean checkDriverAvailable(Long driverId, Date startTime, Date endTime);

        boolean createBatchAssignments(Long planId, List<PlanDriverAssignmentDTO> assignments);

        boolean updateDriverWorkingHours(Long assignmentId, Double workingHours);
}