package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syy.portPilotage.dto.base.TugboatDTO;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.TugboatCompanies;
import syy.portPilotage.entity.Tugboats;
import syy.portPilotage.mapper.TugboatCompaniesMapper;
import syy.portPilotage.mapper.TugboatsMapper;
import syy.portPilotage.service.TugboatsService;
import syy.portPilotage.util.BeanCopyUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TugboatsServiceImpl extends ServiceImpl<TugboatsMapper, Tugboats>
        implements TugboatsService{

    @Autowired
    private TugboatsMapper tugboatsMapper;
    @Autowired
    private TugboatCompaniesMapper tugboatCompaniesMapper;

    @Override
    public Tugboats saveTugboatData(Tugboats tugboat) {
        tugboat.setCreatedAt(new Date());
        tugboat.setUpdatedAt(new Date());
        normalizeOwnerCompany(tugboat);
        tugboatsMapper.insert(tugboat);
        return tugboat;
    }

    @Override
    public Tugboats updateTugboatData(Tugboats tugboat) {
        Tugboats existingTugboat = tugboatsMapper.selectById(tugboat.getId());
        if (existingTugboat == null) {
            throw new RuntimeException("拖轮信息不存在");
        }
        copyNonNullProperties(tugboat, existingTugboat);
        normalizeOwnerCompany(existingTugboat);
        // build_year 是 MySQL YEAR：兼容前端可能传入的 "yyyy-MM-dd"/ISO/数字字符串
        normalizeBuildYear(existingTugboat);
        existingTugboat.setUpdatedAt(new Date());
        tugboatsMapper.updateById(existingTugboat);
        return existingTugboat;
    }

    @Override
    public boolean isTugboatInUse(Long id) {
        Tugboats tugboat = tugboatsMapper.selectById(id);
        return "ACTIVE".equals(tugboat.getStatus()) || "BUSY".equals(tugboat.getStatus());
    }

    @Override
    public boolean deleteTugboatData(Long id) {
        return tugboatsMapper.deleteById(id) > 0;
    }

    @Override
    public List<Tugboats> searchTugboats(String keyword) {
        return tugboatsMapper.selectList(new LambdaQueryWrapper<Tugboats>()
                .like(Tugboats::getTugboatName, keyword)
                .or()
                .like(Tugboats::getTugboatCode, keyword)
                .or()
                .like(Tugboats::getImoNumber, keyword)
                .or()
                .like(Tugboats::getCallSign, keyword));
    }

    @Override
    public List<Tugboats> selectTugboatList(String status, String type) {
        LambdaQueryWrapper<Tugboats> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(status != null, Tugboats::getStatus, status)
                .eq(type != null, Tugboats::getTugboatType, type);
        return tugboatsMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updateTugboatStatus(Long id, Object status) {
        Tugboats tugboat = new Tugboats();
        LambdaQueryWrapper<Tugboats> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tugboats::getId, id);
        tugboat.setStatus(status);
        tugboat.setUpdatedAt(new Date());
        int update = tugboatsMapper.update(tugboat, queryWrapper);
        return update > 0;
    }

    @Override
    public IPage<Tugboats> selectPage(PageQueryDTO query) {
        LambdaQueryWrapper<Tugboats> queryWrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            queryWrapper.like(Tugboats::getTugboatName, query.getKeyword())
                    .or()
                    .like(Tugboats::getTugboatCode, query.getKeyword())
                    .or()
                    .like(Tugboats::getImoNumber, query.getKeyword());
        }

        Page<Tugboats> page = new Page<>(query.getCurrent(), query.getSize());
        return tugboatsMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<TugboatDTO> getAvailableTugboats() {
        LambdaQueryWrapper<Tugboats> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tugboats::getStatus, "AVAILABLE");
        List<Tugboats> tugboatsList = tugboatsMapper.selectList(queryWrapper);
        return BeanCopyUtil.copyList(tugboatsList, TugboatDTO.class);
    }

    @Override
    public List<TugboatDTO> getAllTugboats() {
        List<Tugboats> tugboatsList = tugboatsMapper.selectList(null);
        return BeanCopyUtil.copyList(tugboatsList, TugboatDTO.class);
    }

    @Override
    public TugboatDTO getTugboatById(Long id) {
        Tugboats tugboat = tugboatsMapper.selectById(id);
        if (tugboat == null) {
            return null;
        }
        return BeanCopyUtil.copy(tugboat, TugboatDTO.class);
    }

    
    private void copyNonNullProperties(Tugboats source, Tugboats target) {
        if (source.getTugboatCode() != null) target.setTugboatCode(source.getTugboatCode());
        if (source.getTugboatName() != null) target.setTugboatName(source.getTugboatName());
        if (source.getImoNumber() != null) target.setImoNumber(source.getImoNumber());
        if (source.getCallSign() != null) target.setCallSign(source.getCallSign());
        if (source.getTugboatType() != null) target.setTugboatType(source.getTugboatType());
        if (source.getOwnerCompanyId() != null) target.setOwnerCompanyId(source.getOwnerCompanyId());
        if (source.getOwnerCompany() != null) target.setOwnerCompany(source.getOwnerCompany());
        if (source.getBollardPull() != null) target.setBollardPull(source.getBollardPull());
        if (source.getLength() != null) target.setLength(source.getLength());
        if (source.getPower() != null) target.setPower(source.getPower());
        if (source.getBuildYear() != null) target.setBuildYear(source.getBuildYear());
        if (source.getStatus() != null) target.setStatus(source.getStatus());
        if (source.getCurrentLocation() != null) target.setCurrentLocation(source.getCurrentLocation());
    }

    /**
     * 兼容旧字段 owner_company：
     * - 新前端优先传 ownerCompanyId
     * - 旧前端/历史数据可能只传 ownerCompany(公司名)，这里尽量自动映射到 tugboat_companies
     */
    private void normalizeOwnerCompany(Tugboats tugboat) {
        if (tugboat.getOwnerCompanyId() != null) {
            TugboatCompanies c = tugboatCompaniesMapper.selectById(tugboat.getOwnerCompanyId());
            if (c != null) {
                tugboat.setOwnerCompany(c.getCompanyName());
                tugboat.setOwnerCompanyName(c.getCompanyName());
            }
            return;
        }

        String name = tugboat.getOwnerCompany();
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) return;

        TugboatCompanies exists = tugboatCompaniesMapper.selectOne(
                new LambdaQueryWrapper<TugboatCompanies>().eq(TugboatCompanies::getCompanyName, name),
                false
        );
        if (exists != null) {
            tugboat.setOwnerCompanyId(exists.getId());
            tugboat.setOwnerCompanyName(exists.getCompanyName());
            return;
        }

        TugboatCompanies c = new TugboatCompanies();
        c.setCompanyName(name);
        c.setCompanyCode(generateCompanyCode());
        c.setStatus("ACTIVE");
        c.setCreatedAt(new Date());
        c.setUpdatedAt(new Date());
        tugboatCompaniesMapper.insert(c);

        tugboat.setOwnerCompanyId(c.getId());
        tugboat.setOwnerCompanyName(c.getCompanyName());
    }

    private String generateCompanyCode() {
        String ms = String.valueOf(System.currentTimeMillis());
        String tail = ms.length() > 6 ? ms.substring(ms.length() - 6) : ms;
        return "TC" + tail;
    }

    private void normalizeBuildYear(Tugboats tugboat) {
        Object v = tugboat.getBuildYear();
        if (v == null) return;

        if (v instanceof Number n) {
            int y = n.intValue();
            tugboat.setBuildYear(y);
            return;
        }

        String s = String.valueOf(v).trim();
        if (s.isEmpty()) {
            tugboat.setBuildYear(null);
            return;
        }

        if (s.length() >= 4 && Character.isDigit(s.charAt(0)) && Character.isDigit(s.charAt(1)) && Character.isDigit(s.charAt(2)) && Character.isDigit(s.charAt(3))) {
            try {
                int y = Integer.parseInt(s.substring(0, 4));
                tugboat.setBuildYear(y);
            } catch (NumberFormatException ignored) {
            }
        }
    }
}