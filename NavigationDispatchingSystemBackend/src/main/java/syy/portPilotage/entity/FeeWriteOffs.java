package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 费用核销单表
 * @TableName fee_write_offs
 */
@TableName(value ="fee_write_offs")
@Data
public class FeeWriteOffs implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

        @TableField(value = "write_off_code")
    private String writeOffCode;

    /**
     * 船代ID
     */
    @TableField(value = "agent_id")
    private Long agentId;

        @TableField(value = "write_off_person")
    private String writeOffPerson;

        @TableField(value = "write_off_date")
    private Date writeOffDate;

        @TableField(value = "auditor")
    private String auditor;

        @TableField(value = "audit_time")
    private Date auditTime;

    /**
     * 状态编号
     */
    @TableField(value = "status_code")
    private String statusCode;

    /**
     * 状态名称
     */
    @TableField(value = "status_name")
    private String statusName;

    /**
     * 备注
     */
    @TableField(value = "notes")
    private String notes;

        @TableField(value = "write_off_type")
    private String writeOffType;

    /**
     * 票据属性
     */
    @TableField(value = "document_attribute")
    private String documentAttribute;

        @TableField(value = "write_off_amount")
    private BigDecimal writeOffAmount;

        @TableField(value = "write_off_method")
    private String writeOffMethod;

    /**
     * 银行流水号
     */
    @TableField(value = "bank_reference_number")
    private String bankReferenceNumber;

    /**
     * 凭证号码
     */
    @TableField(value = "voucher_number")
    private String voucherNumber;

        @TableField(value = "finance_confirmer")
    private String financeConfirmer;

        @TableField(value = "finance_confirm_time")
    private Date financeConfirmTime;

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
        FeeWriteOffs other = (FeeWriteOffs) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getWriteOffCode() == null ? other.getWriteOffCode() == null : this.getWriteOffCode().equals(other.getWriteOffCode()))
            && (this.getAgentId() == null ? other.getAgentId() == null : this.getAgentId().equals(other.getAgentId()))
            && (this.getWriteOffPerson() == null ? other.getWriteOffPerson() == null : this.getWriteOffPerson().equals(other.getWriteOffPerson()))
            && (this.getWriteOffDate() == null ? other.getWriteOffDate() == null : this.getWriteOffDate().equals(other.getWriteOffDate()))
            && (this.getAuditor() == null ? other.getAuditor() == null : this.getAuditor().equals(other.getAuditor()))
            && (this.getAuditTime() == null ? other.getAuditTime() == null : this.getAuditTime().equals(other.getAuditTime()))
            && (this.getStatusCode() == null ? other.getStatusCode() == null : this.getStatusCode().equals(other.getStatusCode()))
            && (this.getStatusName() == null ? other.getStatusName() == null : this.getStatusName().equals(other.getStatusName()))
            && (this.getNotes() == null ? other.getNotes() == null : this.getNotes().equals(other.getNotes()))
            && (this.getWriteOffType() == null ? other.getWriteOffType() == null : this.getWriteOffType().equals(other.getWriteOffType()))
            && (this.getDocumentAttribute() == null ? other.getDocumentAttribute() == null : this.getDocumentAttribute().equals(other.getDocumentAttribute()))
            && (this.getWriteOffAmount() == null ? other.getWriteOffAmount() == null : this.getWriteOffAmount().equals(other.getWriteOffAmount()))
            && (this.getWriteOffMethod() == null ? other.getWriteOffMethod() == null : this.getWriteOffMethod().equals(other.getWriteOffMethod()))
            && (this.getBankReferenceNumber() == null ? other.getBankReferenceNumber() == null : this.getBankReferenceNumber().equals(other.getBankReferenceNumber()))
            && (this.getVoucherNumber() == null ? other.getVoucherNumber() == null : this.getVoucherNumber().equals(other.getVoucherNumber()))
            && (this.getFinanceConfirmer() == null ? other.getFinanceConfirmer() == null : this.getFinanceConfirmer().equals(other.getFinanceConfirmer()))
            && (this.getFinanceConfirmTime() == null ? other.getFinanceConfirmTime() == null : this.getFinanceConfirmTime().equals(other.getFinanceConfirmTime()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getWriteOffCode() == null) ? 0 : getWriteOffCode().hashCode());
        result = prime * result + ((getAgentId() == null) ? 0 : getAgentId().hashCode());
        result = prime * result + ((getWriteOffPerson() == null) ? 0 : getWriteOffPerson().hashCode());
        result = prime * result + ((getWriteOffDate() == null) ? 0 : getWriteOffDate().hashCode());
        result = prime * result + ((getAuditor() == null) ? 0 : getAuditor().hashCode());
        result = prime * result + ((getAuditTime() == null) ? 0 : getAuditTime().hashCode());
        result = prime * result + ((getStatusCode() == null) ? 0 : getStatusCode().hashCode());
        result = prime * result + ((getStatusName() == null) ? 0 : getStatusName().hashCode());
        result = prime * result + ((getNotes() == null) ? 0 : getNotes().hashCode());
        result = prime * result + ((getWriteOffType() == null) ? 0 : getWriteOffType().hashCode());
        result = prime * result + ((getDocumentAttribute() == null) ? 0 : getDocumentAttribute().hashCode());
        result = prime * result + ((getWriteOffAmount() == null) ? 0 : getWriteOffAmount().hashCode());
        result = prime * result + ((getWriteOffMethod() == null) ? 0 : getWriteOffMethod().hashCode());
        result = prime * result + ((getBankReferenceNumber() == null) ? 0 : getBankReferenceNumber().hashCode());
        result = prime * result + ((getVoucherNumber() == null) ? 0 : getVoucherNumber().hashCode());
        result = prime * result + ((getFinanceConfirmer() == null) ? 0 : getFinanceConfirmer().hashCode());
        result = prime * result + ((getFinanceConfirmTime() == null) ? 0 : getFinanceConfirmTime().hashCode());
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
        sb.append(", writeOffCode=").append(writeOffCode);
        sb.append(", agentId=").append(agentId);
        sb.append(", writeOffPerson=").append(writeOffPerson);
        sb.append(", writeOffDate=").append(writeOffDate);
        sb.append(", auditor=").append(auditor);
        sb.append(", auditTime=").append(auditTime);
        sb.append(", statusCode=").append(statusCode);
        sb.append(", statusName=").append(statusName);
        sb.append(", notes=").append(notes);
        sb.append(", writeOffType=").append(writeOffType);
        sb.append(", documentAttribute=").append(documentAttribute);
        sb.append(", writeOffAmount=").append(writeOffAmount);
        sb.append(", writeOffMethod=").append(writeOffMethod);
        sb.append(", bankReferenceNumber=").append(bankReferenceNumber);
        sb.append(", voucherNumber=").append(voucherNumber);
        sb.append(", financeConfirmer=").append(financeConfirmer);
        sb.append(", financeConfirmTime=").append(financeConfirmTime);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}