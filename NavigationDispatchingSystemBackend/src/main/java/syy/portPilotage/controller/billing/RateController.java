package syy.portPilotage.controller.billing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.billing.RateConfigDTO;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.StandardRates;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.StandardRatesService;
import syy.portPilotage.util.BeanCopyUtil;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RestController
@RequestMapping("/api/billing/rates")
@RequiredArgsConstructor
@Tag(name = "费率管理", description = "费率配置相关接口")
public class RateController {
    
    @Autowired
    StandardRatesService standardRatesService;
    
    @PostMapping
    @Operation(summary = "创建费率配置")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<Long> create(@Valid @RequestBody RateConfigDTO dto) {
        log.info("创建费率配置: type={}", dto.getRateType());
        
        StandardRates rate = BeanCopyUtil.copy(dto, StandardRates.class);
        rate.setCreatedAt(new Date());
        rate.setUpdatedAt(new Date());
        
        standardRatesService.save(rate);
        
        log.info("费率配置创建成功: id={}", rate.getId());
        return Result.success(rate.getId());
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新费率配置")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RateConfigDTO dto) {
        log.info("更新费率配置: id={}", id);
        
        StandardRates rate = standardRatesService.getById(id);
        if (rate == null) {
            throw new BusinessException(ErrorCode.RATE_NOT_CONFIGURED, "费率配置不存在");
        }
        
        rate.setId(id);
        rate.setUpdatedAt(new Date());
        
        standardRatesService.updateById(rate);
        
        log.info("费率配置更新成功: id={}", id);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除费率配置")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除费率配置: id={}", id);
        
        standardRatesService.removeById(id);
        
        log.info("费率配置删除成功: id={}", id);
        return Result.success();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取费率配置详情")
    public Result<StandardRates> getById(@PathVariable Long id) {
        StandardRates rate = standardRatesService.getById(id);
        if (rate == null) {
            throw new BusinessException(ErrorCode.RATE_NOT_CONFIGURED, "费率配置不存在");
        }
        
        return Result.success(rate);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询费率配置")
    public Result<PageResult<StandardRates>> page(PageQueryDTO query) {
        Page<StandardRates> page = new Page<>(query.getCurrent(), query.getSize());
        
        LambdaQueryWrapper<StandardRates> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StandardRates::getCreatedAt);
        
        IPage<StandardRates> result = standardRatesService.page(page, wrapper);
        
        PageResult<StandardRates> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );
        
        return Result.success(pageResult);
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取所有费率配置")
    public Result<List<StandardRates>> list() {
        List<StandardRates> list = standardRatesService.list();
        return Result.success(list);
    }
}