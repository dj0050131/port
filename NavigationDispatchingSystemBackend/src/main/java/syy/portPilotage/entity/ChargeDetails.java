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
 * 收费明细表
 * @TableName charge_details
 */
@TableName(value ="charge_details")
@Data
public class ChargeDetails implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 计费凭证ID
     */
    @TableField(value = "voucher_id")
    private Long voucherId;

    /**
     * 收费明细编号
     */
    @TableField(value = "charge_detail_code")
    private String chargeDetailCode;

    /**
     * 项目ID
     */
    @TableField(value = "item_id")
    private Long itemId;

    /**
     * 标准收费金额
     */
    @TableField(value = "standard_charge_amount")
    private BigDecimal standardChargeAmount;

    /**
     * 实际收费金额
     */
    @TableField(value = "actual_charge_amount")
    private BigDecimal actualChargeAmount;

    /**
     * 备注
     */
    @TableField(value = "notes")
    private String notes;

    /**
     * 收费时间
     */
    @TableField(value = "charge_time")
    private Date chargeTime;

    /**
     * 收费员
     */
    @TableField(value = "charge_person")
    private String chargePerson;

    /**
     * 收费方式
     */
    @TableField(value = "charge_method")
    private String chargeMethod;

    /**
     * 票据号码
     */
    @TableField(value = "receipt_number")
    private String receiptNumber;

    /**
     * 优惠原因
     */
    @TableField(value = "discount_reason")
    private String discountReason;

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
        ChargeDetails other = (ChargeDetails) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getVoucherId() == null ? other.getVoucherId() == null : this.getVoucherId().equals(other.getVoucherId()))
            && (this.getChargeDetailCode() == null ? other.getChargeDetailCode() == null : this.getChargeDetailCode().equals(other.getChargeDetailCode()))
            && (this.getItemId() == null ? other.getItemId() == null : this.getItemId().equals(other.getItemId()))
            && (this.getStandardChargeAmount() == null ? other.getStandardChargeAmount() == null : this.getStandardChargeAmount().equals(other.getStandardChargeAmount()))
            && (this.getActualChargeAmount() == null ? other.getActualChargeAmount() == null : this.getActualChargeAmount().equals(other.getActualChargeAmount()))
            && (this.getNotes() == null ? other.getNotes() == null : this.getNotes().equals(other.getNotes()))
            && (this.getChargeTime() == null ? other.getChargeTime() == null : this.getChargeTime().equals(other.getChargeTime()))
            && (this.getChargePerson() == null ? other.getChargePerson() == null : this.getChargePerson().equals(other.getChargePerson()))
            && (this.getChargeMethod() == null ? other.getChargeMethod() == null : this.getChargeMethod().equals(other.getChargeMethod()))
            && (this.getReceiptNumber() == null ? other.getReceiptNumber() == null : this.getReceiptNumber().equals(other.getReceiptNumber()))
            && (this.getDiscountReason() == null ? other.getDiscountReason() == null : this.getDiscountReason().equals(other.getDiscountReason()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getVoucherId() == null) ? 0 : getVoucherId().hashCode());
        result = prime * result + ((getChargeDetailCode() == null) ? 0 : getChargeDetailCode().hashCode());
        result = prime * result + ((getItemId() == null) ? 0 : getItemId().hashCode());
        result = prime * result + ((getStandardChargeAmount() == null) ? 0 : getStandardChargeAmount().hashCode());
        result = prime * result + ((getActualChargeAmount() == null) ? 0 : getActualChargeAmount().hashCode());
        result = prime * result + ((getNotes() == null) ? 0 : getNotes().hashCode());
        result = prime * result + ((getChargeTime() == null) ? 0 : getChargeTime().hashCode());
        result = prime * result + ((getChargePerson() == null) ? 0 : getChargePerson().hashCode());
        result = prime * result + ((getChargeMethod() == null) ? 0 : getChargeMethod().hashCode());
        result = prime * result + ((getReceiptNumber() == null) ? 0 : getReceiptNumber().hashCode());
        result = prime * result + ((getDiscountReason() == null) ? 0 : getDiscountReason().hashCode());
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
        sb.append(", voucherId=").append(voucherId);
        sb.append(", chargeDetailCode=").append(chargeDetailCode);
        sb.append(", itemId=").append(itemId);
        sb.append(", standardChargeAmount=").append(standardChargeAmount);
        sb.append(", actualChargeAmount=").append(actualChargeAmount);
        sb.append(", notes=").append(notes);
        sb.append(", chargeTime=").append(chargeTime);
        sb.append(", chargePerson=").append(chargePerson);
        sb.append(", chargeMethod=").append(chargeMethod);
        sb.append(", receiptNumber=").append(receiptNumber);
        sb.append(", discountReason=").append(discountReason);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}