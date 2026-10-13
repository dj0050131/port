package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 引航计划拖轮分配表
 * @TableName plan_tugboat_assignments
 */
@TableName(value ="plan_tugboat_assignments")
@Data
public class PlanTugboatAssignments implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 引航计划ID
     */
    @TableField(value = "plan_id")
    private Long planId;

    /**
     * 拖轮ID
     */
    @TableField(value = "tugboat_id")
    private Long tugboatId;

    /**
     * 分配类型(MAIN/BACKUP)
     */
    @TableField(value = "assignment_type")
    private String assignmentType;

        @TableField(value = "estimated_start_time")
    private Date estimatedStartTime;

        @TableField(value = "estimated_end_time")
    private Date estimatedEndTime;

        @TableField(value = "actual_start_time")
    private Date actualStartTime;

        @TableField(value = "actual_end_time")
    private Date actualEndTime;

    /**
     * 分配状态
     */
    @TableField(value = "status")
    private Object status;

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
        PlanTugboatAssignments other = (PlanTugboatAssignments) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPlanId() == null ? other.getPlanId() == null : this.getPlanId().equals(other.getPlanId()))
            && (this.getTugboatId() == null ? other.getTugboatId() == null : this.getTugboatId().equals(other.getTugboatId()))
            && (this.getAssignmentType() == null ? other.getAssignmentType() == null : this.getAssignmentType().equals(other.getAssignmentType()))
            && (this.getEstimatedStartTime() == null ? other.getEstimatedStartTime() == null : this.getEstimatedStartTime().equals(other.getEstimatedStartTime()))
            && (this.getEstimatedEndTime() == null ? other.getEstimatedEndTime() == null : this.getEstimatedEndTime().equals(other.getEstimatedEndTime()))
            && (this.getActualStartTime() == null ? other.getActualStartTime() == null : this.getActualStartTime().equals(other.getActualStartTime()))
            && (this.getActualEndTime() == null ? other.getActualEndTime() == null : this.getActualEndTime().equals(other.getActualEndTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPlanId() == null) ? 0 : getPlanId().hashCode());
        result = prime * result + ((getTugboatId() == null) ? 0 : getTugboatId().hashCode());
        result = prime * result + ((getAssignmentType() == null) ? 0 : getAssignmentType().hashCode());
        result = prime * result + ((getEstimatedStartTime() == null) ? 0 : getEstimatedStartTime().hashCode());
        result = prime * result + ((getEstimatedEndTime() == null) ? 0 : getEstimatedEndTime().hashCode());
        result = prime * result + ((getActualStartTime() == null) ? 0 : getActualStartTime().hashCode());
        result = prime * result + ((getActualEndTime() == null) ? 0 : getActualEndTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
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
        sb.append(", planId=").append(planId);
        sb.append(", tugboatId=").append(tugboatId);
        sb.append(", assignmentType=").append(assignmentType);
        sb.append(", estimatedStartTime=").append(estimatedStartTime);
        sb.append(", estimatedEndTime=").append(estimatedEndTime);
        sb.append(", actualStartTime=").append(actualStartTime);
        sb.append(", actualEndTime=").append(actualEndTime);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}