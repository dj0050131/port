package syy.portPilotage.controller.billing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.billing.PaymentCallbackRequest;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.*;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.*;
import syy.portPilotage.util.SecurityUtil;
import syy.portPilotage.vo.billing.BillingResultVO;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/billing/charges")
@RequiredArgsConstructor
@Tag(name = "收费确认管理", description = "收费确认相关接口")
public class ChargeController {
    
    private final BillingVouchersService billingVouchersService;
    private final TaxRecordsService taxRecordsService;
    private final TaxItemDetailsService taxItemDetailsService;
    private final InvoiceOperationLogsService invoiceOperationLogsService;
    
    @Autowired
    private FeeWriteOffsService feeWriteOffsService;

    @Value("${external.simulation.base-url:}")
    private String simulationBaseUrl;

    @Value("${server.port:8099}")
    private int serverPort;

    private String getSimulationBaseUrl() {
        if (simulationBaseUrl == null || simulationBaseUrl.trim().isEmpty()) {
            return "http://localhost:" + serverPort;
        }
        return simulationBaseUrl.trim();
    }

        @PostMapping("/paymentCallback")
    @Operation(summary = "模拟外部系统付款回调（内部使用）")
    public Result<BillingResultVO> paymentCallback(@RequestBody PaymentCallbackRequest request) {
        log.info("接收模拟外部系统付款回调: {}", request);
        BillingResultVO vo = billingVouchersService.handlePaymentCallback(request);
        return Result.success(vo);
    }
    
        @GetMapping("/pendingConfirmation")
    @Operation(summary = "获取待确认到款的计费凭证")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE') or hasAuthority('BILLING:PAYMENT_CONFIRMATION:MANAGE')")
    public Result<PageResult<BillingResultVO>> getPendingConfirmationCharges(PageQueryDTO query) {
        log.info("获取待确认到款的计费凭证");
        
        Page<BillingVouchers> page = new Page<>(query.getCurrent(), query.getSize());
        
        LambdaQueryWrapper<BillingVouchers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingVouchers::getAuditStatus, "APPROVED"); // 审核通过
        wrapper.eq(BillingVouchers::getPaymentStatus, "UNPAID"); // 未付款
        wrapper.orderByDesc(BillingVouchers::getUpdatedAt);
        
        IPage<BillingVouchers> result = billingVouchersService.page(page, wrapper);
        
        List<BillingResultVO> voList = result.getRecords().stream()
                .map(billingVouchersService::buildBillingResultVO)
                .collect(Collectors.toList());
        
        PageResult<BillingResultVO> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                voList
        );
        
        log.info("获取待确认到款的计费凭证完成，共{}条记录", result.getTotal());
        return Result.success(pageResult);
    }

        @GetMapping("/paymentNoticeFailed")
    @Operation(summary = "获取付款提示提交外部系统失败的待付款订单")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE') or hasAuthority('BILLING:PAYMENT_NOTICE_FAILURES:VIEW')")
    public Result<PageResult<syy.portPilotage.vo.billing.PaymentNoticeFailureVO>> getPaymentNoticeFailed(PageQueryDTO query,
                                                                                                       @RequestParam(required = false) String keyword) {
        int current =  query.getCurrent();
        int size =  query.getSize();
        int offset = (current - 1) * size;

        List<java.util.Map<String, Object>> rows = invoiceOperationLogsService.selectPaymentNoticeFailed(offset, size, keyword);
        Long total = invoiceOperationLogsService.countPaymentNoticeFailed(keyword);

        List<syy.portPilotage.vo.billing.PaymentNoticeFailureVO> voList = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> r : rows) {
            Object vidObj = r.get("voucherId");
            if (vidObj == null) continue;
            Long voucherId = Long.valueOf(String.valueOf(vidObj));
            BillingVouchers v = billingVouchersService.getById(voucherId);
            if (v == null) continue;
            syy.portPilotage.vo.billing.PaymentNoticeFailureVO vo = new syy.portPilotage.vo.billing.PaymentNoticeFailureVO();
            vo.setVoucher(billingVouchersService.buildBillingResultVO(v));
            vo.setLastAttemptAt((java.util.Date) r.get("operationTime"));
            vo.setErrorMessage(r.get("errorMessage") == null ? null : String.valueOf(r.get("errorMessage")));
            voList.add(vo);
        }

        PageResult<syy.portPilotage.vo.billing.PaymentNoticeFailureVO> pageResult = PageResult.of(
                total == null ? 0 : total,
                current,
                size,
                voList
        );
        return Result.success(pageResult);
    }

        @PostMapping("/paymentNotice/retry/{id}")
    @Operation(summary = "重试提交付款提示到外部系统")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE') or hasAuthority('BILLING:PAYMENT_NOTICE_FAILURES:VIEW')")
    public Result<Boolean> retryPaymentNotice(@PathVariable Long id) {
        boolean ok = billingVouchersService.retryPaymentNotice(id);
        return Result.success(ok);
    }
    
        @PostMapping("/confirmPayment/{id}")
    @Operation(summary = "确认计费凭证到款")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<BillingResultVO> confirmPayment(@PathVariable Long id, 
                                                 @RequestParam BigDecimal receivedAmount,
                                                 @RequestParam String receiptDate) {
        log.info("确认计费凭证到款: id={}, receivedAmount={}, receiptDate={}", id, receivedAmount, receiptDate);

        BillingResultVO resultVO = billingVouchersService.confirmPayment(id, receivedAmount, receiptDate);
        log.info("计费凭证到款确认完成: id={}", id);
        return Result.success(resultVO);
    }

        @GetMapping("/eInvoice/download/{id}")
    @Operation(summary = "下载电子发票(模拟)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<BillingResultVO> downloadElectronicInvoice(@PathVariable Long id) {
        BillingResultVO vo = billingVouchersService.downloadElectronicInvoice(id);
        return Result.success(vo);
    }

        @PostMapping("/eInvoice/send/{id}")
    @Operation(summary = "发送电子发票(模拟)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<BillingResultVO> sendElectronicInvoice(@PathVariable Long id) {
        BillingResultVO vo = billingVouchersService.sendElectronicInvoice(id);
        return Result.success(vo);
    }

        @GetMapping("/eInvoice/detail/{id}")
    @Operation(summary = "获取电子发票详情")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Map<String, Object>> getElectronicInvoiceDetail(@PathVariable Long id) {
        Map<String, Object> detail = billingVouchersService.getElectronicInvoiceDetail(id);
        return Result.success(detail);
    }

        @GetMapping("/eInvoice/pdf/{id}")
    @Operation(summary = "下载电子发票PDF")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ResponseEntity<byte[]> downloadElectronicInvoicePdf(@PathVariable Long id) {
        BillingVouchers voucher = billingVouchersService.getById(id);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND);
        }
        byte[] bytes = billingVouchersService.downloadElectronicInvoicePdf(id);

        String fileName = "invoice_" + (voucher.getVoucherCode() == null ? id : voucher.getVoucherCode()) + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build());
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
    
        @GetMapping("/pendingInvoice")
    @Operation(summary = "获取待处理电子发票的计费凭证(替代纸质发票录入)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<PageResult<BillingResultVO>> getPendingInvoiceCharges(PageQueryDTO query) {
        log.info("获取待处理电子发票的计费凭证");
        
        Page<BillingVouchers> page = new Page<>(query.getCurrent(), query.getSize());
        
        LambdaQueryWrapper<BillingVouchers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingVouchers::getAuditStatus, "APPROVED"); // 审核通过
        wrapper.eq(BillingVouchers::getPaymentStatus, "FULLY_PAID"); // 已付款（付款完成后自动生成电子发票）
        wrapper.and(w -> w.isNull(BillingVouchers::getElectronicInvoiceStatus)
                .or()
                .ne(BillingVouchers::getElectronicInvoiceStatus, "SENT"));
        wrapper.orderByDesc(BillingVouchers::getUpdatedAt);
        
        IPage<BillingVouchers> result = billingVouchersService.page(page, wrapper);
        
        List<BillingResultVO> voList = result.getRecords().stream()
                .map(billingVouchersService::buildBillingResultVO)
                .collect(Collectors.toList());
        
        PageResult<BillingResultVO> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                voList
        );
        
        log.info("获取待处理电子发票的计费凭证完成，共{}条记录", result.getTotal());
        return Result.success(pageResult);
    }
    
    /**
     * 纸质发票录入接口（已弃用：电子发票时代不再使用）
     */
    @PostMapping("/recordInvoice/{id}")
    @Operation(summary = "录入纸质发票信息(已弃用)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<BillingResultVO> recordInvoice(@PathVariable Long id,
                                                @RequestParam String invoiceCode,
                                                @RequestParam String invoiceNumber) {
        log.warn("recordInvoice(纸质发票) 已弃用，但仍被调用: id={}", id);
        
        BillingVouchers voucher = billingVouchersService.getById(id);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND);
        }
        
        // 检查状态，必须是审核通过的才能录入发票
        if (!"APPROVED".equals(voucher.getAuditStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "只有审核通过的计费凭证才能录入发票");
        }
        
        voucher.setInvoiceCode(invoiceCode);
        voucher.setInvoiceNumber(invoiceNumber);
        voucher.setUpdatedAt(new Date());
        voucher.setPaymentStatus("FULLY_PAID");
        
        billingVouchersService.updateById(voucher);
        
        createTaxRecordsForVoucher(voucher);
        
        BillingResultVO resultVO = billingVouchersService.buildBillingResultVO(voucher);
        
        log.info("发票信息录入完成: id={}", id);
        return Result.success(resultVO);
    }
    
    /**
     * 为计费凭证创建税额记录
     * @param voucher 计费凭证
     */
    private void createTaxRecordsForVoucher(BillingVouchers voucher) {
        LambdaQueryWrapper<TaxRecords> taxRecordWrapper = new LambdaQueryWrapper<>();
        taxRecordWrapper.eq(TaxRecords::getVoucherId, voucher.getId());
        if (taxRecordsService.count(taxRecordWrapper) > 0) {
            // 如果已存在，则不重复创建
            return;
        }
        
        TaxRecords taxRecord = new TaxRecords();
        taxRecord.setTaxRecordCode("TAX-" + System.currentTimeMillis()); // 临时生成税额记录编号
        taxRecord.setVoucherId(voucher.getId());
        taxRecord.setBillingVoucherCode(voucher.getVoucherCode());
        taxRecord.setVoyageId(voucher.getVoyageId());
        taxRecord.setVesselId(voucher.getVesselId());
        taxRecord.setAgentId(voucher.getAgentId());
        taxRecord.setTaxableAmount(voucher.getTotalAmount());
        taxRecord.setTotalTaxAmount(voucher.getTotalAmount().multiply(BigDecimal.valueOf(0.09))); // 假设税率9%
        taxRecord.setTaxPeriod(new Date()); // 临时设置为当前日期
        taxRecord.setTaxCalculationTime(new Date());
        taxRecord.setTaxPolicyCode("POLICY-001"); // 临时税率政策编码
        taxRecord.setTaxPolicyName("标准税率政策"); // 临时税率政策名称
        taxRecord.setTaxStatus("CALCULATED");
        taxRecord.setCalculationMethod("AUTO"); // 自动计算
        taxRecord.setDataSource("AUTO"); // 数据来源为自动计算
        taxRecord.setIsActive(1); // 有效
        taxRecord.setCreatedAt(new Date());
        taxRecord.setUpdatedAt(new Date());
        
        taxRecordsService.save(taxRecord);
        
        TaxItemDetails vatItem = new TaxItemDetails();
        vatItem.setTaxRecordId(taxRecord.getId());
        vatItem.setTaxItemCode("VAT"); // 增值税编码
        vatItem.setTaxItemName("增值税"); // 增值税名称
        vatItem.setTaxType("MAIN"); // 主税
        vatItem.setTaxRate(BigDecimal.valueOf(0.09)); // 9%税率
        vatItem.setTaxableBase(voucher.getTotalAmount()); // 计税基数为总金额
        vatItem.setTaxAmount(voucher.getTotalAmount().multiply(BigDecimal.valueOf(0.09))); // 税额
        vatItem.setTaxPolicyReference("POLICY-001"); // 税收政策依据
        vatItem.setIsPreferential(0); // 不享受优惠
        vatItem.setIsAdjusted(0); // 未经调整
        vatItem.setSortOrder(1);
        vatItem.setCreatedAt(new Date());
        vatItem.setUpdatedAt(new Date());
        
        taxItemDetailsService.save(vatItem);
        
        TaxItemDetails additionalItem = new TaxItemDetails();
        additionalItem.setTaxRecordId(taxRecord.getId());
        additionalItem.setTaxItemCode("ADDITIONAL_TAX"); // 附加税编码
        additionalItem.setTaxItemName("城建税"); // 城建税名称
        additionalItem.setTaxType("ADDITIONAL"); // 附加税
        additionalItem.setTaxRate(BigDecimal.valueOf(0.07)); // 7%税率
        additionalItem.setTaxableBase(voucher.getTotalAmount().multiply(BigDecimal.valueOf(0.09))); // 计税基数为增值税额
        additionalItem.setTaxAmount(voucher.getTotalAmount().multiply(BigDecimal.valueOf(0.09)).multiply(BigDecimal.valueOf(0.07))); // 税额
        additionalItem.setTaxPolicyReference("POLICY-002"); // 税收政策依据
        additionalItem.setIsPreferential(0); // 不享受优惠
        additionalItem.setIsAdjusted(0); // 未经调整
        additionalItem.setSortOrder(2);
        additionalItem.setCreatedAt(new Date());
        additionalItem.setUpdatedAt(new Date());
        
        taxItemDetailsService.save(additionalItem);
    }

        @PostMapping("/transfer")
    @Operation(summary = "批量移交计费凭证")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<List<BillingResultVO>> transferBillingVouchers(@RequestParam List<Long> ids,
                                                                @RequestParam String transferTarget,
                                                                @RequestParam String transferType) {
        log.info("批量移交计费凭证: ids={}, transferTarget={}, transferType={}", ids, transferTarget, transferType);

        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要移交的计费凭证");
        }

        // 先校验所有凭证状态，避免“部分成功/部分失败”导致难以追踪
        List<BillingVouchers> vouchers = new ArrayList<>();
        for (Long id : ids) {
            BillingVouchers voucher = billingVouchersService.getById(id);
            if (voucher == null) {
                throw new BusinessException(ErrorCode.BILLING_NOT_FOUND, "计费凭证不存在: " + id);
            }
            // 必须已付款
            if (!"FULLY_PAID".equals(String.valueOf(voucher.getPaymentStatus()))) {
                throw new BusinessException(ErrorCode.INVALID_STATUS, "只有已付款的计费凭证才能移交: " + id);
            }
            // 电子发票未生成时禁止移交（避免“付了款但无票”）
            String eiStatus = voucher.getElectronicInvoiceStatus() == null ? null : voucher.getElectronicInvoiceStatus().toString();
            if (eiStatus == null || "NOT_GENERATED".equals(eiStatus) || "GENERATING".equals(eiStatus)) {
                throw new BusinessException(ErrorCode.INVALID_STATUS, "电子发票未生成，暂不可移交: " + id);
            }
            vouchers.add(voucher);
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("ids", ids);

        Map<String, String> data = new HashMap<>();
        data.put("transferTarget", transferTarget);
        data.put("transferType", transferType);
        requestData.put("data", data);

        boolean externalOk = false; // 外部系统是否“受理成功”
        String externalMsg = null;
        String externalRefNo = null;
        String externalCode = null;
        Date now = new Date();

        // 调用模拟外部系统接口（同步返回是否受理成功；最终结果由模拟系统异步回调）
        try {
            String simulationUrl = getSimulationBaseUrl() + "/api/billing/simulation/submitWriteOff";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);
            ResponseEntity<Result> response = restTemplate.postForEntity(simulationUrl, requestEntity, Result.class);
            
            externalOk = response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess();
            externalCode = String.valueOf(response.getStatusCode().value());
            externalMsg = response.getBody() == null ? ("HTTP " + response.getStatusCode()) : String.valueOf(response.getBody().getMessage());
            externalRefNo = "SIM-WO-" + now.getTime();
            if (!externalOk) {
                log.warn("调用模拟外部系统接口失败: statusCode={}, body={}", response.getStatusCode(), response.getBody());
            } else {
                log.info("调用模拟外部系统接口成功: {}", response.getBody().getData());
            }
        } catch (Exception e) {
            log.error("调用模拟外部系统接口时发生错误", e);
            externalOk = false;
            externalCode = "EXCEPTION";
            externalMsg = e.getMessage();
            externalRefNo = "SIM-WO-" + now.getTime();
        }

        List<BillingResultVO> resultVOList = new ArrayList<>();

        for (BillingVouchers voucher : vouchers) {
            Long id = voucher.getId();
            FeeWriteOffs writeOff = new FeeWriteOffs();
            writeOff.setWriteOffCode("WO" + System.currentTimeMillis() + "-" + id); // 临时生成核销单编号
            writeOff.setAgentId(voucher.getAgentId());
            writeOff.setWriteOffAmount(voucher.getTotalAmount());
            writeOff.setWriteOffType(transferType);
            String operatorName = SecurityUtil.getCurrentUsername();
            writeOff.setWriteOffPerson((operatorName == null || operatorName.isBlank()) ? "System" : operatorName);
            writeOff.setWriteOffDate(new Date());
            // 当前系统未单独实现“交接单审核/财务确认”流程：默认经办即审核，审核时间=移交提交时间
            writeOff.setAuditor(writeOff.getWriteOffPerson());
            writeOff.setAuditTime(now);
            writeOff.setStatusCode(externalOk ? "PROCESSING" : "FAILED");
            writeOff.setStatusName(externalOk ? "处理中" : "移交失败");
            // 用 voucherNumber 作为回调关联键（写入 voucherId）
            writeOff.setVoucherNumber(String.valueOf(id));
            writeOff.setNotes(externalOk
                    ? ("已提交到模拟外部系统，等待处理，refNo=" + externalRefNo)
                    : ("提交模拟外部系统失败，code=" + externalCode + "，msg=" + (externalMsg == null ? "-" : externalMsg)));
            writeOff.setCreatedAt(new Date());
            writeOff.setUpdatedAt(new Date());

            feeWriteOffsService.save(writeOff);

            voucher.setTransferStatus(externalOk ? "PROCESSING" : "FAILED");
            voucher.setTransferTarget(transferTarget);
            // 确保 transferType 是数据库中定义的有效值之一
            if ("MANUAL".equals(transferType) || "AUTO".equals(transferType) || "RETRY".equals(transferType)) {
                voucher.setTransferType(transferType);
            } else {
                voucher.setTransferType("MANUAL"); // 默认值
            }
            voucher.setTransferTime(now);
            Long operatorId = SecurityUtil.getCurrentUserId();
            voucher.setTransferOperatorId(operatorId == null ? 1L : operatorId);
            Integer oldAttempt = voucher.getTransferAttemptCount() == null ? 0 : voucher.getTransferAttemptCount();
            voucher.setTransferAttemptCount(oldAttempt + 1);
            voucher.setLastTransferAttempt(now);
            voucher.setExternalRefNo(externalRefNo);
            voucher.setExternalResponseCode(externalCode);
            voucher.setExternalResponseMsg(externalMsg);
            voucher.setTransferNotes(JSON.toJSONString(Map.of(
                    "transferTarget", transferTarget,
                    "transferType", String.valueOf(voucher.getTransferType()),
                    "externalOk", externalOk, // 受理结果
                    "externalRefNo", externalRefNo,
                    "externalCode", externalCode,
                    "externalMsg", externalMsg == null ? "" : externalMsg
            )));
            voucher.setUpdatedAt(now);

            billingVouchersService.updateById(voucher);

            BillingResultVO resultVO = billingVouchersService.buildBillingResultVO(voucher);
            resultVOList.add(resultVO);
        }

        log.info("批量移交计费凭证完成: ids={}", ids);
        return Result.success(resultVOList);
    }

        @PostMapping("/transferTaxRecords")
    @Operation(summary = "批量移交税额记录")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> transferTaxRecords(@RequestParam List<Long> ids,
                                          @RequestParam String transferTarget,
                                          @RequestParam String transferType) {
        log.info("批量移交税额记录: ids={}, transferTarget={}, transferType={}", ids, transferTarget, transferType);

        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要移交的税额记录");
        }

        List<TaxRecords> taxRecords = new ArrayList<>();
        for (Long id : ids) {
            TaxRecords taxRecord = taxRecordsService.getById(id);
            if (taxRecord == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "税额记录不存在: " + id);
            }
            // 通常应为已审核/已批准才允许移交；但当前业务存在 CALCULATED（已计算）也需要进入移交流程
            String taxStatus = taxRecord.getTaxStatus() == null ? null : String.valueOf(taxRecord.getTaxStatus()).toUpperCase();
            if (taxStatus != null && !"APPROVED".equals(taxStatus) && !"TRANSFER_PENDING".equals(taxStatus) && !"CALCULATED".equals(taxStatus)) {
                throw new BusinessException(ErrorCode.INVALID_STATUS, "税额状态不允许移交: " + id + ", taxStatus=" + taxStatus);
            }
            taxRecords.add(taxRecord);
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("ids", ids);

        Map<String, String> data = new HashMap<>();
        data.put("transferTarget", transferTarget);
        data.put("transferType", transferType);
        requestData.put("data", data);

        boolean externalOk = false;
        String externalMsg = null;
        String externalRefNo = null;
        String externalCode = null;
        Date now = new Date();

        try {
            String simulationUrl = getSimulationBaseUrl() + "/api/billing/simulation/submitTaxRecords";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);
            ResponseEntity<Result> response = restTemplate.postForEntity(simulationUrl, requestEntity, Result.class);
            
            externalOk = response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess();
            externalCode = String.valueOf(response.getStatusCode().value());
            externalMsg = response.getBody() == null ? ("HTTP " + response.getStatusCode()) : String.valueOf(response.getBody().getMessage());
            externalRefNo = "SIM-TAX-" + now.getTime();
            if (!externalOk) {
                log.warn("调用模拟外部系统接口失败: statusCode={}, body={}", response.getStatusCode(), response.getBody());
            } else {
                log.info("调用模拟外部系统接口成功: {}", response.getBody().getData());
            }
        } catch (Exception e) {
            log.error("调用模拟外部系统接口时发生错误", e);
            externalOk = false;
            externalCode = "EXCEPTION";
            externalMsg = e.getMessage();
            externalRefNo = "SIM-TAX-" + now.getTime();
        }

        for (TaxRecords taxRecord : taxRecords) {
            Long id = taxRecord.getId();
            FeeWriteOffs writeOff = new FeeWriteOffs();
            writeOff.setWriteOffCode("WO" + System.currentTimeMillis() + "-" + id); // 临时生成核销单编号
            writeOff.setAgentId(taxRecord.getAgentId());
            writeOff.setWriteOffAmount(taxRecord.getTotalTaxAmount());
            writeOff.setWriteOffType(transferType);
            String operatorName = SecurityUtil.getCurrentUsername();
            writeOff.setWriteOffPerson((operatorName == null || operatorName.isBlank()) ? "System" : operatorName);
            writeOff.setWriteOffDate(new Date());
            writeOff.setAuditor(writeOff.getWriteOffPerson());
            writeOff.setAuditTime(now);
            writeOff.setStatusCode(externalOk ? "PROCESSING" : "FAILED");
            writeOff.setStatusName(externalOk ? "处理中" : "移交失败");
            writeOff.setVoucherNumber("TAX-" + id);
            writeOff.setNotes(externalOk
                    ? ("税额已提交到模拟外部系统，等待处理，refNo=" + externalRefNo)
                    : ("税额提交模拟外部系统失败，code=" + externalCode + "，msg=" + (externalMsg == null ? "-" : externalMsg)));
            writeOff.setCreatedAt(new Date());
            writeOff.setUpdatedAt(new Date());

            feeWriteOffsService.save(writeOff);

            taxRecord.setTransferTarget(transferTarget);
            taxRecord.setTransferTime(now);
            // 异步模式：受理成功先进入待处理，回调后转已移交/失败
            taxRecord.setTaxStatus(externalOk ? "TRANSFER_PENDING" : "TRANSFER_PENDING");
            taxRecord.setTransferNotes(JSON.toJSONString(Map.of(
                    "transferTarget", transferTarget,
                    "transferType", transferType,
                    "externalOk", externalOk, // 受理结果
                    "externalRefNo", externalRefNo,
                    "externalCode", externalCode,
                    "externalMsg", externalMsg == null ? "" : externalMsg
            )));
            if (externalOk) {
                taxRecord.setExternalTaxNo("SIM-TAXNO-" + id);
                taxRecord.setExternalDeclarationNo(externalRefNo);
            }
            Long operatorId = SecurityUtil.getCurrentUserId();
            taxRecord.setTransferOperatorId(operatorId == null ? 1L : operatorId);
            taxRecord.setUpdatedAt(now);

            taxRecordsService.updateById(taxRecord);
        }

        log.info("批量移交税额记录完成: ids={}", ids);
        return Result.success();
    }

        @PostMapping("/transferCallback/writeoff")
    @Operation(summary = "移交回调-计费凭证")
    public Result<Void> transferCallbackWriteOff(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> idsRaw = (List<Integer>) body.get("ids");
            boolean success = Boolean.TRUE.equals(body.get("success"));
            String externalRefNo = body.get("externalRefNo") == null ? null : String.valueOf(body.get("externalRefNo"));
            String msg = body.get("message") == null ? null : String.valueOf(body.get("message"));
            Date now = new Date();

            if (idsRaw == null || idsRaw.isEmpty()) return Result.success();
            for (Integer vid : idsRaw) {
                if (vid == null) continue;
                Long voucherId = Long.valueOf(vid);
                BillingVouchers voucher = billingVouchersService.getById(voucherId);
                if (voucher != null) {
                    voucher.setTransferStatus(success ? "COMPLETED" : "FAILED");
                    voucher.setExternalRefNo(externalRefNo);
                    voucher.setExternalResponseCode(success ? "200" : "500");
                    voucher.setExternalResponseMsg(msg);
                    voucher.setUpdatedAt(now);
                    billingVouchersService.updateById(voucher);
                }

                LambdaUpdateWrapper<FeeWriteOffs> uw = new LambdaUpdateWrapper<FeeWriteOffs>()
                        .eq(FeeWriteOffs::getVoucherNumber, String.valueOf(voucherId))
                        .eq(FeeWriteOffs::getStatusCode, "PROCESSING")
                        .set(FeeWriteOffs::getStatusCode, success ? "COMPLETED" : "FAILED")
                        .set(FeeWriteOffs::getStatusName, success ? "已移交" : "移交失败")
                        .set(FeeWriteOffs::getNotes, success ? ("外部系统处理完成，refNo=" + externalRefNo) : ("外部系统处理失败，msg=" + (msg == null ? "-" : msg)))
                        .set(FeeWriteOffs::getUpdatedAt, now);
                if (success) {
                    uw.set(FeeWriteOffs::getFinanceConfirmer, "SYSTEM_CALLBACK");
                    uw.set(FeeWriteOffs::getFinanceConfirmTime, now);
                    uw.set(FeeWriteOffs::getBankReferenceNumber, externalRefNo);
                }
                feeWriteOffsService.update(uw);
            }
            return Result.success();
        } catch (Exception e) {
            log.error("移交回调-计费凭证处理失败: body={}", body, e);
            return Result.error("callback failed");
        }
    }

        @PostMapping("/transferCallback/taxRecords")
    @Operation(summary = "移交回调-税额记录")
    public Result<Void> transferCallbackTax(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> idsRaw = (List<Integer>) body.get("ids");
            boolean success = Boolean.TRUE.equals(body.get("success"));
            String externalRefNo = body.get("externalRefNo") == null ? null : String.valueOf(body.get("externalRefNo"));
            String msg = body.get("message") == null ? null : String.valueOf(body.get("message"));
            Date now = new Date();

            if (idsRaw == null || idsRaw.isEmpty()) return Result.success();
            for (Integer tid : idsRaw) {
                if (tid == null) continue;
                Long taxId = Long.valueOf(tid);
                TaxRecords taxRecord = taxRecordsService.getById(taxId);
                if (taxRecord != null) {
                    taxRecord.setTaxStatus(success ? "TRANSFERRED" : "TRANSFER_PENDING");
                    taxRecord.setExternalDeclarationNo(externalRefNo);
                    if (success) taxRecord.setExternalTaxNo("SIM-TAXNO-" + taxId);
                    taxRecord.setUpdatedAt(now);
                    taxRecordsService.updateById(taxRecord);
                }

                LambdaUpdateWrapper<FeeWriteOffs> uw = new LambdaUpdateWrapper<FeeWriteOffs>()
                        .eq(FeeWriteOffs::getVoucherNumber, "TAX-" + taxId)
                        .eq(FeeWriteOffs::getStatusCode, "PROCESSING")
                        .set(FeeWriteOffs::getStatusCode, success ? "COMPLETED" : "FAILED")
                        .set(FeeWriteOffs::getStatusName, success ? "已移交" : "移交失败")
                        .set(FeeWriteOffs::getNotes, success ? ("外部系统处理完成，refNo=" + externalRefNo) : ("外部系统处理失败，msg=" + (msg == null ? "-" : msg)))
                        .set(FeeWriteOffs::getUpdatedAt, now);
                if (success) {
                    uw.set(FeeWriteOffs::getFinanceConfirmer, "SYSTEM_CALLBACK");
                    uw.set(FeeWriteOffs::getFinanceConfirmTime, now);
                    uw.set(FeeWriteOffs::getBankReferenceNumber, externalRefNo);
                }
                feeWriteOffsService.update(uw);
            }
            return Result.success();
        } catch (Exception e) {
            log.error("移交回调-税额记录处理失败: body={}", body, e);
            return Result.error("callback failed");
        }
    }

    /**
     * 重试移交（交接单/计费凭证）
     * 说明：前端传入 fee_write_offs.id 列表；后端复用 voucherNumber 解析 voucherId，再次提交模拟外部系统并更新状态
     */
    @PostMapping("/transferRetry/writeoff")
    @Operation(summary = "重试移交-计费凭证交接单")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> retryTransferWriteOffs(@RequestParam List<Long> ids,
                                               @RequestParam(required = false) String transferTarget,
                                               @RequestParam(required = false) String transferType) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要重试的交接单");
        }
        String tt = (transferType == null || transferType.trim().isEmpty()) ? "RETRY" : transferType.trim().toUpperCase();
        if (!"MANUAL".equals(tt) && !"AUTO".equals(tt) && !"RETRY".equals(tt)) {
            tt = "RETRY";
        }
        String target = (transferTarget == null || transferTarget.trim().isEmpty()) ? "SYSTEM_RETRY" : transferTarget.trim();

        List<FeeWriteOffs> writeOffs = feeWriteOffsService.listByIds(ids);
        List<Long> voucherIds = new ArrayList<>();
        for (FeeWriteOffs w : writeOffs) {
            if (w == null) continue;
            String vn = w.getVoucherNumber() == null ? "" : w.getVoucherNumber().trim();
            if (vn.isEmpty() || vn.startsWith("TAX-")) continue;
            try {
                voucherIds.add(Long.valueOf(vn));
            } catch (Exception ignore) {}
        }
        if (voucherIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "所选交接单不包含可重试的计费凭证");
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("ids", voucherIds);
        requestData.put("data", Map.of("transferTarget", target, "transferType", tt));

        boolean externalOk;
        String externalMsg;
        String externalRefNo;
        String externalCode;
        Date now = new Date();

        try {
            String simulationUrl = getSimulationBaseUrl() + "/api/billing/simulation/submitWriteOff";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);
            ResponseEntity<Result> response = restTemplate.postForEntity(simulationUrl, requestEntity, Result.class);
            externalOk = response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess();
            externalCode = String.valueOf(response.getStatusCode().value());
            externalMsg = response.getBody() == null ? ("HTTP " + response.getStatusCode()) : String.valueOf(response.getBody().getMessage());
            externalRefNo = "SIM-WO-" + now.getTime();
        } catch (Exception e) {
            externalOk = false;
            externalCode = "EXCEPTION";
            externalMsg = e.getMessage();
            externalRefNo = "SIM-WO-" + now.getTime();
        }

        for (Long vid : voucherIds) {
            BillingVouchers voucher = billingVouchersService.getById(vid);
            if (voucher == null) continue;
            voucher.setTransferStatus(externalOk ? "PROCESSING" : "FAILED");
            voucher.setTransferTarget(target);
            voucher.setTransferType(tt);
            voucher.setTransferTime(now);
            voucher.setTransferOperatorId(1L);
            Integer oldAttempt = voucher.getTransferAttemptCount() == null ? 0 : voucher.getTransferAttemptCount();
            voucher.setTransferAttemptCount(oldAttempt + 1);
            voucher.setLastTransferAttempt(now);
            voucher.setExternalRefNo(externalRefNo);
            voucher.setExternalResponseCode(externalCode);
            voucher.setExternalResponseMsg(externalMsg);
            voucher.setUpdatedAt(now);
            billingVouchersService.updateById(voucher);
        }

        for (FeeWriteOffs w : writeOffs) {
            if (w == null) continue;
            String vn = w.getVoucherNumber() == null ? "" : w.getVoucherNumber().trim();
            if (vn.isEmpty() || vn.startsWith("TAX-")) continue;
            w.setWriteOffType(tt);
            w.setStatusCode(externalOk ? "PROCESSING" : "FAILED");
            w.setStatusName(externalOk ? "处理中" : "移交失败");
            w.setNotes(externalOk ? ("已重试提交到模拟外部系统，等待处理，refNo=" + externalRefNo)
                    : ("重试提交模拟外部系统失败，code=" + externalCode + "，msg=" + (externalMsg == null ? "-" : externalMsg)));
            w.setUpdatedAt(now);
            feeWriteOffsService.updateById(w);
        }
        return Result.success();
    }

        @PostMapping("/transferRetry/taxRecords")
    @Operation(summary = "重试移交-税额交接单")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<Void> retryTransferTaxWriteOffs(@RequestParam List<Long> ids,
                                                  @RequestParam(required = false) String transferTarget,
                                                  @RequestParam(required = false) String transferType) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要重试的税额交接单");
        }
        String tt = (transferType == null || transferType.trim().isEmpty()) ? "RETRY" : transferType.trim().toUpperCase();
        if (!"MANUAL".equals(tt) && !"AUTO".equals(tt) && !"RETRY".equals(tt)) {
            tt = "RETRY";
        }
        String target = (transferTarget == null || transferTarget.trim().isEmpty()) ? "SYSTEM_RETRY" : transferTarget.trim();

        List<FeeWriteOffs> writeOffs = feeWriteOffsService.listByIds(ids);
        List<Long> taxIds = new ArrayList<>();
        for (FeeWriteOffs w : writeOffs) {
            if (w == null) continue;
            String vn = w.getVoucherNumber() == null ? "" : w.getVoucherNumber().trim();
            if (!vn.startsWith("TAX-")) continue;
            try {
                taxIds.add(Long.valueOf(vn.substring(4)));
            } catch (Exception ignore) {}
        }
        if (taxIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "所选交接单不包含可重试的税额记录");
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("ids", taxIds);
        requestData.put("data", Map.of("transferTarget", target, "transferType", tt));

        boolean externalOk;
        String externalMsg;
        String externalRefNo;
        String externalCode;
        Date now = new Date();

        try {
            String simulationUrl = getSimulationBaseUrl() + "/api/billing/simulation/submitTaxRecords";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);
            ResponseEntity<Result> response = restTemplate.postForEntity(simulationUrl, requestEntity, Result.class);
            externalOk = response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess();
            externalCode = String.valueOf(response.getStatusCode().value());
            externalMsg = response.getBody() == null ? ("HTTP " + response.getStatusCode()) : String.valueOf(response.getBody().getMessage());
            externalRefNo = "SIM-TAX-" + now.getTime();
        } catch (Exception e) {
            externalOk = false;
            externalCode = "EXCEPTION";
            externalMsg = e.getMessage();
            externalRefNo = "SIM-TAX-" + now.getTime();
        }

        for (Long tid : taxIds) {
            TaxRecords tax = taxRecordsService.getById(tid);
            if (tax == null) continue;
            tax.setTransferTarget(target);
            tax.setTransferTime(now);
            tax.setTaxStatus("TRANSFER_PENDING");
            tax.setExternalDeclarationNo(externalRefNo);
            tax.setTransferOperatorId(1L);
            tax.setUpdatedAt(now);
            taxRecordsService.updateById(tax);
        }

        for (FeeWriteOffs w : writeOffs) {
            if (w == null) continue;
            String vn = w.getVoucherNumber() == null ? "" : w.getVoucherNumber().trim();
            if (!vn.startsWith("TAX-")) continue;
            w.setWriteOffType(tt);
            w.setStatusCode(externalOk ? "PROCESSING" : "FAILED");
            w.setStatusName(externalOk ? "处理中" : "移交失败");
            w.setNotes(externalOk ? ("税额已重试提交到模拟外部系统，等待处理，refNo=" + externalRefNo)
                    : ("税额重试提交失败，code=" + externalCode + "，msg=" + (externalMsg == null ? "-" : externalMsg)));
            w.setUpdatedAt(now);
            feeWriteOffsService.updateById(w);
        }
        return Result.success();
    }
    
        @GetMapping("/transferred")
    @Operation(summary = "获取已移交的计费凭证")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<PageResult<BillingResultVO>> getTransferredBillingVouchers(PageQueryDTO query) {
        log.info("获取已移交的计费凭证");
        
        Page<BillingVouchers> page = new Page<>(query.getCurrent(), query.getSize());
        
        LambdaQueryWrapper<BillingVouchers> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(BillingVouchers::getTransferStatus); // 移交状态不为空
        wrapper.ne(BillingVouchers::getTransferStatus, "PENDING"); // 不等于待处理
        wrapper.orderByDesc(BillingVouchers::getTransferTime);
        
        IPage<BillingVouchers> result = billingVouchersService.page(page, wrapper);
        
        List<BillingResultVO> voList = result.getRecords().stream()
                .map(billingVouchersService::buildBillingResultVO)
                .collect(Collectors.toList());
        
        PageResult<BillingResultVO> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                voList
        );
        
        log.info("获取已移交的计费凭证完成，共{}条记录", result.getTotal());
        return Result.success(pageResult);
    }

        @GetMapping("/untransferred")
    @Operation(summary = "获取未移交的计费凭证")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<PageResult<BillingResultVO>> getUntransferredBillingVouchers(PageQueryDTO query) {
        log.info("获取未移交的计费凭证");
        
        Page<BillingVouchers> page = new Page<>(query.getCurrent(), query.getSize());
        
        LambdaQueryWrapper<BillingVouchers> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.isNull(BillingVouchers::getTransferStatus) // 移交状态为空
                .or()
                .eq(BillingVouchers::getTransferStatus, "PENDING")); // 或者等于待处理
        wrapper.eq(BillingVouchers::getPaymentStatus, "FULLY_PAID"); // 已全额付款
        wrapper.orderByDesc(BillingVouchers::getUpdatedAt);
        
        IPage<BillingVouchers> result = billingVouchersService.page(page, wrapper);
        
        List<BillingResultVO> voList = result.getRecords().stream()
                .map(billingVouchersService::buildBillingResultVO)
                .collect(Collectors.toList());
        
        PageResult<BillingResultVO> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                voList
        );
        
        log.info("获取未移交的计费凭证完成，共{}条记录", result.getTotal());
        return Result.success(pageResult);
    }

    @GetMapping("/untransferredTaxRecords")
    @Operation(summary = "获取未移交的税额记录")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public Result<PageResult<BillingResultVO>> getUntransferredTaxRecords(PageQueryDTO query) {
        log.info("获取未移交的税额记录");
        
        Page<TaxRecords> page = new Page<>(query.getCurrent(), query.getSize());
        
        LambdaQueryWrapper<TaxRecords> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.isNull(TaxRecords::getTransferTarget) // 移交目标为空
                .or()
                .isNull(TaxRecords::getTransferTime)); // 或者移交时间为空
        wrapper.orderByDesc(TaxRecords::getUpdatedAt);
        
        IPage<TaxRecords> result = taxRecordsService.page(page, wrapper);
        
        List<BillingResultVO> voList = result.getRecords().stream()
                .map(this::buildTaxRecordResultVO)
                .collect(Collectors.toList());
        
        PageResult<BillingResultVO> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                voList
        );
        
        log.info("获取未移交的税额记录完成，共{}条记录", result.getTotal());
        return Result.success(pageResult);
    }
    
    /**
     * 构建税额记录的VO对象
     * @param taxRecord 税额记录实体
     * @return BillingResultVO 对象
     */
    private BillingResultVO buildTaxRecordResultVO(TaxRecords taxRecord) {
        // 尽量补齐展示字段：税额记录 → 关联计费凭证 → 船舶/代理/签证单信息
        BillingResultVO resultVO;
        try {
            BillingVouchers voucher = taxRecord.getVoucherId() == null ? null : billingVouchersService.getById(taxRecord.getVoucherId());
            resultVO = voucher == null ? new BillingResultVO() : billingVouchersService.buildBillingResultVO(voucher);
        } catch (Exception e) {
            resultVO = new BillingResultVO();
        }

        resultVO.setTaxRecordId(taxRecord.getId());
        resultVO.setTaxRecordCode(taxRecord.getTaxRecordCode());
        resultVO.setVoucherId(taxRecord.getVoucherId());
        resultVO.setTaxableAmount(taxRecord.getTaxableAmount());
        resultVO.setTotalTaxAmount(taxRecord.getTotalTaxAmount());
        resultVO.setTaxStatus(taxRecord.getTaxStatus() != null ? taxRecord.getTaxStatus().toString() : null);
        resultVO.setTransferTarget(taxRecord.getTransferTarget());
        resultVO.setTransferTime(taxRecord.getTransferTime());
        if (resultVO.getCreatedAt() == null) {
            resultVO.setCreatedAt(taxRecord.getCreatedAt());
        }
        return resultVO;
    }
}