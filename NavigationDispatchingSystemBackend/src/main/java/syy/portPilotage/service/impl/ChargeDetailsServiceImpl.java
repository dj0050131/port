package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import syy.portPilotage.entity.ChargeDetails;
import syy.portPilotage.service.ChargeDetailsService;
import syy.portPilotage.mapper.ChargeDetailsMapper;
import syy.portPilotage.service.ChargeItemsService;
import syy.portPilotage.util.CodeGeneratorUtil;

import java.math.BigDecimal;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ChargeDetailsServiceImpl extends ServiceImpl<ChargeDetailsMapper, ChargeDetails>
    implements ChargeDetailsService {

    private final ChargeItemsService chargeItemsService;
    private final CodeGeneratorUtil codeGeneratorUtil;

    public ChargeDetails createBaseChargeDetail(Long voucherId, Long itemId, BigDecimal amount) {
        BigDecimal processedAmount = processAmountPrecision(amount);

        ChargeDetails chargeDetail = new ChargeDetails();
        chargeDetail.setVoucherId(voucherId);
        chargeDetail.setChargeDetailCode(codeGeneratorUtil.generateChargeDetailCode());
        chargeDetail.setItemId(itemId);
        chargeDetail.setStandardChargeAmount(processedAmount);
        chargeDetail.setActualChargeAmount(processedAmount);
        chargeDetail.setChargeTime(new Date());
        chargeDetail.setCreatedAt(new Date());
        chargeDetail.setUpdatedAt(new Date());

        this.save(chargeDetail);
        return chargeDetail;
    }

    private BigDecimal processAmountPrecision(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal scaledAmount = amount.setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal maxValue = new BigDecimal("99999999.99");
        BigDecimal minValue = new BigDecimal("-99999999.99");

        if (scaledAmount.compareTo(maxValue) > 0) {
            scaledAmount = maxValue;
        } else if (scaledAmount.compareTo(minValue) < 0) {
            scaledAmount = minValue;
        }

        return scaledAmount;
    }
}