package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Vehicles;
import syy.portPilotage.mapper.VehiclesMapper;
import syy.portPilotage.service.VehiclesService;

import java.util.Date;
import java.util.List;

@Service
public class VehiclesServiceImpl extends ServiceImpl<VehiclesMapper, Vehicles>
        implements VehiclesService{

    @Autowired
    private VehiclesMapper vehiclesMapper;

    @Override
    public Vehicles saveVehicleData(Vehicles vehicle) {
        vehicle.setCreatedAt(new Date());
        vehicle.setUpdatedAt(new Date());
        vehiclesMapper.insert(vehicle);
        return vehicle;
    }

    @Override
    public Vehicles updateVehicleData(Vehicles vehicle) {
        Vehicles existingVehicle = vehiclesMapper.selectById(vehicle.getId());
        if (existingVehicle == null) {
            throw new RuntimeException("车辆信息不存在");
        }
        copyNonNullProperties(vehicle, existingVehicle);
        existingVehicle.setUpdatedAt(new Date());
        vehiclesMapper.updateById(existingVehicle);
        return existingVehicle;
    }

    @Override
    public boolean isVehicleInUse(Long id) {
        Vehicles vehicle = vehiclesMapper.selectById(id);
        return "IN_USE".equals(vehicle.getVehicleStatus()) || "BUSY".equals(vehicle.getVehicleStatus());
    }

    @Override
    public boolean deleteVehicleData(Long id) {
        return vehiclesMapper.deleteById(id) > 0;
    }

    @Override
    public List<Vehicles> searchVehicles(String keyword) {
        return vehiclesMapper.selectList(new LambdaQueryWrapper<Vehicles>()
                .like(Vehicles::getLicensePlate, keyword)
                .or()
                .like(Vehicles::getVehicleCode, keyword)
                .or()
                .like(Vehicles::getVehicleModel, keyword));
    }

    @Override
    public List<Vehicles> selectVehicleList(String status, String location) {
        LambdaQueryWrapper<Vehicles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(status != null, Vehicles::getVehicleStatus, status)
                .like(location != null, Vehicles::getCurrentLocation, location);
        return vehiclesMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updateVehicleStatus(Long id, Object status) {
        Vehicles vehicle = new Vehicles();
        LambdaQueryWrapper<Vehicles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicles::getId, id);
        vehicle.setVehicleStatus(status);
        vehicle.setUpdatedAt(new Date());
        int update = vehiclesMapper.update(vehicle, queryWrapper);
        return update > 0;
    }

    @Override
    public IPage<Vehicles> selectPage(PageQueryDTO query) {
        LambdaQueryWrapper<Vehicles> queryWrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            queryWrapper.like(Vehicles::getLicensePlate, query.getKeyword())
                    .or()
                    .like(Vehicles::getVehicleCode, query.getKeyword());
//                    .or()
        }

        Page<Vehicles> page = new Page<>(query.getCurrent(), query.getSize());
        return vehiclesMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<Vehicles> getAvailableVehicles() {
        LambdaQueryWrapper<Vehicles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Vehicles::getVehicleStatus, "AVAILABLE");
        return vehiclesMapper.selectList(queryWrapper);
    }

    
    private void copyNonNullProperties(Vehicles source, Vehicles target) {
        if (source.getVehicleCode() != null) target.setVehicleCode(source.getVehicleCode());
        if (source.getLicensePlate() != null) target.setLicensePlate(source.getLicensePlate());
        if (source.getVehicleModel() != null) target.setVehicleModel(source.getVehicleModel());
        if (source.getPurchaseDate() != null) target.setPurchaseDate(source.getPurchaseDate());
        if (source.getPassengerCapacity() != null) target.setPassengerCapacity(source.getPassengerCapacity());
        if (source.getMainPerformance() != null) target.setMainPerformance(source.getMainPerformance());
        if (source.getTotalKilometers() != null) target.setTotalKilometers(source.getTotalKilometers());
        if (source.getVehicleStatus() != null) target.setVehicleStatus(source.getVehicleStatus());
        if (source.getCurrentLocation() != null) target.setCurrentLocation(source.getCurrentLocation());
        if (source.getColor() != null) target.setColor(source.getColor());
        if (source.getLastMaintenanceDate() != null) target.setLastMaintenanceDate(source.getLastMaintenanceDate());
        if (source.getMaintenanceInterval() != null) target.setMaintenanceInterval(source.getMaintenanceInterval());
    }
}
