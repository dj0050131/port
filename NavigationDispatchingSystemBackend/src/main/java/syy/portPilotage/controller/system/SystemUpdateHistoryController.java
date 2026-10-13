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
import syy.portPilotage.entity.SystemUpdateHistory;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.mapper.SystemUpdateHistoryMapper;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/system/update-history")
@RequiredArgsConstructor
@Tag(name = "更新历史", description = "系统更新历史（发布日志）")
public class SystemUpdateHistoryController {

    private final SystemUpdateHistoryMapper mapper;

    @GetMapping("/page")
    @Operation(summary = "分页查询更新历史")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:UPDATE_HISTORY:VIEW') or hasAuthority('SYSTEM:UPDATE_HISTORY:EDIT')")
    public Result<PageResult<SystemUpdateHistory>> page(PageQueryDTO query,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Integer published) {
        int current = query.getCurrent();
        int size = query.getSize();
        int offset = (current - 1) * size;

        List<SystemUpdateHistory> records = mapper.selectPage(offset, size,
                keyword == null ? null : keyword.trim(),
                published);
        Long total = mapper.countPage(keyword == null ? null : keyword.trim(), published);
        return Result.success(PageResult.of(total == null ? 0 : total, current, size, records));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取更新历史详情")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:UPDATE_HISTORY:VIEW') or hasAuthority('SYSTEM:UPDATE_HISTORY:EDIT')")
    public Result<SystemUpdateHistory> detail(@PathVariable Long id) {
        SystemUpdateHistory record = mapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "记录不存在");
        }
        return Result.success(record);
    }

    @PostMapping
    @Operation(summary = "新增更新历史")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:UPDATE_HISTORY:EDIT')")
    public Result<Long> create(@RequestBody SystemUpdateHistory body) {
        if (body == null || body.getTitle() == null || body.getTitle().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "标题不能为空");
        }
        Date now = new Date();
        body.setTitle(body.getTitle().trim());
        if (body.getVersion() != null) body.setVersion(body.getVersion().trim());
        if (body.getPublished() == null) body.setPublished(0);
        if (body.getPublished() != null && body.getPublished() == 1 && body.getPublishedAt() == null) {
            body.setPublishedAt(now);
        }
        body.setCreatedAt(now);
        body.setUpdatedAt(now);
        mapper.insert(body);
        return Result.success(body.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新更新历史")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:UPDATE_HISTORY:EDIT')")
    public Result<Void> update(@PathVariable Long id, @RequestBody SystemUpdateHistory body) {
        SystemUpdateHistory existing = mapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "记录不存在");
        }
        Date now = new Date();
        body.setId(id);
        if (body.getTitle() != null) body.setTitle(body.getTitle().trim());
        if (body.getVersion() != null) body.setVersion(body.getVersion().trim());

        // 发布逻辑：从未发布 -> 发布，且未给时间则自动补
        if (body.getPublished() != null && body.getPublished() == 1 && body.getPublishedAt() == null) {
            body.setPublishedAt(now);
        }
        body.setUpdatedAt(now);
        mapper.updateById(body);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除更新历史")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:UPDATE_HISTORY:EDIT')")
    public Result<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.success();
    }
}
