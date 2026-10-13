package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import syy.portPilotage.dto.base.TugboatDTO;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Tugboats;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TugboatsService extends IService<Tugboats> {

    Tugboats saveTugboatData(Tugboats tugboats);

    Tugboats updateTugboatData(Tugboats tugboats);

    boolean isTugboatInUse(Long id);

    boolean deleteTugboatData(Long id);

    List<Tugboats> searchTugboats(String keyword);

    List<Tugboats> selectTugboatList(String status, String type);

    boolean updateTugboatStatus(Long id, Object status);

    IPage<Tugboats> selectPage(PageQueryDTO query);

    List<TugboatDTO> getAvailableTugboats();

    List<TugboatDTO> getAllTugboats();

    TugboatDTO getTugboatById(Long id);
}