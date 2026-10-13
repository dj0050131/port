package syy.portPilotage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import syy.portPilotage.entity.ChargeItems;

public interface ChargeItemsService extends IService<ChargeItems> {
    
    /**
     * 根据项目编码查找收费项目
     * 
     * @param itemCode 项目编码
     * @return 收费项目
     */
    ChargeItems findByItemCode(String itemCode);
}
