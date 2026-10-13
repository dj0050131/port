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
import syy.portPilotage.entity.Anchorages;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.AnchoragesService;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/base/anchorages")
@RequiredArgsConstructor
@Tag(name = "锚地信息管理", description = "锚地信息相关接口")
public class AnchoragesController {

    @Autowired
    private AnchoragesService anchoragesService;

    @PostMapping
    @Operation(summary = "创建锚地信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody Anchorages anchorage) {
        log.info("创建锚地信息: anchorageCode={}, anchorageName={}", anchorage.getAnchorageCode(), anchorage.getAnchorageNameZh());
        Anchorages savedAnchorage = anchoragesService.saveAnchorageData(anchorage);
        log.info("锚地信息创建成功: id={}, code={}", savedAnchorage.getId(), savedAnchorage.getAnchorageCode());
        return Result.success(savedAnchorage.getId());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "更新锚地信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Anchorages> update(@PathVariable Long id, @Valid @RequestBody Anchorages anchorage) {
        log.info("更新锚地信息: id={}", id);

        Anchorages existingAnchorage = anchoragesService.getById(id);
        if (existingAnchorage == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "锚地信息不存在");
        }

        anchorage.setId(id);
        Anchorages updatedAnchorage = anchoragesService.updateAnchorageData(anchorage);
        log.info("锚地信息更新成功: id={}", id);
        return Result.success(updatedAnchorage);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除锚地信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除锚地信息: id={}", id);

        Anchorages anchorage = anchoragesService.getById(id);
        if (anchorage == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "锚地信息不存在");
        }

        boolean data = anchoragesService.deleteAnchorageData(id);
        if (!data) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除锚地信息失败");
        }
        log.info("锚地信息删除成功: id={}", id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取锚地信息详情")
    public Result<Anchorages> getById(@PathVariable Long id) {
        Anchorages anchorage = anchoragesService.getById(id);
        return Result.success(anchorage);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询锚地信息")
    public Result<PageResult<Anchorages>> page(PageQueryDTO query) {
        IPage<Anchorages> result = anchoragesService.selectPage(query);

        PageResult<Anchorages> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "根据港区ID获取锚地列表")
    public Result<Object> listByPortAreaId(@RequestParam(required = false) Long portAreaId) {
        log.info("根据港区ID获取锚地列表: portAreaId={}", portAreaId);
        if (portAreaId != null){
            return Result.success(anchoragesService.selectByPortAreaId(portAreaId));
        }else {
            return Result.success(anchoragesService.list());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索锚地")
    public Result<Object> search(@RequestParam String keyword) {
        log.info("搜索锚地: keyword={}", keyword);
        return Result.success(anchoragesService.searchAnchorages(keyword));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新锚地状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam String status) {
        log.info("更新锚地状态: id={}, status={}", id, status);

        Anchorages anchorage = anchoragesService.getById(id);
        if (anchorage == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "锚地信息不存在");
        }

        boolean updateResult = anchoragesService.updateAnchorageStatus(id, status);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新锚地状态失败");
        }
        log.info("锚地状态更新成功: id={}, status={}", id, status);
        return Result.success();
    }
}