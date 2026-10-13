package syy.portPilotage.service;

import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.base.TugboatAssignmentDTO;
import syy.portPilotage.entity.PlanTugboatAssignments;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

public interface PlanTugboatAssignmentsService extends IService<PlanTugboatAssignments> {

        boolean updateAssignmentStatus(Long planId, Long tugboatId, String status);

        boolean batchUpdateAssignmentStatus(Long planId, String status);

        boolean updateActualTimes(Long planId, Long tugboatId, Date actualStartTime, Date actualEndTime);

        boolean checkTugboatAvailable(Long tugboatId, Date startTime, Date endTime);

        boolean createBatchAssignments(Long planId, List<TugboatAssignmentDTO> assignments);

        List<PlanTugboatAssignments> getAssignmentsByPlanId(Long planId);

        List<PlanTugboatAssignments> getAssignmentsByTugboatId(Long tugboatId, Date startTime, Date endTime);
}