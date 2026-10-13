package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName plan_confirmations
 */
@TableName(value ="plan_confirmations")
@Data
public class PlanConfirmations implements Serializable {
    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 引航计划ID
     */
    @TableField(value = "plan_id")
    private Long planId;

    /**
     * 资源类型
     */
    @TableField(value = "resource_type")
    private Object resourceType;

    /**
     * 资源ID（引航员/驾驶员/引航艇/拖轮ID）
     */
    @TableField(value = "resource_id")
    private Long resourceId;

        @TableField(value = "confirm_method")
    private Object confirmMethod;

        @TableField(value = "confirmed_by")
    private Long confirmedBy;

        @TableField(value = "confirmed_at")
    private Date confirmedAt;

    /**
     * 
     */
    @TableField(value = "confirmation_status")
    private Object confirmationStatus;

    /**
     * 备注
     */
    @TableField(value = "notes")
    private String notes;

    /**
     * 
     */
    @TableField(value = "created_at")
    private Date createdAt;

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
        PlanConfirmations other = (PlanConfirmations) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPlanId() == null ? other.getPlanId() == null : this.getPlanId().equals(other.getPlanId()))
            && (this.getResourceType() == null ? other.getResourceType() == null : this.getResourceType().equals(other.getResourceType()))
            && (this.getResourceId() == null ? other.getResourceId() == null : this.getResourceId().equals(other.getResourceId()))
            && (this.getConfirmMethod() == null ? other.getConfirmMethod() == null : this.getConfirmMethod().equals(other.getConfirmMethod()))
            && (this.getConfirmedBy() == null ? other.getConfirmedBy() == null : this.getConfirmedBy().equals(other.getConfirmedBy()))
            && (this.getConfirmedAt() == null ? other.getConfirmedAt() == null : this.getConfirmedAt().equals(other.getConfirmedAt()))
            && (this.getConfirmationStatus() == null ? other.getConfirmationStatus() == null : this.getConfirmationStatus().equals(other.getConfirmationStatus()))
            && (this.getNotes() == null ? other.getNotes() == null : this.getNotes().equals(other.getNotes()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPlanId() == null) ? 0 : getPlanId().hashCode());
        result = prime * result + ((getResourceType() == null) ? 0 : getResourceType().hashCode());
        result = prime * result + ((getResourceId() == null) ? 0 : getResourceId().hashCode());
        result = prime * result + ((getConfirmMethod() == null) ? 0 : getConfirmMethod().hashCode());
        result = prime * result + ((getConfirmedBy() == null) ? 0 : getConfirmedBy().hashCode());
        result = prime * result + ((getConfirmedAt() == null) ? 0 : getConfirmedAt().hashCode());
        result = prime * result + ((getConfirmationStatus() == null) ? 0 : getConfirmationStatus().hashCode());
        result = prime * result + ((getNotes() == null) ? 0 : getNotes().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", planId=").append(planId);
        sb.append(", resourceType=").append(resourceType);
        sb.append(", resourceId=").append(resourceId);
        sb.append(", confirmMethod=").append(confirmMethod);
        sb.append(", confirmedBy=").append(confirmedBy);
        sb.append(", confirmedAt=").append(confirmedAt);
        sb.append(", confirmationStatus=").append(confirmationStatus);
        sb.append(", notes=").append(notes);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}