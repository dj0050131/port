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
import syy.portPilotage.entity.Vehicles;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.VehiclesService;

@Slf4j
@RestController
@RequestMapping("/api/base/vehicles")
@RequiredArgsConstructor
@Tag(name = "车辆信息管理", description = "车辆信息相关接口")
public class VehiclesController {

    @Autowired
    private VehiclesService vehiclesService;

    @PostMapping
    @Operation(summary = "创建车辆信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody Vehicles vehicle) {
        log.info("创建车辆信息: licensePlate={}, vehicleCode={}", vehicle.getLicensePlate(), vehicle.getVehicleCode());
        Vehicles savedVehicle = vehiclesService.saveVehicleData(vehicle);
        log.info("车辆信息创建成功: id={}, licensePlate={}", savedVehicle.getId(), savedVehicle.getLicensePlate());
        return Result.success(savedVehicle.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新车辆信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Vehicles> update(@PathVariable Long id, @Valid @RequestBody Vehicles vehicle) {
        log.info("更新车辆信息: id={}", id);

        Vehicles existingVehicle = vehiclesService.getById(id);
        if (existingVehicle == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "车辆信息不存在");
        }

        vehicle.setId(id);
        Vehicles updatedVehicle = vehiclesService.updateVehicleData(vehicle);
        log.info("车辆信息更新成功: id={}", id);
        return Result.success(updatedVehicle);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除车辆信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除车辆信息: id={}", id);

        Vehicles vehicle = vehiclesService.getById(id);
        if (vehicle == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "车辆信息不存在");
        }

        if (vehiclesService.isVehicleInUse(id)) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE, "该车辆正在使用中，无法删除");
        }

        boolean success = vehiclesService.deleteVehicleData(id);
        return success ? Result.success() : Result.error(ErrorCode.OPERATION_FAILED, "删除车辆信息失败");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取车辆信息详情")
    public Result<Vehicles> getById(@PathVariable Long id) {
        Vehicles vehicle = vehiclesService.getById(id);
        return Result.success(vehicle);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询车辆信息")
    public Result<PageResult<Vehicles>> page(PageQueryDTO query) {
        IPage<Vehicles> result = vehiclesService.selectPage(query);

        PageResult<Vehicles> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获取车辆列表")
    public Result<Object> list(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String location) {
        log.info("获取车辆列表: status={}, location={}", status, location);
        return Result.success(vehiclesService.selectVehicleList(status, location));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索车辆")
    public Result<Object> search(@RequestParam String keyword) {
        log.info("搜索车辆: keyword={}", keyword);
        return Result.success(vehiclesService.searchVehicles(keyword));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新车辆状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Object status) {
        log.info("更新车辆状态: id={}, status={}", id, status);

        Vehicles vehicle = vehiclesService.getById(id);
        if (vehicle == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "车辆信息不存在");
        }

        boolean success = vehiclesService.updateVehicleStatus(id, status);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新车辆状态失败");
        }
        log.info("车辆状态更新成功: id={}, status={}", id, status);
        return Result.success();
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用车辆列表")
    public Result<Object> getAvailableVehicles() {
        log.info("获取可用车辆列表");
        return Result.success(vehiclesService.getAvailableVehicles());
    }
}