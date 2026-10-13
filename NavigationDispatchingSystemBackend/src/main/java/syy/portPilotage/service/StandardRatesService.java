package syy.portPilotage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import syy.portPilotage.entity.StandardRates;
import syy.portPilotage.enums.LocationTypeEnum;

import java.math.BigDecimal;

public interface StandardRatesService extends IService<StandardRates> {
    
    StandardRates findApplicableRate(
            Long certificateStartLocationId,
            LocationTypeEnum certificateStartLocationType,
            Long certificateEndLocationId,
            LocationTypeEnum certificateEndLocationType,
            BigDecimal vesselNetTonnage);
}
