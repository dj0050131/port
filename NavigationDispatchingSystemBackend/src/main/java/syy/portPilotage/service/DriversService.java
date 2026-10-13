package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Drivers;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DriversService extends IService<Drivers> {

    Drivers saveDriverData(Drivers drivers);

    Drivers updateDriverData(Drivers drivers);

    boolean isDriverInUse(Long id);

    boolean deleteDriverData(Long id);

    List<Drivers> searchDrivers(String keyword);

    List<Drivers> selectDriverList(String level, Object status);

    boolean updateDriverStatus(Long id, Object status);

    IPage<Drivers> selectPage(PageQueryDTO query);

    List<Drivers> getAvailableDrivers();
}
