package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.entity.TaxRecords;
import syy.portPilotage.service.TaxRecordsService;
import syy.portPilotage.mapper.TaxRecordsMapper;
import org.springframework.stereotype.Service;

@Service
public class TaxRecordsServiceImpl extends ServiceImpl<TaxRecordsMapper, TaxRecords>
    implements TaxRecordsService{

}
