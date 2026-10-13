package syy.portPilotage.mapper;

import syy.portPilotage.dto.plan.PlanConfirmationQueryDTO;
import syy.portPilotage.entity.PlanConfirmations;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import syy.portPilotage.entity.PlanConfirmations;

import java.util.List;
import java.util.Map;

@Mapper
public interface PlanConfirmationsMapper extends BaseMapper<PlanConfirmations> {

        Map<String, Object> getPlanConfirmationStats(@Param("planId") Long planId);

        List<Map<String, Object>>   selectConfirmationList(@Param("query") PlanConfirmationQueryDTO query);

        Long selectConfirmationCount(@Param("query") PlanConfirmationQueryDTO query);

        boolean existsConfirmation(@Param("planId") Long planId,
                               @Param("resourceType") String resourceType,
                               @Param("resourceId") Long resourceId);
        List<Map<String, Object>> selectResourceConfirmationList(@Param("query") PlanConfirmationQueryDTO query);

        Long selectResourceConfirmationCount(@Param("query") PlanConfirmationQueryDTO query);

        List<Map<String, Object>> selectPlanConfirmationStats(@Param("planId") Long planId);

        List<Map<String, Object>> selectPilotTasks(@Param("pilotId") Long pilotId,
                                               @Param("status") String status,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

        Long selectPilotTasksCount(@Param("pilotId") Long pilotId,
                               @Param("status") String status);

        List<Map<String, Object>> selectDriverTasks(@Param("driverId") Long driverId,
                                                @Param("status") String status,
                                                @Param("offset") int offset,
                                                @Param("pageSize") int pageSize);

        Long selectDriverTasksCount(@Param("driverId") Long driverId,
                               @Param("status") String status);

        Map<String, Object> selectPilotTaskById(@Param("pilotId") Long pilotId,
                                           @Param("assignmentId") Long assignmentId);

        Map<String, Object> selectDriverTaskById(@Param("driverId") Long driverId,
                                            @Param("assignmentId") Long assignmentId);

}