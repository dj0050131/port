package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.entity.TaxItemDetails;
import syy.portPilotage.service.TaxItemDetailsService;
import syy.portPilotage.mapper.TaxItemDetailsMapper;
import org.springframework.stereotype.Service;

@Service
public class TaxItemDetailsServiceImpl extends ServiceImpl<TaxItemDetailsMapper, TaxItemDetails>
    implements TaxItemDetailsService{

}
