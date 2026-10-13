package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Anchorages;
import syy.portPilotage.mapper.AnchoragesMapper;
import syy.portPilotage.service.AnchoragesService;

import java.util.Date;
import java.util.List;

/**
 * 锚地信息服务实现类
 */
@Service
@RequiredArgsConstructor
public class AnchoragesServiceImpl extends ServiceImpl<AnchoragesMapper, Anchorages> implements AnchoragesService {

    private final AnchoragesMapper anchoragesMapper;

    @Override
    public Anchorages saveAnchorageData(Anchorages anchorage) {
        anchorage.setCreatedAt(new Date());
        anchorage.setUpdatedAt(new Date());
        anchoragesMapper.insert(anchorage);
        return anchorage;
    }

    @Override
    public Anchorages updateAnchorageData(Anchorages postAnchorage) {
        Anchorages existingAnchorage = anchoragesMapper.selectById(postAnchorage.getId());
        if (existingAnchorage == null) {
            throw new RuntimeException("锚地信息不存在");
        }

        copyNonNullProperties(postAnchorage, existingAnchorage);
        existingAnchorage.setUpdatedAt(new Date());
        anchoragesMapper.updateById(existingAnchorage);
        return existingAnchorage;
    }

    @Override
    public boolean deleteAnchorageData(Long id) {
        return anchoragesMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateAnchorageStatus(Long id, String status) {
        Anchorages anchorage = new Anchorages();
        anchorage.setId(id);
        anchorage.setStatus(status);
        anchorage.setUpdatedAt(new Date());
        return anchoragesMapper.updateById(anchorage) > 0;
    }

    @Override
    public IPage<Anchorages> selectPage(PageQueryDTO query) {
        LambdaQueryWrapper<Anchorages> queryWrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = query.getKeyword().trim();
            queryWrapper.like(Anchorages::getAnchorageNameZh, keyword)
                    .or()
                    .like(Anchorages::getAnchorageNameEn, keyword)
                    .or()
                    .like(Anchorages::getAnchorageCode, keyword);
        }
        Page<Anchorages> page = new Page<>(query.getCurrent(), query.getSize());
        return anchoragesMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<Anchorages> selectByPortAreaId(Long portAreaId) {
        LambdaQueryWrapper<Anchorages> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Anchorages::getPortAreaId, portAreaId);
        return anchoragesMapper.selectList(queryWrapper);
    }

    @Override
    public List<Anchorages> searchAnchorages(String keyword) {
        LambdaQueryWrapper<Anchorages> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Anchorages::getAnchorageNameZh, keyword)
                .or()
                .like(Anchorages::getAnchorageNameEn, keyword)
                .or()
                .like(Anchorages::getAnchorageCode, keyword);
        return anchoragesMapper.selectList(queryWrapper);
    }

    
    private void copyNonNullProperties(Anchorages source, Anchorages target) {
        if (source.getPortAreaId() != null) target.setPortAreaId(source.getPortAreaId());
        if (source.getAnchorageCode() != null) target.setAnchorageCode(source.getAnchorageCode());
        if (source.getAnchorageNameZh() != null) target.setAnchorageNameZh(source.getAnchorageNameZh());
        if (source.getAnchorageNameEn() != null) target.setAnchorageNameEn(source.getAnchorageNameEn());
        if (source.getAnchorageType() != null) target.setAnchorageType(source.getAnchorageType());
        if (source.getCapacity() != null) target.setCapacity(source.getCapacity());
        if (source.getMaxDraft() != null) target.setMaxDraft(source.getMaxDraft());
        if (source.getMinDraft() != null) target.setMinDraft(source.getMinDraft());
        if (source.getLongitude() != null) target.setLongitude(source.getLongitude());
        if (source.getLatitude() != null) target.setLatitude(source.getLatitude());
        if (source.getRadius() != null) target.setRadius(source.getRadius());
        if (source.getBoundaryCoordinates() != null) target.setBoundaryCoordinates(source.getBoundaryCoordinates());
        if (source.getTideRestriction() != null) target.setTideRestriction(source.getTideRestriction());
        if (source.getWeatherRestriction() != null) target.setWeatherRestriction(source.getWeatherRestriction());
        if (source.getStatus() != null) target.setStatus(source.getStatus());
    }
}