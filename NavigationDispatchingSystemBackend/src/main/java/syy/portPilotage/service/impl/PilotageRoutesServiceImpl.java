package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.entity.PilotageRoutes;
import syy.portPilotage.service.PilotageRoutesService;
import syy.portPilotage.mapper.PilotageRoutesMapper;
import org.springframework.stereotype.Service;

@Service
public class PilotageRoutesServiceImpl extends ServiceImpl<PilotageRoutesMapper, PilotageRoutes>
    implements PilotageRoutesService{

}
