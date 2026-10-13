package syy.portPilotage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.billing.BillingApprovalRequest;
import syy.portPilotage.dto.billing.PaymentCallbackRequest;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.BillingVouchers;
import syy.portPilotage.entity.PilotageCertificates;
import syy.portPilotage.vo.billing.BillingResultVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BillingVouchersService extends IService<BillingVouchers> {

    BillingVouchers createBillingVoucherFromCertificate(PilotageCertificates certificate);

    BillingResultVO buildBillingResultVO(BillingVouchers voucher);

    List<BillingVouchers> listApprovedVouchersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<Map<String, Object>> getFeeBreakdownByType();

    List<Map<String, Object>> getFeeTrendByMonth();

    BigDecimal getCurrentPeriodTotal();

    BigDecimal getPreviousPeriodTotal();

    List<Map<String, Object>> getCategoryStats();

    boolean approve(BillingApprovalRequest request);

    BillingResultVO confirmPayment(Long id, BigDecimal receivedAmount, String receiptDate);

        BillingResultVO handlePaymentCallback(PaymentCallbackRequest request);

        BillingResultVO downloadElectronicInvoice(Long voucherId);

        BillingResultVO sendElectronicInvoice(Long voucherId);

        Map<String, Object> getElectronicInvoiceDetail(Long voucherId);

        byte[] downloadElectronicInvoicePdf(Long voucherId);

        boolean retryPaymentNotice(Long voucherId);

    PageResult<BillingResultVO> pageBillingVouchers(PageQueryDTO query);
}
