package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import syy.portPilotage.util.jackson.MultiFormatDateDeserializer;

/**
 * 标准费率信息表
 * @TableName standard_rates
 */
@TableName(value ="standard_rates")
@Data
public class StandardRates implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标准费率编号
     */
    @TableField(value = "rate_code")
    private String rateCode;

    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    private Long categoryId;

    /**
     * 类型编号
     */
    @TableField(value = "type_id")
    private String typeId;

        @TableField(value = "effective_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date effectiveDate;

        @TableField(value = "expiry_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Shanghai")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date expiryDate;

    /**
     * 区间ID
     */
    @TableField(value = "route_id")
    private Long routeId;

    /**
     * 海里数
     */
    @TableField(value = "nautical_miles")
    private BigDecimal nauticalMiles;

    /**
     * 备注
     */
    @TableField(value = "notes")
    private String notes;

    /**
     * 适用船舶最小净吨
     */
    @TableField(value = "applicable_vessel_min_net_ton")
    private BigDecimal applicableVesselMinNetTon;

    /**
     * 适用船舶最大净吨
     */
    @TableField(value = "applicable_vessel_max_net_ton")
    private BigDecimal applicableVesselMaxNetTon;

    /**
     * 状态
     */
    @TableField(value = "rate_status")
    private Object rateStatus;

    /**
     * 货币单位
     */
    @TableField(value = "currency_unit")
    private String currencyUnit;

        @TableField(value = "created_by")
    private Long createdBy;

        @TableField(value = "updated_by")
    private Long updatedBy;

        @TableField(value = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date createdAt;

        @TableField(value = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    private Date updatedAt;

    /**
     * 关联的费率规则数量
     */
    @TableField(value = "rule_count")
    private Integer ruleCount;

        @TableField(value = "condition_count")
    private Integer conditionCount;

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
        StandardRates other = (StandardRates) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getRateCode() == null ? other.getRateCode() == null : this.getRateCode().equals(other.getRateCode()))
            && (this.getCategoryId() == null ? other.getCategoryId() == null : this.getCategoryId().equals(other.getCategoryId()))
            && (this.getTypeId() == null ? other.getTypeId() == null : this.getTypeId().equals(other.getTypeId()))
            && (this.getEffectiveDate() == null ? other.getEffectiveDate() == null : this.getEffectiveDate().equals(other.getEffectiveDate()))
            && (this.getExpiryDate() == null ? other.getExpiryDate() == null : this.getExpiryDate().equals(other.getExpiryDate()))
            && (this.getRouteId() == null ? other.getRouteId() == null : this.getRouteId().equals(other.getRouteId()))
            && (this.getNauticalMiles() == null ? other.getNauticalMiles() == null : this.getNauticalMiles().equals(other.getNauticalMiles()))
            && (this.getNotes() == null ? other.getNotes() == null : this.getNotes().equals(other.getNotes()))
            && (this.getApplicableVesselMinNetTon() == null ? other.getApplicableVesselMinNetTon() == null : this.getApplicableVesselMinNetTon().equals(other.getApplicableVesselMinNetTon()))
            && (this.getApplicableVesselMaxNetTon() == null ? other.getApplicableVesselMaxNetTon() == null : this.getApplicableVesselMaxNetTon().equals(other.getApplicableVesselMaxNetTon()))
            && (this.getRateStatus() == null ? other.getRateStatus() == null : this.getRateStatus().equals(other.getRateStatus()))
            && (this.getCurrencyUnit() == null ? other.getCurrencyUnit() == null : this.getCurrencyUnit().equals(other.getCurrencyUnit()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()))
            && (this.getRuleCount() == null ? other.getRuleCount() == null : this.getRuleCount().equals(other.getRuleCount()))
            && (this.getConditionCount() == null ? other.getConditionCount() == null : this.getConditionCount().equals(other.getConditionCount()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getRateCode() == null) ? 0 : getRateCode().hashCode());
        result = prime * result + ((getCategoryId() == null) ? 0 : getCategoryId().hashCode());
        result = prime * result + ((getTypeId() == null) ? 0 : getTypeId().hashCode());
        result = prime * result + ((getEffectiveDate() == null) ? 0 : getEffectiveDate().hashCode());
        result = prime * result + ((getExpiryDate() == null) ? 0 : getExpiryDate().hashCode());
        result = prime * result + ((getRouteId() == null) ? 0 : getRouteId().hashCode());
        result = prime * result + ((getNauticalMiles() == null) ? 0 : getNauticalMiles().hashCode());
        result = prime * result + ((getNotes() == null) ? 0 : getNotes().hashCode());
        result = prime * result + ((getApplicableVesselMinNetTon() == null) ? 0 : getApplicableVesselMinNetTon().hashCode());
        result = prime * result + ((getApplicableVesselMaxNetTon() == null) ? 0 : getApplicableVesselMaxNetTon().hashCode());
        result = prime * result + ((getRateStatus() == null) ? 0 : getRateStatus().hashCode());
        result = prime * result + ((getCurrencyUnit() == null) ? 0 : getCurrencyUnit().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getRuleCount() == null) ? 0 : getRuleCount().hashCode());
        result = prime * result + ((getConditionCount() == null) ? 0 : getConditionCount().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", rateCode=").append(rateCode);
        sb.append(", categoryId=").append(categoryId);
        sb.append(", typeId=").append(typeId);
        sb.append(", effectiveDate=").append(effectiveDate);
        sb.append(", expiryDate=").append(expiryDate);
        sb.append(", routeId=").append(routeId);
        sb.append(", nauticalMiles=").append(nauticalMiles);
        sb.append(", notes=").append(notes);
        sb.append(", applicableVesselMinNetTon=").append(applicableVesselMinNetTon);
        sb.append(", applicableVesselMaxNetTon=").append(applicableVesselMaxNetTon);
        sb.append(", rateStatus=").append(rateStatus);
        sb.append(", currencyUnit=").append(currencyUnit);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", ruleCount=").append(ruleCount);
        sb.append(", conditionCount=").append(conditionCount);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}