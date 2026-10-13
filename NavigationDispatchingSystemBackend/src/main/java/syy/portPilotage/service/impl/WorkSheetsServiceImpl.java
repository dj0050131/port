package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.entity.WorkSheets;
import syy.portPilotage.service.WorkSheetsService;
import syy.portPilotage.mapper.WorkSheetsMapper;
import org.springframework.stereotype.Service;

@Service
public class WorkSheetsServiceImpl extends ServiceImpl<WorkSheetsMapper, WorkSheets>
    implements WorkSheetsService{

}
