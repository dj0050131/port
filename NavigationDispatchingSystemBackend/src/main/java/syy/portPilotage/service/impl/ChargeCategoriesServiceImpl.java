package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.entity.ChargeCategories;
import syy.portPilotage.service.ChargeCategoriesService;
import syy.portPilotage.mapper.ChargeCategoriesMapper;
import org.springframework.stereotype.Service;

@Service
public class ChargeCategoriesServiceImpl extends ServiceImpl<ChargeCategoriesMapper, ChargeCategories>
    implements ChargeCategoriesService {

}
