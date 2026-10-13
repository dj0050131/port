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
 * 代理公司信息表
 * @TableName agents
 */
@TableName(value ="agents")
@Data
public class Agents implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 代理公司代码
     */
    @TableField(value = "agent_code")
    private String agentCode;

    /**
     * 代理公司名称
     */
    @TableField(value = "company_name")
    private String companyName;

    /**
     * 代理公司简称
     */
    @TableField(value = "short_name")
    private String shortName;

    /**
     * 负责人
     */
    @TableField(value = "principal")
    private String principal;

    /**
     * 联系人
     */
    @TableField(value = "contact_person")
    private String contactPerson;

    /**
     * 联系电话
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 公司地址
     */
    @TableField(value = "address")
    private String address;

    /**
     * E-mail
     */
    @TableField(value = "email")
    private String email;

    /**
     * 信用等级
     */
    @TableField(value = "credit_rating")
    private Object creditRating;

    /**
     * 缴费状态
     */
    @TableField(value = "payment_status")
    private Object paymentStatus;

    /**
     * 营业执照号
     */
    @TableField(value = "business_license")
    private String businessLicense;

        @TableField(value = "registered_capital")
    private BigDecimal registeredCapital;

        @TableField(value = "default_taxpayer_id")
    private String defaultTaxpayerId;

        @TableField(value = "default_bank_name")
    private String defaultBankName;

        @TableField(value = "default_bank_account")
    private String defaultBankAccount;

        @TableField(value = "default_address")
    private String defaultAddress;

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
        Agents other = (Agents) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAgentCode() == null ? other.getAgentCode() == null : this.getAgentCode().equals(other.getAgentCode()))
            && (this.getCompanyName() == null ? other.getCompanyName() == null : this.getCompanyName().equals(other.getCompanyName()))
            && (this.getShortName() == null ? other.getShortName() == null : this.getShortName().equals(other.getShortName()))
            && (this.getPrincipal() == null ? other.getPrincipal() == null : this.getPrincipal().equals(other.getPrincipal()))
            && (this.getContactPerson() == null ? other.getContactPerson() == null : this.getContactPerson().equals(other.getContactPerson()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getAddress() == null ? other.getAddress() == null : this.getAddress().equals(other.getAddress()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getCreditRating() == null ? other.getCreditRating() == null : this.getCreditRating().equals(other.getCreditRating()))
            && (this.getPaymentStatus() == null ? other.getPaymentStatus() == null : this.getPaymentStatus().equals(other.getPaymentStatus()))
            && (this.getBusinessLicense() == null ? other.getBusinessLicense() == null : this.getBusinessLicense().equals(other.getBusinessLicense()))
            && (this.getRegisteredCapital() == null ? other.getRegisteredCapital() == null : this.getRegisteredCapital().equals(other.getRegisteredCapital()))
            && (this.getDefaultTaxpayerId() == null ? other.getDefaultTaxpayerId() == null : this.getDefaultTaxpayerId().equals(other.getDefaultTaxpayerId()))
            && (this.getDefaultBankName() == null ? other.getDefaultBankName() == null : this.getDefaultBankName().equals(other.getDefaultBankName()))
            && (this.getDefaultBankAccount() == null ? other.getDefaultBankAccount() == null : this.getDefaultBankAccount().equals(other.getDefaultBankAccount()))
            && (this.getDefaultAddress() == null ? other.getDefaultAddress() == null : this.getDefaultAddress().equals(other.getDefaultAddress()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAgentCode() == null) ? 0 : getAgentCode().hashCode());
        result = prime * result + ((getCompanyName() == null) ? 0 : getCompanyName().hashCode());
        result = prime * result + ((getShortName() == null) ? 0 : getShortName().hashCode());
        result = prime * result + ((getPrincipal() == null) ? 0 : getPrincipal().hashCode());
        result = prime * result + ((getContactPerson() == null) ? 0 : getContactPerson().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getAddress() == null) ? 0 : getAddress().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getCreditRating() == null) ? 0 : getCreditRating().hashCode());
        result = prime * result + ((getPaymentStatus() == null) ? 0 : getPaymentStatus().hashCode());
        result = prime * result + ((getBusinessLicense() == null) ? 0 : getBusinessLicense().hashCode());
        result = prime * result + ((getRegisteredCapital() == null) ? 0 : getRegisteredCapital().hashCode());
        result = prime * result + ((getDefaultTaxpayerId() == null) ? 0 : getDefaultTaxpayerId().hashCode());
        result = prime * result + ((getDefaultBankName() == null) ? 0 : getDefaultBankName().hashCode());
        result = prime * result + ((getDefaultBankAccount() == null) ? 0 : getDefaultBankAccount().hashCode());
        result = prime * result + ((getDefaultAddress() == null) ? 0 : getDefaultAddress().hashCode());
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
        sb.append(", agentCode=").append(agentCode);
        sb.append(", companyName=").append(companyName);
        sb.append(", shortName=").append(shortName);
        sb.append(", principal=").append(principal);
        sb.append(", contactPerson=").append(contactPerson);
        sb.append(", phone=").append(phone);
        sb.append(", address=").append(address);
        sb.append(", email=").append(email);
        sb.append(", creditRating=").append(creditRating);
        sb.append(", paymentStatus=").append(paymentStatus);
        sb.append(", businessLicense=").append(businessLicense);
        sb.append(", registeredCapital=").append(registeredCapital);
        sb.append(", defaultTaxpayerId=").append(defaultTaxpayerId);
        sb.append(", defaultBankName=").append(defaultBankName);
        sb.append(", defaultBankAccount=").append(defaultBankAccount);
        sb.append(", defaultAddress=").append(defaultAddress);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}