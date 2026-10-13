package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.PortAreas;
import syy.portPilotage.service.PortAreasService;
import syy.portPilotage.mapper.PortAreasMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortAreasServiceImpl extends ServiceImpl<PortAreasMapper, PortAreas>
    implements PortAreasService{

    private final PortAreasMapper portAreasMapper;

    @Override
    public PortAreas savePortAreaData(PortAreas portArea) {
        portArea.setCreatedAt(new Date());
        portArea.setUpdatedAt(new Date());
        portAreasMapper.insert(portArea);
        return portArea;
    }

    @Override
    public PortAreas updatePortAreaData(PortAreas postPortArea) {
        PortAreas existingPortArea = portAreasMapper.selectById(postPortArea.getId());
        if (existingPortArea == null) {
            throw new RuntimeException("港区信息不存在");
        }

        copyNonNullProperties(postPortArea, existingPortArea);
        existingPortArea.setUpdatedAt(new Date());
        portAreasMapper.updateById(existingPortArea);
        return existingPortArea;
    }

    @Override
    public boolean deletePortAreaData(Long id) {
        return portAreasMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updatePortAreaStatus(Long id, String status) {
        PortAreas portArea = new PortAreas();
        portArea.setId(id);
        portArea.setStatus(status);
        portArea.setUpdatedAt(new Date());
        return portAreasMapper.updateById(portArea) > 0;
    }

    @Override
    public IPage<PortAreas> selectPage(PageQueryDTO query) {
        LambdaQueryWrapper<PortAreas> queryWrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = query.getKeyword().trim();
            queryWrapper.like(PortAreas::getAreaNameZh, keyword)
                    .or()
                    .like(PortAreas::getAreaNameEn, keyword)
                    .or()
                    .like(PortAreas::getAreaCode, keyword);
        }
        Page<PortAreas> page = new Page<>(query.getCurrent(), query.getSize());
        return portAreasMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<PortAreas> selectByPortId(Long portId) {
        LambdaQueryWrapper<PortAreas> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PortAreas::getPortId, portId);
        return portAreasMapper.selectList(queryWrapper);
    }

    @Override
    public List<PortAreas> searchPortAreas(String keyword) {
        LambdaQueryWrapper<PortAreas> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(PortAreas::getAreaNameZh, keyword)
                .or()
                .like(PortAreas::getAreaNameEn, keyword)
                .or()
                .like(PortAreas::getAreaCode, keyword);
        return portAreasMapper.selectList(queryWrapper);
    }

    
    private void copyNonNullProperties(PortAreas source, PortAreas target) {
        if (source.getPortId() != null) target.setPortId(source.getPortId());
        if (source.getAreaCode() != null) target.setAreaCode(source.getAreaCode());
        if (source.getAreaNameZh() != null) target.setAreaNameZh(source.getAreaNameZh());
        if (source.getAreaNameEn() != null) target.setAreaNameEn(source.getAreaNameEn());
        if (source.getAreaType() != null) target.setAreaType(source.getAreaType());
        if (source.getManagementCompany() != null) target.setManagementCompany(source.getManagementCompany());
        if (source.getContactPhone() != null) target.setContactPhone(source.getContactPhone());
        if (source.getLongitude() != null) target.setLongitude(source.getLongitude());
        if (source.getLatitude() != null) target.setLatitude(source.getLatitude());
        if (source.getBoundaryCoordinates() != null) target.setBoundaryCoordinates(source.getBoundaryCoordinates());
        if (source.getStatus() != null) target.setStatus(source.getStatus());
    }
}
