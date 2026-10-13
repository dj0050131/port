package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Berths;

import java.util.List;

public interface BerthsService extends IService<Berths> {

        Berths saveBerthData(Berths berth);

        Berths updateBerthData(Berths berth);

        boolean deleteBerthData(Long id);

        boolean updateBerthStatus(Long id, String status);

        IPage<Berths> selectPage(PageQueryDTO query);

        List<Berths> selectByWharvesId(String wharvesId);

        List<Berths> searchBerths(String keyword);
}
