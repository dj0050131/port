package syy.portPilotage.dto.billing;

import lombok.Data;

import java.util.List;

// 审核请求的数据传输对象
@Data
public class BillingApprovalRequest {
    private List<Long> ids;
    private Boolean approved;
    private String rejectionReason;
}