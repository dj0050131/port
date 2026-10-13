package syy.portPilotage.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.AuthPermissions;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.AuthPermissionsService;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/system/auth-permissions")
@Tag(name = "权限码信息", description = "权限码信息表（用于可视化配置身份权限）")
public class AuthPermissionsController {

    private final AuthPermissionsService service;

    public AuthPermissionsController(AuthPermissionsService service) {
        this.service = service;
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询权限码")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:AUTH_PERMISSIONS:MANAGE')")
    public Result<PageResult<AuthPermissions>> page(PageQueryDTO query,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String permGroup) {
        LambdaQueryWrapper<AuthPermissions> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(AuthPermissions::getPermCode, kw)
                    .or()
                    .like(AuthPermissions::getPermName, kw));
        }
        if (permGroup != null && !permGroup.trim().isEmpty()) {
            wrapper.eq(AuthPermissions::getPermGroup, permGroup.trim());
        }
        wrapper.orderByAsc(AuthPermissions::getSortOrder).orderByAsc(AuthPermissions::getId);
        IPage<AuthPermissions> page = service.page(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/list")
    @Operation(summary = "获取权限码列表")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:AUTH_PERMISSIONS:MANAGE')")
    public Result<List<AuthPermissions>> list(@RequestParam(required = false) String permGroup) {
        LambdaQueryWrapper<AuthPermissions> wrapper = new LambdaQueryWrapper<>();
        if (permGroup != null && !permGroup.trim().isEmpty()) {
            wrapper.eq(AuthPermissions::getPermGroup, permGroup.trim());
        }
        wrapper.orderByAsc(AuthPermissions::getSortOrder).orderByAsc(AuthPermissions::getId);
        return Result.success(service.list(wrapper));
    }

    @PostMapping
    @Operation(summary = "新增权限码")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:AUTH_PERMISSIONS:MANAGE')")
    public Result<Long> create(@RequestBody AuthPermissions body) {
        if (body == null || body.getPermCode() == null || body.getPermCode().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "permCode不能为空");
        }
        if (body.getPermName() == null || body.getPermName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "permName不能为空");
        }
        String code = body.getPermCode().trim();
        boolean exists = service.count(new LambdaQueryWrapper<AuthPermissions>().eq(AuthPermissions::getPermCode, code)) > 0;
        if (exists) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "权限码已存在");
        }
        body.setPermCode(code);
        body.setPermName(body.getPermName().trim());
        if (body.getIsSystem() == null) body.setIsSystem(false);
        if (body.getSortOrder() == null) body.setSortOrder(0);
        body.setCreatedAt(new Date());
        body.setUpdatedAt(new Date());
        service.save(body);
        return Result.success(body.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新权限码")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:AUTH_PERMISSIONS:MANAGE')")
    public Result<Void> update(@PathVariable Long id, @RequestBody AuthPermissions body) {
        AuthPermissions existing = service.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "权限码不存在");
        }
        if (body.getPermCode() != null && !body.getPermCode().trim().isEmpty()) {
            String code = body.getPermCode().trim();
            boolean exists = service.count(new LambdaQueryWrapper<AuthPermissions>()
                    .eq(AuthPermissions::getPermCode, code)
                    .ne(AuthPermissions::getId, id)) > 0;
            if (exists) throw new BusinessException(ErrorCode.ALREADY_EXISTS, "权限码已存在");
            existing.setPermCode(code);
        }
        if (body.getPermName() != null && !body.getPermName().trim().isEmpty()) {
            existing.setPermName(body.getPermName().trim());
        }
        if (body.getPermGroup() != null) existing.setPermGroup(body.getPermGroup());
        if (body.getDescription() != null) existing.setDescription(body.getDescription());
        if (body.getIsSystem() != null) existing.setIsSystem(body.getIsSystem());
        if (body.getSortOrder() != null) existing.setSortOrder(body.getSortOrder());
        existing.setUpdatedAt(new Date());
        service.updateById(existing);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限码")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:AUTH_PERMISSIONS:MANAGE')")
    public Result<Void> delete(@PathVariable Long id) {
        service.removeById(id);
        return Result.success();
    }
}
