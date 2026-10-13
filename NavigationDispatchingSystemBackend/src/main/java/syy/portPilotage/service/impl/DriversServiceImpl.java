package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Drivers;
import syy.portPilotage.entity.PilotagePlans;
import syy.portPilotage.enums.PlanStatusEnum;
import syy.portPilotage.mapper.PilotagePlansMapper;
import syy.portPilotage.mapper.DriversMapper;
import syy.portPilotage.service.DriversService;

import java.util.Date;
import java.util.List;

@Service
public class DriversServiceImpl extends ServiceImpl<DriversMapper, Drivers>
        implements DriversService{

    @Autowired
    private DriversMapper driversMapper;
    @Autowired
    private PilotagePlansMapper pilotagePlansMapper;

    @Override
    public Drivers saveDriverData(Drivers driver) {
        driver.setCreatedAt(new Date());
        driver.setUpdatedAt(new Date());
        driversMapper.insert(driver);
        return driver;
    }

    @Override
    public Drivers updateDriverData(Drivers driver) {
        Drivers existingDriver = driversMapper.selectById(driver.getId());
        if (existingDriver == null) {
            throw new RuntimeException("驾驶员信息不存在");
        }
        copyNonNullProperties(driver, existingDriver);
        existingDriver.setUpdatedAt(new Date());
        driversMapper.updateById(existingDriver);
        return existingDriver;
    }

    @Override
    public boolean isDriverInUse(Long id) {
        // “是否在用”应基于计划占用，而不是驾驶员自身在岗状态（新建默认 ON_DUTY 会误判导致无法删除）
        Long cnt = pilotagePlansMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PilotagePlans>()
                        .eq(PilotagePlans::getAssignedDriverId, id)
                        .notIn(PilotagePlans::getPlanStatus,
                                PlanStatusEnum.CANCELLED.getCode(),
                                PlanStatusEnum.COMPLETED.getCode(),
                                PlanStatusEnum.CERTIFICATED.getCode(),
                                PlanStatusEnum.BILLED.getCode(),
                                PlanStatusEnum.PAID.getCode()
                        )
        );
        return cnt != null && cnt > 0;
    }

    @Override
    public boolean deleteDriverData(Long id) {
        return driversMapper.deleteById(id) > 0;
    }

    @Override
    public List<Drivers> searchDrivers(String keyword) {
        return driversMapper.selectList(new LambdaQueryWrapper<Drivers>()
                .like(Drivers::getName, keyword)
                .or()
                .like(Drivers::getDriverCode, keyword)
                .or()
                .like(Drivers::getLicenseNumber, keyword));
    }

    @Override
    public List<Drivers> selectDriverList(String level, Object status) {
        LambdaQueryWrapper<Drivers> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(level != null, Drivers::getLevel, level)
                .eq(status != null, Drivers::getOnDutyStatus, status);
        return driversMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updateDriverStatus(Long id, Object status) {
        Drivers driver = new Drivers();
        LambdaQueryWrapper<Drivers> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Drivers::getId, id);
        driver.setOnDutyStatus(status);
        driver.setUpdatedAt(new Date());
        int update = driversMapper.update(driver, queryWrapper);
        return update > 0;
    }

    @Override
    public IPage<Drivers> selectPage(PageQueryDTO query) {
        LambdaQueryWrapper<Drivers> queryWrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            queryWrapper.like(Drivers::getName, query.getKeyword())
                    .or()
                    .like(Drivers::getDriverCode, query.getKeyword())
                    .or()
                    .like(Drivers::getLicenseNumber, query.getKeyword());
        }

        Page<Drivers> page = new Page<>(query.getCurrent(), query.getSize());
        return driversMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<Drivers> getAvailableDrivers() {
        LambdaQueryWrapper<Drivers> queryWrapper = new LambdaQueryWrapper<>();
        // on_duty_status 枚举：ON_DUTY/RESTING/LEAVE/ASSIGNED/OPERATING
        // “可用”默认取 ON_DUTY（资源冲突/占用由计划创建时的冲突校验再兜底）
        queryWrapper.eq(Drivers::getOnDutyStatus, "ON_DUTY");
        return driversMapper.selectList(queryWrapper);
    }

    
    private void copyNonNullProperties(Drivers source, Drivers target) {
        if (source.getDriverCode() != null) target.setDriverCode(source.getDriverCode());
        if (source.getName() != null) target.setName(source.getName());
        if (source.getBirthDate() != null) target.setBirthDate(source.getBirthDate());
        if (source.getLevel() != null) target.setLevel(source.getLevel());
        if (source.getLicenseNumber() != null) target.setLicenseNumber(source.getLicenseNumber());
        if (source.getVehicleId() != null) target.setVehicleId(source.getVehicleId());
        if (source.getJoinDate() != null) target.setJoinDate(source.getJoinDate());
        if (source.getWorkExperience() != null) target.setWorkExperience(source.getWorkExperience());
        if (source.getPhone() != null) target.setPhone(source.getPhone());
        if (source.getMobile() != null) target.setMobile(source.getMobile());
        if (source.getEmail() != null) target.setEmail(source.getEmail());
        if (source.getShortNumber() != null) target.setShortNumber(source.getShortNumber());
        if (source.getOnDutyStatus() != null) target.setOnDutyStatus(source.getOnDutyStatus());
        if (source.getEmergencyContact() != null) target.setEmergencyContact(source.getEmergencyContact());
        if (source.getLicenseType() != null) target.setLicenseType(source.getLicenseType());
        if (source.getLicenseExpiry() != null) target.setLicenseExpiry(source.getLicenseExpiry());
        if (source.getUserId() != null) target.setUserId(source.getUserId());
    }
}
