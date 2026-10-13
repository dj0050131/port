package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.PilotBoats;
import syy.portPilotage.mapper.PilotBoatsMapper;
import syy.portPilotage.service.PilotBoatsService;

import java.util.Date;
import java.util.List;

@Service
public class PilotBoatsServiceImpl extends ServiceImpl<PilotBoatsMapper, PilotBoats>
        implements PilotBoatsService{

    @Autowired
    private PilotBoatsMapper pilotBoatsMapper;

    @Override
    public PilotBoats savePilotBoatData(PilotBoats pilotBoat) {
        pilotBoat.setCreatedAt(new Date());
        pilotBoat.setUpdatedAt(new Date());
        pilotBoatsMapper.insert(pilotBoat);
        return pilotBoat;
    }

    @Override
    public PilotBoats updatePilotBoatData(PilotBoats pilotBoat) {
        PilotBoats existingBoat = pilotBoatsMapper.selectById(pilotBoat.getId());
        if (existingBoat == null) {
            throw new RuntimeException("引航艇信息不存在");
        }
        copyNonNullProperties(pilotBoat, existingBoat);
        existingBoat.setUpdatedAt(new Date());
        pilotBoatsMapper.updateById(existingBoat);
        return existingBoat;
    }

    @Override
    public boolean isPilotBoatInUse(Long id) {
        PilotBoats boat = pilotBoatsMapper.selectById(id);
        return "ACTIVE".equals(boat.getCurrentStatus()) || "BUSY".equals(boat.getCurrentStatus());
    }

    @Override
    public boolean deletePilotBoatData(Long id) {
        return pilotBoatsMapper.deleteById(id) > 0;
    }

    @Override
    public List<PilotBoats> searchPilotBoats(String keyword) {
        return pilotBoatsMapper.selectList(new LambdaQueryWrapper<PilotBoats>()
                .like(PilotBoats::getBoatName, keyword)
                .or()
                .like(PilotBoats::getVesselCallSign, keyword)
                .or()
                .like(PilotBoats::getImoNumber, keyword));
    }

    @Override
    public List<PilotBoats> selectPilotBoatList(String status, String location) {
        LambdaQueryWrapper<PilotBoats> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(status != null, PilotBoats::getCurrentStatus, status)
                .like(location != null, PilotBoats::getCurrentLocation, location);
        return pilotBoatsMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updatePilotBoatStatus(Long id, Object status) {
        PilotBoats boat = new PilotBoats();
        LambdaQueryWrapper<PilotBoats> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PilotBoats::getId, id);
        boat.setCurrentStatus(status);
        boat.setUpdatedAt(new Date());
        int update = pilotBoatsMapper.update(boat, queryWrapper);
        return update > 0;
    }

    @Override
    public IPage<PilotBoats> selectPage(PageQueryDTO query) {
        LambdaQueryWrapper<PilotBoats> queryWrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            queryWrapper.like(PilotBoats::getBoatName, query.getKeyword())
                    .or()
                    .like(PilotBoats::getVesselCallSign, query.getKeyword())
                    .or()
                    .like(PilotBoats::getImoNumber, query.getKeyword());
        }

        Page<PilotBoats> page = new Page<>(query.getCurrent(), query.getSize());
        return pilotBoatsMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<PilotBoats> getAvailableBoats() {
        LambdaQueryWrapper<PilotBoats> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PilotBoats::getCurrentStatus, "AVAILABLE");
        return pilotBoatsMapper.selectList(queryWrapper);
    }

    
    private void copyNonNullProperties(PilotBoats source, PilotBoats target) {
        if (source.getVesselCallSign() != null) target.setVesselCallSign(source.getVesselCallSign());
        if (source.getBoatName() != null) target.setBoatName(source.getBoatName());
        if (source.getImoNumber() != null) target.setImoNumber(source.getImoNumber());
        if (source.getOperationRange() != null) target.setOperationRange(source.getOperationRange());
        if (source.getEngineModel() != null) target.setEngineModel(source.getEngineModel());
        if (source.getEnginePower() != null) target.setEnginePower(source.getEnginePower());
        if (source.getRpmThrustForward() != null) target.setRpmThrustForward(source.getRpmThrustForward());
        if (source.getRpmThrustReverse() != null) target.setRpmThrustReverse(source.getRpmThrustReverse());
        if (source.getTowingForce() != null) target.setTowingForce(source.getTowingForce());
        if (source.getNavigationEquipment() != null) target.setNavigationEquipment(source.getNavigationEquipment());
        if (source.getBuildDate() != null) target.setBuildDate(source.getBuildDate());
        if (source.getShipyard() != null) target.setShipyard(source.getShipyard());
        if (source.getMaintenanceRecord() != null) target.setMaintenanceRecord(source.getMaintenanceRecord());
        if (source.getCurrentStatus() != null) target.setCurrentStatus(source.getCurrentStatus());
        if (source.getCurrentLocation() != null) target.setCurrentLocation(source.getCurrentLocation());
        if (source.getMaxSpeed() != null) target.setMaxSpeed(source.getMaxSpeed());
        if (source.getPassengerCapacity() != null) target.setPassengerCapacity(source.getPassengerCapacity());
        if (source.getLastMaintenanceDate() != null) target.setLastMaintenanceDate(source.getLastMaintenanceDate());
    }
}
