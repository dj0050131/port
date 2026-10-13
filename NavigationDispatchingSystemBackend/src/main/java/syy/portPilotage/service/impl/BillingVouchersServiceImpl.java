package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.billing.BillingApprovalRequest;
import syy.portPilotage.dto.billing.PaymentCallbackRequest;
import syy.portPilotage.dto.billing.PaymentNoticeRequest;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.*;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.mapper.PilotageCertificatesMapper;
import syy.portPilotage.mapper.PilotagePlansMapper;
import syy.portPilotage.mapper.SystemParametersMapper;
import syy.portPilotage.rabbitmq.service.MessageService;
import syy.portPilotage.service.AgentsService;
import syy.portPilotage.service.AgentInvoiceInfoService;
import syy.portPilotage.service.BillingVouchersService;
import syy.portPilotage.mapper.BillingVouchersMapper;
import syy.portPilotage.service.ChargeCategoriesService;
import syy.portPilotage.service.ChargeDetailsService;
import syy.portPilotage.service.ChargeItemsService;
import syy.portPilotage.service.InvoiceOperationLogsService;
import syy.portPilotage.service.TaxItemDetailsService;
import syy.portPilotage.service.TaxRecordsService;
import syy.portPilotage.service.VesselsService;
import syy.portPilotage.service.VoyagesService;
import syy.portPilotage.util.CodeGeneratorUtil;
import syy.portPilotage.vo.billing.BillingResultVO;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson.JSON;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class BillingVouchersServiceImpl extends ServiceImpl<BillingVouchersMapper, BillingVouchers>
        implements BillingVouchersService {

    private final CodeGeneratorUtil codeGeneratorUtil;
    private final VesselsService vesselsService;
    private final ChargeDetailsService chargeDetailsService;
    private final ChargeItemsService chargeItemsService;
    private final ChargeCategoriesService chargeCategoriesService;
    private final BillingVouchersMapper billingVouchersMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private AgentsService agentsService;
    @Autowired
    private PilotageCertificatesMapper pilotageCertificatesMapper;
    @Autowired
    private PilotagePlansMapper pilotagePlansMapper;
    @Autowired
    private TaxRecordsService taxRecordsService;
    @Autowired
    private TaxItemDetailsService taxItemDetailsService;
    @Autowired
    private InvoiceOperationLogsService invoiceOperationLogsService;
    @Autowired
    private VoyagesService voyagesService;
    @Autowired
    private AgentInvoiceInfoService agentInvoiceInfoService;
    @Autowired
    private SystemParametersMapper systemParametersMapper;

    // 模拟外部系统 baseUrl，默认跟随后端自身端口
    @Value("${external.simulation.base-url:}")
    private String simulationBaseUrl;

    @Value("${server.port:8099}")
    private int serverPort;

    public BillingVouchers createBillingVoucherFromCertificate(PilotageCertificates certificate) {
        BillingVouchers voucher = new BillingVouchers();
        voucher.setCertificateId(certificate.getId());
        voucher.setVoyageId(certificate.getVoyageId());
        voucher.setVesselId(certificate.getVesselId());

        // 设置代理ID（如果有）
        if (certificate.getInboundAgentId() != null) {
            voucher.setAgentId(certificate.getInboundAgentId());
        } else if (certificate.getOutboundAgentId() != null) {
            voucher.setAgentId(certificate.getOutboundAgentId());
        }

        voucher.setStartBerthId(certificate.getStartLocationId());
        voucher.setEndBerthId(certificate.getEndLocationId());

        voucher.setPilotageStartTime(certificate.getPilotageStartTime());
        voucher.setPilotageEndTime(certificate.getPilotageEndTime());

        voucher.setVoucherCode(codeGeneratorUtil.generateVoucherCode());

        voucher.setBillingMethod("STANDARD");
        voucher.setChargeType("PILOTAGE");
        voucher.setAuditStatus("PENDING");
        voucher.setPaymentStatus("UNPAID");
        voucher.setCreatedAt(new Date());
        voucher.setUpdatedAt(new Date());

        this.save(voucher);

        return voucher;
    }

    private void sendPaymentNotificationEmail(BillingVouchers voucher) {
        try {
            Vessels vessel = vesselsService.getById(voucher.getVesselId());
            if (vessel != null && vessel.getManagementCompany() != null && !vessel.getManagementCompany().isEmpty()) {
                // managementCompany字段是String类型，需要转换为Long
                long agentId;
                try {
                    agentId = Long.parseLong(vessel.getManagementCompany());
                } catch (NumberFormatException e) {
                    // 如果无法转换为Long，可能是格式不正确，跳过发送邮件
                    return;
                }

                Agents agent = agentsService.getById(agentId);

                if (agent != null && agent.getEmail() != null && !agent.getEmail().isEmpty()) {
                    String subject = "计费凭证付款通知 - " + voucher.getVoucherCode();
                    String content = buildPaymentNotificationEmailContent(voucher, agent, vessel);

                    messageService.sendEmail(agent.getEmail(), subject, content, true);
                }
            }
        } catch (Exception e) {
            // 记录异常但不中断主流程
            e.printStackTrace();
        }
    }

    private String buildPaymentNotificationEmailContent(BillingVouchers voucher, Agents agent, Vessels vessel) {
        StringBuilder content = new StringBuilder();
        content.append("<h2>计费凭证付款通知</h2>");
        content.append("<p>尊敬的 ").append(agent.getCompanyName()).append("：</p>");
        content.append("<p>您好！</p>");
        content.append("<p>我们已为您公司的船舶 <strong>").append(vessel.getNameZh()).append(" (" + vessel.getNameEn() + ")</strong> 生成了新的计费凭证，请注意查收并按时付款。</p>");

        content.append("<div style=\"margin: 20px 0;\">\n");
        content.append("<table border=\"1\" cellpadding=\"8\" cellspacing=\"0\" style=\"border-collapse: collapse; width: 100%;\">\n");
        content.append("<tr style=\"background-color: #f2f2f2;\">\n");
        content.append("<td colspan=\"2\" style=\"font-weight: bold; text-align: center;\">计费凭证详情</td>\n");
        content.append("</tr>\n");
        content.append("<tr><td>凭证编号</td><td>").append(voucher.getVoucherCode()).append("</td></tr>\n");
        content.append("<tr><td>船舶名称</td><td>").append(vessel.getNameZh()).append(" (" + vessel.getNameEn() + ")</td></tr>\n");
        content.append("<tr><td>收费类型</td><td>").append(voucher.getChargeType()).append("</td></tr>\n");
        content.append("<tr><td>审核状态</td><td>").append(voucher.getAuditStatus()).append("</td></tr>\n");
        content.append("<tr><td>付款状态</td><td>").append(voucher.getPaymentStatus()).append("</td></tr>\n");
        content.append("<tr><td>引航开始时间</td><td>").append(voucher.getPilotageStartTime()).append("</td></tr>\n");
        content.append("<tr><td>引航结束时间</td><td>").append(voucher.getPilotageEndTime()).append("</td></tr>\n");
        content.append("</table>\n");
        content.append("</div>\n");

        content.append("<p>请在收到通知后及时处理相关付款事宜。如有任何疑问，请随时与我们联系。</p>");
        content.append("<p>感谢您的合作！</p>");
        content.append("<p>港口引航调度系统</p>");

        return content.toString();
    }

    public BillingResultVO buildBillingResultVO(BillingVouchers voucher) {
        BillingResultVO resultVO = new BillingResultVO();
        resultVO.setVoucherId(voucher.getId());
        resultVO.setVoucherCode(voucher.getVoucherCode());
        resultVO.setCertificateId(voucher.getCertificateId());
        resultVO.setVesselId(voucher.getVesselId());
        resultVO.setAgentId(voucher.getAgentId());
        resultVO.setBillingMethod(voucher.getBillingMethod());
        resultVO.setAuditStatus(voucher.getAuditStatus() != null ? voucher.getAuditStatus().toString() : null);
        resultVO.setPaymentStatus(voucher.getPaymentStatus() != null ? voucher.getPaymentStatus().toString() : null);
        resultVO.setDueDate(voucher.getDueDate());
        resultVO.setCreatedAt(voucher.getCreatedAt());
        resultVO.setPilotageStartTime(voucher.getPilotageStartTime());
        resultVO.setPilotageEndTime(voucher.getPilotageEndTime());
        resultVO.setInvoiceCode(voucher.getInvoiceCode());
        resultVO.setInvoiceNumber(voucher.getInvoiceNumber());
        if ((resultVO.getInvoiceNumber() == null || resultVO.getInvoiceNumber().trim().isEmpty())
                && (resultVO.getInvoiceCode() != null && !resultVO.getInvoiceCode().trim().isEmpty())) {
            resultVO.setInvoiceNumber(resultVO.getInvoiceCode());
        }
        if (resultVO.getInvoiceNumber() == null || resultVO.getInvoiceNumber().trim().isEmpty()) {
            resultVO.setInvoiceNumber("V" + voucher.getId());
        }

        resultVO.setElectronicInvoiceStatus(voucher.getElectronicInvoiceStatus() != null ? voucher.getElectronicInvoiceStatus().toString() : null);
        resultVO.setElectronicGeneratedAt(voucher.getElectronicGeneratedAt());
        resultVO.setElectronicGeneratedBy(voucher.getElectronicGeneratedBy());
        resultVO.setElectronicSentAt(voucher.getElectronicSentAt());
        resultVO.setElectronicSentBy(voucher.getElectronicSentBy());
        resultVO.setElectronicSentTo(voucher.getElectronicSentTo());
        resultVO.setCheckCode(voucher.getCheckCode());
        resultVO.setInvoiceNotes(voucher.getInvoiceNotes());
        resultVO.setDownloadCount(voucher.getDownloadCount());
        resultVO.setLastDownloadAt(voucher.getLastDownloadAt());

        // 移交信息
        resultVO.setTransferStatus(voucher.getTransferStatus() == null ? null : voucher.getTransferStatus().toString());
        resultVO.setTransferTarget(voucher.getTransferTarget());
        resultVO.setTransferTime(voucher.getTransferTime());

        PilotageCertificates certificate = null;
        if (voucher.getCertificateId() != null) {
            certificate = pilotageCertificatesMapper.selectById(voucher.getCertificateId());
            if (certificate != null) {
                resultVO.setCertificateCode(certificate.getCertificateCode());
                resultVO.setStartLocationName(certificate.getStartLocationName());
                resultVO.setEndLocationName(certificate.getEndLocationName());

                resultVO.setPlanId(certificate.getPlanId());
                if (certificate.getPlanId() != null) {
                    try {
                        PilotagePlans p = pilotagePlansMapper.selectById(certificate.getPlanId());
                        if (p != null) {
                            resultVO.setPlanCode(p.getPlanCode());
                        }
                    } catch (Exception ignore) {
                        // ignore
                    }
                }

                if (resultVO.getPilotageStartTime() == null) {
                    resultVO.setPilotageStartTime(certificate.getPilotageStartTime());
                }
                if (resultVO.getPilotageEndTime() == null) {
                    resultVO.setPilotageEndTime(certificate.getPilotageEndTime());
                }
            }
        }

        if (voucher.getVesselId() != null) {
            Vessels vessel = vesselsService.getById(voucher.getVesselId());
            if (vessel != null) {
                resultVO.setVesselNameZh(vessel.getNameZh());
                resultVO.setVesselNameEn(vessel.getNameEn());
                resultVO.setGrossTonnage(vessel.getGrossTonnage());
                resultVO.setNetTonnage(vessel.getNetTonnage());
            }
        }

        if (voucher.getAgentId() != null) {
            Agents agent = agentsService.getById(voucher.getAgentId());
            if (agent != null) {
                resultVO.setAgentName(agent.getCompanyName());
            }
        }

        if (voucher.getId() != null) {
            LambdaQueryWrapper<ChargeDetails> chargeDetailWrapper = new LambdaQueryWrapper<>();
            chargeDetailWrapper.eq(ChargeDetails::getVoucherId, voucher.getId());
            List<ChargeDetails> chargeDetails = chargeDetailsService.list(chargeDetailWrapper);

            List<BillingResultVO.ChargeDetailVO> chargeDetailVOs = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal baseAmount = BigDecimal.ZERO;
            BigDecimal surchargeAmount = BigDecimal.ZERO;
            BigDecimal discountAmount = BigDecimal.ZERO;

            Set<Long> itemIds = chargeDetails.stream()
                    .map(ChargeDetails::getItemId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, ChargeItems> itemMap = itemIds.isEmpty()
                    ? new HashMap<>()
                    : chargeItemsService.listByIds(itemIds).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(ChargeItems::getId, it -> it, (a, b) -> a));

            Set<Long> categoryIds = itemMap.values().stream()
                    .map(ChargeItems::getCategoryId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, ChargeCategories> categoryMap = categoryIds.isEmpty()
                    ? new HashMap<>()
                    : chargeCategoriesService.listByIds(categoryIds).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(ChargeCategories::getId, c -> c, (a, b) -> a));

            for (ChargeDetails detail : chargeDetails) {
                BillingResultVO.ChargeDetailVO chargeDetailVO = new BillingResultVO.ChargeDetailVO();
                chargeDetailVO.setAmount(detail.getActualChargeAmount());
                chargeDetailVO.setStandardAmount(detail.getStandardChargeAmount());
                chargeDetailVO.setNotes(detail.getNotes());
                chargeDetailVO.setChargeTime(detail.getChargeTime());

                chargeDetailVO.setQuantity(BigDecimal.ONE);
                chargeDetailVO.setUnitPrice(detail.getStandardChargeAmount());

                if (detail.getItemId() != null) {
                    ChargeItems chargeItem = itemMap.get(detail.getItemId());
                    if (chargeItem != null) {
                        chargeDetailVO.setChargeName(chargeItem.getItemName());
                        chargeDetailVO.setDescription(chargeItem.getItemNotes());

                        // 分类名称
                        if (chargeItem.getCategoryId() != null) {
                            ChargeCategories cat = categoryMap.get(chargeItem.getCategoryId());
                            if (cat != null) {
                                chargeDetailVO.setChargeCategory(cat.getCategoryName());
                            }
                        }

                        // 聚合：基础/附加/折扣（优先按分类，其次按名称兜底）
                        BigDecimal amt = detail.getActualChargeAmount() == null ? BigDecimal.ZERO : detail.getActualChargeAmount();
                        String itemName = chargeItem.getItemName() == null ? "" : chargeItem.getItemName();
                        String categoryName = chargeDetailVO.getChargeCategory() == null ? "" : chargeDetailVO.getChargeCategory();

                        boolean isDiscount = isDiscountItem(itemName, categoryName, detail, amt);
                        boolean isSurcharge = !isDiscount && isSurchargeItem(itemName, categoryName);
                        boolean isBasePilotage = !isDiscount && !isSurcharge && isPilotageBaseItem(itemName, categoryName);

                        if (isDiscount) {
                            // 折扣金额建议输出为正值（表示减免额度）
                            discountAmount = discountAmount.add(amt.abs());
                        } else if (isSurcharge) {
                            surchargeAmount = surchargeAmount.add(amt);
                        } else if (isBasePilotage) {
                            baseAmount = baseAmount.add(amt);
                        }
                    }
                }

                chargeDetailVOs.add(chargeDetailVO);

                // 累计总金额
                if (detail.getActualChargeAmount() != null) {
                    totalAmount = totalAmount.add(detail.getActualChargeAmount());
                }
            }

            resultVO.setChargeDetails(chargeDetailVOs);
            resultVO.setTotalAmount(totalAmount);
            resultVO.setBaseAmount(baseAmount);
            resultVO.setSurchargeAmount(surchargeAmount);
            resultVO.setDiscountAmount(discountAmount);
        }

        return resultVO;
    }

        private boolean isDiscountItem(String itemName, String categoryName, ChargeDetails detail, BigDecimal amount) {
        if (detail != null && detail.getDiscountReason() != null && !detail.getDiscountReason().trim().isEmpty()) {
            return true;
        }
        // 金额为负通常代表减免/折扣
        if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
            return true;
        }
        String n = (itemName == null ? "" : itemName);
        String c = (categoryName == null ? "" : categoryName);
        return n.contains("折扣") || n.contains("减免") || n.contains("优惠") || n.contains("减收")
                || c.contains("折扣") || c.contains("减免") || c.contains("优惠");
    }

        private boolean isSurchargeItem(String itemName, String categoryName) {
        String n = (itemName == null ? "" : itemName);
        String c = (categoryName == null ? "" : categoryName);
        if (c.contains("附加") || c.contains("加收")) {
            return true;
        }
        return n.contains("附加") || n.contains("加收") || n.contains("夜") || n.contains("节假日") || n.contains("周末") || n.contains("天气");
    }

        private boolean isPilotageBaseItem(String itemName, String categoryName) {
        String n = (itemName == null ? "" : itemName);
        String c = (categoryName == null ? "" : categoryName);
        if (c.contains("引航")) {
            // 分类是引航类且不是附加/折扣，则归为基础
            return true;
        }
        return n.contains("引航") && !n.contains("附加") && !n.contains("加收");
    }

    @Override
    public List<BillingVouchers> listApprovedVouchersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        LambdaQueryWrapper<BillingVouchers> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BillingVouchers::getAuditStatus, "APPROVED"); // 只查询已审核的凭证
        // 约定：endDate 为“结束时间的开区间”，即 createdAt < endDate（避免 23:59:59.999... 边界问题）
        queryWrapper.ge(BillingVouchers::getCreatedAt, startDate);
        queryWrapper.lt(BillingVouchers::getCreatedAt, endDate);
        return this.list(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getFeeBreakdownByType() {
        return billingVouchersMapper.getFeeBreakdownByType();
    }

    @Override
    public List<Map<String, Object>> getFeeTrendByMonth() {
        return billingVouchersMapper.getFeeTrendByMonth();
    }

    @Override
    public BigDecimal getCurrentPeriodTotal() {
        return billingVouchersMapper.getCurrentPeriodTotal();
    }

    @Override
    public BigDecimal getPreviousPeriodTotal() {
        return billingVouchersMapper.getPreviousPeriodTotal();
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        return billingVouchersMapper.getCategoryStats();
    }

    @Override
    public boolean approve(BillingApprovalRequest request) {

        List<Long> ids = request.getIds();
        Boolean approved = request.getApproved();
        String rejectionReason = request.getRejectionReason();

        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要审核的计费凭证");
        }

        for (Long id : ids) {
            BillingVouchers voucher = this.getById(id);
            if (voucher == null) {
                throw new BusinessException(ErrorCode.BILLING_NOT_FOUND, "计费凭证不存在: " + id);
            }

            String oldStatus = voucher.getAuditStatus() != null ? voucher.getAuditStatus().toString() : null;
            voucher.setAuditStatus(approved ? "APPROVED" : "REJECTED");
            if (!approved && rejectionReason != null) {
                voucher.setCancellationNotes(rejectionReason);
            }
            voucher.setUpdatedAt(new Date());

            boolean result = this.updateById(voucher);
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "审核计费凭证失败: " + id);
            }

            if (!approved) {
                if (voucher.getCertificateId() != null) {
                    PilotageCertificates certificate = pilotageCertificatesMapper.selectById(voucher.getCertificateId());
                    if (certificate != null) {
                        certificate.setBillingStatus("REBILLING");
                        certificate.setUpdatedAt(new Date());
                        pilotageCertificatesMapper.updateById(certificate);
                    }
                }
            }

            if (approved && !"APPROVED".equals(oldStatus)) {  // 仅当状态从非APPROVED变为APPROVED时发送邮件
                sendPaymentNotificationEmail(voucher);

                // 审核通过后，向“模拟外部系统”发送付款提示（外部系统随机10秒内付款并回调）
                sendPaymentNoticeToSimulation(voucher);
            }
        }
        return true;
    }

    @Override
    public boolean retryPaymentNotice(Long voucherId) {
        if (voucherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "voucherId不能为空");
        }
        BillingVouchers voucher = this.getById(voucherId);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND, "计费凭证不存在: " + voucherId);
        }
        // 仅允许对“审核通过&未付款”的凭证重试（符合业务：待付款）
        if (!"APPROVED".equals(voucher.getAuditStatus()) || !"UNPAID".equals(voucher.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "仅支持对审核通过且未付款的凭证重试付款提示");
        }
        return sendPaymentNoticeToSimulation(voucher);
    }

    @Override
    public PageResult<BillingResultVO> pageBillingVouchers(PageQueryDTO query) {
        Page<BillingVouchers> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<BillingVouchers> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BillingVouchers::getCreatedAt);

        IPage<BillingVouchers> result = this.page(page, wrapper);

        List<BillingResultVO> voList = result.getRecords().stream()
                .map(this::buildBillingResultVO)
                .collect(Collectors.toList());
        return PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                voList
        );
    }

        private boolean sendPaymentNoticeToSimulation(BillingVouchers voucher) {
        Long voucherId = voucher.getId();
        String baseUrl = (simulationBaseUrl == null || simulationBaseUrl.trim().isEmpty())
                ? ("http://localhost:" + serverPort)
                : simulationBaseUrl.trim();
        String simulationUrl = baseUrl + "/api/billing/simulation/notifyPayment";

        InvoiceOperationLogs logEntity = new InvoiceOperationLogs();
        logEntity.setVoucherId(voucherId);
        // DB enum: GENERATE/SEND/DOWNLOAD/VIEW/RED/VOID/PRINT/OTHER
        logEntity.setOperationType("OTHER");
        logEntity.setOperationBy(1L);
        logEntity.setOperationTime(new Date());
        logEntity.setInvoiceNo(voucher.getInvoiceNumber());
        logEntity.setCreatedAt(new Date());

        try {
            PaymentNoticeRequest notice = new PaymentNoticeRequest();
            notice.setIds(java.util.List.of(voucherId));
            notice.setAmount(voucher.getTotalAmount());

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PaymentNoticeRequest> requestEntity = new HttpEntity<>(notice, headers);

            ResponseEntity<syy.portPilotage.common.Result> resp =
                    restTemplate.postForEntity(simulationUrl, requestEntity, syy.portPilotage.common.Result.class);

            boolean ok = resp.getStatusCode().is2xxSuccessful();
            logEntity.setResult(ok ? "SUCCESS" : "FAILED");
            logEntity.setOperationDetails(toJson(Map.of(
                    "action", "PAYMENT_NOTICE",
                    "method", "POST",
                    "url", simulationUrl,
                    "status", String.valueOf(resp.getStatusCode())
            )));
            if (!ok) {
                logEntity.setErrorMessage("HTTP " + resp.getStatusCode());
                log.warn("发送付款提示到模拟外部系统失败: voucherId={}, status={}", voucherId, resp.getStatusCode());
            }
            invoiceOperationLogsService.insertSelective(logEntity);
            return ok;
        } catch (Exception e) {
            // 模拟外部系统不可用不应阻断主流程，但需要可追踪、可重试
            logEntity.setResult("FAILED");
            logEntity.setOperationDetails(toJson(Map.of(
                    "action", "PAYMENT_NOTICE",
                    "method", "POST",
                    "url", simulationUrl
            )));
            logEntity.setErrorMessage(e.getMessage());
            try {
                invoiceOperationLogsService.insertSelective(logEntity);
            } catch (Exception ignore) {
            }
            log.warn("发送付款提示到模拟外部系统异常（可忽略或检查 external.simulation.base-url 配置/服务是否启动）: voucherId={}",
                    voucherId, e);
            return false;
        }
    }

    @Override
    public BillingResultVO confirmPayment(Long id, BigDecimal receivedAmount, String receiptDate) {
        BillingVouchers voucher = this.getById(id);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND);
        }

        if (!"APPROVED".equals(voucher.getAuditStatus()) || !"UNPAID".equals(voucher.getPaymentStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "只有审核通过且未付款的计费凭证才能确认到款");
        }

        Date parsedReceiptDate;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            parsedReceiptDate = dateFormat.parse(receiptDate);
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "日期格式不正确，应为 yyyy-MM-dd 格式");
        }

        voucher.setActualReceivedAmount(receivedAmount);
        voucher.setReceiptDate(parsedReceiptDate);
        voucher.setPaymentStatus("FULLY_PAID");
        voucher.setUpdatedAt(new Date());

        this.updateById(voucher);

        BillingResultVO resultVO = this.buildBillingResultVO(voucher);
        return resultVO;
    }

        @Override
    public BillingResultVO handlePaymentCallback(PaymentCallbackRequest request) {
        if (request == null || request.getVoucherId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "voucherId不能为空");
        }

        BillingVouchers voucher = this.getById(request.getVoucherId());
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND, "计费凭证不存在: " + request.getVoucherId());
        }

        if (!"APPROVED".equals(voucher.getAuditStatus() != null ? voucher.getAuditStatus().toString() : null)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "只有审核通过的计费凭证才能处理付款回调");
        }

        if ("FULLY_PAID".equals(voucher.getPaymentStatus() != null ? voucher.getPaymentStatus().toString() : null)) {
            return this.buildBillingResultVO(voucher);
        }

        BigDecimal receivedAmount = request.getReceivedAmount();
        if (receivedAmount == null) {
            receivedAmount = voucher.getTotalAmount() == null ? BigDecimal.ZERO : voucher.getTotalAmount();
        }

        Date paidAt = request.getPaidAt() == null ? new Date() : request.getPaidAt();

        voucher.setActualReceivedAmount(receivedAmount);
        voucher.setReceiptDate(paidAt);
        voucher.setPaymentStatus("FULLY_PAID");
        voucher.setUpdatedAt(new Date());
        this.updateById(voucher);

        createTaxRecordsForVoucherIfAbsent(voucher);
        generateElectronicInvoiceIfNeeded(voucher);

        return this.buildBillingResultVO(this.getById(voucher.getId()));
    }

    @Override
    public BillingResultVO downloadElectronicInvoice(Long voucherId) {
        if (voucherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "voucherId不能为空");
        }
        BillingVouchers voucher = this.getById(voucherId);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND, "计费凭证不存在: " + voucherId);
        }

        generateElectronicInvoiceIfNeeded(voucher);

        Integer cnt = voucher.getDownloadCount() == null ? 0 : voucher.getDownloadCount();
        voucher.setDownloadCount(cnt + 1);
        voucher.setLastDownloadAt(new Date());
        voucher.setUpdatedAt(new Date());
        this.updateById(voucher);

        try {
            InvoiceOperationLogs logEntity = new InvoiceOperationLogs();
            logEntity.setVoucherId(voucher.getId());
            logEntity.setOperationType("DOWNLOAD");
            logEntity.setOperationBy(1L);
            logEntity.setOperationTime(new Date());
            logEntity.setOperationDetails(toJson(Map.of(
                    "action", "DOWNLOAD_E_INVOICE",
                    "downloadCount", voucher.getDownloadCount()
            )));
            logEntity.setResult("SUCCESS");
            logEntity.setCreatedAt(new Date());
            invoiceOperationLogsService.insertSelective(logEntity);
        } catch (Exception e) {
            log.warn("写入电子发票下载日志失败: voucherId={}", voucher.getId(), e);
        }

        return this.buildBillingResultVO(this.getById(voucher.getId()));
    }

    @Override
    public BillingResultVO sendElectronicInvoice(Long voucherId) {
        if (voucherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "voucherId不能为空");
        }
        BillingVouchers voucher = this.getById(voucherId);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND, "计费凭证不存在: " + voucherId);
        }

        String currentStatus = voucher.getElectronicInvoiceStatus() == null ? null : voucher.getElectronicInvoiceStatus().toString();
        if ("RED".equals(currentStatus) || "VOIDED".equals(currentStatus)) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, "已红冲/已作废的电子发票不允许发送");
        }

        generateElectronicInvoiceIfNeeded(voucher);

        // 找代理邮箱
        String toEmail = null;
        try {
            if (voucher.getAgentId() != null) {
                Agents agent = agentsService.getById(voucher.getAgentId());
                if (agent != null && agent.getEmail() != null && !agent.getEmail().trim().isEmpty()) {
                    toEmail = agent.getEmail().trim();
                }
            }
        } catch (Exception ignore) {}

        voucher.setElectronicInvoiceStatus("SENT");
        voucher.setElectronicSentAt(new Date());
        voucher.setElectronicSentBy(1L);
        voucher.setElectronicSentTo(toEmail);
        voucher.setUpdatedAt(new Date());
        this.updateById(voucher);

        // 邮件通知（如果有邮箱）
        if (toEmail != null) {
            try {
                String subject = "电子发票已生成 - " + voucher.getVoucherCode();
                String content = "<h3>电子发票已生成</h3>"
                        + "<p>计费凭证编号: <b>" + voucher.getVoucherCode() + "</b></p>"
                        + "<p>校验码: <b>" + (voucher.getCheckCode() == null ? "-" : voucher.getCheckCode()) + "</b></p>"
                        + "<p>提示：本系统为模拟电子发票流程，下载请在系统内完成。</p>";
                messageService.sendEmail(toEmail, subject, content, true);
            } catch (Exception e) {
                log.warn("发送电子发票邮件失败: voucherId={}, to={}", voucher.getId(), toEmail, e);
            }
        }

        try {
            InvoiceOperationLogs logEntity = new InvoiceOperationLogs();
            logEntity.setVoucherId(voucher.getId());
            // operation_type 为 ENUM：只能写入既有枚举值；重发通过 details 区分
            logEntity.setOperationType("SEND");
            logEntity.setOperationBy(1L);
            logEntity.setOperationTime(new Date());
            logEntity.setInvoiceNo(voucher.getInvoiceNumber()); // 兼容字段（电子发票无纸票号）
            logEntity.setOperationDetails(toJson(Map.of(
                    "action", "SENT".equals(currentStatus) ? "RESEND_E_INVOICE" : "SEND_E_INVOICE",
                    "to", toEmail == null ? "" : toEmail
            )));
            logEntity.setResult("SUCCESS");
            logEntity.setCreatedAt(new Date());
            invoiceOperationLogsService.insertSelective(logEntity);
        } catch (Exception e) {
            log.warn("写入电子发票发送日志失败: voucherId={}", voucher.getId(), e);
        }

        return this.buildBillingResultVO(this.getById(voucher.getId()));
    }

    private void createTaxRecordsForVoucherIfAbsent(BillingVouchers voucher) {
        if (voucher == null || voucher.getId() == null) return;

        LambdaQueryWrapper<TaxRecords> taxRecordWrapper = new LambdaQueryWrapper<>();
        taxRecordWrapper.eq(TaxRecords::getVoucherId, voucher.getId());
        if (taxRecordsService.count(taxRecordWrapper) > 0) {
            return;
        }

        TaxRecords taxRecord = new TaxRecords();
        taxRecord.setTaxRecordCode("TAX-" + System.currentTimeMillis());
        taxRecord.setVoucherId(voucher.getId());
        taxRecord.setBillingVoucherCode(voucher.getVoucherCode());
        taxRecord.setVoyageId(voucher.getVoyageId());
        taxRecord.setVesselId(voucher.getVesselId());
        taxRecord.setAgentId(voucher.getAgentId());
        taxRecord.setTaxableAmount(voucher.getTotalAmount());
        taxRecord.setTotalTaxAmount(voucher.getTotalAmount() == null ? BigDecimal.ZERO : voucher.getTotalAmount().multiply(BigDecimal.valueOf(0.09)));
        taxRecord.setTaxPeriod(new Date());
        taxRecord.setTaxCalculationTime(new Date());
        taxRecord.setTaxPolicyCode("POLICY-001");
        taxRecord.setTaxPolicyName("标准税率政策");
        taxRecord.setTaxStatus("CALCULATED");
        taxRecord.setCalculationMethod("AUTO");
        taxRecord.setDataSource("AUTO");
        taxRecord.setIsActive(1);
        taxRecord.setCreatedAt(new Date());
        taxRecord.setUpdatedAt(new Date());
        taxRecordsService.save(taxRecord);

        // 主税：增值税（示例税率 9%）
        TaxItemDetails vatItem = new TaxItemDetails();
        vatItem.setTaxRecordId(taxRecord.getId());
        vatItem.setTaxItemCode("VAT");
        vatItem.setTaxItemName("增值税");
        vatItem.setTaxType("MAIN");
        vatItem.setTaxRate(BigDecimal.valueOf(0.09));
        vatItem.setTaxableBase(voucher.getTotalAmount());
        vatItem.setTaxAmount(voucher.getTotalAmount() == null ? BigDecimal.ZERO : voucher.getTotalAmount().multiply(BigDecimal.valueOf(0.09)));
        vatItem.setTaxPolicyReference("POLICY-001");
        vatItem.setIsPreferential(0);
        vatItem.setIsAdjusted(0);
        vatItem.setSortOrder(1);
        vatItem.setCreatedAt(new Date());
        vatItem.setUpdatedAt(new Date());
        taxItemDetailsService.save(vatItem);

        TaxItemDetails additionalItem = new TaxItemDetails();
        additionalItem.setTaxRecordId(taxRecord.getId());
        additionalItem.setTaxItemCode("ADDITIONAL_TAX");
        additionalItem.setTaxItemName("城建税");
        additionalItem.setTaxType("ADDITIONAL");
        additionalItem.setTaxRate(BigDecimal.valueOf(0.07));
        BigDecimal vatAmt = voucher.getTotalAmount() == null ? BigDecimal.ZERO : voucher.getTotalAmount().multiply(BigDecimal.valueOf(0.09));
        additionalItem.setTaxableBase(vatAmt);
        additionalItem.setTaxAmount(vatAmt.multiply(BigDecimal.valueOf(0.07)));
        additionalItem.setTaxPolicyReference("POLICY-002");
        additionalItem.setIsPreferential(0);
        additionalItem.setIsAdjusted(0);
        additionalItem.setSortOrder(2);
        additionalItem.setCreatedAt(new Date());
        additionalItem.setUpdatedAt(new Date());
        taxItemDetailsService.save(additionalItem);
    }

        private void generateElectronicInvoiceIfNeeded(BillingVouchers voucher) {
        if (voucher == null) return;

        String status = voucher.getElectronicInvoiceStatus() == null ? null : voucher.getElectronicInvoiceStatus().toString();
        // 红冲/作废后不允许“自动补生成”，否则会出现“冲红后过一段时间又自动生成新票”的错觉
        if ("GENERATED".equals(status) || "SENT".equals(status) || "RED".equals(status) || "VOIDED".equals(status)) {
            return;
        }

        Date genAt = new Date();
        voucher.setElectronicInvoiceStatus("GENERATED");
        voucher.setElectronicGeneratedAt(genAt);
        voucher.setElectronicGeneratedBy(1L);
        if (voucher.getInvoiceCode() == null || voucher.getInvoiceCode().trim().isEmpty()) {
            voucher.setInvoiceCode("EINV" + String.format("%08d", voucher.getId()));
        }
        if (voucher.getInvoiceNumber() == null || voucher.getInvoiceNumber().trim().isEmpty()) {
            voucher.setInvoiceNumber(String.format("%08d", Math.abs((int) (System.currentTimeMillis() % 100000000))));
        }
        voucher.setCheckCode("CHK-" + voucher.getId() + "-" + (System.currentTimeMillis() % 100000));
        if (voucher.getDownloadCount() == null) {
            voucher.setDownloadCount(0);
        }

        if (voucher.getSnapshotTakenAt() == null) {
            voucher.setSnapshotTakenAt(genAt);
        }
        if (voucher.getSnapshotVersion() == null || voucher.getSnapshotVersion().trim().isEmpty()) {
            voucher.setSnapshotVersion("1.0");
        }
        if (voucher.getVesselSnapshot() == null || voucher.getVesselSnapshot().trim().isEmpty()) {
            try {
                Vessels vessel = voucher.getVesselId() == null ? null : vesselsService.getById(voucher.getVesselId());
                Map<String, Object> m = new LinkedHashMap<>();
                if (vessel != null) {
                    m.put("name_zh", vessel.getNameZh());
                    m.put("name_en", vessel.getNameEn());
                    m.put("call_sign", vessel.getVesselCallSign());
                    m.put("imo_number", vessel.getImoNumber());
                    m.put("vessel_type", vessel.getVesselType());
                    m.put("nationality", vessel.getNationality());
                    m.put("gross_tonnage", vessel.getGrossTonnage());
                    m.put("net_tonnage", vessel.getNetTonnage());
                }
                m.put("snapshot_time", genAt);
                voucher.setVesselSnapshot(JSON.toJSONString(m));
            } catch (Exception ignore) {}
        }
        if (voucher.getVoyageSnapshot() == null || voucher.getVoyageSnapshot().trim().isEmpty()) {
            try {
                Voyages voy = voucher.getVoyageId() == null ? null : voyagesService.getById(voucher.getVoyageId());
                Map<String, Object> m = new LinkedHashMap<>();
                if (voy != null) {
                    m.put("voyage_number", voy.getVoyageNumber());
                    m.put("voyage_type", voy.getVoyageType() != null ? voy.getVoyageType().toString() : null);
                    m.put("eta", voy.getEta());
                    m.put("ata", voy.getAta());
                    m.put("previous_port", voy.getPreviousPort());
                    m.put("next_port", voy.getNextPort());
                    m.put("cargo_name", voy.getCargoName());
                    m.put("cargo_quantity", voy.getCargoQuantity());
                }
                m.put("snapshot_time", genAt);
                voucher.setVoyageSnapshot(JSON.toJSONString(m));
            } catch (Exception ignore) {}
        }
        if (voucher.getAgentSnapshot() == null || voucher.getAgentSnapshot().trim().isEmpty()) {
            try {
                Agents agent = voucher.getAgentId() == null ? null : agentsService.getById(voucher.getAgentId());
                Map<String, Object> m = new LinkedHashMap<>();
                if (agent != null) {
                    m.put("agent_code", agent.getAgentCode());
                    m.put("company_name", agent.getCompanyName());
                    m.put("short_name", agent.getShortName());
                    m.put("contact_person", agent.getContactPerson());
                    m.put("phone", agent.getPhone());
                    m.put("taxpayer_id", agent.getDefaultTaxpayerId());
                }
                m.put("snapshot_time", genAt);
                voucher.setAgentSnapshot(JSON.toJSONString(m));
            } catch (Exception ignore) {}
        }
        if (voucher.getPilotageSnapshot() == null || voucher.getPilotageSnapshot().trim().isEmpty()) {
            try {
                PilotageCertificates certificate = voucher.getCertificateId() == null ? null : pilotageCertificatesMapper.selectById(voucher.getCertificateId());
                Map<String, Object> m = new LinkedHashMap<>();
                if (certificate != null) {
                    m.put("certificate_code", certificate.getCertificateCode());
                    m.put("pilotage_type", certificate.getPilotageType());
                    m.put("start_location_name", certificate.getStartLocationName());
                    m.put("end_location_name", certificate.getEndLocationName());
                    m.put("pilotage_start_time", certificate.getPilotageStartTime());
                    m.put("pilotage_end_time", certificate.getPilotageEndTime());
                }
                m.put("snapshot_time", genAt);
                voucher.setPilotageSnapshot(JSON.toJSONString(m));
            } catch (Exception ignore) {}
        }

        voucher.setUpdatedAt(new Date());
        this.updateById(voucher);

        try {
            InvoiceOperationLogs logEntity = new InvoiceOperationLogs();
            logEntity.setVoucherId(voucher.getId());
            logEntity.setOperationType("GENERATE");
            logEntity.setOperationBy(1L);
            logEntity.setOperationTime(new Date());
            logEntity.setOperationDetails(toJson(Map.of(
                    "action", "GENERATE_E_INVOICE",
                    "checkCode", voucher.getCheckCode()
            )));
            logEntity.setResult("SUCCESS");
            logEntity.setCreatedAt(new Date());
            invoiceOperationLogsService.insertSelective(logEntity);
        } catch (Exception e) {
            // 不影响主流程
            log.warn("写入电子发票操作日志失败: voucherId={}", voucher.getId(), e);
        }
    }

    @Override
    public Map<String, Object> getElectronicInvoiceDetail(Long voucherId) {
        if (voucherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "voucherId不能为空");
        }
        BillingVouchers voucher = this.getById(voucherId);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND, "计费凭证不存在: " + voucherId);
        }

        generateElectronicInvoiceIfNeeded(voucher);
        voucher = this.getById(voucherId);

        Date snapshotAt = voucher.getSnapshotTakenAt() != null ? voucher.getSnapshotTakenAt() : voucher.getElectronicGeneratedAt();
        boolean snapshotAvailable = (voucher.getVesselSnapshot() != null && !voucher.getVesselSnapshot().trim().isEmpty())
                || (voucher.getVoyageSnapshot() != null && !voucher.getVoyageSnapshot().trim().isEmpty())
                || (voucher.getAgentSnapshot() != null && !voucher.getAgentSnapshot().trim().isEmpty())
                || (voucher.getPilotageSnapshot() != null && !voucher.getPilotageSnapshot().trim().isEmpty());

        Map<String, Object> invoiceInfo = new LinkedHashMap<>();
        invoiceInfo.put("voucher_code", voucher.getVoucherCode());
        invoiceInfo.put("invoice_code", voucher.getInvoiceCode());
        invoiceInfo.put("invoice_number", voucher.getInvoiceNumber());
        invoiceInfo.put("invoice_time", voucher.getElectronicGeneratedAt());
        invoiceInfo.put("total_amount", voucher.getTotalAmount());
        invoiceInfo.put("check_code", voucher.getCheckCode());
        invoiceInfo.put("electronic_invoice_status", voucher.getElectronicInvoiceStatus() == null ? null : voucher.getElectronicInvoiceStatus().toString());
        invoiceInfo.put("snapshot_taken_at", snapshotAt);
        invoiceInfo.put("snapshot_version", voucher.getSnapshotVersion());

        Map<String, Object> snapshotInfo = new LinkedHashMap<>();
        snapshotInfo.put("is_snapshot_available", snapshotAvailable);
        snapshotInfo.put("snapshot_time", snapshotAt);
        snapshotInfo.put("warning", snapshotAvailable ? "以下信息为发票生成时的快照，不会随基础信息变更而改变" : "暂无快照信息（系统将展示当前基础信息）");

        Map<String, Object> vesselInfo = parseJsonMapOrEmpty(voucher.getVesselSnapshot());
        Map<String, Object> voyageInfo = parseJsonMapOrEmpty(voucher.getVoyageSnapshot());
        Map<String, Object> agentInfo = parseJsonMapOrEmpty(voucher.getAgentSnapshot());
        Map<String, Object> pilotageInfo = parseJsonMapOrEmpty(voucher.getPilotageSnapshot());

        // 当前信息（用于对比）
        Map<String, Object> currentInfo = new LinkedHashMap<>();
        try {
            Vessels vessel = voucher.getVesselId() == null ? null : vesselsService.getById(voucher.getVesselId());
            Voyages voy = voucher.getVoyageId() == null ? null : voyagesService.getById(voucher.getVoyageId());
            currentInfo.put("vessel_name_zh", vessel != null ? vessel.getNameZh() : null);
            currentInfo.put("voyage_number", voy != null ? voy.getVoyageNumber() : null);
            currentInfo.put("warning", "当前基础信息可能与发票生成时不同");
        } catch (Exception ignore) {}

        Map<String, Object> buyerInfo = new LinkedHashMap<>();
        try {
            AgentInvoiceInfo ai = agentInvoiceInfoService.lambdaQuery()
                    .eq(AgentInvoiceInfo::getBillingVoucherId, voucherId)
                    .last("limit 1")
                    .one();
            if (ai != null) {
                buyerInfo.put("company_name", ai.getPayingUnitName() != null ? ai.getPayingUnitName() : ai.getAgentName());
                buyerInfo.put("taxpayer_id", ai.getTaxpayerId());
                buyerInfo.put("address", ai.getPayingUnitAddress());
                buyerInfo.put("phone", ai.getPayingUnitPhone());
                buyerInfo.put("bank", ai.getPayingUnitBank());
                buyerInfo.put("bank_account", ai.getPayingUnitBankAccount());
            } else {
                Agents agent = voucher.getAgentId() == null ? null : agentsService.getById(voucher.getAgentId());
                if (agent != null) {
                    buyerInfo.put("company_name", agent.getCompanyName());
                    buyerInfo.put("taxpayer_id", agent.getDefaultTaxpayerId());
                    buyerInfo.put("address", agent.getDefaultAddress() != null ? agent.getDefaultAddress() : agent.getAddress());
                    buyerInfo.put("phone", agent.getPhone());
                    buyerInfo.put("bank", agent.getDefaultBankName());
                    buyerInfo.put("bank_account", agent.getDefaultBankAccount());
                }
            }
        } catch (Exception ignore) {}

        // 卖方信息：从 system_parameters 按组读取（你可在表里维护）
        Map<String, Object> sellerInfo = getSellerInfoFromSystemParams();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("invoice_info", invoiceInfo);
        result.put("snapshot_info", snapshotInfo);
        result.put("vessel_info", vesselInfo);
        result.put("voyage_info", voyageInfo);
        result.put("agent_info", agentInfo);
        result.put("pilotage_info", pilotageInfo);
        result.put("buyer_info", buyerInfo);
        result.put("seller_info", sellerInfo);
        result.put("current_info", currentInfo);
        return result;
    }

    @Override
    public byte[] downloadElectronicInvoicePdf(Long voucherId) {
        if (voucherId == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "voucherId不能为空");
        }
        BillingVouchers voucher = this.getById(voucherId);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.BILLING_NOT_FOUND, "计费凭证不存在: " + voucherId);
        }
        generateElectronicInvoiceIfNeeded(voucher);

        Map<String, Object> detail = getElectronicInvoiceDetail(voucherId);
        byte[] pdfBytes = buildInvoicePdf(detail);

        BillingVouchers latest = this.getById(voucherId);
        Integer cnt = latest.getDownloadCount() == null ? 0 : latest.getDownloadCount();
        latest.setDownloadCount(cnt + 1);
        latest.setLastDownloadAt(new Date());
        latest.setUpdatedAt(new Date());
        this.updateById(latest);
        try {
            InvoiceOperationLogs logEntity = new InvoiceOperationLogs();
            logEntity.setVoucherId(latest.getId());
            logEntity.setOperationType("DOWNLOAD");
            logEntity.setOperationBy(1L);
            logEntity.setOperationTime(new Date());
            logEntity.setOperationDetails(toJson(Map.of(
                    "action", "DOWNLOAD_E_INVOICE_PDF",
                    "downloadCount", latest.getDownloadCount()
            )));
            logEntity.setResult("SUCCESS");
            logEntity.setCreatedAt(new Date());
            invoiceOperationLogsService.insertSelective(logEntity);
        } catch (Exception e) {
            log.warn("写入电子发票PDF下载日志失败: voucherId={}", latest.getId(), e);
        }
        return pdfBytes;
    }

    private Map<String, Object> parseJsonMapOrEmpty(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return new LinkedHashMap<>();
        try {
            Object obj = JSON.parse(jsonStr);
            if (obj instanceof Map) {
                //noinspection unchecked
                return new LinkedHashMap<>((Map<String, Object>) obj);
            }
        } catch (Exception ignore) {}
        return new LinkedHashMap<>();
    }

    private Map<String, Object> getSellerInfoFromSystemParams() {
        Map<String, Object> m = new LinkedHashMap<>();
        try {
            // 约定参数组：INVOICE_SELLER
            List<SystemParameters> list = systemParametersMapper.selectByGroup("INVOICE_SELLER");
            if (list != null) {
                for (SystemParameters p : list) {
                    if (p == null || p.getParamKey() == null) continue;
                    String k = p.getParamKey().trim().toLowerCase();
                    String v = p.getParamValue();
                    switch (k) {
                        case "company_name" -> m.put("company_name", v);
                        case "taxpayer_id" -> m.put("taxpayer_id", v);
                        case "address" -> m.put("address", v);
                        case "phone" -> m.put("phone", v);
                        case "bank" -> m.put("bank", v);
                        case "bank_account" -> m.put("bank_account", v);
                        default -> {
                        }
                    }
                }
            }
        } catch (Exception ignore) {}

        m.putIfAbsent("company_name", "");
        m.putIfAbsent("taxpayer_id", "");
        m.putIfAbsent("address", "");
        m.putIfAbsent("phone", "");
        m.putIfAbsent("bank", "");
        m.putIfAbsent("bank_account", "");

        // 若为空，给出配置提示（用户可在“系统管理-参数配置”维护）
        boolean missing = isBlank(m.get("company_name"))
                || isBlank(m.get("taxpayer_id"))
                || isBlank(m.get("address"))
                || isBlank(m.get("phone"))
                || isBlank(m.get("bank"))
                || isBlank(m.get("bank_account"));
        if (missing) {
            m.put("config_hint", "销方信息未配置：请到【系统管理-参数配置】维护参数组 INVOICE_SELLER（company_name/taxpayer_id/address/phone/bank/bank_account）");
            // 对空字段用“未配置”占位，保证页面与PDF能看出来缺什么
            if (isBlank(m.get("company_name"))) m.put("company_name", "未配置");
            if (isBlank(m.get("taxpayer_id"))) m.put("taxpayer_id", "未配置");
            if (isBlank(m.get("address"))) m.put("address", "未配置");
            if (isBlank(m.get("phone"))) m.put("phone", "未配置");
            if (isBlank(m.get("bank"))) m.put("bank", "未配置");
            if (isBlank(m.get("bank_account"))) m.put("bank_account", "未配置");
        }
        return m;
    }

    private boolean isBlank(Object v) {
        return v == null || String.valueOf(v).trim().isEmpty();
    }

    private byte[] buildInvoicePdf(Map<String, Object> detail) {
        // iText 在某些字体缺字形时可能抛 NPE（Glyph 为 null）。这里做两层兜底：
        try {
            return buildInvoicePdfWithFont(detail, resolveCjkFont(), false);
        } catch (Exception e) {
            log.warn("生成电子发票PDF失败，尝试降级生成(ASCII+Helvetica)。原因: {}", e.getMessage(), e);
            try {
                PdfFont fallback = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                return buildInvoicePdfWithFont(detail, fallback, true);
            } catch (Exception e2) {
                // 最终兜底：把原异常抛出，交给全局异常处理
                throw new RuntimeException("生成电子发票PDF失败", e2);
            }
        }
    }

    private byte[] buildInvoicePdfWithFont(Map<String, Object> detail, PdfFont font, boolean asciiOnly) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(30, 30, 30, 30);

        if (font != null) {
            doc.setFont(font);
        }

        doc.add(new Paragraph(sanitizeForFont("电子发票（系统模拟）", font, asciiOnly))
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER));

        @SuppressWarnings("unchecked")
        Map<String, Object> invoiceInfo = (Map<String, Object>) detail.getOrDefault("invoice_info", new LinkedHashMap<>());
        Table t = new Table(UnitValue.createPercentArray(new float[]{28, 72})).useAllAvailableWidth();
        addRow(t, font, asciiOnly, "计费凭证编号", safeStr(invoiceInfo.get("voucher_code")));
        addRow(t, font, asciiOnly, "发票代码", safeStr(invoiceInfo.get("invoice_code")));
        addRow(t, font, asciiOnly, "发票号码", safeStr(invoiceInfo.get("invoice_number")));
        addRow(t, font, asciiOnly, "开票时间", formatDate(invoiceInfo.get("invoice_time")));
        addRow(t, font, asciiOnly, "金额合计", formatMoney(invoiceInfo.get("total_amount")));
        addRow(t, font, asciiOnly, "校验码", safeStr(invoiceInfo.get("check_code")));
        addRow(t, font, asciiOnly, "电子发票状态", safeStr(invoiceInfo.get("electronic_invoice_status")));
        doc.add(t);

        // 船舶信息
        doc.add(new Paragraph(sanitizeForFont("\n船舶信息（快照）", font, asciiOnly)).setFontSize(12));
        @SuppressWarnings("unchecked")
        Map<String, Object> vesselInfo = (Map<String, Object>) detail.getOrDefault("vessel_info", new LinkedHashMap<>());
        Table tv = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
        addRow(tv, font, asciiOnly, "中文船名", safeStr(vesselInfo.get("name_zh")));
        addRow(tv, font, asciiOnly, "英文船名", safeStr(vesselInfo.get("name_en")));
        addRow(tv, font, asciiOnly, "呼号", safeStr(vesselInfo.get("call_sign")));
        addRow(tv, font, asciiOnly, "IMO", safeStr(vesselInfo.get("imo_number")));
        addRow(tv, font, asciiOnly, "船舶类型", safeStr(vesselInfo.get("vessel_type")));
        addRow(tv, font, asciiOnly, "国籍", safeStr(vesselInfo.get("nationality")));
        addRow(tv, font, asciiOnly, "总吨", formatNumber(vesselInfo.get("gross_tonnage")));
        addRow(tv, font, asciiOnly, "净吨", formatNumber(vesselInfo.get("net_tonnage")));
        doc.add(tv);

        // 买卖方信息
        doc.add(new Paragraph(sanitizeForFont("\n购方信息", font, asciiOnly)).setFontSize(12));
        @SuppressWarnings("unchecked")
        Map<String, Object> buyerInfo = (Map<String, Object>) detail.getOrDefault("buyer_info", new LinkedHashMap<>());
        Table tb = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
        addRow(tb, font, asciiOnly, "名称", safeStr(buyerInfo.get("company_name")));
        addRow(tb, font, asciiOnly, "纳税人识别号", safeStr(buyerInfo.get("taxpayer_id")));
        addRow(tb, font, asciiOnly, "地址", safeStr(buyerInfo.get("address")));
        addRow(tb, font, asciiOnly, "电话", safeStr(buyerInfo.get("phone")));
        addRow(tb, font, asciiOnly, "开户行", safeStr(buyerInfo.get("bank")));
        addRow(tb, font, asciiOnly, "账号", safeStr(buyerInfo.get("bank_account")));
        doc.add(tb);

        doc.add(new Paragraph(sanitizeForFont("\n销方信息", font, asciiOnly)).setFontSize(12));
        @SuppressWarnings("unchecked")
        Map<String, Object> sellerInfo = (Map<String, Object>) detail.getOrDefault("seller_info", new LinkedHashMap<>());
        Table ts = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
        addRow(ts, font, asciiOnly, "名称", safeStr(sellerInfo.get("company_name")));
        addRow(ts, font, asciiOnly, "纳税人识别号", safeStr(sellerInfo.get("taxpayer_id")));
        addRow(ts, font, asciiOnly, "地址", safeStr(sellerInfo.get("address")));
        addRow(ts, font, asciiOnly, "电话", safeStr(sellerInfo.get("phone")));
        addRow(ts, font, asciiOnly, "开户行", safeStr(sellerInfo.get("bank")));
        addRow(ts, font, asciiOnly, "账号", safeStr(sellerInfo.get("bank_account")));
        doc.add(ts);

        doc.close();
        return baos.toByteArray();
    }

    private PdfFont resolveCjkFont() {
        // 优先使用 iText 内置 CJK 字体（无需依赖系统字体文件）
        try {
            return PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (Exception ignored) {
        }
        try {
            return PdfFontFactory.createFont("C:\\Windows\\Fonts\\simsun.ttc,0", PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (Exception ignored) {
        }
        // 兜底：使用默认字体（只能显示英文/数字）
        return null;
    }

    private void addRow(Table t, PdfFont font, boolean asciiOnly, String k, String v) {
        String kk = sanitizeForFont(k == null ? "" : k, font, asciiOnly);
        String vv = sanitizeForFont(v == null ? "" : v, font, asciiOnly);
        Cell kCell = new Cell().add(new Paragraph(kk)).setBold();
        Cell vCell = new Cell().add(new Paragraph(vv));
        t.addCell(kCell);
        t.addCell(vCell);
    }

    private String sanitizeForFont(String s, PdfFont font, boolean asciiOnly) {
        if (s == null || s.isEmpty()) return "";
        // ASCII-only 模式：保证任何字体都不会因为缺 glyph 崩溃
        if (asciiOnly) {
            StringBuilder out = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                // 允许常用可见 ASCII + 换行/回车/制表
                if (ch == '\n' || ch == '\r' || ch == '\t' || (ch >= 0x20 && ch <= 0x7E)) {
                    out.append(ch);
                } else {
                    out.append('?');
                }
            }
            return out.toString();
        }
        // 有字体时：按 glyph 可用性过滤，避免 iText 在缺字形时 NPE
        if (font == null) return s;

        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            if (font.containsGlyph(cp)) {
                out.appendCodePoint(cp);
            } else if (font.containsGlyph(0x25A1)) { // '□'
                out.append('\u25A1');
            } else {
                out.append('?');
            }
            i += Character.charCount(cp);
        }
        return out.toString();
    }

    private String safeStr(Object v) {
        if (v == null) return "";
        return String.valueOf(v);
    }

    private String formatDate(Object v) {
        if (v == null) return "";
        try {
            if (v instanceof Date d) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
            }
        } catch (Exception ignored) {
        }
        return String.valueOf(v);
    }

    private String formatMoney(Object v) {
        if (v == null) return "";
        try {
            java.math.BigDecimal bd = new java.math.BigDecimal(String.valueOf(v));
            return "¥" + bd.setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (Exception ignored) {
        }
        return String.valueOf(v);
    }

    private String formatNumber(Object v) {
        if (v == null) return "";
        try {
            return new DecimalFormat("0.##").format(Double.parseDouble(String.valueOf(v)));
        } catch (Exception ignored) {
        }
        return String.valueOf(v);
    }

        private String toJson(Map<String, Object> map) {
        try {
            return JSON.toJSONString(map == null ? Collections.emptyMap() : map);
        } catch (Exception e) {
            return "{}";
        }
    }
}