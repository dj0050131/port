package syy.portPilotage.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 引航计费凭证表
 */
@Schema(description = "引航计费凭证表")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillingVouchers implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 计费凭证编号
     */
    @Schema(description = "计费凭证编号")
    private String voucherCode;

    /**
     * 会计年度编号
     */
    @Schema(description = "会计年度编号")
    private String fiscalYear;

    /**
     * 会计期间编号
     */
    @Schema(description = "会计期间编号")
    private String fiscalPeriod;

    /**
     * 签证单ID
     */
    @Schema(description = "签证单ID")
    private Long certificateId;

    /**
     * 航次ID
     */
    @Schema(description = "航次ID")
    private Long voyageId;

    /**
     * 船舶ID
     */
    @Schema(description = "船舶ID")
    private Long vesselId;

    /**
     * 船代ID
     */
    @Schema(description = "船代ID")
    private Long agentId;

        @Schema(description = "开始泊位ID")
    private Long startBerthId;

        @Schema(description = "结束泊位ID")
    private Long endBerthId;

    /**
     * 计费方式
     */
    @Schema(description = "计费方式")
    private String billingMethod;

        @Schema(description = "收费编码(收费项目编号)")
    private String chargeCode;

    /**
     * 收费类型
     */
    @Schema(description = "收费类型")
    private String chargeType;

        @Schema(description = "开始引航时间")
    private Date pilotageStartTime;

        @Schema(description = "结束引航时间")
    private Date pilotageEndTime;

    /**
     * 航线类型
     */
    @Schema(description = "航线类型")
    private String routeType;

    /**
     * 标准费率二
     */
    @Schema(description = "标准费率二")
    private BigDecimal standardRate2;

    /**
     * 标准费率三
     */
    @Schema(description = "标准费率三")
    private BigDecimal standardRate3;

    /**
     * 实际标准费率二
     */
    @Schema(description = "实际标准费率二")
    private BigDecimal actualStandardRate2;

    /**
     * 实际标准费率三
     */
    @Schema(description = "实际标准费率三")
    private BigDecimal actualStandardRate3;

    /**
     * 实际计费费率2
     */
    @Schema(description = "实际计费费率2")
    private BigDecimal actualBillingRate2;

    /**
     * 实际计费费率3
     */
    @Schema(description = "实际计费费率3")
    private BigDecimal actualBillingRate3;

        @Schema(description = "作废备注")
    private String cancellationNotes;

    /**
     * 免收标记
     */
    @Schema(description = "免收标记")
    private String freeChargeFlag;

    /**
     * 四舍五入
     */
    @Schema(description = "四舍五入")
    private String roundingMethod;

    /**
     * 参考政策折扣
     */
    @Schema(description = "参考政策折扣")
    private BigDecimal policyDiscountReference;

    /**
     * 税务开票标记
     */
    @Schema(description = "税务开票标记")
    private String taxInvoicingFlag;

    /**
     * 税务开票时间
     */
    @Schema(description = "税务开票时间")
    private Date taxInvoicingTime;

    /**
     * 纳税人全称
     */
    @Schema(description = "纳税人全称")
    private String taxpayerFullName;

    /**
     * 纳税人标识号
     */
    @Schema(description = "纳税人标识号")
    private String taxpayerId;

    /**
     * 发票代码
     */
    @Schema(description = "发票代码")
    private String invoiceCode;

    /**
     * 发票号码
     */
    @Schema(description = "发票号码")
    private String invoiceNumber;

    /**
     * 购方开户银行
     */
    @Schema(description = "购方开户银行")
    private String buyerBank;

    /**
     * 总金额
     */
    @Schema(description = "总金额")
    private BigDecimal totalAmount;

        @Schema(description = "审核状态")
    private Object auditStatus;

    /**
     * 付款状态
     */
    @Schema(description = "付款状态")
    private Object paymentStatus;

        @Schema(description = "移交状态")
    private Object transferStatus;

    /**
     * 到期日
     */
    @Schema(description = "到期日")
    private Date dueDate;

    /**
     * 折扣金额
     */
    @Schema(description = "折扣金额")
    private BigDecimal discountAmount;

    /**
     * 滞纳金
     */
    @Schema(description = "滞纳金")
    private BigDecimal lateFee;

    /**
     * 实收金额
     */
    @Schema(description = "实收金额")
    private BigDecimal actualReceivedAmount;

    /**
     * 收款日期
     */
    @Schema(description = "收款日期")
    private Date receiptDate;

        @Schema(description = "创建时间")
    private Date createdAt;

        @Schema(description = "更新时间")
    private Date updatedAt;

        @Schema(description = "移交目标系统（如：省港航稽征系统）")
    private String transferTarget;

        @Schema(description = "移交类型")
    private Object transferType;

        @Schema(description = "移交批次号")
    private String transferBatchNo;

        @Schema(description = "移交时间")
    private Date transferTime;

        @Schema(description = "移交操作人ID")
    private Long transferOperatorId;

        @Schema(description = "移交备注")
    private String transferNotes;

    /**
     * 外部系统参考号
     */
    @Schema(description = "外部系统参考号")
    private String externalRefNo;

    /**
     * 外部系统响应代码
     */
    @Schema(description = "外部系统响应代码")
    private String externalResponseCode;

    /**
     * 外部系统响应消息
     */
    @Schema(description = "外部系统响应消息")
    private String externalResponseMsg;

        @Schema(description = "最后移交尝试时间")
    private Date lastTransferAttempt;

        @Schema(description = "移交尝试次数")
    private Integer transferAttemptCount;

    /**
     * 下次重试时间（用于自动重试）
     */
    @Schema(description = "下次重试时间（用于自动重试）")
    private Date nextRetryTime;

    /**
     * 电子发票状态
     */
    @Schema(description = "电子发票状态")
    private Object electronicInvoiceStatus;

        @Schema(description = "电子发票生成时间")
    private Date electronicGeneratedAt;

        @Schema(description = "电子发票生成人ID")
    private Long electronicGeneratedBy;

        @Schema(description = "电子发票发送时间")
    private Date electronicSentAt;

        @Schema(description = "电子发票发送人ID")
    private Long electronicSentBy;

        @Schema(description = "发送至（邮箱/手机号）")
    private String electronicSentTo;

        @Schema(description = "发票校验码")
    private String checkCode;

    /**
     * 发票备注
     */
    @Schema(description = "发票备注")
    private String invoiceNotes;

        @Schema(description = "下载次数")
    private Integer downloadCount;

        @Schema(description = "最后下载时间")
    private Date lastDownloadAt;

    /**
     * 船舶信息快照(JSON)
     */
    @Schema(description = "船舶信息快照(JSON)")
    private String vesselSnapshot;

    /**
     * 航次信息快照(JSON)
     */
    @Schema(description = "航次信息快照(JSON)")
    private String voyageSnapshot;

    /**
     * 代理信息快照(JSON)
     */
    @Schema(description = "代理信息快照(JSON)")
    private String agentSnapshot;

    /**
     * 引航信息快照(JSON)
     */
    @Schema(description = "引航信息快照(JSON)")
    private String pilotageSnapshot;

        @Schema(description = "快照生成时间")
    private Date snapshotTakenAt;

    /**
     * 快照版本
     */
    @Schema(description = "快照版本")
    private String snapshotVersion;
}