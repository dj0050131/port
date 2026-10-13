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
import syy.portPilotage.entity.Berths;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.BerthsService;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RestController
@RequestMapping("/api/base/berths")
@RequiredArgsConstructor
@Tag(name = "泊位信息管理", description = "泊位信息相关接口")
public class BerthController {

    @Autowired
    private BerthsService berthsService;

    @PostMapping
    @Operation(summary = "创建泊位信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody Berths berth) {
        log.info("创建泊位信息: berthCode={}, berthName={}", berth.getBerthCode(), berth.getBerthName());
        Berths savedBerth = berthsService.saveBerthData(berth);
        log.info("泊位信息创建成功: id={}, code={}", savedBerth.getId(), savedBerth.getBerthCode());
        return Result.success(savedBerth.getId());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "更新泊位信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Berths> update(@PathVariable Long id, @Valid @RequestBody Berths berth) {
        log.info("更新泊位信息: id={}", id);

        Berths existingBerth = berthsService.getById(id);
        if (existingBerth == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "泊位信息不存在");
        }

        berth.setId(id);
        Berths updatedBerth = berthsService.updateBerthData(berth);
        log.info("泊位信息更新成功: id={}", id);
        return Result.success(updatedBerth);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除泊位信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除泊位信息: id={}", id);

        Berths berth = berthsService.getById(id);
        if (berth == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "泊位信息不存在");
        }

        boolean data = berthsService.deleteBerthData(id);
        if (!data) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除泊位信息失败");
        }
        log.info("泊位信息删除成功: id={}", id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取泊位信息详情")
    public Result<Berths> getById(@PathVariable Long id) {
        Berths berth = berthsService.getById(id);
        return Result.success(berth);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询泊位信息")
    public Result<PageResult<Berths>> page(PageQueryDTO query) {
        IPage<Berths> result = berthsService.selectPage(query);

        PageResult<Berths> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "根据码头获取泊位列表")
    public Result<Object> listByPortArea(@RequestParam(required = false) String wharvesId) {
        log.info("根据码头获取泊位列表: wharvesId={}", wharvesId);
        if (wharvesId == null){
            return Result.success(berthsService.list());
        }else {
            return Result.success(berthsService.selectByWharvesId(wharvesId));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索泊位")
    public Result<Object> search(@RequestParam String keyword) {
        log.info("搜索泊位: keyword={}", keyword);
        return Result.success(berthsService.searchBerths(keyword));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新泊位状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam String status) {
        log.info("更新泊位状态: id={}, status={}", id, status);

        Berths berth = berthsService.getById(id);
        if (berth == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "泊位信息不存在");
        }

        boolean updateResult = berthsService.updateBerthStatus(id, status);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新泊位状态失败");
        }
        log.info("泊位状态更新成功: id={}, status={}", id, status);
        return Result.success();
    }
}
