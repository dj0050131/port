package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import syy.portPilotage.entity.TugboatCompanies;
import syy.portPilotage.mapper.TugboatCompaniesMapper;
import syy.portPilotage.service.TugboatCompaniesService;

@Service
public class TugboatCompaniesServiceImpl extends ServiceImpl<TugboatCompaniesMapper, TugboatCompanies>
        implements TugboatCompaniesService {
}
