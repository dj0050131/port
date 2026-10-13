package syy.portPilotage.vo.billing;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class InvoiceVO {
    
    private Long id;
    private String voucherCode;
    private String invoiceCode;
    private String invoiceNumber;
    private String taxpayerFullName;
    private String taxpayerId;
    private BigDecimal totalAmount;
    private Date taxInvoicingTime;
    private String paymentStatus;

    /** 销方信息（电子发票/下载展示使用） */
    private String sellerCompanyName;
    private String sellerTaxpayerId;
    private String sellerAddress;
    private String sellerPhone;
    private String sellerBank;
    private String sellerBankAccount;
}
