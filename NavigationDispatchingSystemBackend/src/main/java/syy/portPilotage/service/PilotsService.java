package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Pilots;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PilotsService extends IService<Pilots> {

    Pilots savePilotData(Pilots pilots);

    Pilots updatePilotData(Pilots pilots);

    boolean isPilotInUse(Long id);

    boolean deletePilotData(Long id);

    List<Pilots> searchPilots(String keyword);

    List<Pilots> selectPilotList(String level, Object status);

    boolean updatePilotStatus(Long id, Object status);

    IPage<Pilots> selectPage(PageQueryDTO query);

    List<Pilots> getAvailablePilots();

    Pilots getPilotByUserId(Long userId);
}
