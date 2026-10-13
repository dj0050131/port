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
 * 税种明细表
 * @TableName tax_item_details
 */
@TableName(value ="tax_item_details")
@Data
public class TaxItemDetails implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

        @TableField(value = "tax_record_id")
    private Long taxRecordId;

        @TableField(value = "tax_item_code")
    private String taxItemCode;

    /**
     * 税种名称
     */
    @TableField(value = "tax_item_name")
    private String taxItemName;

    /**
     * 税种类型：主税、附加税、其他
     */
    @TableField(value = "tax_type")
    private Object taxType;

    /**
     * 税率（如：0.09表示9%）
     */
    @TableField(value = "tax_rate")
    private BigDecimal taxRate;

    /**
     * 计税基数
     */
    @TableField(value = "taxable_base")
    private BigDecimal taxableBase;

    /**
     * 税额
     */
    @TableField(value = "tax_amount")
    private BigDecimal taxAmount;

    /**
     * 税收政策依据
     */
    @TableField(value = "tax_policy_reference")
    private String taxPolicyReference;

    /**
     * 是否享受优惠
     */
    @TableField(value = "is_preferential")
    private Integer isPreferential;

    /**
     * 优惠税率
     */
    @TableField(value = "preferential_rate")
    private BigDecimal preferentialRate;

    /**
     * 优惠减免金额
     */
    @TableField(value = "preferential_amount")
    private BigDecimal preferentialAmount;

    /**
     * 优惠政策依据
     */
    @TableField(value = "preferential_policy")
    private String preferentialPolicy;

    /**
     * 是否经过调整
     */
    @TableField(value = "is_adjusted")
    private Integer isAdjusted;

    /**
     * 调整原因
     */
    @TableField(value = "adjustment_reason")
    private String adjustmentReason;

    /**
     * 调整前税额
     */
    @TableField(value = "previous_tax_amount")
    private BigDecimal previousTaxAmount;

        @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 备注
     */
    @TableField(value = "notes")
    private String notes;

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
        TaxItemDetails other = (TaxItemDetails) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTaxRecordId() == null ? other.getTaxRecordId() == null : this.getTaxRecordId().equals(other.getTaxRecordId()))
            && (this.getTaxItemCode() == null ? other.getTaxItemCode() == null : this.getTaxItemCode().equals(other.getTaxItemCode()))
            && (this.getTaxItemName() == null ? other.getTaxItemName() == null : this.getTaxItemName().equals(other.getTaxItemName()))
            && (this.getTaxType() == null ? other.getTaxType() == null : this.getTaxType().equals(other.getTaxType()))
            && (this.getTaxRate() == null ? other.getTaxRate() == null : this.getTaxRate().equals(other.getTaxRate()))
            && (this.getTaxableBase() == null ? other.getTaxableBase() == null : this.getTaxableBase().equals(other.getTaxableBase()))
            && (this.getTaxAmount() == null ? other.getTaxAmount() == null : this.getTaxAmount().equals(other.getTaxAmount()))
            && (this.getTaxPolicyReference() == null ? other.getTaxPolicyReference() == null : this.getTaxPolicyReference().equals(other.getTaxPolicyReference()))
            && (this.getIsPreferential() == null ? other.getIsPreferential() == null : this.getIsPreferential().equals(other.getIsPreferential()))
            && (this.getPreferentialRate() == null ? other.getPreferentialRate() == null : this.getPreferentialRate().equals(other.getPreferentialRate()))
            && (this.getPreferentialAmount() == null ? other.getPreferentialAmount() == null : this.getPreferentialAmount().equals(other.getPreferentialAmount()))
            && (this.getPreferentialPolicy() == null ? other.getPreferentialPolicy() == null : this.getPreferentialPolicy().equals(other.getPreferentialPolicy()))
            && (this.getIsAdjusted() == null ? other.getIsAdjusted() == null : this.getIsAdjusted().equals(other.getIsAdjusted()))
            && (this.getAdjustmentReason() == null ? other.getAdjustmentReason() == null : this.getAdjustmentReason().equals(other.getAdjustmentReason()))
            && (this.getPreviousTaxAmount() == null ? other.getPreviousTaxAmount() == null : this.getPreviousTaxAmount().equals(other.getPreviousTaxAmount()))
            && (this.getSortOrder() == null ? other.getSortOrder() == null : this.getSortOrder().equals(other.getSortOrder()))
            && (this.getNotes() == null ? other.getNotes() == null : this.getNotes().equals(other.getNotes()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTaxRecordId() == null) ? 0 : getTaxRecordId().hashCode());
        result = prime * result + ((getTaxItemCode() == null) ? 0 : getTaxItemCode().hashCode());
        result = prime * result + ((getTaxItemName() == null) ? 0 : getTaxItemName().hashCode());
        result = prime * result + ((getTaxType() == null) ? 0 : getTaxType().hashCode());
        result = prime * result + ((getTaxRate() == null) ? 0 : getTaxRate().hashCode());
        result = prime * result + ((getTaxableBase() == null) ? 0 : getTaxableBase().hashCode());
        result = prime * result + ((getTaxAmount() == null) ? 0 : getTaxAmount().hashCode());
        result = prime * result + ((getTaxPolicyReference() == null) ? 0 : getTaxPolicyReference().hashCode());
        result = prime * result + ((getIsPreferential() == null) ? 0 : getIsPreferential().hashCode());
        result = prime * result + ((getPreferentialRate() == null) ? 0 : getPreferentialRate().hashCode());
        result = prime * result + ((getPreferentialAmount() == null) ? 0 : getPreferentialAmount().hashCode());
        result = prime * result + ((getPreferentialPolicy() == null) ? 0 : getPreferentialPolicy().hashCode());
        result = prime * result + ((getIsAdjusted() == null) ? 0 : getIsAdjusted().hashCode());
        result = prime * result + ((getAdjustmentReason() == null) ? 0 : getAdjustmentReason().hashCode());
        result = prime * result + ((getPreviousTaxAmount() == null) ? 0 : getPreviousTaxAmount().hashCode());
        result = prime * result + ((getSortOrder() == null) ? 0 : getSortOrder().hashCode());
        result = prime * result + ((getNotes() == null) ? 0 : getNotes().hashCode());
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
        sb.append(", taxRecordId=").append(taxRecordId);
        sb.append(", taxItemCode=").append(taxItemCode);
        sb.append(", taxItemName=").append(taxItemName);
        sb.append(", taxType=").append(taxType);
        sb.append(", taxRate=").append(taxRate);
        sb.append(", taxableBase=").append(taxableBase);
        sb.append(", taxAmount=").append(taxAmount);
        sb.append(", taxPolicyReference=").append(taxPolicyReference);
        sb.append(", isPreferential=").append(isPreferential);
        sb.append(", preferentialRate=").append(preferentialRate);
        sb.append(", preferentialAmount=").append(preferentialAmount);
        sb.append(", preferentialPolicy=").append(preferentialPolicy);
        sb.append(", isAdjusted=").append(isAdjusted);
        sb.append(", adjustmentReason=").append(adjustmentReason);
        sb.append(", previousTaxAmount=").append(previousTaxAmount);
        sb.append(", sortOrder=").append(sortOrder);
        sb.append(", notes=").append(notes);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}