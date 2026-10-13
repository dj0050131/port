package syy.portPilotage.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.AgentInvoiceInfo;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.AgentInvoiceInfoService;
import syy.portPilotage.service.AgentsService;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/base/agentInvoiceInfo")
@RequiredArgsConstructor
@Tag(name = "代理发票信息管理", description = "代理及发票信息管理接口")
public class AgentInvoiceInfoController {

    @Autowired
    private AgentInvoiceInfoService agentInvoiceInfoService;

    @PostMapping
    @Operation(summary = "创建代理发票信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@RequestBody AgentInvoiceInfo agentInvoiceInfo) {
        log.info("创建代理发票信息: agentId={}, voyageId={}", agentInvoiceInfo.getAgentId(), agentInvoiceInfo.getVoyageId());
        agentInvoiceInfo.setCreatedAt(new Date());
        agentInvoiceInfo.setUpdatedAt(new Date());
        boolean result = agentInvoiceInfoService.save(agentInvoiceInfo);
        log.info("代理发票信息创建成功: id={}", agentInvoiceInfo.getId());
        return result ? Result.success(agentInvoiceInfo.getId()): Result.error("代理发票信息创建失败");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新代理发票信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> update(@PathVariable Long id, @RequestBody AgentInvoiceInfo agentInvoiceInfo) {
        log.info("更新代理发票信息: id={}", id);
        AgentInvoiceInfo existing = agentInvoiceInfoService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "代理发票信息不存在");
        }
        agentInvoiceInfo.setId(id);
        agentInvoiceInfo.setUpdatedAt(new Date());
        boolean result = agentInvoiceInfoService.updateById(agentInvoiceInfo);
        log.info("代理发票信息更新成功: id={}", id);
        return result ? Result.success(id): Result.error("代理发票信息更新失败");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除代理发票信息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> delete(@PathVariable Long id) {
        log.info("删除代理发票信息: id={}", id);
        AgentInvoiceInfo existing = agentInvoiceInfoService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "代理发票信息不存在");
        }
        boolean result = agentInvoiceInfoService.removeById(id);
        log.info("代理发票信息删除成功: id={}", id);
        return result ? Result.success(id):Result.error("删除代理发票信息失败");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取代理发票信息详情")
    public Result<AgentInvoiceInfo> getById(@PathVariable Long id) {
        AgentInvoiceInfo agentInvoiceInfo = agentInvoiceInfoService.getById(id);
        if (agentInvoiceInfo == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "代理发票信息不存在");
        }
        return Result.success(agentInvoiceInfo);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询代理发票信息")
    public Result<PageResult<AgentInvoiceInfo>> page(PageQueryDTO query) {
        PageResult<AgentInvoiceInfo> pageResult = agentInvoiceInfoService.pageAgentInvoiceInfo(query);
        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获取代理发票信息列表")
    public Result<List<AgentInvoiceInfo>> list() {
        List<AgentInvoiceInfo> list = agentInvoiceInfoService.list();
        return Result.success(list);
    }

    @GetMapping("/by-agent/{agentId}")
    @Operation(summary = "根据代理公司ID获取发票信息")
    public Result<List<AgentInvoiceInfo>> getByAgentId(@PathVariable Long agentId) {
        log.info("根据代理公司ID获取发票信息: agentId={}", agentId);
        LambdaQueryWrapper<AgentInvoiceInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentInvoiceInfo::getAgentId, agentId);
        List<AgentInvoiceInfo> list = agentInvoiceInfoService.list(wrapper);
        return Result.success(list);
    }

    @GetMapping("/by-voyage/{voyageId}")
    @Operation(summary = "根据航次ID获取发票信息")
    public Result<List<AgentInvoiceInfo>> getByVoyageId(@PathVariable Long voyageId) {
        log.info("根据航次ID获取发票信息: voyageId={}", voyageId);
        LambdaQueryWrapper<AgentInvoiceInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentInvoiceInfo::getVoyageId, voyageId);
        List<AgentInvoiceInfo> list = agentInvoiceInfoService.list(wrapper);
        return Result.success(list);
    }
}