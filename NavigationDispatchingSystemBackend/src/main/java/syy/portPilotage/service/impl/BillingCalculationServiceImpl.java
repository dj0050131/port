package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.certificate.CertificateDetailDTO;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.*;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.*;
import syy.portPilotage.vo.billing.BillingResultVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingCalculationServiceImpl implements BillingCalculationService {
    
    private final VesselsService vesselsService;
    private final BillingVouchersService billingVouchersService;
    private final StandardRatesService standardRatesService;
    private final PilotageRoutesService pilotageRoutesService;
    private final ChargeItemsService chargeItemsService;
    private final ChargeDetailsService chargeDetailsService;
    private final RateRulesService rateRulesService;
    private final RateRuleTypesService rateRuleTypesService;
    private final RateConditionsService rateConditionsService;
    private final PilotageCertificatesService pilotageCertificatesService;

    private static final String AUTO_ROUTE_CODE = "AUTO";

    private void assertRouteReadyForBilling(PilotageCertificates certificate) {
        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }
        String code = certificate.getRouteCode();
        String name = certificate.getRouteName();
        boolean codeEmpty = (code == null || code.trim().isEmpty());
        boolean nameEmpty = (name == null || name.trim().isEmpty());
        boolean isAuto = (!codeEmpty && AUTO_ROUTE_CODE.equalsIgnoreCase(code.trim()));
        if (codeEmpty || nameEmpty || isAuto) {
            throw new BusinessException(
                    ErrorCode.OPERATION_NOT_ALLOWED,
                    "航线区间信息未补齐，禁止计费。请先在签证单中选择/补录航线区间（routeId 或 routeCode/routeName）后再计费。"
            );
        }
    }
    
    /**
     * 根据签证单生成计费凭证
     *
     * @param certificate 签证单
     * @return 生成的计费凭证
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillingVouchers generateBillingVoucherFromCertificate(PilotageCertificates certificate) {
        log.info("根据签证单生成计费凭证: certificateId={}", certificate.getId());

        // 最稳策略：无区间时允许生成签证单，但禁止进入计费
        assertRouteReadyForBilling(certificate);
        
        BillingVouchers voucher = billingVouchersService.createBillingVoucherFromCertificate(certificate);
        
        Vessels vessel = vesselsService.getById(certificate.getVesselId());
        if (vessel == null) {
            throw new BusinessException("未找到船舶信息");
        }
        
        StandardRates standardRate = standardRatesService.findApplicableRate(
                certificate.getStartLocationId(),
                certificate.getStartLocationType(),
                certificate.getEndLocationId(),
                certificate.getEndLocationType(),
                vessel.getNetTonnage());
        
        if (standardRate == null) {
            throw new BusinessException(
                    "未找到适用的标准费率"
                            + "（start=" + certificate.getStartLocationType() + ":" + certificate.getStartLocationId()
                            + ", end=" + certificate.getEndLocationType() + ":" + certificate.getEndLocationId()
                            + ", netTon=" + vessel.getNetTonnage()
                            + "）");
        }

        BigDecimal billableNetTon = vessel.getNetTonnage();
        if (billableNetTon == null) {
            billableNetTon = BigDecimal.ZERO;
        }
        BigDecimal minBillable = new BigDecimal("2000");
        if (billableNetTon.compareTo(minBillable) < 0) {
            billableNetTon = minBillable;
        }
        
        createChargeDetails(voucher, standardRate, certificate, vessel, billableNetTon);

        BigDecimal nauticalMiles = resolveNauticalMiles(certificate, standardRate);
        if (nauticalMiles != null && nauticalMiles.compareTo(new BigDecimal("10")) > 0) {
            BigDecimal excessMiles = nauticalMiles.subtract(new BigDecimal("10"));
            BigDecimal perTonPerMile = new BigDecimal("0.005");
            BigDecimal extraAmount = billableNetTon.multiply(excessMiles).multiply(perTonPerMile);

            ChargeItems overDistanceItem = chargeItemsService.getOne(
                    new LambdaQueryWrapper<ChargeItems>().eq(ChargeItems::getItemCode, "P003")
            );
            if (overDistanceItem != null && overDistanceItem.getId() != null) {
                chargeDetailsService.createBaseChargeDetail(voucher.getId(), overDistanceItem.getId(), extraAmount);
            } else {
                log.warn("超程加收未生成明细：未找到收费项目 P003（超程引航费）");
            }
        }

        refreshVoucherSummaryFields(voucher);
        
        log.info("计费凭证生成成功: voucherId={}", voucher.getId());
        return billingVouchersService.getById(voucher.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillingResultVO startBilling(Long certificateId) {
        PilotageCertificates certificate = pilotageCertificatesService.getById(certificateId);
        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }

        // 检查签证单状态，必须是已确认的才能计费
        if (!"CONFIRMED".equals(certificate.getSubmitStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID, "只有已确认的签证单才能计费");
        }

        BillingVouchers voucher = generateBillingVoucherFromCertificate(certificate);

        certificate.setBillingStatus("BILLED");
        pilotageCertificatesService.updateById(certificate);

        BillingResultVO resultVO = new BillingResultVO();
        resultVO.setVoucherId(voucher.getId());
        resultVO.setVoucherCode(voucher.getVoucherCode());
        resultVO.setCertificateId(certificateId);

        log.info("计费完成: certificateId={}, voucherId={}", certificateId, voucher.getId());
        return resultVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillingResultVO rebillRejectedVoucher(Long id) {

        BillingVouchers voucher = billingVouchersService.getById(id);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND);
        }

        // 检查状态，必须是被退回的才能重新计费
        if (!"REJECTED".equals(voucher.getAuditStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "只有被退回的计费凭证才能重新计费");
        }

        PilotageCertificates certificate = pilotageCertificatesService.getById(voucher.getCertificateId());
        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }

        // 基于签证单重新计算费用信息
        BillingVouchers newVoucher = this.generateBillingVoucherFromCertificate(certificate);

        voucher.setVoucherCode(newVoucher.getVoucherCode());
        voucher.setFiscalYear(newVoucher.getFiscalYear());
        voucher.setFiscalPeriod(newVoucher.getFiscalPeriod());
        voucher.setVoyageId(newVoucher.getVoyageId());
        voucher.setVesselId(newVoucher.getVesselId());
        voucher.setAgentId(newVoucher.getAgentId());
        voucher.setStartBerthId(newVoucher.getStartBerthId());
        voucher.setEndBerthId(newVoucher.getEndBerthId());
        voucher.setBillingMethod(newVoucher.getBillingMethod());
        voucher.setChargeCode(newVoucher.getChargeCode());
        voucher.setChargeType(newVoucher.getChargeType());
        voucher.setPilotageStartTime(newVoucher.getPilotageStartTime());
        voucher.setPilotageEndTime(newVoucher.getPilotageEndTime());
        voucher.setRouteType(newVoucher.getRouteType());
        voucher.setStandardRate2(newVoucher.getStandardRate2());
        voucher.setStandardRate3(newVoucher.getStandardRate3());
        voucher.setActualStandardRate2(newVoucher.getActualStandardRate2());
        voucher.setActualStandardRate3(newVoucher.getActualStandardRate3());
        voucher.setActualBillingRate2(newVoucher.getActualBillingRate2());
        voucher.setActualBillingRate3(newVoucher.getActualBillingRate3());
        voucher.setFreeChargeFlag(newVoucher.getFreeChargeFlag());
        voucher.setRoundingMethod(newVoucher.getRoundingMethod());
        voucher.setPolicyDiscountReference(newVoucher.getPolicyDiscountReference());
        voucher.setTaxInvoicingFlag(newVoucher.getTaxInvoicingFlag());
        voucher.setTaxInvoicingTime(newVoucher.getTaxInvoicingTime());
        voucher.setTaxpayerFullName(newVoucher.getTaxpayerFullName());
        voucher.setTaxpayerId(newVoucher.getTaxpayerId());
        voucher.setInvoiceCode(newVoucher.getInvoiceCode());
        voucher.setInvoiceNumber(newVoucher.getInvoiceNumber());
        voucher.setBuyerBank(newVoucher.getBuyerBank());
        voucher.setTotalAmount(newVoucher.getTotalAmount());
        voucher.setAuditStatus("PENDING");
        voucher.setPaymentStatus(newVoucher.getPaymentStatus());
        voucher.setDueDate(newVoucher.getDueDate());
        voucher.setDiscountAmount(newVoucher.getDiscountAmount());
        voucher.setLateFee(newVoucher.getLateFee());
        voucher.setActualReceivedAmount(newVoucher.getActualReceivedAmount());
        voucher.setReceiptDate(newVoucher.getReceiptDate());
        voucher.setCancellationNotes(null); // 清除驳回原因
        voucher.setUpdatedAt(new Date());

        billingVouchersService.updateById(voucher);

        BillingResultVO resultVO = new BillingResultVO();
        resultVO.setVoucherId(voucher.getId());
        resultVO.setVoucherCode(voucher.getVoucherCode());
        return resultVO;
    }

    @Override
    public BillingResultVO getAuditDetail(Long id) {
        BillingVouchers voucher = billingVouchersService.getById(id);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND);
        }

        BillingResultVO resultVO = billingVouchersService.buildBillingResultVO(voucher);

        // 补充签证单信息
        if (voucher.getCertificateId() != null) {
            PilotageCertificates certificate = pilotageCertificatesService.getById(voucher.getCertificateId());
            if (certificate != null) {
                resultVO.setCertificateCode(certificate.getCertificateCode());

                if (certificate.getVesselId() != null) {
                    Vessels vessel = vesselsService.getById(certificate.getVesselId());
                    if (vessel != null) {
                        resultVO.setVesselNameZh(vessel.getNameZh());
                        resultVO.setVesselNameEn(vessel.getNameEn());
                        resultVO.setGrossTonnage(vessel.getGrossTonnage());
                        resultVO.setNetTonnage(vessel.getNetTonnage());
                    }
                }

                resultVO.setStartLocationName(certificate.getStartLocationName());
                resultVO.setEndLocationName(certificate.getEndLocationName());

                resultVO.setPilotageStartTime(certificate.getPilotageStartTime());
                resultVO.setPilotageEndTime(certificate.getPilotageEndTime());
            }
        }

        LambdaQueryWrapper<ChargeDetails> chargeDetailWrapper = new LambdaQueryWrapper<>();
        chargeDetailWrapper.eq(ChargeDetails::getVoucherId, id);
        List<ChargeDetails> chargeDetails = chargeDetailsService.list(chargeDetailWrapper);

        List<BillingResultVO.ChargeDetailVO> chargeDetailVOs = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal baseAmount = BigDecimal.ZERO;
        BigDecimal surchargeAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = voucher.getDiscountAmount() != null ? voucher.getDiscountAmount() : BigDecimal.ZERO;

        for (ChargeDetails detail : chargeDetails) {
            BillingResultVO.ChargeDetailVO chargeDetailVO = new BillingResultVO.ChargeDetailVO();
            chargeDetailVO.setAmount(detail.getActualChargeAmount());
            chargeDetailVO.setNotes(detail.getNotes());
            chargeDetailVO.setChargeTime(detail.getChargeTime());

            if (detail.getItemId() != null) {
                ChargeItems chargeItem = chargeItemsService.getById(detail.getItemId());
                if (chargeItem != null) {
                    chargeDetailVO.setChargeName(chargeItem.getItemName());
                    chargeDetailVO.setDescription(chargeItem.getItemNotes());

                    // 注意：ChargeItems实体中没有unitPrice字段，所以不能设置
                }
            }

            chargeDetailVOs.add(chargeDetailVO);

            // 累计总金额
            if (detail.getActualChargeAmount() != null) {
                totalAmount = totalAmount.add(detail.getActualChargeAmount());
            }
        }

        resultVO.setChargeDetails(chargeDetailVOs);
        resultVO.setBaseAmount(baseAmount);
        resultVO.setSurchargeAmount(surchargeAmount);
        resultVO.setDiscountAmount(discountAmount);
        resultVO.setTotalAmount(totalAmount);
        return resultVO;
    }

    @Override
    public PageResult<CertificateDetailDTO> getRejectedBillingVouchers(PageQueryDTO query) {

        // 这里不再返回“被退回的凭证”，而是返回“需要重新计费的签证单”（billing_status = REBILLING）
        Page<CertificateDetailDTO> page = new Page<>(query.getCurrent(), query.getSize());

        java.util.Map<String, Object> params = query.getParams() != null ? query.getParams() : new java.util.HashMap<>();
        // 强制限定为“已确认 + 重计费”
        params.put("submitStatus", "CONFIRMED");
        params.put("billingStatus", "REBILLING");

        IPage<CertificateDetailDTO> result = pilotageCertificatesService.selectCertificatePage(page, params);
        return PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );
    }

    /**
     * 航距取值口径统一：
     * - 优先使用 pilotage_routes.distance_nautical（更贴近业务“航次区间距离”）
     * - 兜底使用 standard_rates.nautical_miles（避免历史数据缺失导致 NPE）
     */
    private BigDecimal resolveNauticalMiles(PilotageCertificates certificate, StandardRates standardRate) {
        try {
            if (certificate != null
                    && certificate.getStartLocationType() != null
                    && certificate.getEndLocationType() != null
                    && certificate.getStartLocationId() != null
                    && certificate.getEndLocationId() != null) {
                PilotageRoutes route = pilotageRoutesService.getOne(
                        new LambdaQueryWrapper<PilotageRoutes>()
                                .eq(PilotageRoutes::getStartLocationType, certificate.getStartLocationType().name())
                                .eq(PilotageRoutes::getStartLocationId, certificate.getStartLocationId())
                                .eq(PilotageRoutes::getEndLocationType, certificate.getEndLocationType().name())
                                .eq(PilotageRoutes::getEndLocationId, certificate.getEndLocationId())
                );
                if (route != null && route.getDistanceNautical() != null) {
                    return route.getDistanceNautical();
                }
            }
        } catch (Exception e) {
            log.warn("航距解析失败，使用标准费率 nautical_miles 兜底: {}", e.getMessage());
        }
        return standardRate == null ? null : standardRate.getNauticalMiles();
    }

    
    private void refreshVoucherSummaryFields(BillingVouchers voucher) {
        if (voucher == null || voucher.getId() == null) {
            return;
        }

        List<ChargeDetails> details = chargeDetailsService.list(
                new LambdaQueryWrapper<ChargeDetails>()
                        .eq(ChargeDetails::getVoucherId, voucher.getId())
        );

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (details != null) {
            for (ChargeDetails d : details) {
                BigDecimal std = d.getStandardChargeAmount();
                BigDecimal act = d.getActualChargeAmount();

                if (act != null) {
                    totalAmount = totalAmount.add(act);
                }

                // 折扣金额：优先按“标准-实际”的差额统计（仅当实际 < 标准），否则若实际为负则按绝对值计折扣
                if (std != null && act != null) {
                    if (act.compareTo(std) < 0) {
                        discountAmount = discountAmount.add(std.subtract(act));
                    }
                } else if (act != null && act.compareTo(BigDecimal.ZERO) < 0) {
                    discountAmount = discountAmount.add(act.abs());
                }

                // 若明细本身记录了折扣原因，也认为是折扣项（补充统计口径）
                if (d.getDiscountReason() != null && !d.getDiscountReason().trim().isEmpty()) {
                    if (act != null && act.compareTo(BigDecimal.ZERO) > 0) {
                        discountAmount = discountAmount.add(act);
                    }
                }
            }
        }

        voucher.setTotalAmount(totalAmount);
        voucher.setDiscountAmount(discountAmount);

        if (voucher.getLateFee() == null) {
            voucher.setLateFee(BigDecimal.ZERO);
        }
        if (voucher.getActualReceivedAmount() == null) {
            voucher.setActualReceivedAmount(BigDecimal.ZERO);
        }
        if (voucher.getTransferStatus() == null ) {
            voucher.setTransferStatus("PENDING");
        }

        voucher.setUpdatedAt(new Date());
        billingVouchersService.updateById(voucher);
    }
    
    /**
     * 创建收费明细
     *
     * @param voucher      计费凭证
     * @param standardRate 标准费率
     * @param certificate  签证单
     * @param vessel       船舶
     */
    private void createChargeDetails(BillingVouchers voucher, StandardRates standardRate,
                                     PilotageCertificates certificate, Vessels vessel,
                                     BigDecimal billableNetTon) {
        List<RateRules> rateRules = rateRulesService.list(
                new LambdaQueryWrapper<RateRules>()
                        .eq(RateRules::getStandardRateId, standardRate.getId())
                        .eq(RateRules::getIsActive, 1)
        );

        List<RateConditions> conditions = rateConditionsService.list(
                new LambdaQueryWrapper<RateConditions>()
                        .eq(RateConditions::getStandardRateId, standardRate.getId())
        );
        
        // 预加载规则类型并按 sort_order 排序（百分比类也依赖顺序）
        Set<Long> ruleTypeIds = rateRules.stream()
                .map(RateRules::getRuleTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, RateRuleTypes> ruleTypeMap = ruleTypeIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : rateRuleTypesService.listByIds(ruleTypeIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(RateRuleTypes::getId, rt -> rt, (a, b) -> a));

        rateRules.sort(
                Comparator
                        .comparing((RateRules r) -> {
                            RateRuleTypes rt = ruleTypeMap.get(r.getRuleTypeId());
                            Integer so = rt == null ? null : rt.getSortOrder();
                            return so == null ? Integer.MAX_VALUE : so;
                        })
                        .thenComparingLong(r -> r.getId() == null ? Long.MAX_VALUE : r.getId())
        );

        BigDecimal runningSubtotal = BigDecimal.ZERO;

        for (RateRules rateRule : rateRules) {
            RateRuleTypes ruleType = ruleTypeMap.get(rateRule.getRuleTypeId());
            if (ruleType == null) {
                ruleType = rateRuleTypesService.getById(rateRule.getRuleTypeId());
            }
            if (ruleType == null) continue;

            // 每条规则先做“适用性判断”：净吨区间（rule 覆盖 standard） + rate_conditions（动态）
            if (!isRuleApplicable(standardRate, rateRule, ruleType, conditions, certificate, vessel)) {
                continue;
            }

            Long chargeItemId = ruleType.getChargeItemId();
            if (chargeItemId == null) continue;

            ChargeItems chargeItem = chargeItemsService.getById(chargeItemId);
            if (chargeItem == null) continue;

            BigDecimal amount = calculateFeeByRuleType(standardRate, rateRule, ruleType, certificate, vessel, billableNetTon, runningSubtotal);
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                chargeDetailsService.createBaseChargeDetail(voucher.getId(), chargeItem.getId(), amount);
                runningSubtotal = runningSubtotal.add(amount);
            }
        }
    }
    
    /**
     * 规则适用性判断：
     * - 先看净吨区间（RateRules 可覆盖 StandardRates 的 min/max）
     * - 再评估 rate_conditions（目前按“标准费率维度”全局生效；可通过 condition_type 表达不同字段）
     */
    private boolean isRuleApplicable(StandardRates standardRate,
                                    RateRules rateRule,
                                    RateRuleTypes ruleType,
                                    List<RateConditions> conditions,
                                    PilotageCertificates certificate,
                                    Vessels vessel) {
        if (vessel == null || vessel.getNetTonnage() == null) {
            return false;
        }

        BigDecimal min = rateRule.getApplicableVesselMinNetTon() != null ? rateRule.getApplicableVesselMinNetTon() : standardRate.getApplicableVesselMinNetTon();
        BigDecimal max = rateRule.getApplicableVesselMaxNetTon() != null ? rateRule.getApplicableVesselMaxNetTon() : standardRate.getApplicableVesselMaxNetTon();
        if (min != null && vessel.getNetTonnage().compareTo(min) < 0) return false;
        if (max != null && vessel.getNetTonnage().compareTo(max) > 0) return false;

        if (conditions == null || conditions.isEmpty()) return true;

        for (RateConditions c : conditions) {
            if (!evaluateCondition(c, standardRate, certificate, vessel)) {
                return false;
            }
        }
        return true;
    }

        private boolean evaluateCondition(RateConditions condition, StandardRates standardRate, PilotageCertificates certificate, Vessels vessel) {
        if (condition == null) return true;
        String type = condition.getConditionType();
        String operator = condition.getConditionOperator();
        String value = condition.getConditionValue();

        if (type == null) return true;
        String t = type.trim().toUpperCase();
        String op = operator == null ? "=" : operator.trim().toUpperCase();

        try {
            switch (t) {
                case "NET_TON_RANGE":
                case "NET_TON":
                case "NET_TONNAGE":
                    return evaluateNumericCondition(vessel.getNetTonnage(), op, value);

                case "NAUTICAL_MILES":
                    return evaluateNumericCondition(standardRate.getNauticalMiles(), op, value);

                case "PILOTAGE_HOURS": {
                    BigDecimal hours = calcPilotageHoursCeil(certificate);
                    return evaluateNumericCondition(hours, op, value);
                }

                case "BERTHING_ASSISTANCE_FLAG":
                    return evaluateStringCondition(certificate == null ? null : certificate.getBerthingAssistanceFlag(), op, value);

                case "DETENTION_FLAG":
                    return evaluateStringCondition(certificate == null ? null : certificate.getDetentionFlag(), op, value);

                default:
                    // 未识别的 condition_type：为避免误算，保守返回 true（不阻塞计费）；同时打日志便于配置排查
                    log.warn("未识别的费率条件类型: type={}, operator={}, value={}, conditionId={}", type, operator, value, condition.getId());
                    return true;
            }
        } catch (Exception e) {
            log.error("条件评估失败: conditionId={}, type={}, operator={}, value={}, error={}", condition.getId(), type, operator, value, e.getMessage());
            return false;
        }
    }
    
    /**
     * 根据规则类型计算费用
     * 
     * @param rateRule 费率规则
     * @param ruleType 规则类型
     * @param vessel 船舶
     * @param certificate 签证单
     * @return 费用金额
     */
    private BigDecimal calculateFeeByRuleType(StandardRates standardRate,
                                             RateRules rateRule,
                                             RateRuleTypes ruleType,
                                             PilotageCertificates certificate,
                                             Vessels vessel,
                                             BigDecimal billableNetTon,
                                             BigDecimal runningSubtotal) {
        String method = ruleType.getCalculationMethod() == null ? "PER_NET_TON" : ruleType.getCalculationMethod().trim().toUpperCase();
        BigDecimal rateValue = rateRule.getRateValue() == null ? BigDecimal.ZERO : rateRule.getRateValue();

        switch (method) {
            case "PER_NET_TON": {
                BigDecimal ton = billableNetTon;
                if (ton == null) {
                    ton = vessel == null ? null : vessel.getNetTonnage();
                }
                if (ton == null) return BigDecimal.ZERO;
                return rateValue.multiply(ton);
            }
            case "PER_NAUTICAL_MILE": {
                BigDecimal miles = standardRate.getNauticalMiles();
                if (miles == null) return BigDecimal.ZERO;
                return rateValue.multiply(miles);
            }
            case "PER_HOUR": {
                BigDecimal hours = calcPilotageHoursCeil(certificate);
                if (hours == null) return BigDecimal.ZERO;
                return rateValue.multiply(hours);
            }
            case "FIXED":
                return rateValue;
            case "PERCENTAGE": {
                // 百分比：默认以“已计算的小计”为基数；通过 sort_order 控制先后关系
                if (runningSubtotal == null) runningSubtotal = BigDecimal.ZERO;
                return runningSubtotal
                        .multiply(rateValue)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
            default:
                log.warn("未知 calculation_method: {} (ruleTypeCode={}, ruleTypeId={})", method, ruleType.getRuleTypeCode(), ruleType.getId());
                return BigDecimal.ZERO;
        }
    }

    private BigDecimal calcPilotageHoursCeil(PilotageCertificates certificate) {
        if (certificate == null || certificate.getPilotageStartTime() == null || certificate.getPilotageEndTime() == null) {
            return BigDecimal.ZERO;
        }
        long startMs = certificate.getPilotageStartTime().getTime();
        long endMs = certificate.getPilotageEndTime().getTime();
        long diffMs = endMs - startMs;
        if (diffMs <= 0) return BigDecimal.ZERO;

        long minutes = diffMs / (1000L * 60L);
        if (diffMs % (1000L * 60L) != 0) {
            minutes += 1; // 向上取整到分钟
        }
        long hoursCeil = (minutes + 59) / 60; // 向上取整到小时
        return new BigDecimal(hoursCeil);
    }

    private boolean evaluateStringCondition(String left, String operator, String rawValue) {
        if (left == null) return false;
        String op = operator == null ? "=" : operator.trim().toUpperCase();
        String value = rawValue == null ? "" : rawValue.trim();

        switch (op) {
            case "=":
            case "EQ":
                return left.equals(value);
            case "!=":
            case "NE":
                return !left.equals(value);
            case "IN": {
                String[] parts = value.split(",");
                for (String p : parts) {
                    if (p != null && left.equals(p.trim())) return true;
                }
                return false;
            }
            case "CONTAINS":
            case "LIKE":
                return left.contains(value);
            default:
                return false;
        }
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
                case "!=":
                case "NE":
                    return left.compareTo(new BigDecimal(value)) != 0;
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
                        // 兼容常见的“min-max”区间写法
                        parts = value.split("-");
                    }
                    if (parts.length != 2) return false;
                    BigDecimal min = new BigDecimal(parts[0].trim());
                    BigDecimal max = new BigDecimal(parts[1].trim());
                    return left.compareTo(min) >= 0 && left.compareTo(max) <= 0;
                }
                case "IN": {
                    String[] parts = value.split(",");
                    for (String p : parts) {
                        if (p == null) continue;
                        if (left.compareTo(new BigDecimal(p.trim())) == 0) return true;
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