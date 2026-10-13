package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Vehicles;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface VehiclesService extends IService<Vehicles> {

    Vehicles saveVehicleData(Vehicles vehicles);

    Vehicles updateVehicleData(Vehicles vehicles);

    boolean isVehicleInUse(Long id);

    boolean deleteVehicleData(Long id);

    List<Vehicles> searchVehicles(String keyword);

    List<Vehicles> selectVehicleList(String status, String location);

    boolean updateVehicleStatus(Long id, Object status);

    IPage<Vehicles> selectPage(PageQueryDTO query);

    List<Vehicles> getAvailableVehicles();
}
