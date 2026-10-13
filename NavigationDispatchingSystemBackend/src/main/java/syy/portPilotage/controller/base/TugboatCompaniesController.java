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
import syy.portPilotage.entity.TugboatCompanies;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.TugboatCompaniesService;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/base/tugboatCompanies")
@RequiredArgsConstructor
@Tag(name = "拖轮公司管理", description = "拖轮公司（服务商）基础数据管理接口")
public class TugboatCompaniesController {

    @Autowired
    private TugboatCompaniesService tugboatCompaniesService;

    @PostMapping
    @Operation(summary = "创建拖轮公司")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@RequestBody TugboatCompanies c) {
        if (c.getCompanyCode() == null || c.getCompanyCode().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公司编码不能为空");
        }
        if (c.getCompanyName() == null || c.getCompanyName().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "公司名称不能为空");
        }

        boolean codeExists = tugboatCompaniesService.count(new LambdaQueryWrapper<TugboatCompanies>()
                .eq(TugboatCompanies::getCompanyCode, c.getCompanyCode().trim())) > 0;
        if (codeExists) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "公司编码已存在");
        }
        boolean nameExists = tugboatCompaniesService.count(new LambdaQueryWrapper<TugboatCompanies>()
                .eq(TugboatCompanies::getCompanyName, c.getCompanyName().trim())) > 0;
        if (nameExists) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "公司名称已存在");
        }

        c.setCompanyCode(c.getCompanyCode().trim());
        c.setCompanyName(c.getCompanyName().trim());
        if (c.getStatus() == null || c.getStatus().trim().isEmpty()) {
            c.setStatus("ACTIVE");
        }
        c.setCreatedAt(new Date());
        c.setUpdatedAt(new Date());
        tugboatCompaniesService.save(c);
        return Result.success(c.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新拖轮公司")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @RequestBody TugboatCompanies c) {
        TugboatCompanies existing = tugboatCompaniesService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "拖轮公司不存在");
        }

        if (c.getCompanyCode() != null && !c.getCompanyCode().trim().isEmpty()) {
            String code = c.getCompanyCode().trim();
            boolean codeExists = tugboatCompaniesService.count(new LambdaQueryWrapper<TugboatCompanies>()
                    .eq(TugboatCompanies::getCompanyCode, code)
                    .ne(TugboatCompanies::getId, id)) > 0;
            if (codeExists) {
                throw new BusinessException(ErrorCode.ALREADY_EXISTS, "公司编码已存在");
            }
            existing.setCompanyCode(code);
        }
        if (c.getCompanyName() != null && !c.getCompanyName().trim().isEmpty()) {
            String name = c.getCompanyName().trim();
            boolean nameExists = tugboatCompaniesService.count(new LambdaQueryWrapper<TugboatCompanies>()
                    .eq(TugboatCompanies::getCompanyName, name)
                    .ne(TugboatCompanies::getId, id)) > 0;
            if (nameExists) {
                throw new BusinessException(ErrorCode.ALREADY_EXISTS, "公司名称已存在");
            }
            existing.setCompanyName(name);
        }

        if (c.getContactName() != null) existing.setContactName(c.getContactName());
        if (c.getContactPhone() != null) existing.setContactPhone(c.getContactPhone());
        if (c.getNotes() != null) existing.setNotes(c.getNotes());
        if (c.getStatus() != null && !c.getStatus().trim().isEmpty()) existing.setStatus(c.getStatus().trim());

        existing.setUpdatedAt(new Date());
        tugboatCompaniesService.updateById(existing);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除拖轮公司")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        tugboatCompaniesService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取拖轮公司详情")
    public Result<TugboatCompanies> getById(@PathVariable Long id) {
        TugboatCompanies c = tugboatCompaniesService.getById(id);
        if (c == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "拖轮公司不存在");
        }
        return Result.success(c);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询拖轮公司")
    public Result<PageResult<TugboatCompanies>> page(PageQueryDTO query) {
        Page<TugboatCompanies> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<TugboatCompanies> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TugboatCompanies::getUpdatedAt);
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(TugboatCompanies::getCompanyName, kw)
                    .or()
                    .like(TugboatCompanies::getCompanyCode, kw));
        }
        IPage<TugboatCompanies> result = tugboatCompaniesService.page(page, wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @GetMapping("/list")
    @Operation(summary = "获取拖轮公司列表")
    public Result<List<TugboatCompanies>> list(@RequestParam(required = false) String status) {
        LambdaQueryWrapper<TugboatCompanies> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(TugboatCompanies::getStatus, status.trim());
        }
        wrapper.orderByAsc(TugboatCompanies::getCompanyName);
        return Result.success(tugboatCompaniesService.list(wrapper));
    }
}
