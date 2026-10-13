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
 * 引航计划驾驶员分配表
 * @TableName plan_driver_assignments
 */
@TableName(value ="plan_driver_assignments")
@Data
public class PlanDriverAssignments implements Serializable {
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
     * 驾驶员ID
     */
    @TableField(value = "driver_id")
    private Long driverId;

    /**
     * 车辆分配ID（关联的车辆分配）
     */
    @TableField(value = "vehicle_assignment_id")
    private Long vehicleAssignmentId;

    /**
     * 任务类型（DRIVING-驾驶/STANDBY-备勤/EMERGENCY-应急）
     */
    @TableField(value = "task_type")
    private String taskType;

        @TableField(value = "estimated_start_time")
    private Date estimatedStartTime;

        @TableField(value = "estimated_end_time")
    private Date estimatedEndTime;

        @TableField(value = "actual_start_time")
    private Date actualStartTime;

        @TableField(value = "actual_end_time")
    private Date actualEndTime;

    /**
     * 分配状态(ASSIGNED,CONFIRMED,CANCELLED,COMPLETED)
     */
    @TableField(value = "status")
    private String status;

    /**
     * 出勤时长（小时）
     */
    @TableField(value = "working_hours")
    private BigDecimal workingHours;

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
        PlanDriverAssignments other = (PlanDriverAssignments) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPlanId() == null ? other.getPlanId() == null : this.getPlanId().equals(other.getPlanId()))
            && (this.getDriverId() == null ? other.getDriverId() == null : this.getDriverId().equals(other.getDriverId()))
            && (this.getVehicleAssignmentId() == null ? other.getVehicleAssignmentId() == null : this.getVehicleAssignmentId().equals(other.getVehicleAssignmentId()))
            && (this.getTaskType() == null ? other.getTaskType() == null : this.getTaskType().equals(other.getTaskType()))
            && (this.getEstimatedStartTime() == null ? other.getEstimatedStartTime() == null : this.getEstimatedStartTime().equals(other.getEstimatedStartTime()))
            && (this.getEstimatedEndTime() == null ? other.getEstimatedEndTime() == null : this.getEstimatedEndTime().equals(other.getEstimatedEndTime()))
            && (this.getActualStartTime() == null ? other.getActualStartTime() == null : this.getActualStartTime().equals(other.getActualStartTime()))
            && (this.getActualEndTime() == null ? other.getActualEndTime() == null : this.getActualEndTime().equals(other.getActualEndTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getWorkingHours() == null ? other.getWorkingHours() == null : this.getWorkingHours().equals(other.getWorkingHours()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPlanId() == null) ? 0 : getPlanId().hashCode());
        result = prime * result + ((getDriverId() == null) ? 0 : getDriverId().hashCode());
        result = prime * result + ((getVehicleAssignmentId() == null) ? 0 : getVehicleAssignmentId().hashCode());
        result = prime * result + ((getTaskType() == null) ? 0 : getTaskType().hashCode());
        result = prime * result + ((getEstimatedStartTime() == null) ? 0 : getEstimatedStartTime().hashCode());
        result = prime * result + ((getEstimatedEndTime() == null) ? 0 : getEstimatedEndTime().hashCode());
        result = prime * result + ((getActualStartTime() == null) ? 0 : getActualStartTime().hashCode());
        result = prime * result + ((getActualEndTime() == null) ? 0 : getActualEndTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getWorkingHours() == null) ? 0 : getWorkingHours().hashCode());
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
        sb.append(", driverId=").append(driverId);
        sb.append(", vehicleAssignmentId=").append(vehicleAssignmentId);
        sb.append(", taskType=").append(taskType);
        sb.append(", estimatedStartTime=").append(estimatedStartTime);
        sb.append(", estimatedEndTime=").append(estimatedEndTime);
        sb.append(", actualStartTime=").append(actualStartTime);
        sb.append(", actualEndTime=").append(actualEndTime);
        sb.append(", status=").append(status);
        sb.append(", workingHours=").append(workingHours);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}