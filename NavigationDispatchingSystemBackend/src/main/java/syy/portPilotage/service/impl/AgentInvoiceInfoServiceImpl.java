package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.AgentInvoiceInfo;
import syy.portPilotage.mapper.AgentInvoiceInfoMapper;
import syy.portPilotage.service.AgentInvoiceInfoService;

@Service
@RequiredArgsConstructor
public class AgentInvoiceInfoServiceImpl extends ServiceImpl<AgentInvoiceInfoMapper, AgentInvoiceInfo>
    implements AgentInvoiceInfoService {

    @Override
    public PageResult<AgentInvoiceInfo> pageAgentInvoiceInfo(PageQueryDTO query) {

        Page<AgentInvoiceInfo> page = new Page<>(query.getCurrent(), query.getSize());

        LambdaQueryWrapper<AgentInvoiceInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AgentInvoiceInfo::getCreatedAt);

        IPage<AgentInvoiceInfo> result = this.page(page, wrapper);

        return PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );
    }
}