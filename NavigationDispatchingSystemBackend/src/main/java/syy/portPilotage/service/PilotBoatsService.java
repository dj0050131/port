package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.PilotBoats;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PilotBoatsService extends IService<PilotBoats> {

    PilotBoats savePilotBoatData(PilotBoats pilotBoats);

    PilotBoats updatePilotBoatData(PilotBoats pilotBoats);

    boolean isPilotBoatInUse(Long id);

    boolean deletePilotBoatData(Long id);

    List<PilotBoats> searchPilotBoats(String keyword);

    List<PilotBoats> selectPilotBoatList(String status, String location);

    boolean updatePilotBoatStatus(Long id, Object status);

    IPage<PilotBoats> selectPage(PageQueryDTO query);

    List<PilotBoats> getAvailableBoats();
}
