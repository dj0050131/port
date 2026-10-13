package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Ports;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PortsService extends IService<Ports> {

    Ports savePortData(Ports ports);

    Ports updatePortData(Ports postPort);

    boolean isPortInUse(Long id);

    boolean deletePortData(Long id);

    List<Ports> searchPorts(String keyword);

    List<Ports> selectPortList(String region, String country);

    boolean updatePortStatus(Long id, String status);

    IPage<Ports> selectPage(PageQueryDTO query);
}