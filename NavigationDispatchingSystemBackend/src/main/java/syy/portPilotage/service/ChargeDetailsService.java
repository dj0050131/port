package syy.portPilotage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import syy.portPilotage.entity.ChargeDetails;

import java.math.BigDecimal;

public interface ChargeDetailsService extends IService<ChargeDetails> {
    
    ChargeDetails createBaseChargeDetail(Long voucherId, Long itemId, BigDecimal amount);
}