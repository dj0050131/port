package syy.portPilotage.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.billing.FeeWriteOffDTO;
import syy.portPilotage.dto.certificate.VoucherCreateDTO;
import syy.portPilotage.dto.certificate.CertificateCreateDTO;
import syy.portPilotage.dto.payment.PaymentCreateDTO;
import syy.portPilotage.entity.*;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.service.*;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanStatusFlowServiceImpl {

    private final PilotagePlansService pilotagePlansService;
    private final PilotageCertificatesService pilotageCertificatesService;
    private final BillingVouchersService billingVouchersService;
    private final PaymentInfoService paymentInfoService;
    private final ChargeDetailsService chargeDetailsService;
    private final FeeWriteOffsService feeWriteOffsService;

        @Transactional(rollbackFor = Exception.class)
    public PilotageCertificates createCertificateAndUpdatePlan(Long planId, CertificateCreateDTO dto) {
        log.info("创建签证单并更新计划状态: planId={}", planId);

        PilotagePlans plan = pilotagePlansService.getById(planId);
        if (plan == null) {
            throw new BusinessException("引航计划不存在");
        }

        if (!"COMPLETED".equals(plan.getPlanStatus())) {
            throw new BusinessException("只有已完成的计划才能创建签证单");
        }

        PilotageCertificates certificate = new PilotageCertificates();
        BeanUtils.copyProperties(dto, certificate);
        certificate.setPlanId(planId);
        certificate.setVesselId(plan.getVesselId());
        certificate.setVoyageId(plan.getVoyageId());
        certificate.setSubmitStatus("SUBMITTED");
        certificate.setBillingStatus("UNBILLED");
        certificate.setCreatedAt(new Date());

        pilotageCertificatesService.save(certificate);

        plan.setPlanStatus("CERTIFICATED");
        plan.setUpdatedAt(new Date());
        pilotagePlansService.updateById(plan);

        log.info("签证单创建成功，计划状态更新为CERTIFICATED: planId={}, certificateId={}",
                planId, certificate.getId());

        return certificate;
    }

        @Transactional(rollbackFor = Exception.class)
    public BillingVouchers createBillingVoucherAndUpdatePlan(Long certificateId, VoucherCreateDTO dto) {
        log.info("创建计费凭证并更新计划状态: certificateId={}", certificateId);

        PilotageCertificates certificate = pilotageCertificatesService.getById(certificateId);
        if (certificate == null) {
            throw new BusinessException("签证单不存在");
        }

        if (!"SUBMITTED".equals(certificate.getSubmitStatus())) {
            throw new BusinessException("只有已提交的签证单才能创建计费凭证");
        }

        PilotagePlans plan = pilotagePlansService.getById(certificate.getPlanId());
        if (plan == null) {
            throw new BusinessException("引航计划不存在");
        }

        if (!"CERTIFICATED".equals(plan.getPlanStatus())) {
            throw new BusinessException("只有已签证的计划才能创建计费凭证");
        }

        BillingVouchers voucher = new BillingVouchers();
        BeanUtils.copyProperties(dto, voucher);
        voucher.setCertificateId(certificateId);
        voucher.setVoyageId(certificate.getVoyageId());
        voucher.setVesselId(certificate.getVesselId());
        voucher.setAuditStatus("PENDING");
        voucher.setPaymentStatus("UNPAID");
        voucher.setDueDate(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)); // 30天后
        voucher.setCreatedAt(new Date());

        billingVouchersService.save(voucher);

        certificate.setBillingStatus("BILLED");
        pilotageCertificatesService.updateById(certificate);

        plan.setPlanStatus("BILLED");
        plan.setUpdatedAt(new Date());
        pilotagePlansService.updateById(plan);

        log.info("计费凭证创建成功，计划状态更新为BILLED: planId={}, voucherId={}",
                plan.getId(), voucher.getId());

        return voucher;
    }

        @Transactional(rollbackFor = Exception.class)
    public PaymentInfo recordPaymentAndUpdatePlan(Long voucherId, PaymentCreateDTO dto) {
        log.info("记录付款并更新计划状态: voucherId={}", voucherId);

        BillingVouchers voucher = billingVouchersService.getById(voucherId);
        if (voucher == null) {
            throw new BusinessException("计费凭证不存在");
        }

        PilotageCertificates certificate = pilotageCertificatesService.getById(voucher.getCertificateId());
        if (certificate == null) {
            throw new BusinessException("签证单不存在");
        }

        PilotagePlans plan = pilotagePlansService.getById(certificate.getPlanId());
        if (plan == null) {
            throw new BusinessException("引航计划不存在");
        }

        if (!"BILLED".equals(plan.getPlanStatus())) {
            throw new BusinessException("只有已计费的计划才能记录付款");
        }

        if (dto.getPaymentAmount().compareTo(voucher.getTotalAmount()) < 0) {
            log.warn("部分付款: voucherId={}, 应付款={}, 实付款={}",
                    voucherId, voucher.getTotalAmount(), dto.getPaymentAmount());
            voucher.setPaymentStatus("PARTIAL");
        } else {
            voucher.setPaymentStatus("FULLY_PAID");
        }

        voucher.setActualReceivedAmount(dto.getPaymentAmount());
        voucher.setReceiptDate(dto.getPaymentDate());
        billingVouchersService.updateById(voucher);

        PaymentInfo payment = new PaymentInfo();
        BeanUtils.copyProperties(dto, payment);
        payment.setBillingVoucherId(voucherId);
        payment.setPaymentStatus("PAID");
        payment.setCreatedAt(new Date());

        paymentInfoService.save(payment);

        if ("FULLY_PAID".equals(voucher.getPaymentStatus())) {
            plan.setPlanStatus("PAID");
            plan.setUpdatedAt(new Date());
            pilotagePlansService.updateById(plan);

            log.info("全额付款完成，计划状态更新为PAID: planId={}, voucherId={}",
                    plan.getId(), voucherId);
        } else {
            log.info("部分付款完成，计划状态保持BILLED: planId={}, voucherId={}",
                    plan.getId(), voucherId);
        }

        return payment;
    }

        @Transactional(rollbackFor = Exception.class)
    public FeeWriteOffs createWriteOffAndUpdatePlan(Long voucherId, FeeWriteOffDTO dto) {
        log.info("创建费用核销单并更新计划状态: voucherId={}", voucherId);

        BillingVouchers voucher = billingVouchersService.getById(voucherId);
        if (voucher == null) {
            throw new BusinessException("计费凭证不存在");
        }

        if (!"FULLY_PAID".equals(voucher.getPaymentStatus())) {
            throw new BusinessException("只有已全额付款的凭证才能核销");
        }

        FeeWriteOffs writeOff = new FeeWriteOffs();
        BeanUtils.copyProperties(dto, writeOff);
        writeOff.setAgentId(voucher.getAgentId());
        writeOff.setWriteOffAmount(voucher.getTotalAmount());
        writeOff.setStatusCode("COMPLETED");
        writeOff.setStatusName("已核销");
        writeOff.setCreatedAt(new Date());

        feeWriteOffsService.save(writeOff);

        PilotageCertificates certificate = pilotageCertificatesService.getById(voucher.getCertificateId());
        PilotagePlans plan = pilotagePlansService.getById(certificate.getPlanId());

        // 根据业务需求决定是否需要额外状态

        log.info("费用核销完成: planId={}, voucherId={}, writeOffId={}",
                plan.getId(), voucherId, writeOff.getId());

        return writeOff;
    }
}