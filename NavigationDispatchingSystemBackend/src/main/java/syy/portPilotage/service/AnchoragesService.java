package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Anchorages;

import java.util.List;

public interface AnchoragesService extends IService<Anchorages> {

        Anchorages saveAnchorageData(Anchorages anchorage);

        Anchorages updateAnchorageData(Anchorages anchorage);

        boolean deleteAnchorageData(Long id);

        boolean updateAnchorageStatus(Long id, String status);

        IPage<Anchorages> selectPage(PageQueryDTO query);

        List<Anchorages> selectByPortAreaId(Long portAreaId);

        List<Anchorages> searchAnchorages(String keyword);
}