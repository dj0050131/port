package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Wharves;

import java.util.List;

public interface WharvesService extends IService<Wharves> {

        Wharves saveWharfData(Wharves wharf);

        Wharves updateWharfData(Wharves wharf);

        boolean deleteWharfData(Long id);

        boolean updateWharfStatus(Long id, String status);

        IPage<Wharves> selectPage(PageQueryDTO query);

        List<Wharves> selectByPortAreaId(Long portAreaId);

        List<Wharves> searchWharves(String keyword);
}