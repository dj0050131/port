package syy.portPilotage.vo.billing;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class BillingResultVO {
    
    private Long voucherId;
    private String voucherCode;
    private Long certificateId;
    private String certificateCode;
    private Long planId;
    private String planCode;
    private Long vesselId;
    private String vesselNameZh;
    private String vesselNameEn;
    private BigDecimal grossTonnage;
    private BigDecimal netTonnage;
    private Long agentId;
    private String agentName;
    private String startLocationName;
    private String endLocationName;
    
    private BigDecimal baseAmount;
    private BigDecimal surchargeAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    
    private List<ChargeDetailVO> chargeDetails;
    
    private String billingMethod;
    private String auditStatus;
    private String paymentStatus;
    private Date dueDate;
    private Date createdAt;
    private Date pilotageStartTime;
    private Date pilotageEndTime;

    // 纸质发票字段（兼容旧数据；电子发票时代可不再使用）
    private String invoiceCode;
    private String invoiceNumber;

    // 电子发票字段
    private String electronicInvoiceStatus;
    private Date electronicGeneratedAt;
    private Long electronicGeneratedBy;
    private Date electronicSentAt;
    private Long electronicSentBy;
    private String electronicSentTo;
    private String checkCode;
    private String invoiceNotes;
    private Integer downloadCount;
    private Date lastDownloadAt;

    // 移交状态（计费凭证移交）
    private String transferStatus;
    
    private Long taxRecordId;
    private String taxRecordCode;
    private BigDecimal taxableAmount;
    private BigDecimal totalTaxAmount;
    private String taxStatus;
    private String transferTarget;
    private Date transferTime;
    
    @Data
    public static class ChargeDetailVO {
        private String chargeName;
        private String chargeCategory;
        private BigDecimal unitPrice;
        private BigDecimal quantity;
        private BigDecimal standardAmount;
        private BigDecimal amount;
        private String description;
        private String notes;
        private Date chargeTime;
    }
}