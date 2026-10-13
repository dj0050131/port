package syy.portPilotage.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import syy.portPilotage.entity.Ports;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.mapper.PortsMapper;
import syy.portPilotage.service.PortsService;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RestController
@RequestMapping("/api/base/ports")
@RequiredArgsConstructor
@Tag(name = "港口信息管理", description = "港口信息相关接口")
public class PortsController {

    @Autowired
    private PortsService portsService;
    @Autowired
    private PortsMapper portsMapper;

    @PostMapping
    @Operation(summary = "创建港口信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Long> create(@Valid @RequestBody Ports postPort) {
        log.info("创建港口信息: portCode={}, portName={}", postPort.getPortCode(), postPort.getPortNameZh());
        Ports ports = portsService.savePortData(postPort);
        log.info("港口信息创建成功: id={}, code={}", ports.getId(), ports.getPortCode());
        return Result.success(ports.getId());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "更新港口信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Ports> update(@PathVariable Long id, @Valid @RequestBody Ports postPort) {
        log.info("更新港口信息: id={}", id);

        Ports port = portsService.getById(id);
        if (port == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "港口信息不存在");
        }

        Ports updatedPort = portsService.updatePortData(postPort);
        log.info("港口信息更新成功: id={}", id);
        return Result.success(updatedPort);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除港口信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除港口信息: id={}", id);

        Ports port = portsService.getById(id);
        if (port == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "港口信息不存在");
        }

        // 检查港口是否被使用
        if (portsService.isPortInUse(id)) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE, "该港口已被使用，无法删除");
        }

        boolean data = portsService.deletePortData(id);
        return data ? Result.success() : Result.error(ErrorCode.OPERATION_FAILED, "删除港口信息失败");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取港口信息详情")
    public Result<Ports> getById(@PathVariable Long id) {
        Ports port = portsService.getById(id);
        return Result.success(port);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询港口信息")
    public Result<PageResult<Ports>> page(PageQueryDTO query) {
        IPage<Ports> result = portsService.selectPage(query);

        PageResult<Ports> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获取港口列表")
    public Result<Object> list(@RequestParam(required = false) String region,
                               @RequestParam(required = false) String country) {
        log.info("获取港口列表: region={}, country={}", region, country);
        return Result.success(portsService.selectPortList(region, country));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索港口")
    public Result<Object> search(@RequestParam String keyword) {
        log.info("搜索港口: keyword={}", keyword);
        return Result.success(portsService.searchPorts(keyword));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新港口状态")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam String status) {
        log.info("更新港口状态: id={}, status={}", id, status);

        Ports port = portsService.getById(id);
        if (port == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "港口信息不存在");
        }

        boolean updatePortStatus = portsService.updatePortStatus(id, status);
        if (!updatePortStatus) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新港口状态失败");
        }
        log.info("港口状态更新成功: id={}, status={}", id, status);
        return Result.success();

    }
}