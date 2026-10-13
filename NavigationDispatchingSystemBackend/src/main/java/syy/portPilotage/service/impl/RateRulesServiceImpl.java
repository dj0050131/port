package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.entity.RateRules;
import syy.portPilotage.service.RateRulesService;
import syy.portPilotage.mapper.RateRulesMapper;
import org.springframework.stereotype.Service;

@Service
public class RateRulesServiceImpl extends ServiceImpl<RateRulesMapper, RateRules>
    implements RateRulesService{

}
