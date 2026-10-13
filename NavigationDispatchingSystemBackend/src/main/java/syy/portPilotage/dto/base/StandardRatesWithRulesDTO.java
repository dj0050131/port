package syy.portPilotage.dto.base;

import lombok.Data;
import syy.portPilotage.entity.RateRuleTypes;
import syy.portPilotage.entity.RateRules;
import syy.portPilotage.entity.StandardRates;

import java.util.List;
import java.util.Map;
@Data
public class StandardRatesWithRulesDTO {
    private StandardRates standardRate;
    private List<RateRules> rateRules;
    private Map<Long, RateRuleTypes> ruleTypeMap;
}