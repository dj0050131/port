package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.entity.PilotageRoutes;
import syy.portPilotage.entity.RateConditions;
import syy.portPilotage.entity.StandardRates;
import syy.portPilotage.enums.LocationTypeEnum;
import syy.portPilotage.service.PilotageRoutesService;
import syy.portPilotage.service.RateConditionsService;
import syy.portPilotage.service.StandardRatesService;
import syy.portPilotage.mapper.StandardRatesMapper;
import syy.portPilotage.redis.service.CacheService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class StandardRatesServiceImpl extends ServiceImpl<StandardRatesMapper, StandardRates>
    implements StandardRatesService{
    
    private final PilotageRoutesService pilotageRoutesService;
    private final RateConditionsService rateConditionsService;
    private final CacheService cacheService;
    
    public StandardRates findApplicableRate(
            Long certificateStartLocationId,
            LocationTypeEnum certificateStartLocationType,
            Long certificateEndLocationId,
            LocationTypeEnum certificateEndLocationType,
            BigDecimal vesselNetTonnage) {
        
        String cacheKey = String.format("rate:%s:%s:%s:%s:%s",
                certificateStartLocationType != null ? certificateStartLocationType.name() : "null",
                certificateStartLocationId,
                certificateEndLocationType != null ? certificateEndLocationType.name() : "null",
                certificateEndLocationId,
                vesselNetTonnage != null ? vesselNetTonnage.toString() : "null");
        
        StandardRates cached = cacheService.getRate(cacheKey, StandardRates.class);
        if (cached != null) {
            log.debug("从缓存获取标准费率: cacheKey={}", cacheKey);
            return cached;
        }
        
        // 根据起始和结束位置确定引航区间
        LambdaQueryWrapper<PilotageRoutes> routeQueryWrapper = new LambdaQueryWrapper<>();
        routeQueryWrapper.eq(PilotageRoutes::getStartLocationId, certificateStartLocationId);
        String startType = certificateStartLocationType == null ? null : certificateStartLocationType.name();
        String endType = certificateEndLocationType == null ? null : certificateEndLocationType.name();
        routeQueryWrapper.eq(PilotageRoutes::getStartLocationType, startType);
        routeQueryWrapper.eq(PilotageRoutes::getEndLocationId, certificateEndLocationId);
        routeQueryWrapper.eq(PilotageRoutes::getEndLocationType, endType);
        PilotageRoutes pilotageRoute = pilotageRoutesService.getOne(routeQueryWrapper);

        if (pilotageRoute == null) {
            try {
                org.slf4j.LoggerFactory.getLogger(StandardRatesServiceImpl.class).warn(
                        "标准费率匹配失败：未找到引航区间 start={}:{}, end={}:{}, netTon={}",
                        startType, certificateStartLocationId, endType, certificateEndLocationId, vesselNetTonnage
                );
            } catch (Exception ignored) {}
            return null;
        }

        LambdaQueryWrapper<StandardRates> rateQueryWrapper = new LambdaQueryWrapper<>();
        rateQueryWrapper.eq(StandardRates::getRouteId, pilotageRoute.getId());
        rateQueryWrapper.eq(StandardRates::getRateStatus, "VALID");

        Date now = new Date();
        rateQueryWrapper.le(StandardRates::getEffectiveDate, now);
        rateQueryWrapper.and(w -> w.isNull(StandardRates::getExpiryDate).or().ge(StandardRates::getExpiryDate, now));

        // 净吨区间：min/max 允许为空（表示不限制）
        rateQueryWrapper.and(w -> w
                .isNull(StandardRates::getApplicableVesselMinNetTon)
                .or()
                .le(StandardRates::getApplicableVesselMinNetTon, vesselNetTonnage));
        rateQueryWrapper.and(w -> w
                .isNull(StandardRates::getApplicableVesselMaxNetTon)
                .or()
                .ge(StandardRates::getApplicableVesselMaxNetTon, vesselNetTonnage));

        rateQueryWrapper.orderByDesc(StandardRates::getEffectiveDate).orderByDesc(StandardRates::getId);

        List<StandardRates> candidates = this.list(rateQueryWrapper);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        for (StandardRates candidate : candidates) {
            List<RateConditions> conditions = rateConditionsService.list(
                    new LambdaQueryWrapper<RateConditions>()
                            .eq(RateConditions::getStandardRateId, candidate.getId())
            );

            if (conditions == null || conditions.isEmpty()) {
                return candidate;
            }

            boolean ok = conditions.stream().allMatch(c -> {
                String type = c.getConditionType();
                if (type == null) {
                    return true;
                }
                if ("NET_TON_RANGE".equalsIgnoreCase(type) || "NET_TON".equalsIgnoreCase(type) || "NET_TONNAGE".equalsIgnoreCase(type)) {
                    return evaluateNumericCondition(vesselNetTonnage, c.getConditionOperator(), c.getConditionValue());
                }
                // 其他条件留到计费阶段（有签证单上下文）再判断
                return true;
            });

            if (ok) {
                // 缓存结果
                cacheService.setRate(cacheKey, candidate);
                return candidate;
            }
        }

        // 即使没找到，也缓存 null 结果（较短时间），避免频繁查询
        cacheService.setRate(cacheKey, null);
        return null;
    }

        private boolean evaluateNumericCondition(BigDecimal left, String operator, String rawValue) {
        if (left == null) {
            return false;
        }
        String op = operator == null ? "=" : operator.trim().toUpperCase();
        String value = rawValue == null ? "" : rawValue.trim();

        try {
            switch (op) {
                case "=":
                case "EQ":
                    return left.compareTo(new BigDecimal(value)) == 0;
                case ">":
                case "GT":
                    return left.compareTo(new BigDecimal(value)) > 0;
                case ">=":
                case "GE":
                    return left.compareTo(new BigDecimal(value)) >= 0;
                case "<":
                case "LT":
                    return left.compareTo(new BigDecimal(value)) < 0;
                case "<=":
                case "LE":
                    return left.compareTo(new BigDecimal(value)) <= 0;
                case "BETWEEN": {
                    String[] parts = value.split(",");
                    if (parts.length != 2) {
                        return false;
                    }
                    BigDecimal min = new BigDecimal(parts[0].trim());
                    BigDecimal max = new BigDecimal(parts[1].trim());
                    return left.compareTo(min) >= 0 && left.compareTo(max) <= 0;
                }
                case "IN": {
                    String[] parts = value.split(",");
                    for (String p : parts) {
                        if (p == null) continue;
                        if (left.compareTo(new BigDecimal(p.trim())) == 0) {
                            return true;
                        }
                    }
                    return false;
                }
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
