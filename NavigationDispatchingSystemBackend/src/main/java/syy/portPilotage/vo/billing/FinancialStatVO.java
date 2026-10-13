package syy.portPilotage.vo.billing;

import lombok.Data;
import syy.portPilotage.vo.common.TrendPointVO;

import java.math.BigDecimal;
import java.util.List;

@Data
public class FinancialStatVO {
    
    private BigDecimal totalRevenue;
    private BigDecimal monthRevenue;
    private BigDecimal todayRevenue;
    private Long totalInvoices;
    private Long unpaidInvoices;
    private BigDecimal unpaidAmount;
    private BigDecimal overdueAmount;
    private List<TrendPointVO> trends;
}
