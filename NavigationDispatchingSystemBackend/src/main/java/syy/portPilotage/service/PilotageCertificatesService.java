// PilotageCertificatesService 接口扩展
package syy.portPilotage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.certificate.*;
import syy.portPilotage.dto.statistics.*;
import syy.portPilotage.entity.PilotageCertificates;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PilotageCertificatesService extends IService<PilotageCertificates> {

        CertificateResultDTO createCertificateFromPlan(CertificateCreateRequest request);

        Map<String, Object> getCertificateList(CertificateQueryDTO query);

        CertificateDetailDTO getCertificateDetail(Long certificateId);

        boolean updateCertificate(Long certificateId, CertificateUpdateDTO dto);

        boolean submitCertificate(Long certificateId, CertificateSubmitDTO dto);

        boolean confirmCertificate(Long certificateId);

    
    boolean pilotSignCertificate(Long certificateId);

        CertificateDetailDTO getCertificateByPlanId(Long planId);

        VoucherResultDTO createBillingVoucher(VoucherCreateRequest request);

    @Transactional(rollbackFor = Exception.class)
    boolean checkAndConfirmDispatch(Long planId);

        CertificateResultDTO createCertificateFromPlanAuto(Long planId);

    boolean deleteCertificate(Long certificateId);

    void exportCertifivates(String ids, @Valid CertificateQueryDTO query, HttpServletResponse response) throws IOException;

    IPage<CertificateDetailDTO> selectCertificatePage(com.baomidou.mybatisplus.extension.plugins.pagination.Page<syy.portPilotage.dto.certificate.CertificateDetailDTO> page, java.util.Map<java.lang.String,java.lang.Object> params);
}
