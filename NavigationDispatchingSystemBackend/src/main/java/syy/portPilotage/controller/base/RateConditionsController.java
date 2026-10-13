package syy.portPilotage.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.RateConditions;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.RateConditionsService;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/base/rateConditions")
@RequiredArgsConstructor
@Tag(name = "费率条件配置", description = "rate_conditions 条件配置接口")
public class RateConditionsController {

    private final RateConditionsService rateConditionsService;

        private static final java.util.Set<String> SUPPORTED_TYPES = java.util.Set.of(
            "NET_TON_RANGE", "NET_TON", "NET_TONNAGE",
            "NAUTICAL_MILES",
            "PILOTAGE_HOURS",
            "BERTHING_ASSISTANCE_FLAG",
            "DETENTION_FLAG"
    );

    /**
     * 后端支持的 operator（与 BillingCalculationServiceImpl 对齐）
     */
    private static final java.util.Set<String> SUPPORTED_OPERATORS = java.util.Set.of(
            "EQ", "NE", "GT", "GE", "LT", "LE",
            "IN", "LIKE", "CONTAINS", "BETWEEN",
            "=", "!=", ">", ">=", "<", "<="
    );

    private String normalizeOp(String op) {
        if (op == null || op.trim().isEmpty()) return "EQ";
        String v = op.trim().toUpperCase();
        return switch (v) {
            case "=" -> "EQ";
            case "!=" -> "NE";
            case ">" -> "GT";
            case ">=" -> "GE";
            case "<" -> "LT";
            case "<=" -> "LE";
            default -> v;
        };
    }

    private String normalizeType(String type) {
        if (type == null) return null;
        return type.trim().toUpperCase();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询费率条件")
    @PreAuthorize("hasRole('ADMIN') or hasAnyRole('FINANCE','DISPATCHER') or hasAuthority('BILLING:PARAMETERS:MANAGE')")
    public Result<PageResult<RateConditions>> page(PageQueryDTO query,
                                                   @RequestParam(required = false) Long standardRateId,
                                                   @RequestParam(required = false) String conditionType,
                                                   @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<RateConditions> wrapper = new LambdaQueryWrapper<>();
        if (standardRateId != null && standardRateId > 0) {
            wrapper.eq(RateConditions::getStandardRateId, standardRateId);
        }
        if (conditionType != null && !conditionType.trim().isEmpty()) {
            wrapper.eq(RateConditions::getConditionType, conditionType.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = keyword.trim();
            wrapper.and(w -> w.like(RateConditions::getConditionValue, k).or().like(RateConditions::getDescription, k));
        }
        wrapper.orderByDesc(RateConditions::getId);

        IPage<RateConditions> result = rateConditionsService.page(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @GetMapping("/list")
    @Operation(summary = "按标准费率ID查询费率条件列表")
    @PreAuthorize("hasRole('ADMIN') or hasAnyRole('FINANCE','DISPATCHER') or hasAuthority('BILLING:PARAMETERS:MANAGE')")
    public Result<List<RateConditions>> list(@RequestParam Long standardRateId) {
        if (standardRateId == null || standardRateId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "standardRateId 不合法");
        }
        List<RateConditions> list = rateConditionsService.list(new LambdaQueryWrapper<RateConditions>()
                .eq(RateConditions::getStandardRateId, standardRateId)
                .orderByAsc(RateConditions::getId));
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取费率条件详情")
    @PreAuthorize("hasRole('ADMIN') or hasAnyRole('FINANCE','DISPATCHER') or hasAuthority('BILLING:PARAMETERS:MANAGE')")
    public Result<RateConditions> getById(@PathVariable Long id) {
        RateConditions rc = rateConditionsService.getById(id);
        if (rc == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "费率条件不存在");
        }
        return Result.success(rc);
    }

    @PostMapping
    @Operation(summary = "新增费率条件")
    @PreAuthorize("hasRole('ADMIN') or hasAnyRole('FINANCE','DISPATCHER') or hasAuthority('BILLING:PARAMETERS:MANAGE')")
    public Result<Long> create(@RequestBody RateConditions rc) {
        if (rc == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        if (rc.getStandardRateId() == null || rc.getStandardRateId() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "standardRateId 不合法");
        }
        if (rc.getConditionType() == null || rc.getConditionType().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "conditionType 不能为空");
        }
        if (rc.getConditionValue() == null || rc.getConditionValue().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "conditionValue 不能为空");
        }
        String type = normalizeType(rc.getConditionType());
        String op = normalizeOp(rc.getConditionOperator());
        if (type == null || type.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "conditionType 不能为空");
        }
        if (!SUPPORTED_TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的 conditionType: " + type);
        }
        if (!SUPPORTED_OPERATORS.contains(op)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的 conditionOperator: " + op);
        }
        rc.setConditionType(type);
        rc.setConditionOperator(op);
        rc.setConditionValue(rc.getConditionValue().trim());
        rc.setCreatedAt(new Date());
        rc.setUpdatedAt(new Date());
        rateConditionsService.save(rc);
        return Result.success(rc.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新费率条件")
    @PreAuthorize("hasRole('ADMIN') or hasAnyRole('FINANCE','DISPATCHER') or hasAuthority('BILLING:PARAMETERS:MANAGE')")
    public Result<Void> update(@PathVariable Long id, @RequestBody RateConditions rc) {
        RateConditions existing = rateConditionsService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "费率条件不存在");
        }
        if (rc == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        String type = rc.getConditionType() == null ? null : normalizeType(rc.getConditionType());
        String op = rc.getConditionOperator() == null ? null : normalizeOp(rc.getConditionOperator());
        String value = rc.getConditionValue() == null ? null : rc.getConditionValue().trim();

        if (type != null && !type.isEmpty() && !SUPPORTED_TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的 conditionType: " + type);
        }
        if (op != null && !op.isEmpty() && !SUPPORTED_OPERATORS.contains(op)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的 conditionOperator: " + op);
        }

        RateConditions updateEntity = new RateConditions();
        updateEntity.setId(id);
        updateEntity.setUpdatedAt(new Date());
        if (rc.getStandardRateId() != null && rc.getStandardRateId() > 0) updateEntity.setStandardRateId(rc.getStandardRateId());
        if (type != null && !type.isEmpty()) updateEntity.setConditionType(type);
        if (op != null && !op.isEmpty()) updateEntity.setConditionOperator(op);
        if (value != null && !value.isEmpty()) updateEntity.setConditionValue(value);
        if (rc.getDescription() != null) updateEntity.setDescription(rc.getDescription());

        rateConditionsService.updateById(updateEntity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除费率条件")
    @PreAuthorize("hasRole('ADMIN') or hasAnyRole('FINANCE','DISPATCHER') or hasAuthority('BILLING:PARAMETERS:MANAGE')")
    public Result<Void> delete(@PathVariable Long id) {
        RateConditions existing = rateConditionsService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "费率条件不存在");
        }
        rateConditionsService.removeById(id);
        return Result.success();
    }
}