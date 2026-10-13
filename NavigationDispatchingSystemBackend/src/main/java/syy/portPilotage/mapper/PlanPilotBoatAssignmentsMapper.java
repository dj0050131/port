package syy.portPilotage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import syy.portPilotage.entity.PlanPilotBoatAssignments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

@Mapper
public interface PlanPilotBoatAssignmentsMapper extends BaseMapper<PlanPilotBoatAssignments> {
        int insertBatch(@Param("list") List<PlanPilotBoatAssignments> list);
        int updateAssignmentStatus(@Param("planId") Long planId,
                               @Param("pilotBoatId") Long pilotBoatId,
                               @Param("status") String status);

        int updateActualTimes(@Param("planId") Long planId,
                          @Param("pilotBoatId") Long pilotBoatId,
                          @Param("actualStartTime") java.util.Date actualStartTime,
                          @Param("actualEndTime") java.util.Date actualEndTime);
}
