package syy.portPilotage.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.ChargeItems;
import syy.portPilotage.entity.RateRuleTypes;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.ChargeItemsService;
import syy.portPilotage.service.RateRuleTypesService;

import java.util.Date;
import java.util.List;

/**
 * 收费项目（ChargeItems）基础数据接口
 */
@Slf4j
@RestController
@RequestMapping("/api/base/chargeItems")
@RequiredArgsConstructor
@Tag(name = "收费项目管理", description = "收费项目相关接口")
public class ChargeItemsController {

    @Autowired
    private ChargeItemsService chargeItemsService;

    @Autowired
    private RateRuleTypesService rateRuleTypesService;

    @GetMapping("/page")
    @Operation(summary = "分页查询收费项目")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','AGENT','FINANCE')")
    public Result<PageResult<ChargeItems>> page(PageQueryDTO query,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ChargeItems> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(ChargeItems::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim();
            wrapper.and(w -> w.like(ChargeItems::getItemCode, k).or().like(ChargeItems::getItemName, k));
        }
        wrapper.orderByAsc(ChargeItems::getSortOrder).orderByAsc(ChargeItems::getId);

        IPage<ChargeItems> result = chargeItemsService.page(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取收费项目详情")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','AGENT','FINANCE')")
    public Result<ChargeItems> getById(@PathVariable Long id) {
        ChargeItems item = chargeItemsService.getById(id);
        if (item == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "收费项目不存在");
        }
        return Result.success(item);
    }

    @GetMapping("/list")
    @Operation(summary = "获取收费项目列表")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','AGENT','FINANCE')")
    public Result<List<ChargeItems>> list(@RequestParam(required = false) Long categoryId) {
        List<ChargeItems> list;
        if (categoryId != null) {
            list = chargeItemsService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChargeItems>()
                            .eq(ChargeItems::getCategoryId, categoryId)
                            .orderByAsc(ChargeItems::getSortOrder)
                            .orderByAsc(ChargeItems::getId)
            );
        } else {
            list = chargeItemsService.list(
                    new LambdaQueryWrapper<ChargeItems>()
                            .orderByAsc(ChargeItems::getSortOrder)
                            .orderByAsc(ChargeItems::getId)
            );
        }
        return Result.success(list);
    }

    @PostMapping
    @Operation(summary = "创建收费项目")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Long> create(@RequestBody ChargeItems item) {
        if (item == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        if (item.getItemCode() == null || item.getItemCode().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "itemCode 不能为空");
        }
        if (item.getCategoryId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "categoryId 不能为空");
        }
        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "itemName 不能为空");
        }

        ChargeItems exists = chargeItemsService.findByItemCode(item.getItemCode().trim());
        if (exists != null) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "收费项目编码已存在");
        }

        item.setItemCode(item.getItemCode().trim());
        item.setItemName(item.getItemName().trim());
        item.setCreatedAt(new Date());
        item.setUpdatedAt(new Date());
        chargeItemsService.save(item);
        return Result.success(item.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新收费项目")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> update(@PathVariable Long id, @RequestBody ChargeItems item) {
        ChargeItems existing = chargeItemsService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "收费项目不存在");
        }
        if (item == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        if (item.getItemCode() != null && !item.getItemCode().trim().isEmpty()) {
            String nextCode = item.getItemCode().trim();
            if (!nextCode.equals(existing.getItemCode())) {
                ChargeItems codeExists = chargeItemsService.findByItemCode(nextCode);
                if (codeExists != null && !codeExists.getId().equals(id)) {
                    throw new BusinessException(ErrorCode.ALREADY_EXISTS, "收费项目编码已存在");
                }
            }
            item.setItemCode(nextCode);
        }
        if (item.getItemName() != null) {
            item.setItemName(item.getItemName().trim());
        }
        item.setId(id);
        item.setUpdatedAt(new Date());
        chargeItemsService.updateById(item);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除收费项目")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> delete(@PathVariable Long id) {
        ChargeItems existing = chargeItemsService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "收费项目不存在");
        }
        long used = rateRuleTypesService.count(new LambdaQueryWrapper<RateRuleTypes>()
                .eq(RateRuleTypes::getChargeItemId, id));
        if (used > 0) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE, "该收费项目已被费率规则类型关联，无法删除");
        }
        chargeItemsService.removeById(id);
        return Result.success();
    }
}
