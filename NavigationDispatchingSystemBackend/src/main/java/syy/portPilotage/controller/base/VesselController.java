package syy.portPilotage.controller.base;

import cn.hutool.core.util.StrUtil;
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
import syy.portPilotage.dto.base.VesselQueryDTO;
import syy.portPilotage.entity.Vessels;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.VesselsService;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RestController
@RequestMapping("/api/base/vessels")
@RequiredArgsConstructor
@Tag(name = "船舶管理", description = "船舶基础数据管理接口")
public class VesselController {
    
    @Autowired
    VesselsService vesselsService;
    
    @PostMapping
    @Operation(summary = "创建船舶")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Long> create(@RequestBody Vessels vessel) {
        log.info("创建船舶: callSign={}", vessel.getVesselCallSign());
        
        vessel.setCreatedAt(new Date());
        vessel.setUpdatedAt(new Date());
        
        vesselsService.save(vessel);
        
        log.info("船舶创建成功: id={}", vessel.getId());
        return Result.success(vessel.getId());
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新船舶")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Vessels vessel) {
        log.info("更新船舶: id={}", id);
        
        Vessels existingVessel = vesselsService.getById(id);
        if (existingVessel == null) {
            throw new BusinessException(ErrorCode.VESSEL_NOT_FOUND);
        }
        
        vessel.setId(id);
        vessel.setUpdatedAt(new Date());
        
        vesselsService.updateById(vessel);
        
        log.info("船舶更新成功: id={}", id);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除船舶")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除船舶: id={}", id);
        
        vesselsService.removeById(id);
        
        log.info("船舶删除成功: id={}", id);
        return Result.success();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取船舶详情")
    public Result<Vessels> getById(@PathVariable Long id) {
        Vessels vessel = vesselsService.getById(id);
        if (vessel == null) {
            throw new BusinessException(ErrorCode.VESSEL_NOT_FOUND);
        }
        
        return Result.success(vessel);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询船舶")
    public Result<PageResult<Vessels>> page(VesselQueryDTO query) {
        Page<Vessels> page = new Page<>(query.getCurrent(), query.getSize());
        
        LambdaQueryWrapper<Vessels> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.like(Vessels::getNameZh, keyword)
                    .or()
                    .like(Vessels::getNameEn, keyword)
                    .or()
                    .like(Vessels::getVesselCallSign, keyword)
                    .or()
                    .like(Vessels::getImoNumber, keyword);
        }
        
        if (StrUtil.isNotBlank(query.getVesselName())) {
            wrapper.like(Vessels::getVesselCallSign, query.getVesselName());
        }
        if (StrUtil.isNotBlank(query.getVesselType())) {
            wrapper.eq(Vessels::getVesselType, query.getVesselType());
        }
        if (StrUtil.isNotBlank(query.getNationality())) {
            wrapper.eq(Vessels::getNationality, query.getNationality());
        }
        if (StrUtil.isNotBlank(query.getCallSign())) {
            wrapper.eq(Vessels::getVesselCallSign, query.getCallSign());
        }
        if (StrUtil.isNotBlank(query.getImoNumber())) {
            wrapper.eq(Vessels::getImoNumber, query.getImoNumber());
        }
        
        wrapper.orderByDesc(Vessels::getCreatedAt);
        
        IPage<Vessels> result = vesselsService.page(page, wrapper);
        
        PageResult<Vessels> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );
        
        return Result.success(pageResult);
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取所有船舶列表")
    public Result<List<Vessels>> list() {
        List<Vessels> list = vesselsService.list();
        return Result.success(list);
    }
}