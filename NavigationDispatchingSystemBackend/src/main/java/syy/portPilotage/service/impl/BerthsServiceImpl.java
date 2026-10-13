package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Berths;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.mapper.BerthsMapper;
import syy.portPilotage.service.BerthsService;
import syy.portPilotage.redis.service.CacheService;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BerthsServiceImpl extends ServiceImpl<BerthsMapper, Berths>
        implements BerthsService {

    private final CacheService cacheService;

    public Berths getById(Long id) {
        if (id == null) {
            return null;
        }
        Berths cached = cacheService.getBerth(id, Berths.class);
        if (cached != null) {
            log.debug("从缓存获取泊位信息: id={}", id);
            return cached;
        }
        Berths berth = super.getById(id);
        if (berth != null) {
            cacheService.setBerth(id, berth);
        }
        return berth;
    }

    @Override
    public Berths saveBerthData(Berths berth) {
        berth.setCreatedAt(new Date());
        berth.setUpdatedAt(new Date());
        this.save(berth);
        if (berth.getId() != null) {
            cacheService.setBerth(berth.getId(), berth);
        }
        return berth;
    }

    @Override
    public Berths updateBerthData(Berths postBerth) {
        Berths existingBerth = this.getById(postBerth.getId());
        if (existingBerth == null) {
            log.info("泊位信息不存在: {}", ErrorCode.BERTH_NOT_FOUND);
            throw new RuntimeException("泊位信息不存在");
        }

        copyNonNullProperties(postBerth, existingBerth);
        existingBerth.setUpdatedAt(new Date());
        this.updateById(existingBerth);
        if (existingBerth.getId() != null) {
            cacheService.setBerth(existingBerth.getId(), existingBerth);
        }
        return existingBerth;
    }

    @Override
    public boolean deleteBerthData(Long id) {
        boolean result = this.removeById(id);
        if (result) {
            cacheService.deleteBerth(id);
        }
        return result;
    }

    @Override
    public boolean updateBerthStatus(Long id, String status) {
        Berths berth = new Berths();
        berth.setId(id);
        berth.setBerthStatus(status);
        berth.setUpdatedAt(new Date());
        return this.updateById(berth);
    }

    @Override
    public IPage<Berths> selectPage(PageQueryDTO query) {
        LambdaQueryWrapper<Berths> queryWrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = query.getKeyword().trim();
            queryWrapper.like(Berths::getBerthName, keyword)
                    .or()
                    .like(Berths::getBerthCode, keyword)
                    .or()
                    .like(Berths::getPortArea, keyword);
        }
        Page<Berths> page = new Page<>(query.getCurrent(), query.getSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<Berths> selectByWharvesId(String wharvesId) {
        LambdaQueryWrapper<Berths> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Berths::getWharfId, wharvesId);
        return this.list(queryWrapper);
    }

    @Override
    public List<Berths> searchBerths(String keyword) {
        LambdaQueryWrapper<Berths> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Berths::getBerthName, keyword)
                .or()
                .like(Berths::getBerthCode, keyword)
                .or()
                .like(Berths::getPortArea, keyword);
        return this.list(queryWrapper);
    }

    
    private void copyNonNullProperties(Berths source, Berths target) {
        if (source.getBerthCode() != null) target.setBerthCode(source.getBerthCode());
        if (source.getPortArea() != null) target.setPortArea(source.getPortArea());
        if (source.getBerthType() != null) target.setBerthType(source.getBerthType());
        if (source.getBerthName() != null) target.setBerthName(source.getBerthName());
        if (source.getWharfDirection() != null) target.setWharfDirection(source.getWharfDirection());
        if (source.getLongitude() != null) target.setLongitude(source.getLongitude());
        if (source.getLatitude() != null) target.setLatitude(source.getLatitude());
        if (source.getStartLatitude() != null) target.setStartLatitude(source.getStartLatitude());
        if (source.getStartLongitude() != null) target.setStartLongitude(source.getStartLongitude());
        if (source.getEndLatitude() != null) target.setEndLatitude(source.getEndLatitude());
        if (source.getEndLongitude() != null) target.setEndLongitude(source.getEndLongitude());
        if (source.getBerthDirection() != null) target.setBerthDirection(source.getBerthDirection());
        if (source.getMaxDraft() != null) target.setMaxDraft(source.getMaxDraft());
        if (source.getBerthLength() != null) target.setBerthLength(source.getBerthLength());
        if (source.getBerthDepth() != null) target.setBerthDepth(source.getBerthDepth());
        if (source.getBerthStatus() != null) target.setBerthStatus(source.getBerthStatus());
        if (source.getContactPhone() != null) target.setContactPhone(source.getContactPhone());
    }
}
