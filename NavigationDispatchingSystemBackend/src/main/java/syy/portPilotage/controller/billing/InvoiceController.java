package syy.portPilotage.controller.billing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.billing.InvoicePrintDTO;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.BillingVouchers;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.BillingVouchersService;
import syy.portPilotage.service.InvoiceOperationLogsService;
import syy.portPilotage.service.TaxRecordsService;
import syy.portPilotage.mapper.SystemParametersMapper;
import syy.portPilotage.mapper.PilotageCertificatesMapper;
import syy.portPilotage.util.IdGenerator;
import syy.portPilotage.vo.billing.InvoiceVO;
import syy.portPilotage.vo.billing.BillingResultVO;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import syy.portPilotage.entity.InvoiceOperationLogs;
import syy.portPilotage.entity.TaxRecords;

@Slf4j
@RestController
@RequestMapping("/api/billing/invoices")
@RequiredArgsConstructor
@Tag(name = "票据管理", description = "票据相关接口")
public class InvoiceController {
    
    @Autowired
    BillingVouchersService billingVouchersService;

    @Autowired
    InvoiceOperationLogsService invoiceOperationLogsService;

    @Autowired
    private SystemParametersMapper systemParametersMapper;

    @Autowired
    private PilotageCertificatesMapper pilotageCertificatesMapper;

    @Autowired
    private TaxRecordsService taxRecordsService;
    
    @PostMapping("/issue")
    @Operation(summary = "开具发票")
    @PreAuthorize("hasRole('FINANCE')")
    public Result<InvoiceVO> issue(@Valid @RequestBody InvoicePrintDTO dto) {
        log.info("开具发票: voucherId={}", dto.getVoucherId());
        
        BillingVouchers voucher = billingVouchersService.getById(dto.getVoucherId());
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND);
        }
        
        if (!"APPROVED".equals(voucher.getAuditStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "计费凭证未审核通过");
        }
        
        voucher.setInvoiceCode(IdGenerator.generateInvoiceCode());
        voucher.setInvoiceNumber(IdGenerator.nextIdStr());
        voucher.setTaxpayerFullName(dto.getTaxpayerFullName());
        voucher.setTaxpayerId(dto.getTaxpayerId());
        voucher.setBuyerBank(dto.getBuyerBank());
        voucher.setTaxInvoicingFlag("Y");
        voucher.setTaxInvoicingTime(new Date());
        voucher.setUpdatedAt(new Date());
        
        billingVouchersService.updateById(voucher);
        
        InvoiceVO invoiceVO = new InvoiceVO();
        invoiceVO.setId(voucher.getId());
        invoiceVO.setVoucherCode(voucher.getVoucherCode());
        invoiceVO.setInvoiceCode(voucher.getInvoiceCode());
        invoiceVO.setInvoiceNumber(voucher.getInvoiceNumber());
        invoiceVO.setTotalAmount(voucher.getTotalAmount());

        // 补充销方信息（来自 system_parameters: INVOICE_SELLER）
        Map<String, String> seller = getSellerInfo();
        invoiceVO.setSellerCompanyName(seller.get("company_name"));
        invoiceVO.setSellerTaxpayerId(seller.get("taxpayer_id"));
        invoiceVO.setSellerAddress(seller.get("address"));
        invoiceVO.setSellerPhone(seller.get("phone"));
        invoiceVO.setSellerBank(seller.get("bank"));
        invoiceVO.setSellerBankAccount(seller.get("bank_account"));
        
        log.info("发票开具成功: id={}", voucher.getId());
        return Result.success(invoiceVO);
    }
    
        @GetMapping("/page")
    @Operation(summary = "分页查询发票列表")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<PageResult<BillingResultVO>> page(PageQueryDTO query,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String status) {
        Page<BillingVouchers> page = new Page<>(query.getCurrent(), query.getSize());

        LambdaQueryWrapper<BillingVouchers> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(BillingVouchers::getElectronicInvoiceStatus, status.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(BillingVouchers::getInvoiceNumber, kw)
                    .or()
                    .like(BillingVouchers::getVoucherCode, kw)
                    .or()
                    .like(BillingVouchers::getInvoiceCode, kw));
            if (kw.length() > 1 && (kw.startsWith("V") || kw.startsWith("v"))) {
                try {
                    long id = Long.parseLong(kw.substring(1));
                    wrapper.or().eq(BillingVouchers::getId, id);
                } catch (NumberFormatException ignore) {
                    // ignore non-numeric id
                }
            }
        } else if (status == null || status.trim().isEmpty()) {
            wrapper.isNotNull(BillingVouchers::getElectronicInvoiceStatus);
        }
        wrapper.orderByDesc(BillingVouchers::getElectronicGeneratedAt);

        IPage<BillingVouchers> result = billingVouchersService.page(page, wrapper);
        List<BillingResultVO> voList = result.getRecords().stream()
                .map(billingVouchersService::buildBillingResultVO)
                .toList();

        return Result.success(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList));
    }

        @PostMapping("/red")
    @Operation(summary = "发票冲红(批量)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> red(@RequestParam List<Long> ids, @RequestParam String reason) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要冲红的发票");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "冲红原因不能为空");
        }

        List<Long> invalidIds = new ArrayList<>();
        for (Long id : ids) {
            BillingVouchers v = billingVouchersService.getById(id);
            if (v == null) continue;
            String st = v.getElectronicInvoiceStatus() == null ? null : v.getElectronicInvoiceStatus().toString();
            if ("RED".equals(st) || "VOIDED".equals(st)) {
                invalidIds.add(id);
            }
        }
        if (!invalidIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "以下发票已冲红/已作废，不能再次冲红: " + invalidIds);
        }

        // 业务约束：已移交（计费凭证/税额）的发票，不允许在本系统内冲红，避免与外部系统数据不一致
        List<Long> transferredIds = new ArrayList<>();
        for (Long id : ids) {
            BillingVouchers v = billingVouchersService.getById(id);
            if (v == null) continue;
            if (isVoucherTransferredOrProcessing(v) || hasTaxTransferInProgressOrDone(v.getId())) {
                transferredIds.add(id);
            }
        }
        if (!transferredIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "以下发票已进入移交流程/已移交，禁止冲红: " + transferredIds);
        }

        for (Long id : ids) {
            BillingVouchers v = billingVouchersService.getById(id);
            if (v == null) continue;
            String st = v.getElectronicInvoiceStatus() == null ? null : v.getElectronicInvoiceStatus().toString();
            v.setElectronicInvoiceStatus("RED");
            v.setInvoiceNotes(reason);
            v.setUpdatedAt(new Date());
            billingVouchersService.updateById(v);

            // 冲红后续流程：标记签证单进入“重计费”队列
            // 说明：本系统为模拟电子发票，冲红后通常需要重新计费生成新凭证，再走审核/付款/开票流程
            try {
                if (v.getCertificateId() != null) {
                    syy.portPilotage.entity.PilotageCertificates cert = pilotageCertificatesMapper.selectById(v.getCertificateId());
                    if (cert != null) {
                        cert.setBillingStatus("REBILLING");
                        cert.setUpdatedAt(new Date());
                        pilotageCertificatesMapper.updateById(cert);
                    }
                }
            } catch (Exception e) {
                log.warn("冲红后标记签证单重计费失败: voucherId={}, certificateId={}", v.getId(), v.getCertificateId(), e);
            }

            InvoiceOperationLogs logEntity = new InvoiceOperationLogs();
            logEntity.setVoucherId(v.getId());
            logEntity.setOperationType("RED");
            logEntity.setOperationBy(1L);
            logEntity.setOperationTime(new Date());
            logEntity.setInvoiceNo(v.getInvoiceNumber());
            logEntity.setOperationDetails(JSON.toJSONString(Map.of(
                    "action", "RED",
                    "reason", reason
            )));
            logEntity.setResult("SUCCESS");
            logEntity.setCreatedAt(new Date());
            invoiceOperationLogsService.insertSelective(logEntity);
        }
        return Result.success();
    }

        @PostMapping("/void")
    @Operation(summary = "发票作废(批量)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> voidInvoice(@RequestParam List<Long> ids, @RequestParam String reason) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要作废的发票");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "作废原因不能为空");
        }

        List<Long> invalidIds = new ArrayList<>();
        for (Long id : ids) {
            BillingVouchers v = billingVouchersService.getById(id);
            if (v == null) continue;
            String st = v.getElectronicInvoiceStatus() == null ? null : v.getElectronicInvoiceStatus().toString();
            if ("VOIDED".equals(st) || "RED".equals(st)) {
                invalidIds.add(id);
            }
        }
        if (!invalidIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "以下发票已冲红/已作废，不能再次作废: " + invalidIds);
        }

        // 业务约束：已移交（计费凭证/税额）的发票，不允许在本系统内作废，避免与外部系统数据不一致
        List<Long> transferredIds = new ArrayList<>();
        for (Long id : ids) {
            BillingVouchers v = billingVouchersService.getById(id);
            if (v == null) continue;
            if (isVoucherTransferredOrProcessing(v) || hasTaxTransferInProgressOrDone(v.getId())) {
                transferredIds.add(id);
            }
        }
        if (!transferredIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "以下发票已进入移交流程/已移交，禁止作废: " + transferredIds);
        }

        for (Long id : ids) {
            BillingVouchers v = billingVouchersService.getById(id);
            if (v == null) continue;
            v.setElectronicInvoiceStatus("VOIDED");
            v.setCancellationNotes(reason);
            v.setUpdatedAt(new Date());
            billingVouchersService.updateById(v);

            InvoiceOperationLogs logEntity = new InvoiceOperationLogs();
            logEntity.setVoucherId(v.getId());
            logEntity.setOperationType("VOID");
            logEntity.setOperationBy(1L);
            logEntity.setOperationTime(new Date());
            logEntity.setInvoiceNo(v.getInvoiceNumber());
            logEntity.setOperationDetails(JSON.toJSONString(Map.of(
                    "action", "VOID",
                    "reason", reason
            )));
            logEntity.setResult("SUCCESS");
            logEntity.setCreatedAt(new Date());
            invoiceOperationLogsService.insertSelective(logEntity);
        }
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "作废发票(兼容旧接口)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam String reason) {
        return voidInvoice(List.of(id), reason);
    }
    
    @GetMapping("/print/{id}")
    @Operation(summary = "打印发票")
    public Result<InvoiceVO> print(@PathVariable Long id) {
        log.info("打印发票: id={}", id);
        
        BillingVouchers voucher = billingVouchersService.getById(id);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND);
        }
        
        InvoiceVO invoiceVO = new InvoiceVO();
        invoiceVO.setId(voucher.getId());
        invoiceVO.setVoucherCode(voucher.getVoucherCode());
        invoiceVO.setInvoiceCode(voucher.getInvoiceCode());
        invoiceVO.setInvoiceNumber(voucher.getInvoiceNumber());
        invoiceVO.setTaxpayerFullName(voucher.getTaxpayerFullName());
        invoiceVO.setTaxpayerId(voucher.getTaxpayerId());
        invoiceVO.setTotalAmount(voucher.getTotalAmount());
        invoiceVO.setTaxInvoicingTime(voucher.getTaxInvoicingTime());

        Map<String, String> seller = getSellerInfo();
        invoiceVO.setSellerCompanyName(seller.get("company_name"));
        invoiceVO.setSellerTaxpayerId(seller.get("taxpayer_id"));
        invoiceVO.setSellerAddress(seller.get("address"));
        invoiceVO.setSellerPhone(seller.get("phone"));
        invoiceVO.setSellerBank(seller.get("bank"));
        invoiceVO.setSellerBankAccount(seller.get("bank_account"));
        
        return Result.success(invoiceVO);
    }

    private Map<String, String> getSellerInfo() {
        Map<String, String> m = new java.util.LinkedHashMap<>();
        m.put("company_name", "");
        m.put("taxpayer_id", "");
        m.put("address", "");
        m.put("phone", "");
        m.put("bank", "");
        m.put("bank_account", "");

        try {
            List<syy.portPilotage.entity.SystemParameters> list = systemParametersMapper.selectByGroup("INVOICE_SELLER");
            if (list != null) {
                for (syy.portPilotage.entity.SystemParameters p : list) {
                    if (p == null || p.getParamKey() == null) continue;
                    String k = p.getParamKey().trim().toLowerCase();
                    String v = p.getParamValue();
                    if (m.containsKey(k)) {
                        m.put(k, v == null ? "" : v);
                    }
                }
            }
        } catch (Exception ignore) {}
        return m;
    }

    private boolean isVoucherTransferredOrProcessing(BillingVouchers v) {
        String ts = v == null || v.getTransferStatus() == null ? "" : String.valueOf(v.getTransferStatus()).toUpperCase();
        // PROCESSING：已提交外部系统等待回调；COMPLETED：外部系统已处理完成
        return "PROCESSING".equals(ts) || "COMPLETED".equals(ts);
    }

    private boolean hasTaxTransferInProgressOrDone(Long voucherId) {
        if (voucherId == null) return false;
        try {
            LambdaQueryWrapper<TaxRecords> w = new LambdaQueryWrapper<>();
            w.eq(TaxRecords::getVoucherId, voucherId);
            // TRANSFER_PENDING / TRANSFERRED 都视为“已进入移交流程/已移交”
            w.and(x -> x.eq(TaxRecords::getTaxStatus, "TRANSFER_PENDING")
                    .or()
                    .eq(TaxRecords::getTaxStatus, "TRANSFERRED"));
            return taxRecordsService.count(w) > 0;
        } catch (Exception e) {
            // 查询异常时，为了安全起见不放行（避免误冲红造成不一致）
            return true;
        }
    }
}
