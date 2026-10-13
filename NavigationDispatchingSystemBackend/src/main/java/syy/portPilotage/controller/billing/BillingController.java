package syy.portPilotage.controller.billing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.billing.BillingApprovalRequest;
import syy.portPilotage.dto.certificate.CertificateDetailDTO;
import syy.portPilotage.dto.certificate.VoucherCreateRequest;
import syy.portPilotage.dto.certificate.VoucherResultDTO;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.*;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.mapper.PilotageCertificatesMapper;
import syy.portPilotage.service.*;
import syy.portPilotage.vo.billing.BillingResultVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/billing/vouchers")
@RequiredArgsConstructor
@Tag(name = "费用计算管理", description = "费用计算相关接口")
public class BillingController {
    
    private final BillingVouchersService billingVouchersService;
    private final BillingCalculationService billingCalculationService;
    private final PilotageCertificatesService pilotageCertificatesService;
    // private final StandardRatesService standardRatesService; // 暂未使用
    @PostMapping("/voucher/create")
    @Operation(summary = "创建计费凭证", description = "根据签证单创建计费凭证，自动计算费用")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数错误"),
            @ApiResponse(responseCode = "403", description = "权限不足"),
            @ApiResponse(responseCode = "500", description = "系统错误")
    })
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE') or hasAuthority('BILLING:CHARGING:MANAGE')")
    public Result<VoucherResultDTO> createBillingVoucher(
            @Parameter(description = "创建计费凭证请求", required = true) @Valid @RequestBody VoucherCreateRequest request) {
        log.info("创建计费凭证: certificateId={}", request.getCertificateId());

        VoucherResultDTO result = pilotageCertificatesService.createBillingVoucher(request);
        return Result.success(result);
    }

    @PostMapping("/startBilling/{certificateId}")
    @Operation(summary = "开始计费", description = "对指定的签证单开始计费，生成计费凭证")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "计费成功"),
            @ApiResponse(responseCode = "404", description = "签证单不存在"),
            @ApiResponse(responseCode = "400", description = "签证单状态无效"),
            @ApiResponse(responseCode = "403", description = "权限不足")
    })
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE', 'DISPATCHER') or hasAuthority('BILLING:CHARGING:MANAGE')")
    public Result<BillingResultVO> startBilling(
            @Parameter(description = "签证单ID", required = true, example = "1") @PathVariable Long certificateId) {
        log.info("开始计费: certificateId={}", certificateId);
        BillingResultVO resultVO = billingCalculationService.startBilling(certificateId);
        return Result.success(resultVO);
    }
    
    // 将具体路径的接口放在前面，避免路径冲突
    
    @GetMapping("/rejected")
    @Operation(summary = "获取被退回的审核签证单")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE') or hasAuthority('BILLING:CHARGING:MANAGE')")
    public Result<PageResult<CertificateDetailDTO>> getRejectedBillingVouchers(PageQueryDTO query) {
        log.info("获取被退回的审核签证单（重计费列表）");
        PageResult<CertificateDetailDTO> pageResult = billingCalculationService.getRejectedBillingVouchers(query);
        log.info("获取重计费签证单完成，共{}条记录", pageResult.getTotal());
        return Result.success(pageResult);
    }
    
    @PostMapping("/rebill/{id}")
    @Operation(summary = "将退回的签证单重新计费")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE') or hasAuthority('BILLING:CHARGING:MANAGE')")
    public Result<BillingResultVO> rebillRejectedVoucher(@PathVariable Long id) {
        log.info("将退回的签证单重新计费: id={}", id);
        BillingResultVO resultVO = billingCalculationService.rebillRejectedVoucher(id);
        log.info("重新计费完成: certificateId={}, voucherId={}", resultVO.getCertificateId(), resultVO.getVoucherId());
        return Result.success(resultVO);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取计费凭证详情")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE') or hasAuthority('BILLING:CHARGING:MANAGE') or hasAuthority('BILLING:AUDIT:MANAGE') or hasAuthority('BILLING:PAYMENT_CONFIRMATION:MANAGE') or hasAuthority('BILLING:TRANSFER:MANAGE') or hasAuthority('BILLING:INVOICE_VOID:MANAGE') or hasAuthority('BILLING:PAYMENT_NOTICE_FAILURES:VIEW') or hasAuthority('BILLING:VISA_LIST:VIEW')")
    public Result<BillingResultVO> getById(@PathVariable Long id) {
        BillingVouchers voucher = billingVouchersService.getById(id);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND);
        }
        BillingResultVO resultVO = billingVouchersService.buildBillingResultVO(voucher);
        return Result.success(resultVO);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询计费凭证")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE') or hasAuthority('BILLING:CHARGING:MANAGE') or hasAuthority('BILLING:AUDIT:MANAGE') or hasAuthority('BILLING:PAYMENT_CONFIRMATION:MANAGE') or hasAuthority('BILLING:TRANSFER:MANAGE') or hasAuthority('BILLING:INVOICE_VOID:MANAGE') or hasAuthority('BILLING:PAYMENT_NOTICE_FAILURES:VIEW') or hasAuthority('BILLING:VISA_LIST:VIEW')")
    public Result<PageResult<BillingResultVO>> page(PageQueryDTO query) {
        PageResult<BillingResultVO> pageResult = billingVouchersService.pageBillingVouchers(query);
        return Result.success(pageResult);
    }
    
    @GetMapping("/byCertificate/{certificateId}")
    @Operation(summary = "根据签证单ID获取计费凭证详情")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE') or hasAuthority('BILLING:CHARGING:MANAGE') or hasAuthority('BILLING:AUDIT:MANAGE') or hasAuthority('BILLING:PAYMENT_CONFIRMATION:MANAGE') or hasAuthority('BILLING:TRANSFER:MANAGE') or hasAuthority('BILLING:INVOICE_VOID:MANAGE') or hasAuthority('BILLING:PAYMENT_NOTICE_FAILURES:VIEW') or hasAuthority('BILLING:VISA_LIST:VIEW')")
    public Result<BillingResultVO> getByCertificateId(@PathVariable Long certificateId) {
        LambdaQueryWrapper<BillingVouchers> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingVouchers::getCertificateId, certificateId);
        BillingVouchers voucher = billingVouchersService.getOne(wrapper);
        
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND);
        }
        
        BillingResultVO resultVO = billingVouchersService.buildBillingResultVO(voucher);
        
        return Result.success(resultVO);
    }
    
    @GetMapping("/{id}/auditDetails")
    @Operation(summary = "查看审核详情")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE') or hasAuthority('BILLING:AUDIT:MANAGE')")
    public Result<BillingResultVO> getAuditDetails(@PathVariable Long id) {
        log.info("查看审核详情: id={}", id);
        BillingResultVO resultVO = billingCalculationService.getAuditDetail(id);
        log.info("审核详情查看完成: id={}", id);
        return Result.success(resultVO);
    }

    @PostMapping("/approve")
    @Operation(summary = "审核计费凭证(支持批量)")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE') or hasAuthority('BILLING:AUDIT:MANAGE')")
    public Result<Void> approve(@RequestBody BillingApprovalRequest request) {
        log.info("审核计费凭证: ids={}, approved={}", request.getIds(), request.getApproved());
        boolean approve = billingVouchersService.approve(request);
        return approve ? Result.success() : Result.error(ErrorCode.OPERATION_FAILED);
    }

}
