package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.dto.pilotage.PilotageRequestDTO;
import syy.portPilotage.entity.Ports;
import syy.portPilotage.enums.PortStatusEnum;
import syy.portPilotage.mapper.PortsMapper;
import syy.portPilotage.service.PortsService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PortsServiceImpl extends ServiceImpl<PortsMapper, Ports>
    implements PortsService{

    @Autowired
    private PortsMapper portsMapper;
    @Override
    public Ports savePortData(Ports port) {
        Ports ports = new Ports();
        ports.setPortCode(port.getPortCode());
        ports.setPortNameZh(port.getPortNameZh());
        ports.setPortNameEn(port.getPortNameEn());
        ports.setCountry(port.getCountry());
        ports.setRegion(port.getRegion());
        ports.setLongitude(port.getLongitude());
        ports.setLatitude(port.getLatitude());
        ports.setTimezone(port.getTimezone());
        ports.setStatus(port.getStatus());
        ports.setCreatedAt(new Date());
        ports.setUpdatedAt(new Date());
        portsMapper.insert(port);
        return port;
    }

    @Override
    public Ports updatePortData(Ports postPort) {
        Ports existingPort = portsMapper.selectById(postPort.getId());
        if (existingPort == null) {
            throw new RuntimeException("港口信息不存在");
        }
        copyNonNullProperties(postPort, existingPort);
        existingPort.setUpdatedAt(new Date());
        portsMapper.updateById(existingPort);
        return existingPort;
    }

    @Override
    public boolean isPortInUse(Long id) {
        Ports port = portsMapper.selectById(id);
        return port.getStatus().equals(PortStatusEnum.ACTIVE);
    }

    @Override
    public boolean deletePortData(Long id) {
        return portsMapper.deleteById(id) > 0;
    }

    @Override
    public List<Ports> searchPorts(String keyword) {
        return portsMapper.selectList(new LambdaQueryWrapper<Ports>()
                .like(Ports::getPortNameZh, keyword)
                .or()
                .like(Ports::getPortNameEn, keyword));
    }

    @Override
    public List<Ports> selectPortList(String region, String country) {
        LambdaQueryWrapper<Ports> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(region != null, Ports::getRegion, region)
                .eq(country != null, Ports::getCountry, country);
        return portsMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updatePortStatus(Long id, String status) {
        Ports port = new Ports();
        LambdaQueryWrapper<Ports> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Ports::getId, id);
        port.setStatus(status);
        int update = portsMapper.update(port, queryWrapper);
        return update > 0;
    }

    @Override
    public IPage<Ports> selectPage(PageQueryDTO query) {
        LambdaQueryWrapper<Ports> queryWrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = query.getKeyword().trim();
            queryWrapper.and(w -> w.like(Ports::getPortNameZh, keyword)
                    .or()
                    .like(Ports::getPortNameEn, keyword)
                    .or()
                    .like(Ports::getPortCode, keyword));
        }
        Page<Ports> page = new Page<>(query.getCurrent(), query.getSize());
        return portsMapper.selectPage(page, queryWrapper);
    }

    /**
     * 复制非null属性到目标对象
     * @param source 源对象
     * @param target 目标对象
     */
    private void copyNonNullProperties(Ports source, Ports target) {
        if (source.getPortCode() != null) target.setPortCode(source.getPortCode());
        if (source.getPortNameZh() != null) target.setPortNameZh(source.getPortNameZh());
        if (source.getPortNameEn() != null) target.setPortNameEn(source.getPortNameEn());
        if (source.getCountry() != null) target.setCountry(source.getCountry());
        if (source.getRegion() != null) target.setRegion(source.getRegion());
        if (source.getLongitude() != null) target.setLongitude(source.getLongitude());
        if (source.getLatitude() != null) target.setLatitude(source.getLatitude());
        if (source.getTimezone() != null) target.setTimezone(source.getTimezone());
        if (source.getStatus() != null) target.setStatus(source.getStatus());
    }
}
