package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import syy.portPilotage.entity.FeeWriteOffs;
import syy.portPilotage.entity.PaymentInfo;
import syy.portPilotage.mapper.FeeWriteOffsMapper;
import syy.portPilotage.mapper.PaymentInfoMapper;
import syy.portPilotage.service.FeeWriteOffsService;
import syy.portPilotage.service.PaymentInfoService;

@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo>
        implements PaymentInfoService {

}