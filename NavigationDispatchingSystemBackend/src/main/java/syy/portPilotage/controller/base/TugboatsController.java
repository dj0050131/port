package syy.portPilotage.controller.base;

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
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Tugboats;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.TugboatsService;

@Slf4j
@RestController
@RequestMapping("/api/base/tugboats")
@RequiredArgsConstructor
@Tag(name = "拖轮信息管理", description = "拖轮信息相关接口")
public class TugboatsController {

    @Autowired
    private TugboatsService tugboatsService;

    @PostMapping
    @Operation(summary = "创建拖轮信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody Tugboats tugboats) {
        log.info("创建拖轮信息: tugboatName={}, tugboatCode={}", tugboats.getTugboatName(), tugboats.getTugboatCode());
        Tugboats savedTugboat = tugboatsService.saveTugboatData(tugboats);
        log.info("拖轮信息创建成功: id={}, name={}", savedTugboat.getId(), savedTugboat.getTugboatName());
        return Result.success(savedTugboat.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新拖轮信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Tugboats> update(@PathVariable Long id, @Valid @RequestBody Tugboats tugboats) {
        log.info("更新拖轮信息: id={}", id);

        Tugboats existingTugboat = tugboatsService.getById(id);
        if (existingTugboat == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "拖轮信息不存在");
        }

        tugboats.setId(id);
        Tugboats updatedTugboat = tugboatsService.updateTugboatData(tugboats);
        log.info("拖轮信息更新成功: id={}", id);
        return Result.success(updatedTugboat);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除拖轮信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除拖轮信息: id={}", id);

        Tugboats tugboat = tugboatsService.getById(id);
        if (tugboat == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "拖轮信息不存在");
        }

        if (tugboatsService.isTugboatInUse(id)) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE, "该拖轮正在使用中，无法删除");
        }

        boolean success = tugboatsService.deleteTugboatData(id);
        return success ? Result.success() : Result.error(ErrorCode.OPERATION_FAILED, "删除拖轮信息失败");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取拖轮信息详情")
    public Result<Tugboats> getById(@PathVariable Long id) {
        Tugboats tugboat = tugboatsService.getById(id);
        return Result.success(tugboat);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询拖轮信息")
    public Result<PageResult<Tugboats>> page(PageQueryDTO query) {
        IPage<Tugboats> result = tugboatsService.selectPage(query);

        PageResult<Tugboats> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获取拖轮列表")
    public Result<Object> list(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String type) {
        log.info("获取拖轮列表: status={}, type={}", status, type);
        return Result.success(tugboatsService.selectTugboatList(status, type));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索拖轮")
    public Result<Object> search(@RequestParam String keyword) {
        log.info("搜索拖轮: keyword={}", keyword);
        return Result.success(tugboatsService.searchTugboats(keyword));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新拖轮状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Object status) {
        log.info("更新拖轮状态: id={}, status={}", id, status);

        Tugboats tugboat = tugboatsService.getById(id);
        if (tugboat == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "拖轮信息不存在");
        }

        boolean success = tugboatsService.updateTugboatStatus(id, status);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新拖轮状态失败");
        }
        log.info("拖轮状态更新成功: id={}, status={}", id, status);
        return Result.success();
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用拖轮列表")
    public Result<Object> getAvailableTugboats() {
        log.info("获取可用拖轮列表");
        return Result.success(tugboatsService.getAvailableTugboats());
    }
}