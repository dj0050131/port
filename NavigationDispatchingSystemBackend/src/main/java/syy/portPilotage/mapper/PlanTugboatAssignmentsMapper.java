package syy.portPilotage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import syy.portPilotage.entity.PlanTugboatAssignments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

@Mapper
public interface PlanTugboatAssignmentsMapper extends BaseMapper<PlanTugboatAssignments> {
        int insertBatch(@Param("list") List<PlanTugboatAssignments> list);

        int updateAssignmentStatus(@Param("planId") Long planId,
                               @Param("tugboatId") Long tugboatId,
                               @Param("status") String status);

        int updateActualTimes(@Param("planId") Long planId,
                          @Param("tugboatId") Long tugboatId,
                          @Param("actualStartTime") java.util.Date actualStartTime,
                          @Param("actualEndTime") java.util.Date actualEndTime);
}
