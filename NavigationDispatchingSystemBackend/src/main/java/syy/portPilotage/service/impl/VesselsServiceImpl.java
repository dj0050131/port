package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.entity.Vessels;
import syy.portPilotage.service.VesselsService;
import syy.portPilotage.mapper.VesselsMapper;
import syy.portPilotage.redis.service.CacheService;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VesselsServiceImpl extends ServiceImpl<VesselsMapper, Vessels>
        implements VesselsService{

    private final CacheService cacheService;

    public Vessels getById(Long id) {
        if (id == null) {
            return null;
        }
        Vessels cached = cacheService.getVessel(id, Vessels.class);
        if (cached != null) {
            log.debug("从缓存获取船舶信息: id={}", id);
            return cached;
        }
        Vessels vessel = super.getById(id);
        if (vessel != null) {
            cacheService.setVessel(id, vessel);
        }
        return vessel;
    }

    @Override
    public Vessels getByImoNumber(String imoNumber) {
        if (imoNumber == null || imoNumber.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<Vessels> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vessels::getImoNumber, imoNumber);
        Vessels vessel = this.getOne(queryWrapper);
        // 如果找到，缓存起来
        if (vessel != null && vessel.getId() != null) {
            cacheService.setVessel(vessel.getId(), vessel);
        }
        return vessel;
    }

    @Override
    public List<Vessels> getByVesselName(String vesselName) {
        LambdaQueryWrapper<Vessels> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Vessels::getNameZh, vesselName).or().like(Vessels::getNameEn, vesselName);
        return this.list(queryWrapper);
    }

    @Override
    public List<Vessels> getByVesselType(String vesselType) {
        LambdaQueryWrapper<Vessels> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vessels::getVesselType, vesselType);
        return this.list(queryWrapper);
    }

    @Override
    public List<Vessels> getByNationality(String nationality) {
        LambdaQueryWrapper<Vessels> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vessels::getNationality, nationality);
        return this.list(queryWrapper);
    }

    @Override
    public boolean createVessel(Vessels vessel) {
        vessel.setCreatedAt(new Date());
        vessel.setUpdatedAt(new Date());
        boolean result = this.save(vessel);
        if (result) {
            log.info("创建船舶信息成功: id={}, vesselName={}", vessel.getId(), vessel.getNameZh());
            if (vessel.getId() != null) {
                cacheService.setVessel(vessel.getId(), vessel);
            }
        } else {
            log.error("创建船舶信息失败: vesselName={}", vessel.getNameZh());
        }
        return result;
    }

    @Override
    public boolean updateVessel(Vessels vessel) {
        vessel.setUpdatedAt(new Date());
        boolean result = this.updateById(vessel);
        if (result) {
            log.info("更新船舶信息成功: id={}, vesselName={}", vessel.getId(), vessel.getNameZh());
            if (vessel.getId() != null) {
                cacheService.setVessel(vessel.getId(), vessel);
            }
        } else {
            log.error("更新船舶信息失败: id={}", vessel.getId());
        }
        return result;
    }

    @Override
    public boolean deleteVessel(Long id) {
        boolean result = this.removeById(id);
        if (result) {
            log.info("删除船舶信息成功: id={}", id);
            cacheService.deleteVessel(id);
        } else {
            log.error("删除船舶信息失败: id={}", id);
        }
        return result;
    }

    @Override
    public boolean isBlacklisted(Long vesselId) {
        Vessels vessel = this.getById(vesselId);
        if (vessel != null && vessel.getBlacklistRecord() != null) {
            return !vessel.getBlacklistRecord().isEmpty();
        }
        return false;
    }
}
