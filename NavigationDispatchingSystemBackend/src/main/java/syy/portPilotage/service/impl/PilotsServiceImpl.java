package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Pilots;
import syy.portPilotage.entity.PilotagePlans;
import syy.portPilotage.enums.PlanStatusEnum;
import syy.portPilotage.mapper.PilotagePlansMapper;
import syy.portPilotage.mapper.PilotsMapper;
import syy.portPilotage.service.PilotsService;
import syy.portPilotage.redis.service.CacheService;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PilotsServiceImpl extends ServiceImpl<PilotsMapper, Pilots>
        implements PilotsService{

    @Autowired
    private PilotsMapper pilotsMapper;
    @Autowired
    private PilotagePlansMapper pilotagePlansMapper;
    private final CacheService cacheService;

    public Pilots getById(Long id) {
        if (id == null) {
            return null;
        }
        Pilots cached = cacheService.getPilot(id, Pilots.class);
        if (cached != null) {
            return cached;
        }
        Pilots pilot = super.getById(id);
        if (pilot != null) {
            cacheService.setPilot(id, pilot);
        }
        return pilot;
    }

    @Override
    public Pilots savePilotData(Pilots pilot) {
        pilot.setCreatedAt(new Date());
        pilot.setUpdatedAt(new Date());
        pilotsMapper.insert(pilot);
        if (pilot.getId() != null) {
            cacheService.setPilot(pilot.getId(), pilot);
        }
        return pilot;
    }

    @Override
    public Pilots updatePilotData(Pilots pilot) {
        Pilots existingPilot = pilotsMapper.selectById(pilot.getId());
        if (existingPilot == null) {
            throw new RuntimeException("引航员信息不存在");
        }
        copyNonNullProperties(pilot, existingPilot);
        existingPilot.setUpdatedAt(new Date());
        pilotsMapper.updateById(existingPilot);
        if (existingPilot.getId() != null) {
            cacheService.setPilot(existingPilot.getId(), existingPilot);
        }
        return existingPilot;
    }

    @Override
    public boolean isPilotInUse(Long id) {
        // “是否在用”应基于计划占用，而不是引航员自身在岗状态（新建默认 ON_DUTY 会误判导致无法删除）
        Long cnt = pilotagePlansMapper.selectCount(
                new LambdaQueryWrapper<PilotagePlans>()
                        .eq(PilotagePlans::getAssignedPilotId, id)
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
    public boolean deletePilotData(Long id) {
        boolean result = pilotsMapper.deleteById(id) > 0;
        if (result) {
            cacheService.deletePilot(id);
        }
        return result;
    }

    @Override
    public List<Pilots> searchPilots(String keyword) {
        return pilotsMapper.selectList(new LambdaQueryWrapper<Pilots>()
                .like(Pilots::getPilotName, keyword)
                .or()
                .like(Pilots::getPilotCode, keyword)
                .or()
                .like(Pilots::getCertificateNo, keyword));
    }

    @Override
    public List<Pilots> selectPilotList(String level, Object status) {
        LambdaQueryWrapper<Pilots> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(level != null, Pilots::getPilotLevel, level)
                .eq(status != null, Pilots::getOnDutyStatus, status);
        return pilotsMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updatePilotStatus(Long id, Object status) {
        Pilots pilot = new Pilots();
        LambdaQueryWrapper<Pilots> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Pilots::getId, id);
        pilot.setOnDutyStatus(status);
        pilot.setUpdatedAt(new Date());
        int update = pilotsMapper.update(pilot, queryWrapper);
        return update > 0;
    }

    @Override
    public IPage<Pilots> selectPage(PageQueryDTO query) {
        LambdaQueryWrapper<Pilots> queryWrapper = new LambdaQueryWrapper<>();

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            queryWrapper.like(Pilots::getPilotName, query.getKeyword())
                    .or()
                    .like(Pilots::getPilotCode, query.getKeyword())
                    .or()
                    .like(Pilots::getCertificateNo, query.getKeyword());
        }

        Page<Pilots> page = new Page<>(query.getCurrent(), query.getSize());
        return pilotsMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<Pilots> getAvailablePilots() {
        LambdaQueryWrapper<Pilots> queryWrapper = new LambdaQueryWrapper<>();
        // on_duty_status 枚举：ON_DUTY/RESTING/LEAVE/ASSIGNED/OPERATING
        // “可用”默认取 ON_DUTY（资源冲突/占用由计划创建时的冲突校验再兜底）
        queryWrapper.eq(Pilots::getOnDutyStatus, "ON_DUTY");
        return pilotsMapper.selectList(queryWrapper);
    }

    @Override
    public Pilots getPilotByUserId(Long userId) {
        LambdaQueryWrapper<Pilots> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Pilots::getUserId, userId);
        return pilotsMapper.selectOne(queryWrapper);
    }

    
    private void copyNonNullProperties(Pilots source, Pilots target) {
        if (source.getPilotCode() != null) target.setPilotCode(source.getPilotCode());
        if (source.getPilotName() != null) target.setPilotName(source.getPilotName());
        if (source.getPilotLevel() != null) target.setPilotLevel(source.getPilotLevel());
        if (source.getCareerStartDate() != null) target.setCareerStartDate(source.getCareerStartDate());
        if (source.getOtherExperience() != null) target.setOtherExperience(source.getOtherExperience());
        if (source.getHealthStatus() != null) target.setHealthStatus(source.getHealthStatus());
        if (source.getAccidentRecord() != null) target.setAccidentRecord(source.getAccidentRecord());
        if (source.getTotalCargoTonnage() != null) target.setTotalCargoTonnage(source.getTotalCargoTonnage());
        if (source.getTotalOperations() != null) target.setTotalOperations(source.getTotalOperations());
        if (source.getPhone() != null) target.setPhone(source.getPhone());
        if (source.getSimCard() != null) target.setSimCard(source.getSimCard());
        if (source.getDataSimCard() != null) target.setDataSimCard(source.getDataSimCard());
        if (source.getWirelessIp() != null) target.setWirelessIp(source.getWirelessIp());
        if (source.getShortNumber() != null) target.setShortNumber(source.getShortNumber());
        if (source.getEmail() != null) target.setEmail(source.getEmail());
        if (source.getOnDutyStatus() != null) target.setOnDutyStatus(source.getOnDutyStatus());
        if (source.getEmergencyContact() != null) target.setEmergencyContact(source.getEmergencyContact());
        if (source.getEmergencyPhone() != null) target.setEmergencyPhone(source.getEmergencyPhone());
        if (source.getCertificateNo() != null) target.setCertificateNo(source.getCertificateNo());
        if (source.getCertificateExpiry() != null) target.setCertificateExpiry(source.getCertificateExpiry());
        if (source.getUserId() != null) target.setUserId(source.getUserId());
    }
}
