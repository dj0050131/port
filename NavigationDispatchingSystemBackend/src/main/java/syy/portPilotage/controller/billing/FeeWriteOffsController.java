package syy.portPilotage.controller.billing;

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
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.FeeWriteOffs;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.FeeWriteOffsService;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/billing/writeoffs")
@RequiredArgsConstructor
@Tag(name = "费用核销管理", description = "费用核销相关接口")
public class FeeWriteOffsController {

    @Autowired
    private FeeWriteOffsService feeWriteOffsService;

        @PostMapping
    @Operation(summary = "创建费用核销单")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Long> createFeeWriteOff(@RequestBody FeeWriteOffs writeOff) {
        log.info("创建费用核销单: writeOff={}", writeOff);

        if (writeOff.getAgentId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "船代ID不能为空");
        }

        writeOff.setCreatedAt(new Date());
        writeOff.setUpdatedAt(new Date());

        feeWriteOffsService.save(writeOff);

        log.info("费用核销单创建成功: id={}", writeOff.getId());
        return Result.success(writeOff.getId());
    }

        @GetMapping("/{id}")
    @Operation(summary = "获取费用核销单详情")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<FeeWriteOffs> getFeeWriteOffById(@PathVariable Long id) {
        FeeWriteOffs writeOff = feeWriteOffsService.getById(id);
        if (writeOff == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "费用核销单不存在");
        }

        return Result.success(writeOff);
    }

        @PutMapping("/{id}")
    @Operation(summary = "更新费用核销单")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> updateFeeWriteOff(@PathVariable Long id, @RequestBody FeeWriteOffs writeOff) {
        log.info("更新费用核销单: id={}, writeOff={}", id, writeOff);

        FeeWriteOffs existing = feeWriteOffsService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "费用核销单不存在");
        }

        writeOff.setId(id);
        writeOff.setUpdatedAt(new Date());

        feeWriteOffsService.updateById(writeOff);

        log.info("费用核销单更新成功: id={}", id);
        return Result.success();
    }

        @DeleteMapping("/{id}")
    @Operation(summary = "删除费用核销单")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteFeeWriteOff(@PathVariable Long id) {
        log.info("删除费用核销单: id={}", id);

        FeeWriteOffs existing = feeWriteOffsService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "费用核销单不存在");
        }

        feeWriteOffsService.removeById(id);

        log.info("费用核销单删除成功: id={}", id);
        return Result.success();
    }

        @DeleteMapping("/batch")
    @Operation(summary = "批量删除费用核销单")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> deleteFeeWriteOffBatch(@RequestParam List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要删除的交接单");
        }
        feeWriteOffsService.removeByIds(ids);
        return Result.success();
    }

        @GetMapping("/page")
    @Operation(summary = "分页查询费用核销单")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<PageResult<FeeWriteOffs>> pageFeeWriteOffs(PageQueryDTO query) {
        log.info("分页查询费用核销单");

        Page<FeeWriteOffs> page = new Page<>(query.getCurrent(), query.getSize());

        LambdaQueryWrapper<FeeWriteOffs> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FeeWriteOffs::getCreatedAt);

        IPage<FeeWriteOffs> result = feeWriteOffsService.page(page, wrapper);

        PageResult<FeeWriteOffs> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        log.info("分页查询费用核销单完成，共{}条记录", result.getTotal());
        return Result.success(pageResult);
    }
}