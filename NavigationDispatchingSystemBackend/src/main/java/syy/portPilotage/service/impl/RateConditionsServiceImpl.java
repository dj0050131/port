package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.entity.RateConditions;
import syy.portPilotage.service.RateConditionsService;
import syy.portPilotage.mapper.RateConditionsMapper;
import org.springframework.stereotype.Service;

@Service
public class RateConditionsServiceImpl extends ServiceImpl<RateConditionsMapper, RateConditions>
    implements RateConditionsService{

}
