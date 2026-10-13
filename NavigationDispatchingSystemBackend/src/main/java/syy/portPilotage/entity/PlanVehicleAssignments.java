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
 * 引航计划车辆分配表
 * @TableName plan_vehicle_assignments
 */
@TableName(value ="plan_vehicle_assignments")
@Data
public class PlanVehicleAssignments implements Serializable {
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
     * 车辆ID
     */
    @TableField(value = "vehicle_id")
    private Long vehicleId;

    /**
     * 用途（PILOT_TRANSPORT-接送引航员/MATERIAL_TRANSPORT-物资运输/EMERGENCY_RESPONSE-应急响应/OTHER-其他）
     */
    @TableField(value = "purpose")
    private String purpose;

    /**
     * 用车需求说明
     */
    @TableField(value = "requirements")
    private String requirements;

        @TableField(value = "estimated_start_time")
    private Date estimatedStartTime;

        @TableField(value = "estimated_end_time")
    private Date estimatedEndTime;

        @TableField(value = "actual_start_time")
    private Date actualStartTime;

        @TableField(value = "actual_end_time")
    private Date actualEndTime;

    /**
     * 实际里程（公里）
     */
    @TableField(value = "actual_mileage")
    private BigDecimal actualMileage;

    /**
     * 分配状态(ASSIGNED,CONFIRMED,CANCELLED,COMPLETED)
     */
    @TableField(value = "status")
    private String status;

    /**
     * 油料消耗（升）
     */
    @TableField(value = "fuel_consumption")
    private BigDecimal fuelConsumption;

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
        PlanVehicleAssignments other = (PlanVehicleAssignments) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPlanId() == null ? other.getPlanId() == null : this.getPlanId().equals(other.getPlanId()))
            && (this.getVehicleId() == null ? other.getVehicleId() == null : this.getVehicleId().equals(other.getVehicleId()))
            && (this.getPurpose() == null ? other.getPurpose() == null : this.getPurpose().equals(other.getPurpose()))
            && (this.getRequirements() == null ? other.getRequirements() == null : this.getRequirements().equals(other.getRequirements()))
            && (this.getEstimatedStartTime() == null ? other.getEstimatedStartTime() == null : this.getEstimatedStartTime().equals(other.getEstimatedStartTime()))
            && (this.getEstimatedEndTime() == null ? other.getEstimatedEndTime() == null : this.getEstimatedEndTime().equals(other.getEstimatedEndTime()))
            && (this.getActualStartTime() == null ? other.getActualStartTime() == null : this.getActualStartTime().equals(other.getActualStartTime()))
            && (this.getActualEndTime() == null ? other.getActualEndTime() == null : this.getActualEndTime().equals(other.getActualEndTime()))
            && (this.getActualMileage() == null ? other.getActualMileage() == null : this.getActualMileage().equals(other.getActualMileage()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getFuelConsumption() == null ? other.getFuelConsumption() == null : this.getFuelConsumption().equals(other.getFuelConsumption()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPlanId() == null) ? 0 : getPlanId().hashCode());
        result = prime * result + ((getVehicleId() == null) ? 0 : getVehicleId().hashCode());
        result = prime * result + ((getPurpose() == null) ? 0 : getPurpose().hashCode());
        result = prime * result + ((getRequirements() == null) ? 0 : getRequirements().hashCode());
        result = prime * result + ((getEstimatedStartTime() == null) ? 0 : getEstimatedStartTime().hashCode());
        result = prime * result + ((getEstimatedEndTime() == null) ? 0 : getEstimatedEndTime().hashCode());
        result = prime * result + ((getActualStartTime() == null) ? 0 : getActualStartTime().hashCode());
        result = prime * result + ((getActualEndTime() == null) ? 0 : getActualEndTime().hashCode());
        result = prime * result + ((getActualMileage() == null) ? 0 : getActualMileage().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getFuelConsumption() == null) ? 0 : getFuelConsumption().hashCode());
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
        sb.append(", vehicleId=").append(vehicleId);
        sb.append(", purpose=").append(purpose);
        sb.append(", requirements=").append(requirements);
        sb.append(", estimatedStartTime=").append(estimatedStartTime);
        sb.append(", estimatedEndTime=").append(estimatedEndTime);
        sb.append(", actualStartTime=").append(actualStartTime);
        sb.append(", actualEndTime=").append(actualEndTime);
        sb.append(", actualMileage=").append(actualMileage);
        sb.append(", status=").append(status);
        sb.append(", fuelConsumption=").append(fuelConsumption);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}