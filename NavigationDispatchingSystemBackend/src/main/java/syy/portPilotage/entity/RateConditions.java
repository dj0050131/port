package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@TableName(value ="rate_conditions")
@Data
public class RateConditions implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联原standard_rates表的ID
     */
    @TableField(value = "standard_rate_id")
    private Long standardRateId;

        @TableField(value = "condition_type")
    private String conditionType;

        @TableField(value = "condition_operator")
    private String conditionOperator;

        @TableField(value = "condition_value")
    private String conditionValue;

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
        RateConditions other = (RateConditions) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getStandardRateId() == null ? other.getStandardRateId() == null : this.getStandardRateId().equals(other.getStandardRateId()))
            && (this.getConditionType() == null ? other.getConditionType() == null : this.getConditionType().equals(other.getConditionType()))
            && (this.getConditionOperator() == null ? other.getConditionOperator() == null : this.getConditionOperator().equals(other.getConditionOperator()))
            && (this.getConditionValue() == null ? other.getConditionValue() == null : this.getConditionValue().equals(other.getConditionValue()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getStandardRateId() == null) ? 0 : getStandardRateId().hashCode());
        result = prime * result + ((getConditionType() == null) ? 0 : getConditionType().hashCode());
        result = prime * result + ((getConditionOperator() == null) ? 0 : getConditionOperator().hashCode());
        result = prime * result + ((getConditionValue() == null) ? 0 : getConditionValue().hashCode());
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
        sb.append(", standardRateId=").append(standardRateId);
        sb.append(", conditionType=").append(conditionType);
        sb.append(", conditionOperator=").append(conditionOperator);
        sb.append(", conditionValue=").append(conditionValue);
        sb.append(", description=").append(description);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}