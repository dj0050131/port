package syy.portPilotage.service;

import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.AgentInvoiceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AgentInvoiceInfoService extends IService<AgentInvoiceInfo> {

    PageResult<AgentInvoiceInfo> pageAgentInvoiceInfo(PageQueryDTO query);
}