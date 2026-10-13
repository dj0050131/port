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
import syy.portPilotage.entity.Drivers;
import syy.portPilotage.entity.Users;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.DriversService;
import syy.portPilotage.service.UsersService;

@Slf4j
@RestController
@RequestMapping("/api/base/drivers")
@RequiredArgsConstructor
@Tag(name = "驾驶员信息管理", description = "驾驶员信息相关接口")
public class DriversController {

    @Autowired
    private DriversService driversService;
    @Autowired
    private UsersService usersService;

    @PostMapping
    @Operation(summary = "创建驾驶员信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody Drivers driver) {
        log.info("创建驾驶员信息: name={}, driverCode={}", driver.getName(), driver.getDriverCode());
        if (driver.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "关联用户不能为空");
        }
        Users u = usersService.getById(driver.getUserId());
        if (u == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "关联用户不存在");
        }
        // 统一以用户表为准，避免重复/乱填
        driver.setName(u.getUsername());
        driver.setPhone(u.getPhone());
        driver.setEmail(u.getEmail());
        if (driver.getUserId() != null) {
            boolean exists = driversService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Drivers>()
                    .eq(Drivers::getUserId, driver.getUserId())).stream().findFirst().isPresent();
            if (exists) {
                throw new BusinessException(ErrorCode.ALREADY_EXISTS, "该用户已关联驾驶员，不能重复关联");
            }
        }
        Drivers savedDriver = driversService.saveDriverData(driver);
        log.info("驾驶员信息创建成功: id={}, name={}", savedDriver.getId(), savedDriver.getName());
        return Result.success(savedDriver.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新驾驶员信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Drivers> update(@PathVariable Long id, @Valid @RequestBody Drivers driver) {
        log.info("更新驾驶员信息: id={}", id);

        Drivers existingDriver = driversService.getById(id);
        if (existingDriver == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "驾驶员信息不存在");
        }

        if (driver.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "关联用户不能为空");
        }
        Users u = usersService.getById(driver.getUserId());
        if (u == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "关联用户不存在");
        }
        // 统一以用户表为准，避免重复/乱填
        driver.setName(u.getUsername());
        driver.setPhone(u.getPhone());
        driver.setEmail(u.getEmail());

        if (driver.getUserId() != null) {
            Drivers exists = driversService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Drivers>()
                    .eq(Drivers::getUserId, driver.getUserId()), false);
            if (exists != null && !exists.getId().equals(id)) {
                throw new BusinessException(ErrorCode.ALREADY_EXISTS, "该用户已关联其他驾驶员，不能重复关联");
            }
        }

        driver.setId(id);
        Drivers updatedDriver = driversService.updateDriverData(driver);
        log.info("驾驶员信息更新成功: id={}", id);
        return Result.success(updatedDriver);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除驾驶员信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除驾驶员信息: id={}", id);

        Drivers driver = driversService.getById(id);
        if (driver == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "驾驶员信息不存在");
        }

        if (driversService.isDriverInUse(id)) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE, "该驾驶员已被引航计划占用，无法删除");
        }

        boolean success = driversService.deleteDriverData(id);
        return success ? Result.success() : Result.error(ErrorCode.OPERATION_FAILED, "删除驾驶员信息失败");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取驾驶员信息详情")
    public Result<Drivers> getById(@PathVariable Long id) {
        Drivers driver = driversService.getById(id);
        return Result.success(driver);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询驾驶员信息")
    public Result<PageResult<Drivers>> page(PageQueryDTO query) {
        IPage<Drivers> result = driversService.selectPage(query);

        PageResult<Drivers> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获取驾驶员列表")
    public Result<Object> list(@RequestParam(required = false) String level,
                               @RequestParam(required = false) String status) {
        log.info("获取驾驶员列表: level={}, status={}", level, status);
        return Result.success(driversService.selectDriverList(level, status));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索驾驶员")
    public Result<Object> search(@RequestParam String keyword) {
        log.info("搜索驾驶员: keyword={}", keyword);
        return Result.success(driversService.searchDrivers(keyword));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新驾驶员在岗状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Object status) {
        log.info("更新驾驶员在岗状态: id={}, status={}", id, status);

        Drivers driver = driversService.getById(id);
        if (driver == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "驾驶员信息不存在");
        }

        boolean success = driversService.updateDriverStatus(id, status);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新驾驶员状态失败");
        }
        log.info("驾驶员状态更新成功: id={}, status={}", id, status);
        return Result.success();
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用驾驶员列表")
    public Result<Object> getAvailableDrivers() {
        log.info("获取可用驾驶员列表");
        return Result.success(driversService.getAvailableDrivers());
    }
}