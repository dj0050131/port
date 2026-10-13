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
import syy.portPilotage.entity.PilotageRoutes;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.PilotageRoutesService;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/base/pilotageRoutes")
@RequiredArgsConstructor
@Tag(name = "引航区间信息管理", description = "引航区间信息相关接口")
public class PilotageRoutesController {

    @Autowired
    private PilotageRoutesService pilotageRoutesService;

    @PostMapping
    @Operation(summary = "创建引航区间信息")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('BILLING:DIRECT_ROUTE:MANAGE') or hasAnyRole('FINANCE','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody PilotageRoutes pilotageRoute) {
        log.info("创建引航区间信息: routeCode={}, routeName={}", pilotageRoute.getRouteCode(), pilotageRoute.getRouteName());
        
        pilotageRoute.setCreatedAt(new Date());
        pilotageRoute.setUpdatedAt(new Date());
        
        pilotageRoutesService.save(pilotageRoute);
        
        log.info("引航区间信息创建成功: id={}", pilotageRoute.getId());
        return Result.success(pilotageRoute.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新引航区间信息")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('BILLING:DIRECT_ROUTE:MANAGE') or hasAnyRole('FINANCE','DISPATCHER')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PilotageRoutes pilotageRoute) {
        log.info("更新引航区间信息: id={}", id);

        PilotageRoutes existing = pilotageRoutesService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "引航区间信息不存在");
        }

        pilotageRoute.setId(id);
        pilotageRoute.setUpdatedAt(new Date());

        pilotageRoutesService.updateById(pilotageRoute);

        log.info("引航区间信息更新成功: id={}", id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除引航区间信息")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('BILLING:DIRECT_ROUTE:MANAGE') or hasAnyRole('FINANCE','DISPATCHER')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除引航区间信息: id={}", id);

        PilotageRoutes existing = pilotageRoutesService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "引航区间信息不存在");
        }

        pilotageRoutesService.removeById(id);

        log.info("引航区间信息删除成功: id={}", id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取引航区间信息详情")
    public Result<PilotageRoutes> getById(@PathVariable Long id) {
        PilotageRoutes pilotageRoute = pilotageRoutesService.getById(id);
        if (pilotageRoute == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "引航区间信息不存在");
        }

        return Result.success(pilotageRoute);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询引航区间信息")
    public Result<PageResult<PilotageRoutes>> page(PageQueryDTO query) {
        IPage<PilotageRoutes> result = pilotageRoutesService.page(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(query.getCurrent(), query.getSize())
        );

        PageResult<PilotageRoutes> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有引航区间信息列表")
    public Result<List<PilotageRoutes>> list() {
        List<PilotageRoutes> list = pilotageRoutesService.list();
        return Result.success(list);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索引航区间信息")
    public Result<List<PilotageRoutes>> search(@RequestParam String keyword) {
        log.info("搜索引航区间信息: keyword={}", keyword);
        
        List<PilotageRoutes> list = pilotageRoutesService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PilotageRoutes>()
                        .like(PilotageRoutes::getRouteCode, keyword)
                        .or()
                        .like(PilotageRoutes::getRouteName, keyword)
        );
        
        return Result.success(list);
    }

    @GetMapping("/byStartLocation")
    @Operation(summary = "根据起始位置获取引航区间信息")
    public Result<List<PilotageRoutes>> getByStartLocation(@RequestParam String startLocationType,
                                                         @RequestParam Long startLocationId) {
        log.info("根据起始位置获取引航区间信息: startLocationType={}, startLocationId={}", startLocationType, startLocationId);
        
        List<PilotageRoutes> list = pilotageRoutesService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PilotageRoutes>()
                        .eq(PilotageRoutes::getStartLocationType, startLocationType)
                        .eq(PilotageRoutes::getStartLocationId, startLocationId)
        );
        
        return Result.success(list);
    }

    @GetMapping("/byEndLocation")
    @Operation(summary = "根据结束位置获取引航区间信息")
    public Result<List<PilotageRoutes>> getByEndLocation(@RequestParam String endLocationType,
                                                       @RequestParam Long endLocationId) {
        log.info("根据结束位置获取引航区间信息: endLocationType={}, endLocationId={}", endLocationType, endLocationId);
        
        List<PilotageRoutes> list = pilotageRoutesService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PilotageRoutes>()
                        .eq(PilotageRoutes::getEndLocationType, endLocationType)
                        .eq(PilotageRoutes::getEndLocationId, endLocationId)
        );
        
        return Result.success(list);
    }

    @GetMapping("/byStatus")
    @Operation(summary = "根据状态获取引航区间信息")
    public Result<List<PilotageRoutes>> getByStatus(@RequestParam String status) {
        log.info("根据状态获取引航区间信息: status={}", status);
        
        List<PilotageRoutes> list = pilotageRoutesService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PilotageRoutes>()
                        .eq(PilotageRoutes::getStatus, status)
        );
        
        return Result.success(list);
    }
}