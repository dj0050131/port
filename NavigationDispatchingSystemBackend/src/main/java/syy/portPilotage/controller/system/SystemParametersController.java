package syy.portPilotage.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.SystemParameters;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.mapper.SystemParametersMapper;
import syy.portPilotage.security.component.AuthzConfigService;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/system/parameters")
@RequiredArgsConstructor
@Tag(name = "系统参数", description = "system_parameters 参数配置接口")
public class SystemParametersController {

    private final SystemParametersMapper systemParametersMapper;
    private final AuthzConfigService authzConfigService;

    @GetMapping("/page")
    @Operation(summary = "分页查询系统参数")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:PARAMETERS:VIEW') or hasAuthority('SYSTEM:PARAMETERS:EDIT')")
    public Result<PageResult<SystemParameters>> page(PageQueryDTO query,
                                                    @RequestParam(required = false) String paramGroup,
                                                    @RequestParam(required = false) String keyword) {
        int current =  query.getCurrent();
        int size =  query.getSize();
        int offset = (current - 1) * size;

        List<SystemParameters> records = systemParametersMapper.selectPage(offset, size, paramGroup, keyword);
        Long total = systemParametersMapper.countPage(paramGroup, keyword);

        return Result.success(PageResult.of(total == null ? 0 : total, current, size, records));
    }

    @GetMapping("/group/{group}")
    @Operation(summary = "按参数组查询")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:PARAMETERS:VIEW') or hasAuthority('SYSTEM:PARAMETERS:EDIT')")
    public Result<List<SystemParameters>> byGroup(@PathVariable("group") String group) {
        return Result.success(systemParametersMapper.selectByGroup(group));
    }

    @PostMapping
    @Operation(summary = "新增系统参数")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:PARAMETERS:EDIT')")
    public Result<Long> create(@RequestBody SystemParameters param) {
        if (param == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        if (param.getParamGroup() == null || param.getParamGroup().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "paramGroup不能为空");
        }
        if (param.getParamKey() == null || param.getParamKey().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "paramKey不能为空");
        }
        if (param.getCreatedAt() == null) param.setCreatedAt(new Date());
        param.setUpdatedAt(new Date());
        if (param.getIsSystem() == null) param.setIsSystem(false);

        systemParametersMapper.insertSelective(param);
        if ("AUTH".equalsIgnoreCase(param.getParamGroup())) {
            authzConfigService.invalidate();
        }
        return Result.success(param.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新系统参数")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:PARAMETERS:EDIT')")
    public Result<Void> update(@PathVariable Long id, @RequestBody SystemParameters param) {
        SystemParameters existing = systemParametersMapper.selectByPrimaryKey(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "系统参数不存在");
        }
        param.setId(id);
        param.setUpdatedAt(new Date());
        systemParametersMapper.updateByPrimaryKeySelective(param);
        if ("AUTH".equalsIgnoreCase(existing.getParamGroup()) || (param.getParamGroup() != null && "AUTH".equalsIgnoreCase(param.getParamGroup()))) {
            authzConfigService.invalidate();
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除系统参数")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:PARAMETERS:EDIT')")
    public Result<Void> delete(@PathVariable Long id) {
        SystemParameters existing = systemParametersMapper.selectByPrimaryKey(id);
        systemParametersMapper.deleteByPrimaryKey(id);
        if (existing != null && "AUTH".equalsIgnoreCase(existing.getParamGroup())) {
            authzConfigService.invalidate();
        }
        return Result.success();
    }
}
