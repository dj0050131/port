package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Wharves;
import syy.portPilotage.mapper.WharvesMapper;
import syy.portPilotage.service.WharvesService;

import java.util.Date;
import java.util.List;

/**
 * 码头信息服务实现类
 */
@Service
@RequiredArgsConstructor
public class WharvesServiceImpl extends ServiceImpl<WharvesMapper, Wharves> implements WharvesService {

    private final WharvesMapper wharvesMapper;

    @Override
    public Wharves saveWharfData(Wharves wharf) {
        wharf.setCreatedAt(new Date());
        wharf.setUpdatedAt(new Date());
        wharvesMapper.insert(wharf);
        return wharf;
    }

    @Override
    public Wharves updateWharfData(Wharves postWharf) {
        Wharves existingWharf = wharvesMapper.selectById(postWharf.getId());
        if (existingWharf == null) {
            throw new RuntimeException("码头信息不存在");
        }

        copyNonNullProperties(postWharf, existingWharf);
        existingWharf.setUpdatedAt(new Date());
        wharvesMapper.updateById(existingWharf);
        return existingWharf;
    }

    @Override
    public boolean deleteWharfData(Long id) {
        return wharvesMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateWharfStatus(Long id, String status) {
        Wharves wharf = new Wharves();
        wharf.setId(id);
        wharf.setOperationalStatus(status);
        wharf.setUpdatedAt(new Date());
        return wharvesMapper.updateById(wharf) > 0;
    }

    @Override
    public IPage<Wharves> selectPage(PageQueryDTO query) {
        LambdaQueryWrapper<Wharves> queryWrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = query.getKeyword().trim();
            queryWrapper.like(Wharves::getWharfNameZh, keyword)
                    .or()
                    .like(Wharves::getWharfNameEn, keyword)
                    .or()
                    .like(Wharves::getWharfCode, keyword);
        }
        Page<Wharves> page = new Page<>(query.getCurrent(), query.getSize());
        return wharvesMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<Wharves> selectByPortAreaId(Long portAreaId) {
        LambdaQueryWrapper<Wharves> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Wharves::getPortAreaId, portAreaId);
        return wharvesMapper.selectList(queryWrapper);
    }

    @Override
    public List<Wharves> searchWharves(String keyword) {
        LambdaQueryWrapper<Wharves> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Wharves::getWharfNameZh, keyword)
                .or()
                .like(Wharves::getWharfNameEn, keyword)
                .or()
                .like(Wharves::getWharfCode, keyword);
        return wharvesMapper.selectList(queryWrapper);
    }

    
    private void copyNonNullProperties(Wharves source, Wharves target) {
        if (source.getPortAreaId() != null) target.setPortAreaId(source.getPortAreaId());
        if (source.getWharfCode() != null) target.setWharfCode(source.getWharfCode());
        if (source.getWharfNameZh() != null) target.setWharfNameZh(source.getWharfNameZh());
        if (source.getWharfNameEn() != null) target.setWharfNameEn(source.getWharfNameEn());
        if (source.getWharfLength() != null) target.setWharfLength(source.getWharfLength());
        if (source.getWharfDepth() != null) target.setWharfDepth(source.getWharfDepth());
        if (source.getConstructionDate() != null) target.setConstructionDate(source.getConstructionDate());
        if (source.getWharfType() != null) target.setWharfType(source.getWharfType());
        if (source.getOperationalStatus() != null) target.setOperationalStatus(source.getOperationalStatus());
        if (source.getManagementCompany() != null) target.setManagementCompany(source.getManagementCompany());
        if (source.getContactPhone() != null) target.setContactPhone(source.getContactPhone());
        if (source.getLongitude() != null) target.setLongitude(source.getLongitude());
        if (source.getLatitude() != null) target.setLatitude(source.getLatitude());
    }
}