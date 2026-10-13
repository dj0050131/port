package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.entity.ChargeItems;
import syy.portPilotage.service.ChargeItemsService;
import syy.portPilotage.mapper.ChargeItemsMapper;
import org.springframework.stereotype.Service;

@Service
public class ChargeItemsServiceImpl extends ServiceImpl<ChargeItemsMapper, ChargeItems>
    implements ChargeItemsService {
    
    /**
     * 根据项目编码查找收费项目
     * 
     * @param itemCode 项目编码
     * @return 收费项目
     */
    public ChargeItems findByItemCode(String itemCode) {
        LambdaQueryWrapper<ChargeItems> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChargeItems::getItemCode, itemCode);
        return this.getOne(queryWrapper);
    }
}
