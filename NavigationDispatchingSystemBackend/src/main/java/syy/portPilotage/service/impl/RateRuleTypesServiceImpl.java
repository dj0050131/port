package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.entity.RateRuleTypes;
import syy.portPilotage.service.RateRuleTypesService;
import syy.portPilotage.mapper.RateRuleTypesMapper;
import org.springframework.stereotype.Service;

@Service
public class RateRuleTypesServiceImpl extends ServiceImpl<RateRuleTypesMapper, RateRuleTypes>
    implements RateRuleTypesService{

}
