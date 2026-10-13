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
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.FiscalYears;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.mapper.FiscalYearsMapper;
import syy.portPilotage.service.FiscalYearsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/billing/fiscalYears")
@RequiredArgsConstructor
@Tag(name = "会计年度管理", description = "会计年度及指标设定接口")
public class FiscalYearController {

    private final FiscalYearsService fiscalYearsService;
    private final FiscalYearsMapper fiscalYearsMapper;

    @GetMapping("/page")
    @Operation(summary = "分页查询会计年度")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<PageResult<FiscalYears>> page(PageQueryDTO query) {
        ensureCurrentYearExists();
        Page<FiscalYears> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<FiscalYears> wrapper = new LambdaQueryWrapper<FiscalYears>()
                .orderByDesc(FiscalYears::getYear)
                .orderByDesc(FiscalYears::getId);

        IPage<FiscalYears> result = fiscalYearsService.page(page, wrapper);
        if (result.getRecords() != null) {
            for (FiscalYears fy : result.getRecords()) {
                recalcAndPersist(fy);
            }
        }
        return Result.success(PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        ));
    }

    @GetMapping("/list")
    @Operation(summary = "获取全部会计年度")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<List<FiscalYears>> list() {
        ensureCurrentYearExists();
        List<FiscalYears> list = fiscalYearsService.list(
                new LambdaQueryWrapper<FiscalYears>().orderByDesc(FiscalYears::getYear).orderByDesc(FiscalYears::getId)
        );
        if (list != null) {
            for (FiscalYears fy : list) {
                recalcAndPersist(fy);
            }
        }
        return Result.success(list);
    }

    @PostMapping
    @Operation(summary = "新增会计年度")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Long> create(@Valid @RequestBody FiscalYears dto) {
        if (dto == null || dto.getYear() == null || dto.getYear().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "year不能为空");
        }
        String year = dto.getYear().trim();
        boolean exists = fiscalYearsService.count(new LambdaQueryWrapper<FiscalYears>().eq(FiscalYears::getYear, year)) > 0;
        if (exists) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "会计年度已存在: " + year);
        }

        FiscalYears fy = new FiscalYears();
        fy.setYear(year);
        fy.setTargetTonnage(dto.getTargetTonnage());
        fy.setTargetRevenue(dto.getTargetRevenue());
        fy.setEstimatedRevenue(dto.getEstimatedRevenue());
        fy.setStatus(dto.getStatus() != null && dto.getStatus());
        fy.setCreatedAt(new Date());
        fy.setUpdatedAt(new Date());

        fiscalYearsService.save(fy);
        recalcAndPersist(fy);
        return Result.success(fy.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新会计年度")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> update(@PathVariable Long id, @RequestBody FiscalYears dto) {
        FiscalYears existing = fiscalYearsService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会计年度不存在");
        }

        // year 不允许改成重复
        if (dto.getYear() != null && !dto.getYear().trim().isEmpty()) {
            String year = dto.getYear().trim();
            boolean dup = fiscalYearsService.count(new LambdaQueryWrapper<FiscalYears>()
                    .eq(FiscalYears::getYear, year)
                    .ne(FiscalYears::getId, id)) > 0;
            if (dup) {
                throw new BusinessException(ErrorCode.ALREADY_EXISTS, "会计年度已存在: " + year);
            }
            existing.setYear(year);
        }

        if (dto.getTargetTonnage() != null) existing.setTargetTonnage(dto.getTargetTonnage());
        if (dto.getTargetRevenue() != null) existing.setTargetRevenue(dto.getTargetRevenue());
        if (dto.getEstimatedRevenue() != null) existing.setEstimatedRevenue(dto.getEstimatedRevenue());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        existing.setUpdatedAt(new Date());

        fiscalYearsService.updateById(existing);
        recalcAndPersist(existing);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启用/停用会计年度")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Boolean status) {
        FiscalYears existing = fiscalYearsService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会计年度不存在");
        }
        existing.setStatus(status != null && status);
        existing.setUpdatedAt(new Date());
        fiscalYearsService.updateById(existing);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除会计年度")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        fiscalYearsService.removeById(id);
        return Result.success();
    }

        private void ensureCurrentYearExists() {
        long cnt = fiscalYearsService.count();
        if (cnt > 0) return;
        String y = String.valueOf(Year.now().getValue());
        FiscalYears fy = new FiscalYears();
        fy.setYear(y);
        fy.setStatus(true);
        fy.setCreatedAt(new Date());
        fy.setUpdatedAt(new Date());
        fiscalYearsService.save(fy);
        recalcAndPersist(fy);
    }

    /**
     * 自动汇总口径（建议）：
     * - 吨位：按年度内“已计费且未被拒绝”的计费凭证，累加 vessels.net_tonnage（每次作业按船舶净吨计一次）
     * - 已完成收入：优先取年度内 audit_status=APPROVED 的 total_amount；若为 0，则退化为“未被拒绝”的 billedSum
     * - 预估收入：若是当年，按(未被拒绝 billedSum)做月均 * 12 的 run-rate；往年则等于已完成收入
     */
    private void recalcAndPersist(FiscalYears fy) {
        if (fy == null || fy.getId() == null || fy.getYear() == null || fy.getYear().trim().isEmpty()) return;
        String year = fy.getYear().trim();
        int y;
        try {
            y = Integer.parseInt(year);
        } catch (Exception ignored) {
            return;
        }

        String start = String.format("%04d-01-01 00:00:00", y);
        String end = String.format("%04d-01-01 00:00:00", y + 1);

        BigDecimal billed = nz(fiscalYearsMapper.sumBilledAmount(start, end));
        BigDecimal approved = nz(fiscalYearsMapper.sumApprovedAmount(start, end));
        BigDecimal tonnage = nz(fiscalYearsMapper.sumNetTonnage(start, end));

        BigDecimal actualRevenue = approved.compareTo(BigDecimal.ZERO) > 0 ? approved : billed;

        int currentYear = LocalDate.now(ZoneId.systemDefault()).getYear();
        BigDecimal forecast;
        if (y < currentYear) {
            forecast = actualRevenue;
        } else if (y == currentYear) {
            int month = LocalDate.now(ZoneId.systemDefault()).getMonthValue();
            if (month <= 0) {
                forecast = billed;
            } else {
                // run-rate：按已过月份推全年
                forecast = billed.divide(BigDecimal.valueOf(month), 6, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(12));
            }
        } else {
            // 未来年度：用上一年度已完成收入作为保守预测（也可后续引入“增长率”系统参数）
            forecast = actualRevenue;
        }

        fy.setActualTonnage(tonnage);
        fy.setActualRevenue(actualRevenue);
        fy.setForecastRevenue(forecast);
        fy.setLastCalculatedAt(new Date());
        fy.setUpdatedAt(new Date());

        fiscalYearsService.updateById(fy);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
