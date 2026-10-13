package syy.portPilotage.vo.screen;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

@Data
public class FeeStatisticsVO {
    private BigDecimal total;
    private String currency;
    private Long transactions;
    private BigDecimal growth;
    private List<FeeBreakdownItemVO> breakdown;
    private List<FeeTrendItemVO> trend;
    private List<FeeCategoryItemVO> category;
//        total: number
//        currency: string
//        transactions: number
//        growth: number
//        breakdown: FeeBreakdownItem[]
//    }
    @Data
    public static class FeeBreakdownItemVO {
        private String label;
        private BigDecimal value;
//        private String type; // 费用类型
    }
    
    @Data
    public static class FeeTrendItemVO {
        private String time;
        private BigDecimal value;
    }
    
    @Data
    public static class FeeCategoryItemVO {
        private String name;
        private BigDecimal value;
    }
    
    
//    @Data
//    }
//
//    @Data
//    }
}