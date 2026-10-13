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
import syy.portPilotage.entity.Pilots;
import syy.portPilotage.entity.Users;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.PilotsService;
import syy.portPilotage.service.UsersService;

@Slf4j
@RestController
@RequestMapping("/api/base/pilots")
@RequiredArgsConstructor
@Tag(name = "引航员信息管理", description = "引航员信息相关接口")
public class PilotsController {

    @Autowired
    private PilotsService pilotsService;
    @Autowired
    private UsersService usersService;

    @PostMapping
    @Operation(summary = "创建引航员信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody Pilots pilot) {
        log.info("创建引航员信息: pilotName={}, pilotCode={}", pilot.getPilotName(), pilot.getPilotCode());
        if (pilot.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "关联用户不能为空");
        }
        Users u = usersService.getById(pilot.getUserId());
        if (u == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "关联用户不存在");
        }
        // 统一以用户表为准，避免重复/乱填
        pilot.setPilotName(u.getUsername());
        pilot.setPhone(u.getPhone());
        pilot.setEmail(u.getEmail());
        if (pilot.getUserId() != null) {
            Pilots exists = pilotsService.getPilotByUserId(pilot.getUserId());
            if (exists != null) {
                throw new BusinessException(ErrorCode.ALREADY_EXISTS, "该用户已关联引航员，不能重复关联");
            }
        }
        Pilots savedPilot = pilotsService.savePilotData(pilot);
        log.info("引航员信息创建成功: id={}, name={}", savedPilot.getId(), savedPilot.getPilotName());
        return Result.success(savedPilot.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新引航员信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Pilots> update(@PathVariable Long id, @Valid @RequestBody Pilots pilot) {
        log.info("更新引航员信息: id={}", id);

        Pilots existingPilot = pilotsService.getById(id);
        if (existingPilot == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "引航员信息不存在");
        }

        if (pilot.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "关联用户不能为空");
        }
        Users u = usersService.getById(pilot.getUserId());
        if (u == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "关联用户不存在");
        }
        // 统一以用户表为准，避免重复/乱填
        pilot.setPilotName(u.getUsername());
        pilot.setPhone(u.getPhone());
        pilot.setEmail(u.getEmail());

        if (pilot.getUserId() != null) {
            Pilots exists = pilotsService.getPilotByUserId(pilot.getUserId());
            if (exists != null && !exists.getId().equals(id)) {
                throw new BusinessException(ErrorCode.ALREADY_EXISTS, "该用户已关联其他引航员，不能重复关联");
            }
        }

        pilot.setId(id);
        Pilots updatedPilot = pilotsService.updatePilotData(pilot);
        log.info("引航员信息更新成功: id={}", id);
        return Result.success(updatedPilot);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除引航员信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除引航员信息: id={}", id);

        Pilots pilot = pilotsService.getById(id);
        if (pilot == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "引航员信息不存在");
        }

        if (pilotsService.isPilotInUse(id)) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE, "该引航员已被引航计划占用，无法删除");
        }

        boolean success = pilotsService.deletePilotData(id);
        return success ? Result.success() : Result.error(ErrorCode.OPERATION_FAILED, "删除引航员信息失败");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取引航员信息详情")
    public Result<Pilots> getById(@PathVariable Long id) {
        Pilots pilot = pilotsService.getById(id);
        return Result.success(pilot);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询引航员信息")
    public Result<PageResult<Pilots>> page(PageQueryDTO query) {
        IPage<Pilots> result = pilotsService.selectPage(query);

        PageResult<Pilots> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获取引航员列表")
    public Result<Object> list(@RequestParam(required = false) String level,
                               @RequestParam(required = false) String status) {
        log.info("获取引航员列表: level={}, status={}", level, status);
        return Result.success(pilotsService.selectPilotList(level, status));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索引航员")
    public Result<Object> search(@RequestParam String keyword) {
        log.info("搜索引航员: keyword={}", keyword);
        return Result.success(pilotsService.searchPilots(keyword));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新引航员在岗状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Object status) {
        log.info("更新引航员在岗状态: id={}, status={}", id, status);

        Pilots pilot = pilotsService.getById(id);
        if (pilot == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "引航员信息不存在");
        }

        boolean success = pilotsService.updatePilotStatus(id, status);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新引航员状态失败");
        }
        log.info("引航员状态更新成功: id={}, status={}", id, status);
        return Result.success();
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用引航员列表")
    public Result<Object> getAvailablePilots() {
        log.info("获取可用引航员列表");
        return Result.success(pilotsService.getAvailablePilots());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID获取引航员信息")
    public Result<Pilots> getByUserId(@PathVariable Long userId) {
        Pilots pilot = pilotsService.getPilotByUserId(userId);
        return Result.success(pilot);
    }
}