package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import syy.portPilotage.entity.FiscalYears;
import syy.portPilotage.mapper.FiscalYearsMapper;
import syy.portPilotage.service.FiscalYearsService;

@Service
public class FiscalYearsServiceImpl extends ServiceImpl<FiscalYearsMapper, FiscalYears> implements FiscalYearsService {
}
