package syy.portPilotage.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.base.StandardRatesWithRulesDTO;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.RateRuleTypes;
import syy.portPilotage.entity.RateRules;
import syy.portPilotage.entity.RateConditions;
import syy.portPilotage.entity.StandardRates;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.RateConditionsService;
import syy.portPilotage.service.RateRuleTypesService;
import syy.portPilotage.service.RateRulesService;
import syy.portPilotage.service.StandardRatesService;
import syy.portPilotage.util.SecurityUtil;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/base/standardRates")
@RequiredArgsConstructor
@Tag(name = "标准费率信息管理", description = "标准费率信息相关接口")
public class StandardRatesController {

    @Autowired
    private StandardRatesService standardRatesService;
    
    @Autowired
    private RateRulesService rateRulesService;

    @Autowired
    private RateConditionsService rateConditionsService;
    
    @Autowired
    private RateRuleTypesService rateRuleTypesService;

    @PostMapping
    @Operation(summary = "创建标准费率信息及费率规则")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('BILLING:PARAMETERS:MANAGE')")
    public Result<Long> create(@Valid @RequestBody StandardRatesWithRulesDTO standardRateWithRules) {
        log.info("创建标准费率信息及费率规则: rateCode={}", standardRateWithRules.getStandardRate().getRateCode());
        
        StandardRates standardRate = standardRateWithRules.getStandardRate();

        // created_by / updated_by：强制使用当前登录用户，避免前端传 0 触发外键约束
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "未获取到当前登录用户，无法保存标准费率");
        }
        standardRate.setCreatedBy(currentUserId);
        standardRate.setUpdatedBy(currentUserId);
        if (standardRate.getRateStatus() == null) {
            standardRate.setRateStatus("VALID");
        } else {
            String rs = String.valueOf(standardRate.getRateStatus()).trim().toUpperCase();
            if (!"VALID".equals(rs) && !"INVALID".equals(rs)) {
                standardRate.setRateStatus("VALID");
            } else {
                standardRate.setRateStatus(rs);
            }
        }
        if (standardRate.getCurrencyUnit() == null || String.valueOf(standardRate.getCurrencyUnit()).trim().isEmpty()) {
            standardRate.setCurrencyUnit("CNY");
        }
        standardRate.setCreatedAt(new Date());
        standardRate.setUpdatedAt(new Date());
        
        standardRatesService.save(standardRate);
        
        if (standardRateWithRules.getRateRules() != null && !standardRateWithRules.getRateRules().isEmpty()) {
            List<RateRules> rules = standardRateWithRules.getRateRules().stream()
                    .map(rule -> {
                        rule.setStandardRateId(standardRate.getId());
                        rule.setCreatedAt(new Date());
                        rule.setUpdatedAt(new Date());
                        return rule;
                    })
                    .collect(Collectors.toList());
            rateRulesService.saveBatch(rules);
        }
        
        log.info("标准费率信息及费率规则创建成功: id={}", standardRate.getId());
        return Result.success(standardRate.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标准费率信息及费率规则")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('BILLING:PARAMETERS:MANAGE')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StandardRatesWithRulesDTO standardRateWithRules) {
        log.info("更新标准费率信息及费率规则: id={}", id);

        StandardRates existing = standardRatesService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "标准费率信息不存在");
        }

        StandardRates standardRate = standardRateWithRules.getStandardRate();
        standardRate.setId(id);
        // updated_by：强制使用当前登录用户；created_by 保留原值
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId != null) {
            standardRate.setUpdatedBy(currentUserId);
        } else {
            standardRate.setUpdatedBy(existing.getUpdatedBy());
        }
        standardRate.setCreatedBy(existing.getCreatedBy());
        if (standardRate.getRateStatus() == null) {
            standardRate.setRateStatus(existing.getRateStatus() == null ? "VALID" : String.valueOf(existing.getRateStatus()));
        } else {
            String rs = String.valueOf(standardRate.getRateStatus()).trim().toUpperCase();
            if (!"VALID".equals(rs) && !"INVALID".equals(rs)) {
                standardRate.setRateStatus(existing.getRateStatus() == null ? "VALID" : String.valueOf(existing.getRateStatus()));
            } else {
                standardRate.setRateStatus(rs);
            }
        }
        if (standardRate.getCurrencyUnit() == null || String.valueOf(standardRate.getCurrencyUnit()).trim().isEmpty()) {
            standardRate.setCurrencyUnit(existing.getCurrencyUnit() == null ? "CNY" : existing.getCurrencyUnit());
        }
        standardRate.setUpdatedAt(new Date());

        standardRatesService.updateById(standardRate);

        rateRulesService.remove(new LambdaQueryWrapper<RateRules>().eq(RateRules::getStandardRateId, id));
        
        if (standardRateWithRules.getRateRules() != null && !standardRateWithRules.getRateRules().isEmpty()) {
            List<RateRules> rules = standardRateWithRules.getRateRules().stream()
                    .map(rule -> {
                        rule.setStandardRateId(id);
                        rule.setUpdatedAt(new Date());
                        if (rule.getCreatedAt() == null) {
                            rule.setCreatedAt(new Date());
                        }
                        return rule;
                    })
                    .collect(Collectors.toList());
            rateRulesService.saveBatch(rules);
        }

        log.info("标准费率信息及费率规则更新成功: id={}", id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标准费率信息")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('BILLING:PARAMETERS:MANAGE')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除标准费率信息: id={}", id);

        StandardRates existing = standardRatesService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "标准费率信息不存在");
        }

        // 先删除依赖数据，避免外键约束导致删除失败
        rateRulesService.remove(new LambdaQueryWrapper<RateRules>().eq(RateRules::getStandardRateId, id));
        rateConditionsService.remove(new LambdaQueryWrapper<RateConditions>().eq(RateConditions::getStandardRateId, id));

        standardRatesService.removeById(id);

        log.info("标准费率信息删除成功: id={}", id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取标准费率信息详情（包含费率规则）")
    public Result<StandardRatesWithRulesDTO> getById(@PathVariable Long id) {
        StandardRates standardRate = standardRatesService.getById(id);
        if (standardRate == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "标准费率信息不存在");
        }

        List<RateRules> rateRules = rateRulesService.list(
                new LambdaQueryWrapper<RateRules>()
                        .eq(RateRules::getStandardRateId, id)
        );
        
        Map<Long, RateRuleTypes> ruleTypeMap = rateRuleTypesService.list().stream()
                .collect(Collectors.toMap(RateRuleTypes::getId, r -> r));

        StandardRatesWithRulesDTO result = new StandardRatesWithRulesDTO();
        result.setStandardRate(standardRate);
        result.setRateRules(rateRules);
        result.setRuleTypeMap(ruleTypeMap);

        return Result.success(result);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询标准费率信息（包含规则统计）")
    public Result<PageResult<StandardRates>> page(PageQueryDTO query) {
        IPage<StandardRates> result = standardRatesService.page(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(query.getCurrent(), query.getSize())
        );

        PageResult<StandardRates> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有标准费率信息列表（包含规则统计）")
    public Result<List<StandardRates>> list() {
        List<StandardRates> list = standardRatesService.list();
        return Result.success(list);
    }

    @GetMapping("/byCategory/{categoryId}")
    @Operation(summary = "根据分类ID获取标准费率信息")
    public Result<List<StandardRates>> getByCategoryId(@PathVariable Long categoryId) {
        log.info("根据分类ID获取标准费率信息: categoryId={}", categoryId);

        List<StandardRates> list = standardRatesService.list(
                new LambdaQueryWrapper<StandardRates>()
                        .eq(StandardRates::getCategoryId, categoryId)
        );

        return Result.success(list);
    }

    @GetMapping("/byRoute/{routeId}")
    @Operation(summary = "根据航线ID获取标准费率信息")
    public Result<List<StandardRates>> getByRouteId(@PathVariable Long routeId) {
        log.info("根据航线ID获取标准费率信息: routeId={}", routeId);

        List<StandardRates> list = standardRatesService.list(
                new LambdaQueryWrapper<StandardRates>()
                        .eq(StandardRates::getRouteId, routeId)
        );

        return Result.success(list);
    }

    @GetMapping("/active")
    @Operation(summary = "获取当前生效的标准费率信息")
    public Result<List<StandardRates>> getActiveRates() {
        log.info("获取当前生效的标准费率信息");

        Date now = new Date();
        List<StandardRates> list = standardRatesService.list(
                new LambdaQueryWrapper<StandardRates>()
                        .le(StandardRates::getEffectiveDate, now)
                        .and(w -> w.isNull(StandardRates::getExpiryDate).or().ge(StandardRates::getExpiryDate, now))
                        // 只返回有效费率，避免前端出现 “disabled: wrong unit rates”
                        .eq(StandardRates::getRateStatus, "VALID")
        );

        return Result.success(list);
    }
    
    @GetMapping("/search")
    @Operation(summary = "根据费率编码或备注搜索标准费率信息")
    public Result<List<StandardRates>> search(@RequestParam String keyword) {
        log.info("搜索标准费率信息: keyword={}", keyword);
        
        List<StandardRates> list = standardRatesService.list(
                new LambdaQueryWrapper<StandardRates>()
                        .like(StandardRates::getRateCode, keyword)
                        .or()
                        .like(StandardRates::getNotes, keyword)
        );
        
        return Result.success(list);
    }
    
    @GetMapping("/byNetTonnage")
    @Operation(summary = "根据船舶净吨获取适用的标准费率信息")
    public Result<List<StandardRates>> getByNetTonnage(@RequestParam BigDecimal minNetTonnage, 
                                                      @RequestParam BigDecimal maxNetTonnage) {
        log.info("根据船舶净吨获取标准费率信息: minNetTonnage={}, maxNetTonnage={}", minNetTonnage, maxNetTonnage);
        
        List<StandardRates> list = standardRatesService.list(
                new LambdaQueryWrapper<StandardRates>()
                        .le(StandardRates::getApplicableVesselMinNetTon, minNetTonnage)
                        .gt(StandardRates::getApplicableVesselMaxNetTon, maxNetTonnage)
        );
        
        return Result.success(list);
    }
    
    @GetMapping("/byDateRange")
    @Operation(summary = "根据日期范围获取标准费率信息")
    public Result<List<StandardRates>> getByDateRange(@RequestParam Date startDate, 
                                                     @RequestParam Date endDate) {
        log.info("根据日期范围获取标准费率信息: startDate={}, endDate={}", startDate, endDate);
        
        List<StandardRates> list = standardRatesService.list(
                new LambdaQueryWrapper<StandardRates>()
                        .le(StandardRates::getEffectiveDate, endDate)
                        .ge(StandardRates::getExpiryDate, startDate)
        );
        
        return Result.success(list);
    }
    
    @GetMapping("/byRuleTypeName")
    @Operation(summary = "根据费率规则类型名称获取标准费率信息")
    public Result<List<StandardRatesWithRulesDTO>> getByRuleTypeName(@RequestParam String ruleTypeName) {
        log.info("根据费率规则类型名称获取标准费率信息: ruleTypeName={}", ruleTypeName);
        
        RateRuleTypes ruleType = rateRuleTypesService.getOne(
                new LambdaQueryWrapper<RateRuleTypes>()
                        .eq(RateRuleTypes::getRuleTypeName, ruleTypeName)
        );
        
        if (ruleType == null) {
            return Result.success(List.of());
        }
        
        List<RateRules> rateRules = rateRulesService.list(
                new LambdaQueryWrapper<RateRules>()
                        .eq(RateRules::getRuleTypeId, ruleType.getId())
        );
        
        List<Long> standardRateIds = rateRules.stream()
                .map(RateRules::getStandardRateId)
                .distinct()
                .collect(Collectors.toList());
        
        if (standardRateIds.isEmpty()) {
            return Result.success(List.of());
        }
        
        List<StandardRates> standardRates = standardRatesService.list(
                new LambdaQueryWrapper<StandardRates>()
                        .in(StandardRates::getId, standardRateIds)
        );
        
        List<StandardRatesWithRulesDTO> result = standardRates.stream().map(rate -> {
            StandardRatesWithRulesDTO dto = new StandardRatesWithRulesDTO();
            dto.setStandardRate(rate);
            
            List<RateRules> rules = rateRules.stream()
                    .filter(rule -> rule.getStandardRateId().equals(rate.getId()))
                    .collect(Collectors.toList());
            dto.setRateRules(rules);
            
            return dto;
        }).collect(Collectors.toList());
        
        return Result.success(result);
    }
    
    @GetMapping("/rules/types")
    @Operation(summary = "获取所有费率规则类型")
    public Result<List<RateRuleTypes>> getAllRateRuleTypes() {
        log.info("获取所有费率规则类型");
        
        List<RateRuleTypes> list = rateRuleTypesService.list();
        return Result.success(list);
    }
    
    @GetMapping("/rules/types/system")
    @Operation(summary = "获取所有系统内置费率规则类型")
    public Result<List<RateRuleTypes>> getSystemRateRuleTypes() {
        log.info("获取所有系统内置费率规则类型");
        
        List<RateRuleTypes> list = rateRuleTypesService.list(
                new LambdaQueryWrapper<RateRuleTypes>()
                        .eq(RateRuleTypes::getIsSystem, 1)
        );
        
        return Result.success(list);
    }

}