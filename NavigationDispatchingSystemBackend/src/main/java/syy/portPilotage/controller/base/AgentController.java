package syy.portPilotage.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Agents;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.AgentsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/base/agents")
@RequiredArgsConstructor
@Tag(name = "代理公司管理", description = "代理公司基础数据管理接口")
public class AgentController {
    
    @Autowired
    AgentsService agentsService;
    
    @PostMapping
    @Operation(summary = "创建代理公司")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@RequestBody Agents agent) {
        log.info("创建代理公司: name={}", agent.getCompanyName());
        boolean result = agentsService.save(agent);
        log.info("代理公司创建成功: id={}", agent.getId());
        return result ? Result.success(agent.getId()) : Result.error("代理公司添加失败");
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新代理公司")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> update(@PathVariable Long id, @RequestBody Agents agent) {
        log.info("更新代理公司: id={}", id);
        boolean result = agentsService.updateById(agent);
        log.info("代理公司更新成功: id={}", id);
        return result ?Result.success(id) : Result.error("代理公司修改失败");
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除代理公司")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> delete(@PathVariable Long id) {
        log.info("删除代理公司: id={}", id);
        boolean result = agentsService.removeById(id);
        log.info("代理公司删除成功: id={}", id);
        return result? Result.success(id) : Result.error("代理公司删除失败");
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取代理公司详情")
    public Result<Agents> getById(@PathVariable Long id) {
        Agents agent = agentsService.getById(id);
        if (agent == null) {
            throw new BusinessException(ErrorCode.AGENT_NOT_FOUND);
        }
        return Result.success(agent);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询代理公司")
    public Result<PageResult<Agents>> page(PageQueryDTO query) {
        PageResult<Agents> pageResult = agentsService.pageAgent(query);
        return Result.success(pageResult);
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取所有代理公司列表")
    public Result<List<Agents>> list() {
        List<Agents> list = agentsService.list();
        return Result.success(list);
    }
}