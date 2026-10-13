package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 费率规则类型表
 * @TableName rate_rule_types
 */
@TableName(value ="rate_rule_types")
@Data
public class RateRuleTypes implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

        @TableField(value = "rule_type_code")
    private String ruleTypeCode;

    /**
     * 规则类型名称
     */
    @TableField(value = "rule_type_name")
    private String ruleTypeName;

        @TableField(value = "calculation_method")
    private String calculationMethod;

    /**
     * 关联的收费项目ID（用于计费明细）
     */
    @TableField(value = "charge_item_id")
    private Long chargeItemId;

    /**
     * 是否系统内置
     */
    @TableField(value = "is_system")
    private Integer isSystem;

        @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

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
        RateRuleTypes other = (RateRuleTypes) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getRuleTypeCode() == null ? other.getRuleTypeCode() == null : this.getRuleTypeCode().equals(other.getRuleTypeCode()))
            && (this.getRuleTypeName() == null ? other.getRuleTypeName() == null : this.getRuleTypeName().equals(other.getRuleTypeName()))
            && (this.getCalculationMethod() == null ? other.getCalculationMethod() == null : this.getCalculationMethod().equals(other.getCalculationMethod()))
            && (this.getChargeItemId() == null ? other.getChargeItemId() == null : this.getChargeItemId().equals(other.getChargeItemId()))
            && (this.getIsSystem() == null ? other.getIsSystem() == null : this.getIsSystem().equals(other.getIsSystem()))
            && (this.getSortOrder() == null ? other.getSortOrder() == null : this.getSortOrder().equals(other.getSortOrder()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getRuleTypeCode() == null) ? 0 : getRuleTypeCode().hashCode());
        result = prime * result + ((getRuleTypeName() == null) ? 0 : getRuleTypeName().hashCode());
        result = prime * result + ((getCalculationMethod() == null) ? 0 : getCalculationMethod().hashCode());
        result = prime * result + ((getChargeItemId() == null) ? 0 : getChargeItemId().hashCode());
        result = prime * result + ((getIsSystem() == null) ? 0 : getIsSystem().hashCode());
        result = prime * result + ((getSortOrder() == null) ? 0 : getSortOrder().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
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
        sb.append(", ruleTypeCode=").append(ruleTypeCode);
        sb.append(", ruleTypeName=").append(ruleTypeName);
        sb.append(", calculationMethod=").append(calculationMethod);
        sb.append(", chargeItemId=").append(chargeItemId);
        sb.append(", isSystem=").append(isSystem);
        sb.append(", sortOrder=").append(sortOrder);
        sb.append(", description=").append(description);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}