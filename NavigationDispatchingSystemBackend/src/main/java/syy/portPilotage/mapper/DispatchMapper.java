package syy.portPilotage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import syy.portPilotage.dto.dispatch.DispatchPlanDTO;
import syy.portPilotage.dto.dispatch.DispatchQueryDTO;
import syy.portPilotage.entity.PilotagePlans;

import java.util.List;
import java.util.Map;

@Mapper
public interface DispatchMapper extends BaseMapper<PilotagePlans> {

    /**
     * 查询调度列表（使用MyBatis-Plus分页）
     * @param page 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<DispatchPlanDTO> selectDispatchPage(IPage<DispatchPlanDTO> page, @Param("params") Map<String, Object> params);

        Map<String, Object> selectDispatchDetail(@Param("planId") Long planId);

        Map<String, Object> selectPlanConfirmationStats(@Param("planId") Long planId);
}