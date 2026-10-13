package syy.portPilotage.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.RateRuleTypes;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.RateRuleTypesService;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/base/rateRuleTypes")
@RequiredArgsConstructor
@Tag(name = "费率规则类型管理", description = "费率规则类型相关接口")
public class RateRuleTypesController {

    @Autowired
    private RateRuleTypesService rateRuleTypesService;

    @PostMapping
    @Operation(summary = "创建费率规则类型")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('BASE:RATE_RULE_TYPES:MANAGE') or hasAnyRole('FINANCE','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody RateRuleTypes rateRuleType) {
        log.info("创建费率规则类型: ruleTypeName={}", rateRuleType.getRuleTypeName());
        
        // 检查规则类型编码是否已存在
        RateRuleTypes existing = rateRuleTypesService.getOne(
            new LambdaQueryWrapper<RateRuleTypes>()
                .eq(RateRuleTypes::getRuleTypeCode, rateRuleType.getRuleTypeCode())
        );
        
        if (existing != null) {
            throw new BusinessException(ErrorCode.DATA_EXISTS, ErrorCode.DATA_EXISTS.getMessage());
        }
        
        rateRuleType.setCreatedAt(new Date());
        rateRuleType.setUpdatedAt(new Date());
        
        rateRuleTypesService.save(rateRuleType);
        
        log.info("费率规则类型创建成功: id={}", rateRuleType.getId());
        return Result.success(rateRuleType.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新费率规则类型")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('BASE:RATE_RULE_TYPES:MANAGE') or hasAnyRole('FINANCE','DISPATCHER')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RateRuleTypes rateRuleType) {
        log.info("更新费率规则类型: id={}", id);

        RateRuleTypes existing = rateRuleTypesService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, ErrorCode.DATA_NOT_FOUND.getMessage());
        }

        // 如果修改了规则类型编码，需要检查是否与其他记录冲突
        if (!existing.getRuleTypeCode().equals(rateRuleType.getRuleTypeCode())) {
            RateRuleTypes codeExisting = rateRuleTypesService.getOne(
                new LambdaQueryWrapper<RateRuleTypes>()
                    .eq(RateRuleTypes::getRuleTypeCode, rateRuleType.getRuleTypeCode())
            );
            
            if (codeExisting != null) {
                throw new BusinessException(ErrorCode.DATA_EXISTS, ErrorCode.DATA_EXISTS.getMessage());
            }
        }

        rateRuleType.setId(id);
        rateRuleType.setUpdatedAt(new Date());

        rateRuleTypesService.updateById(rateRuleType);

        log.info("费率规则类型更新成功: id={}", id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除费率规则类型")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('BASE:RATE_RULE_TYPES:MANAGE') or hasAnyRole('FINANCE','DISPATCHER')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除费率规则类型: id={}", id);

        RateRuleTypes existing = rateRuleTypesService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, ErrorCode.DATA_NOT_FOUND.getMessage());
        }
        
        // 检查是否有规则在使用此类型

        rateRuleTypesService.removeById(id);

        log.info("费率规则类型删除成功: id={}", id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取费率规则类型详情")
    public Result<RateRuleTypes> getById(@PathVariable Long id) {
        RateRuleTypes rateRuleType = rateRuleTypesService.getById(id);
        if (rateRuleType == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, ErrorCode.DATA_NOT_FOUND.getMessage());
        }

        return Result.success(rateRuleType);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询费率规则类型")
    public Result<PageResult<RateRuleTypes>> page(PageQueryDTO query) {
        LambdaQueryWrapper<RateRuleTypes> wrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = query.getKeyword().trim();
            wrapper.like(RateRuleTypes::getRuleTypeCode, keyword)
                    .or()
                    .like(RateRuleTypes::getRuleTypeName, keyword);
        }
        // 按 sort_order 排序，保证前端“排序”字段生效
        wrapper.orderByAsc(RateRuleTypes::getSortOrder).orderByAsc(RateRuleTypes::getId);
        IPage<RateRuleTypes> result = rateRuleTypesService.page(
                new Page<>(query.getCurrent(), query.getSize()), wrapper
        );

        PageResult<RateRuleTypes> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有费率规则类型列表")
    public Result<List<RateRuleTypes>> list() {
        List<RateRuleTypes> list = rateRuleTypesService.list(
                new LambdaQueryWrapper<RateRuleTypes>()
                        .orderByAsc(RateRuleTypes::getSortOrder)
                        .orderByAsc(RateRuleTypes::getId)
        );
        return Result.success(list);
    }

    @GetMapping("/system")
    @Operation(summary = "获取所有系统内置费率规则类型")
    public Result<List<RateRuleTypes>> getSystemRateRuleTypes() {
        log.info("获取所有系统内置费率规则类型");

        List<RateRuleTypes> list = rateRuleTypesService.list(
                new LambdaQueryWrapper<RateRuleTypes>()
                        .eq(RateRuleTypes::getIsSystem, 1)
                        .orderByAsc(RateRuleTypes::getSortOrder)
                        .orderByAsc(RateRuleTypes::getId)
        );

        return Result.success(list);
    }
    
    @GetMapping("/search")
    @Operation(summary = "根据规则类型编码或名称搜索费率规则类型")
    public Result<List<RateRuleTypes>> search(@RequestParam String keyword) {
        log.info("搜索费率规则类型: keyword={}", keyword);
        
        List<RateRuleTypes> list = rateRuleTypesService.list(
                new LambdaQueryWrapper<RateRuleTypes>()
                        .like(RateRuleTypes::getRuleTypeCode, keyword)
                        .or()
                        .like(RateRuleTypes::getRuleTypeName, keyword)
                        .orderByAsc(RateRuleTypes::getSortOrder)
                        .orderByAsc(RateRuleTypes::getId)
        );
        
        return Result.success(list);
    }
}