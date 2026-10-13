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
import syy.portPilotage.entity.PortAreas;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.PortAreasService;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RestController
@RequestMapping("/api/base/portAreas")
@RequiredArgsConstructor
@Tag(name = "港区信息管理", description = "港区信息相关接口")
public class PortAreasController {

    @Autowired
    private PortAreasService portAreasService;

    @PostMapping
    @Operation(summary = "创建港区信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody PortAreas portArea) {
        log.info("创建港区信息: areaCode={}, areaName={}", portArea.getAreaCode(), portArea.getAreaNameZh());
        PortAreas savedPortArea = portAreasService.savePortAreaData(portArea);
        log.info("港区信息创建成功: id={}, code={}", savedPortArea.getId(), savedPortArea.getAreaCode());
        return Result.success(savedPortArea.getId());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "更新港区信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<PortAreas> update(@PathVariable Long id, @Valid @RequestBody PortAreas portArea) {
        log.info("更新港区信息: id={}", id);

        PortAreas existingPortArea = portAreasService.getById(id);
        if (existingPortArea == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "港区信息不存在");
        }

        portArea.setId(id);
        PortAreas updatedPortArea = portAreasService.updatePortAreaData(portArea);
        log.info("港区信息更新成功: id={}", id);
        return Result.success(updatedPortArea);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除港区信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除港区信息: id={}", id);

        PortAreas portArea = portAreasService.getById(id);
        if (portArea == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "港区信息不存在");
        }

        boolean data = portAreasService.deletePortAreaData(id);
        if (!data) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除港区信息失败");
        }
        log.info("港区信息删除成功: id={}", id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取港区信息详情")
    public Result<PortAreas> getById(@PathVariable Long id) {
        PortAreas portArea = portAreasService.getById(id);
        return Result.success(portArea);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询港区信息")
    public Result<PageResult<PortAreas>> page(PageQueryDTO query) {
        IPage<PortAreas> result = portAreasService.selectPage(query);

        PageResult<PortAreas> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "根据港口ID获取港区列表")
    public Result<Object> listByPortId(@RequestParam Long portId) {
        log.info("根据港口ID获取港区列表: portId={}", portId);
        return Result.success(portAreasService.selectByPortId(portId));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索港区")
    public Result<Object> search(@RequestParam String keyword) {
        log.info("搜索港区: keyword={}", keyword);
        return Result.success(portAreasService.searchPortAreas(keyword));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新港区状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam String status) {
        log.info("更新港区状态: id={}, status={}", id, status);

        PortAreas portArea = portAreasService.getById(id);
        if (portArea == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "港区信息不存在");
        }

        boolean updateResult = portAreasService.updatePortAreaStatus(id, status);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新港区状态失败");
        }
        log.info("港区状态更新成功: id={}, status={}", id, status);
        return Result.success();
    }
}