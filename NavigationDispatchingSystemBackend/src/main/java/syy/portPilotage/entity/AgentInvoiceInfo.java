package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 代理及发票信息表
 * @TableName agent_invoice_info
 */
@TableName(value ="agent_invoice_info")
@Data
public class AgentInvoiceInfo implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 代理公司ID
     */
    @TableField(value = "agent_id")
    private Long agentId;

    /**
     * 航次ID
     */
    @TableField(value = "voyage_id")
    private Long voyageId;

    /**
     * 代理名称
     */
    @TableField(value = "agent_name")
    private String agentName;

    /**
     * 付款单位名称
     */
    @TableField(value = "paying_unit_name")
    private String payingUnitName;

    /**
     * 发票类型
     */
    @TableField(value = "invoice_type")
    private String invoiceType;

    /**
     * 纳税人识别号
     */
    @TableField(value = "taxpayer_id")
    private String taxpayerId;

    /**
     * 付款单位地址
     */
    @TableField(value = "paying_unit_address")
    private String payingUnitAddress;

    /**
     * 付款单位电话
     */
    @TableField(value = "paying_unit_phone")
    private String payingUnitPhone;

    /**
     * 付款单位开户银行
     */
    @TableField(value = "paying_unit_bank")
    private String payingUnitBank;

    /**
     * 付款单位银行账号
     */
    @TableField(value = "paying_unit_bank_account")
    private String payingUnitBankAccount;

    /**
     * 关联计费凭证ID
     */
    @TableField(value = "billing_voucher_id")
    private Long billingVoucherId;

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
        AgentInvoiceInfo other = (AgentInvoiceInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAgentId() == null ? other.getAgentId() == null : this.getAgentId().equals(other.getAgentId()))
            && (this.getVoyageId() == null ? other.getVoyageId() == null : this.getVoyageId().equals(other.getVoyageId()))
            && (this.getAgentName() == null ? other.getAgentName() == null : this.getAgentName().equals(other.getAgentName()))
            && (this.getPayingUnitName() == null ? other.getPayingUnitName() == null : this.getPayingUnitName().equals(other.getPayingUnitName()))
            && (this.getInvoiceType() == null ? other.getInvoiceType() == null : this.getInvoiceType().equals(other.getInvoiceType()))
            && (this.getTaxpayerId() == null ? other.getTaxpayerId() == null : this.getTaxpayerId().equals(other.getTaxpayerId()))
            && (this.getPayingUnitAddress() == null ? other.getPayingUnitAddress() == null : this.getPayingUnitAddress().equals(other.getPayingUnitAddress()))
            && (this.getPayingUnitPhone() == null ? other.getPayingUnitPhone() == null : this.getPayingUnitPhone().equals(other.getPayingUnitPhone()))
            && (this.getPayingUnitBank() == null ? other.getPayingUnitBank() == null : this.getPayingUnitBank().equals(other.getPayingUnitBank()))
            && (this.getPayingUnitBankAccount() == null ? other.getPayingUnitBankAccount() == null : this.getPayingUnitBankAccount().equals(other.getPayingUnitBankAccount()))
            && (this.getBillingVoucherId() == null ? other.getBillingVoucherId() == null : this.getBillingVoucherId().equals(other.getBillingVoucherId()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAgentId() == null) ? 0 : getAgentId().hashCode());
        result = prime * result + ((getVoyageId() == null) ? 0 : getVoyageId().hashCode());
        result = prime * result + ((getAgentName() == null) ? 0 : getAgentName().hashCode());
        result = prime * result + ((getPayingUnitName() == null) ? 0 : getPayingUnitName().hashCode());
        result = prime * result + ((getInvoiceType() == null) ? 0 : getInvoiceType().hashCode());
        result = prime * result + ((getTaxpayerId() == null) ? 0 : getTaxpayerId().hashCode());
        result = prime * result + ((getPayingUnitAddress() == null) ? 0 : getPayingUnitAddress().hashCode());
        result = prime * result + ((getPayingUnitPhone() == null) ? 0 : getPayingUnitPhone().hashCode());
        result = prime * result + ((getPayingUnitBank() == null) ? 0 : getPayingUnitBank().hashCode());
        result = prime * result + ((getPayingUnitBankAccount() == null) ? 0 : getPayingUnitBankAccount().hashCode());
        result = prime * result + ((getBillingVoucherId() == null) ? 0 : getBillingVoucherId().hashCode());
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
        sb.append(", agentId=").append(agentId);
        sb.append(", voyageId=").append(voyageId);
        sb.append(", agentName=").append(agentName);
        sb.append(", payingUnitName=").append(payingUnitName);
        sb.append(", invoiceType=").append(invoiceType);
        sb.append(", taxpayerId=").append(taxpayerId);
        sb.append(", payingUnitAddress=").append(payingUnitAddress);
        sb.append(", payingUnitPhone=").append(payingUnitPhone);
        sb.append(", payingUnitBank=").append(payingUnitBank);
        sb.append(", payingUnitBankAccount=").append(payingUnitBankAccount);
        sb.append(", billingVoucherId=").append(billingVoucherId);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}