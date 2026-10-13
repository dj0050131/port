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
 * 付款信息表
 * @TableName payment_info
 */
@TableName(value ="payment_info")
@Data
public class PaymentInfo implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 计费凭证ID
     */
    @TableField(value = "billing_voucher_id")
    private Long billingVoucherId;

    /**
     * 收款单位
     */
    @TableField(value = "payee_unit")
    private String payeeUnit;

    /**
     * 收款账号
     */
    @TableField(value = "payee_bank_account")
    private String payeeBankAccount;

    /**
     * 收款开户行
     */
    @TableField(value = "payee_bank_name")
    private String payeeBankName;

    /**
     * 付款金额
     */
    @TableField(value = "payment_amount")
    private BigDecimal paymentAmount;

    /**
     * 付款日期
     */
    @TableField(value = "payment_date")
    private Date paymentDate;

    /**
     * 付款方式
     */
    @TableField(value = "payment_method")
    private String paymentMethod;

    /**
     * 付款状态
     */
    @TableField(value = "payment_status")
    private Object paymentStatus;

    /**
     * 应付款日期
     */
    @TableField(value = "due_date")
    private Date dueDate;

    /**
     * 实际付款日期
     */
    @TableField(value = "actual_payment_date")
    private Date actualPaymentDate;

    /**
     * 付款备注
     */
    @TableField(value = "payment_remark")
    private String paymentRemark;

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
        PaymentInfo other = (PaymentInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBillingVoucherId() == null ? other.getBillingVoucherId() == null : this.getBillingVoucherId().equals(other.getBillingVoucherId()))
            && (this.getPayeeUnit() == null ? other.getPayeeUnit() == null : this.getPayeeUnit().equals(other.getPayeeUnit()))
            && (this.getPayeeBankAccount() == null ? other.getPayeeBankAccount() == null : this.getPayeeBankAccount().equals(other.getPayeeBankAccount()))
            && (this.getPayeeBankName() == null ? other.getPayeeBankName() == null : this.getPayeeBankName().equals(other.getPayeeBankName()))
            && (this.getPaymentAmount() == null ? other.getPaymentAmount() == null : this.getPaymentAmount().equals(other.getPaymentAmount()))
            && (this.getPaymentDate() == null ? other.getPaymentDate() == null : this.getPaymentDate().equals(other.getPaymentDate()))
            && (this.getPaymentMethod() == null ? other.getPaymentMethod() == null : this.getPaymentMethod().equals(other.getPaymentMethod()))
            && (this.getPaymentStatus() == null ? other.getPaymentStatus() == null : this.getPaymentStatus().equals(other.getPaymentStatus()))
            && (this.getDueDate() == null ? other.getDueDate() == null : this.getDueDate().equals(other.getDueDate()))
            && (this.getActualPaymentDate() == null ? other.getActualPaymentDate() == null : this.getActualPaymentDate().equals(other.getActualPaymentDate()))
            && (this.getPaymentRemark() == null ? other.getPaymentRemark() == null : this.getPaymentRemark().equals(other.getPaymentRemark()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBillingVoucherId() == null) ? 0 : getBillingVoucherId().hashCode());
        result = prime * result + ((getPayeeUnit() == null) ? 0 : getPayeeUnit().hashCode());
        result = prime * result + ((getPayeeBankAccount() == null) ? 0 : getPayeeBankAccount().hashCode());
        result = prime * result + ((getPayeeBankName() == null) ? 0 : getPayeeBankName().hashCode());
        result = prime * result + ((getPaymentAmount() == null) ? 0 : getPaymentAmount().hashCode());
        result = prime * result + ((getPaymentDate() == null) ? 0 : getPaymentDate().hashCode());
        result = prime * result + ((getPaymentMethod() == null) ? 0 : getPaymentMethod().hashCode());
        result = prime * result + ((getPaymentStatus() == null) ? 0 : getPaymentStatus().hashCode());
        result = prime * result + ((getDueDate() == null) ? 0 : getDueDate().hashCode());
        result = prime * result + ((getActualPaymentDate() == null) ? 0 : getActualPaymentDate().hashCode());
        result = prime * result + ((getPaymentRemark() == null) ? 0 : getPaymentRemark().hashCode());
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
        sb.append(", billingVoucherId=").append(billingVoucherId);
        sb.append(", payeeUnit=").append(payeeUnit);
        sb.append(", payeeBankAccount=").append(payeeBankAccount);
        sb.append(", payeeBankName=").append(payeeBankName);
        sb.append(", paymentAmount=").append(paymentAmount);
        sb.append(", paymentDate=").append(paymentDate);
        sb.append(", paymentMethod=").append(paymentMethod);
        sb.append(", paymentStatus=").append(paymentStatus);
        sb.append(", dueDate=").append(dueDate);
        sb.append(", actualPaymentDate=").append(actualPaymentDate);
        sb.append(", paymentRemark=").append(paymentRemark);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}