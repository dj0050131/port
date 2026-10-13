package syy.portPilotage.service;

import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Agents;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface AgentsService extends IService<Agents> {

    PageResult<Agents> pageAgent(PageQueryDTO query);
}
