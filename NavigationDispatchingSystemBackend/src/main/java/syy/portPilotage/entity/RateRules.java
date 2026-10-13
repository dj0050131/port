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
 * 费率规则表
 * @TableName rate_rules
 */
@TableName(value ="rate_rules")
@Data
public class RateRules implements Serializable {
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

    /**
     * 费率规则类型ID
     */
    @TableField(value = "rule_type_id")
    private Long ruleTypeId;

    /**
     * 费率值
     */
    @TableField(value = "rate_value")
    private BigDecimal rateValue;

    /**
     * 适用船舶最小净吨（如果为空，则继承原表的）
     */
    @TableField(value = "applicable_vessel_min_net_ton")
    private BigDecimal applicableVesselMinNetTon;

    /**
     * 适用船舶最大净吨（如果为空，则继承原表的）
     */
    @TableField(value = "applicable_vessel_max_net_ton")
    private BigDecimal applicableVesselMaxNetTon;

    /**
     * 备注
     */
    @TableField(value = "notes")
    private String notes;

        @TableField(value = "is_active")
    private Integer isActive;

    /**
     * 版本号
     */
    @TableField(value = "version")
    private Integer version;

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
        RateRules other = (RateRules) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getStandardRateId() == null ? other.getStandardRateId() == null : this.getStandardRateId().equals(other.getStandardRateId()))
            && (this.getRuleTypeId() == null ? other.getRuleTypeId() == null : this.getRuleTypeId().equals(other.getRuleTypeId()))
            && (this.getRateValue() == null ? other.getRateValue() == null : this.getRateValue().equals(other.getRateValue()))
            && (this.getApplicableVesselMinNetTon() == null ? other.getApplicableVesselMinNetTon() == null : this.getApplicableVesselMinNetTon().equals(other.getApplicableVesselMinNetTon()))
            && (this.getApplicableVesselMaxNetTon() == null ? other.getApplicableVesselMaxNetTon() == null : this.getApplicableVesselMaxNetTon().equals(other.getApplicableVesselMaxNetTon()))
            && (this.getNotes() == null ? other.getNotes() == null : this.getNotes().equals(other.getNotes()))
            && (this.getIsActive() == null ? other.getIsActive() == null : this.getIsActive().equals(other.getIsActive()))
            && (this.getVersion() == null ? other.getVersion() == null : this.getVersion().equals(other.getVersion()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getStandardRateId() == null) ? 0 : getStandardRateId().hashCode());
        result = prime * result + ((getRuleTypeId() == null) ? 0 : getRuleTypeId().hashCode());
        result = prime * result + ((getRateValue() == null) ? 0 : getRateValue().hashCode());
        result = prime * result + ((getApplicableVesselMinNetTon() == null) ? 0 : getApplicableVesselMinNetTon().hashCode());
        result = prime * result + ((getApplicableVesselMaxNetTon() == null) ? 0 : getApplicableVesselMaxNetTon().hashCode());
        result = prime * result + ((getNotes() == null) ? 0 : getNotes().hashCode());
        result = prime * result + ((getIsActive() == null) ? 0 : getIsActive().hashCode());
        result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
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
        sb.append(", ruleTypeId=").append(ruleTypeId);
        sb.append(", rateValue=").append(rateValue);
        sb.append(", applicableVesselMinNetTon=").append(applicableVesselMinNetTon);
        sb.append(", applicableVesselMaxNetTon=").append(applicableVesselMaxNetTon);
        sb.append(", notes=").append(notes);
        sb.append(", isActive=").append(isActive);
        sb.append(", version=").append(version);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}