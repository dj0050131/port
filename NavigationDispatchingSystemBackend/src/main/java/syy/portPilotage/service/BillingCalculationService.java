package syy.portPilotage.service;

import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.certificate.CertificateDetailDTO;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.BillingVouchers;
import syy.portPilotage.entity.PilotageCertificates;
import syy.portPilotage.vo.billing.BillingResultVO;

public interface BillingCalculationService {
    
    /**
     * 根据签证单生成计费凭证
     *
     * @param certificate 签证单
     * @return 生成的计费凭证
     */
    BillingVouchers generateBillingVoucherFromCertificate(PilotageCertificates certificate);

    BillingResultVO startBilling(Long certificateId);

    BillingResultVO rebillRejectedVoucher(Long id);

    BillingResultVO getAuditDetail(Long id);

    PageResult<CertificateDetailDTO> getRejectedBillingVouchers(PageQueryDTO query);
}