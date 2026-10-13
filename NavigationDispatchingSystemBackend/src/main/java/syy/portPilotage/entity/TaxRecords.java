package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@TableName(value ="tax_records")
@Data
public class TaxRecords implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

        @TableField(value = "tax_record_code")
    private String taxRecordCode;

    /**
     * 关联的计费凭证ID
     */
    @TableField(value = "voucher_id")
    private Long voucherId;

        @TableField(value = "billing_voucher_code")
    private String billingVoucherCode;

    /**
     * 关联航次ID
     */
    @TableField(value = "voyage_id")
    private Long voyageId;

    /**
     * 关联船舶ID
     */
    @TableField(value = "vessel_id")
    private Long vesselId;

    /**
     * 代理公司ID
     */
    @TableField(value = "agent_id")
    private Long agentId;

    /**
     * 纳税人识别号
     */
    @TableField(value = "taxpayer_id")
    private String taxpayerId;

    /**
     * 纳税人名称
     */
    @TableField(value = "taxpayer_name")
    private String taxpayerName;

    /**
     * 纳税人地址
     */
    @TableField(value = "taxpayer_address")
    private String taxpayerAddress;

    /**
     * 纳税人电话
     */
    @TableField(value = "taxpayer_phone")
    private String taxpayerPhone;

    /**
     * 纳税人开户银行
     */
    @TableField(value = "taxpayer_bank")
    private String taxpayerBank;

    /**
     * 纳税人银行账号
     */
    @TableField(value = "taxpayer_bank_account")
    private String taxpayerBankAccount;

    /**
     * 应税金额（通常是计费凭证总金额）
     */
    @TableField(value = "taxable_amount")
    private BigDecimal taxableAmount;

    /**
     * 税额合计
     */
    @TableField(value = "total_tax_amount")
    private BigDecimal totalTaxAmount;

    /**
     * 所属期（格式：YYYY-MM-01）
     */
    @TableField(value = "tax_period")
    private Date taxPeriod;

        @TableField(value = "tax_calculation_time")
    private Date taxCalculationTime;

        @TableField(value = "tax_policy_code")
    private String taxPolicyCode;

    /**
     * 税收政策名称
     */
    @TableField(value = "tax_policy_name")
    private String taxPolicyName;

    /**
     * 是否免税
     */
    @TableField(value = "is_tax_exempt")
    private Integer isTaxExempt;

    /**
     * 免税原因
     */
    @TableField(value = "tax_exempt_reason")
    private String taxExemptReason;

    /**
     * 免税批文号
     */
    @TableField(value = "tax_exempt_approval_no")
    private String taxExemptApprovalNo;

    /**
     * 税额状态
     */
    @TableField(value = "tax_status")
    private Object taxStatus;

        @TableField(value = "transfer_target")
    private String transferTarget;

        @TableField(value = "transfer_time")
    private Date transferTime;

    /**
     * 外部系统税票号
     */
    @TableField(value = "external_tax_no")
    private String externalTaxNo;

    /**
     * 外部系统申报流水号
     */
    @TableField(value = "external_declaration_no")
    private String externalDeclarationNo;

        @TableField(value = "transfer_operator_id")
    private Long transferOperatorId;

        @TableField(value = "transfer_notes")
    private String transferNotes;

        @TableField(value = "reviewer_id")
    private Long reviewerId;

        @TableField(value = "review_time")
    private Date reviewTime;

        @TableField(value = "review_notes")
    private String reviewNotes;

    /**
     * 批准人ID
     */
    @TableField(value = "approver_id")
    private Long approverId;

    /**
     * 批准时间
     */
    @TableField(value = "approve_time")
    private Date approveTime;

        @TableField(value = "calculation_method")
    private Object calculationMethod;

        @TableField(value = "data_source")
    private String dataSource;

    /**
     * 版本号（用于乐观锁）
     */
    @TableField(value = "version")
    private Integer version;

    /**
     * 是否有效
     */
    @TableField(value = "is_active")
    private Integer isActive;

        @TableField(value = "created_by")
    private Long createdBy;

        @TableField(value = "updated_by")
    private Long updatedBy;

        @TableField(value = "created_at")
    private Date createdAt;

        @TableField(value = "updated_at")
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TaxRecords other = (TaxRecords) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTaxRecordCode() == null ? other.getTaxRecordCode() == null : this.getTaxRecordCode().equals(other.getTaxRecordCode()))
            && (this.getVoucherId() == null ? other.getVoucherId() == null : this.getVoucherId().equals(other.getVoucherId()))
            && (this.getBillingVoucherCode() == null ? other.getBillingVoucherCode() == null : this.getBillingVoucherCode().equals(other.getBillingVoucherCode()))
            && (this.getVoyageId() == null ? other.getVoyageId() == null : this.getVoyageId().equals(other.getVoyageId()))
            && (this.getVesselId() == null ? other.getVesselId() == null : this.getVesselId().equals(other.getVesselId()))
            && (this.getAgentId() == null ? other.getAgentId() == null : this.getAgentId().equals(other.getAgentId()))
            && (this.getTaxpayerId() == null ? other.getTaxpayerId() == null : this.getTaxpayerId().equals(other.getTaxpayerId()))
            && (this.getTaxpayerName() == null ? other.getTaxpayerName() == null : this.getTaxpayerName().equals(other.getTaxpayerName()))
            && (this.getTaxpayerAddress() == null ? other.getTaxpayerAddress() == null : this.getTaxpayerAddress().equals(other.getTaxpayerAddress()))
            && (this.getTaxpayerPhone() == null ? other.getTaxpayerPhone() == null : this.getTaxpayerPhone().equals(other.getTaxpayerPhone()))
            && (this.getTaxpayerBank() == null ? other.getTaxpayerBank() == null : this.getTaxpayerBank().equals(other.getTaxpayerBank()))
            && (this.getTaxpayerBankAccount() == null ? other.getTaxpayerBankAccount() == null : this.getTaxpayerBankAccount().equals(other.getTaxpayerBankAccount()))
            && (this.getTaxableAmount() == null ? other.getTaxableAmount() == null : this.getTaxableAmount().equals(other.getTaxableAmount()))
            && (this.getTotalTaxAmount() == null ? other.getTotalTaxAmount() == null : this.getTotalTaxAmount().equals(other.getTotalTaxAmount()))
            && (this.getTaxPeriod() == null ? other.getTaxPeriod() == null : this.getTaxPeriod().equals(other.getTaxPeriod()))
            && (this.getTaxCalculationTime() == null ? other.getTaxCalculationTime() == null : this.getTaxCalculationTime().equals(other.getTaxCalculationTime()))
            && (this.getTaxPolicyCode() == null ? other.getTaxPolicyCode() == null : this.getTaxPolicyCode().equals(other.getTaxPolicyCode()))
            && (this.getTaxPolicyName() == null ? other.getTaxPolicyName() == null : this.getTaxPolicyName().equals(other.getTaxPolicyName()))
            && (this.getIsTaxExempt() == null ? other.getIsTaxExempt() == null : this.getIsTaxExempt().equals(other.getIsTaxExempt()))
            && (this.getTaxExemptReason() == null ? other.getTaxExemptReason() == null : this.getTaxExemptReason().equals(other.getTaxExemptReason()))
            && (this.getTaxExemptApprovalNo() == null ? other.getTaxExemptApprovalNo() == null : this.getTaxExemptApprovalNo().equals(other.getTaxExemptApprovalNo()))
            && (this.getTaxStatus() == null ? other.getTaxStatus() == null : this.getTaxStatus().equals(other.getTaxStatus()))
            && (this.getTransferTarget() == null ? other.getTransferTarget() == null : this.getTransferTarget().equals(other.getTransferTarget()))
            && (this.getTransferTime() == null ? other.getTransferTime() == null : this.getTransferTime().equals(other.getTransferTime()))
            && (this.getExternalTaxNo() == null ? other.getExternalTaxNo() == null : this.getExternalTaxNo().equals(other.getExternalTaxNo()))
            && (this.getExternalDeclarationNo() == null ? other.getExternalDeclarationNo() == null : this.getExternalDeclarationNo().equals(other.getExternalDeclarationNo()))
            && (this.getTransferOperatorId() == null ? other.getTransferOperatorId() == null : this.getTransferOperatorId().equals(other.getTransferOperatorId()))
            && (this.getTransferNotes() == null ? other.getTransferNotes() == null : this.getTransferNotes().equals(other.getTransferNotes()))
            && (this.getReviewerId() == null ? other.getReviewerId() == null : this.getReviewerId().equals(other.getReviewerId()))
            && (this.getReviewTime() == null ? other.getReviewTime() == null : this.getReviewTime().equals(other.getReviewTime()))
            && (this.getReviewNotes() == null ? other.getReviewNotes() == null : this.getReviewNotes().equals(other.getReviewNotes()))
            && (this.getApproverId() == null ? other.getApproverId() == null : this.getApproverId().equals(other.getApproverId()))
            && (this.getApproveTime() == null ? other.getApproveTime() == null : this.getApproveTime().equals(other.getApproveTime()))
            && (this.getCalculationMethod() == null ? other.getCalculationMethod() == null : this.getCalculationMethod().equals(other.getCalculationMethod()))
            && (this.getDataSource() == null ? other.getDataSource() == null : this.getDataSource().equals(other.getDataSource()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getIsActive() == null ? other.getIsActive() == null : this.getIsActive().equals(other.getIsActive()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTaxRecordCode() == null) ? 0 : getTaxRecordCode().hashCode());
        result = prime * result + ((getVoucherId() == null) ? 0 : getVoucherId().hashCode());
        result = prime * result + ((getBillingVoucherCode() == null) ? 0 : getBillingVoucherCode().hashCode());
        result = prime * result + ((getVoyageId() == null) ? 0 : getVoyageId().hashCode());
        result = prime * result + ((getVesselId() == null) ? 0 : getVesselId().hashCode());
        result = prime * result + ((getAgentId() == null) ? 0 : getAgentId().hashCode());
        result = prime * result + ((getTaxpayerId() == null) ? 0 : getTaxpayerId().hashCode());
        result = prime * result + ((getTaxpayerName() == null) ? 0 : getTaxpayerName().hashCode());
        result = prime * result + ((getTaxpayerAddress() == null) ? 0 : getTaxpayerAddress().hashCode());
        result = prime * result + ((getTaxpayerPhone() == null) ? 0 : getTaxpayerPhone().hashCode());
        result = prime * result + ((getTaxpayerBank() == null) ? 0 : getTaxpayerBank().hashCode());
        result = prime * result + ((getTaxpayerBankAccount() == null) ? 0 : getTaxpayerBankAccount().hashCode());
        result = prime * result + ((getTaxableAmount() == null) ? 0 : getTaxableAmount().hashCode());
        result = prime * result + ((getTotalTaxAmount() == null) ? 0 : getTotalTaxAmount().hashCode());
        result = prime * result + ((getTaxPeriod() == null) ? 0 : getTaxPeriod().hashCode());
        result = prime * result + ((getTaxCalculationTime() == null) ? 0 : getTaxCalculationTime().hashCode());
        result = prime * result + ((getTaxPolicyCode() == null) ? 0 : getTaxPolicyCode().hashCode());
        result = prime * result + ((getTaxPolicyName() == null) ? 0 : getTaxPolicyName().hashCode());
        result = prime * result + ((getIsTaxExempt() == null) ? 0 : getIsTaxExempt().hashCode());
        result = prime * result + ((getTaxExemptReason() == null) ? 0 : getTaxExemptReason().hashCode());
        result = prime * result + ((getTaxExemptApprovalNo() == null) ? 0 : getTaxExemptApprovalNo().hashCode());
        result = prime * result + ((getTaxStatus() == null) ? 0 : getTaxStatus().hashCode());
        result = prime * result + ((getTransferTarget() == null) ? 0 : getTransferTarget().hashCode());
        result = prime * result + ((getTransferTime() == null) ? 0 : getTransferTime().hashCode());
        result = prime * result + ((getExternalTaxNo() == null) ? 0 : getExternalTaxNo().hashCode());
        result = prime * result + ((getExternalDeclarationNo() == null) ? 0 : getExternalDeclarationNo().hashCode());
        result = prime * result + ((getTransferOperatorId() == null) ? 0 : getTransferOperatorId().hashCode());
        result = prime * result + ((getTransferNotes() == null) ? 0 : getTransferNotes().hashCode());
        result = prime * result + ((getReviewerId() == null) ? 0 : getReviewerId().hashCode());
        result = prime * result + ((getReviewTime() == null) ? 0 : getReviewTime().hashCode());
        result = prime * result + ((getReviewNotes() == null) ? 0 : getReviewNotes().hashCode());
        result = prime * result + ((getApproverId() == null) ? 0 : getApproverId().hashCode());
        result = prime * result + ((getApproveTime() == null) ? 0 : getApproveTime().hashCode());
        result = prime * result + ((getCalculationMethod() == null) ? 0 : getCalculationMethod().hashCode());
        result = prime * result + ((getDataSource() == null) ? 0 : getDataSource().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
        result = prime * result + ((getIsActive() == null) ? 0 : getIsActive().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", taxRecordCode=").append(taxRecordCode);
        sb.append(", voucherId=").append(voucherId);
        sb.append(", billingVoucherCode=").append(billingVoucherCode);
        sb.append(", voyageId=").append(voyageId);
        sb.append(", vesselId=").append(vesselId);
        sb.append(", agentId=").append(agentId);
        sb.append(", taxpayerId=").append(taxpayerId);
        sb.append(", taxpayerName=").append(taxpayerName);
        sb.append(", taxpayerAddress=").append(taxpayerAddress);
        sb.append(", taxpayerPhone=").append(taxpayerPhone);
        sb.append(", taxpayerBank=").append(taxpayerBank);
        sb.append(", taxpayerBankAccount=").append(taxpayerBankAccount);
        sb.append(", taxableAmount=").append(taxableAmount);
        sb.append(", totalTaxAmount=").append(totalTaxAmount);
        sb.append(", taxPeriod=").append(taxPeriod);
        sb.append(", taxCalculationTime=").append(taxCalculationTime);
        sb.append(", taxPolicyCode=").append(taxPolicyCode);
        sb.append(", taxPolicyName=").append(taxPolicyName);
        sb.append(", isTaxExempt=").append(isTaxExempt);
        sb.append(", taxExemptReason=").append(taxExemptReason);
        sb.append(", taxExemptApprovalNo=").append(taxExemptApprovalNo);
        sb.append(", taxStatus=").append(taxStatus);
        sb.append(", transferTarget=").append(transferTarget);
        sb.append(", transferTime=").append(transferTime);
        sb.append(", externalTaxNo=").append(externalTaxNo);
        sb.append(", externalDeclarationNo=").append(externalDeclarationNo);
        sb.append(", transferOperatorId=").append(transferOperatorId);
        sb.append(", transferNotes=").append(transferNotes);
        sb.append(", reviewerId=").append(reviewerId);
        sb.append(", reviewTime=").append(reviewTime);
        sb.append(", reviewNotes=").append(reviewNotes);
        sb.append(", approverId=").append(approverId);
        sb.append(", approveTime=").append(approveTime);
        sb.append(", calculationMethod=").append(calculationMethod);
        sb.append(", dataSource=").append(dataSource);
        sb.append(", version=").append(version);
        sb.append(", isActive=").append(isActive);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}