package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.PortAreas;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PortAreasService extends IService<PortAreas> {
        PortAreas savePortAreaData(PortAreas portArea);

        PortAreas updatePortAreaData(PortAreas portArea);

        boolean deletePortAreaData(Long id);

        boolean updatePortAreaStatus(Long id, String status);

        IPage<PortAreas> selectPage(PageQueryDTO query);

        List<PortAreas> selectByPortId(Long portId);

        List<PortAreas> searchPortAreas(String keyword);
}