package syy.portPilotage.service;

import syy.portPilotage.dto.plan.PlanVehicleAssignmentDTO;
import syy.portPilotage.entity.PlanVehicleAssignments;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

public interface PlanVehicleAssignmentsService extends IService<PlanVehicleAssignments> {
        boolean updateAssignmentStatus(Long planId, Long vehicleId, String status);

        boolean batchUpdateAssignmentStatus(Long planId, String status);

        boolean updateActualTimes(Long planId, Long vehicleId, Date actualStartTime, Date actualEndTime);

        boolean checkVehicleAvailable(Long vehicleId, Date startTime, Date endTime);

        boolean createBatchAssignments(Long planId, List<PlanVehicleAssignmentDTO> assignments);

        boolean updateVehicleUsage(Long assignmentId, Double mileage, Double fuelConsumption);
}