package syy.portPilotage.controller.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
import syy.portPilotage.entity.Wharves;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.WharvesService;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RestController
@RequestMapping("/api/base/wharves")
@RequiredArgsConstructor
@Tag(name = "码头信息管理", description = "码头信息相关接口")
public class WharvesController {

    @Autowired
    private WharvesService wharvesService;

    @PostMapping
    @Operation(summary = "创建码头信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody Wharves wharf) {
        log.info("创建码头信息: wharfCode={}, wharfName={}", wharf.getWharfCode(), wharf.getWharfNameZh());
        Wharves savedWharf = wharvesService.saveWharfData(wharf);
        log.info("码头信息创建成功: id={}, code={}", savedWharf.getId(), savedWharf.getWharfCode());
        return Result.success(savedWharf.getId());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "更新码头信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Wharves> update(@PathVariable Long id, @Valid @RequestBody Wharves wharf) {
        log.info("更新码头信息: id={}", id);

        Wharves existingWharf = wharvesService.getById(id);
        if (existingWharf == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "码头信息不存在");
        }

        wharf.setId(id);
        Wharves updatedWharf = wharvesService.updateWharfData(wharf);
        log.info("码头信息更新成功: id={}", id);
        return Result.success(updatedWharf);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除码头信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除码头信息: id={}", id);

        Wharves wharf = wharvesService.getById(id);
        if (wharf == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "码头信息不存在");
        }

        boolean data = wharvesService.deleteWharfData(id);
        if (!data) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除码头信息失败");
        }
        log.info("码头信息删除成功: id={}", id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取码头信息详情")
    public Result<Wharves> getById(@PathVariable Long id) {
        Wharves wharf = wharvesService.getById(id);
        return Result.success(wharf);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询码头信息")
    public Result<PageResult<Wharves>> page(PageQueryDTO query) {
        IPage<Wharves> result = wharvesService.selectPage(query);

        PageResult<Wharves> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "根据港区ID获取码头列表")
    public Result<Object> listByPortAreaId(@RequestParam(required = false) Long portAreaId) {
        log.info("根据港区ID获取码头列表: portAreaId={}", portAreaId);
        if (portAreaId == null) {
            return Result.success(wharvesService.list());
        }
        return Result.success(wharvesService.selectByPortAreaId(portAreaId));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索码头")
    public Result<Object> search(@RequestParam String keyword) {
        log.info("搜索码头: keyword={}", keyword);
        return Result.success(wharvesService.searchWharves(keyword));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新码头状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam String status) {
        log.info("更新码头状态: id={}, status={}", id, status);

        Wharves wharf = wharvesService.getById(id);
        if (wharf == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "码头信息不存在");
        }

        boolean updateResult = wharvesService.updateWharfStatus(id, status);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新码头状态失败");
        }
        log.info("码头状态更新成功: id={}, status={}", id, status);
        return Result.success();
    }
}