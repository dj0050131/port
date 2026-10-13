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
import syy.portPilotage.entity.PilotBoats;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.PilotBoatsService;

@Slf4j
@RestController
@RequestMapping("/api/base/pilotBoats")
@RequiredArgsConstructor
@Tag(name = "引航艇信息管理", description = "引航艇信息相关接口")
public class PilotBoatsController {

    @Autowired
    private PilotBoatsService pilotBoatsService;

    @PostMapping
    @Operation(summary = "创建引航艇信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody PilotBoats pilotBoats) {
        log.info("创建引航艇信息: boatName={}, callSign={}", pilotBoats.getBoatName(), pilotBoats.getVesselCallSign());
        PilotBoats savedBoat = pilotBoatsService.savePilotBoatData(pilotBoats);
        log.info("引航艇信息创建成功: id={}, name={}", savedBoat.getId(), savedBoat.getBoatName());
        return Result.success(savedBoat.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新引航艇信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<PilotBoats> update(@PathVariable Long id, @Valid @RequestBody PilotBoats pilotBoats) {
        log.info("更新引航艇信息: id={}", id);

        PilotBoats existingBoat = pilotBoatsService.getById(id);
        if (existingBoat == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "引航艇信息不存在");
        }

        pilotBoats.setId(id);
        PilotBoats updatedBoat = pilotBoatsService.updatePilotBoatData(pilotBoats);
        log.info("引航艇信息更新成功: id={}", id);
        return Result.success(updatedBoat);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除引航艇信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除引航艇信息: id={}", id);

        PilotBoats boat = pilotBoatsService.getById(id);
        if (boat == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "引航艇信息不存在");
        }

        // 检查引航艇是否被使用
        if (pilotBoatsService.isPilotBoatInUse(id)) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE, "该引航艇正在使用中，无法删除");
        }

        boolean success = pilotBoatsService.deletePilotBoatData(id);
        return success ? Result.success() : Result.error(ErrorCode.OPERATION_FAILED, "删除引航艇信息失败");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取引航艇信息详情")
    public Result<PilotBoats> getById(@PathVariable Long id) {
        PilotBoats boat = pilotBoatsService.getById(id);
        return Result.success(boat);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询引航艇信息")
    public Result<PageResult<PilotBoats>> page(PageQueryDTO query) {
        IPage<PilotBoats> result = pilotBoatsService.selectPage(query);

        PageResult<PilotBoats> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获取引航艇列表")
    public Result<Object> list(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String location) {
        log.info("获取引航艇列表: status={}, location={}", status, location);
        return Result.success(pilotBoatsService.selectPilotBoatList(status, location));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索引航艇")
    public Result<Object> search(@RequestParam String keyword) {
        log.info("搜索引航艇: keyword={}", keyword);
        return Result.success(pilotBoatsService.searchPilotBoats(keyword));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新引航艇状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Object status) {
        log.info("更新引航艇状态: id={}, status={}", id, status);

        PilotBoats boat = pilotBoatsService.getById(id);
        if (boat == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "引航艇信息不存在");
        }

        boolean success = pilotBoatsService.updatePilotBoatStatus(id, status);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新引航艇状态失败");
        }
        log.info("引航艇状态更新成功: id={}, status={}", id, status);
        return Result.success();
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用引航艇列表")
    public Result<Object> getAvailableBoats() {
        log.info("获取可用引航艇列表");
        return Result.success(pilotBoatsService.getAvailableBoats());
    }
}