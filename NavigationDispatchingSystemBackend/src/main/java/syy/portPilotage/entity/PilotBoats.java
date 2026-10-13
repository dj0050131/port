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
 * 引航艇基本信息表
 * @TableName pilot_boats
 */
@TableName(value ="pilot_boats")
@Data
public class PilotBoats implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 船舶呼号
     */
    @TableField(value = "vessel_call_sign")
    private String vesselCallSign;

    /**
     * 艇名
     */
    @TableField(value = "boat_name")
    private String boatName;

    /**
     * IMO编号
     */
    @TableField(value = "imo_number")
    private String imoNumber;

    /**
     * 作业范围
     */
    @TableField(value = "operation_range")
    private String operationRange;

    /**
     * 主机型号
     */
    @TableField(value = "engine_model")
    private String engineModel;

    /**
     * 主机功率
     */
    @TableField(value = "engine_power")
    private String enginePower;

    /**
     * RPM\拖力（正拖）
     */
    @TableField(value = "rpm_thrust_forward")
    private String rpmThrustForward;

    /**
     * RPM\拖力（倒拖）
     */
    @TableField(value = "rpm_thrust_reverse")
    private String rpmThrustReverse;

    /**
     * 托力
     */
    @TableField(value = "towing_force")
    private String towingForce;

    /**
     * 导航设备
     */
    @TableField(value = "navigation_equipment")
    private String navigationEquipment;

    /**
     * 建造年月
     */
    @TableField(value = "build_date")
    private Date buildDate;

    /**
     * 建造厂
     */
    @TableField(value = "shipyard")
    private String shipyard;

        @TableField(value = "maintenance_record")
    private String maintenanceRecord;

    /**
     * 当前状态
     */
    @TableField(value = "current_status")
    private Object currentStatus;

    /**
     * 当前位置
     */
    @TableField(value = "current_location")
    private String currentLocation;

    /**
     * 最大航速
     */
    @TableField(value = "max_speed")
    private BigDecimal maxSpeed;

    /**
     * 乘员定额
     */
    @TableField(value = "passenger_capacity")
    private Integer passengerCapacity;

    /**
     * 最近维护日期
     */
    @TableField(value = "last_maintenance_date")
    private Date lastMaintenanceDate;

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
        PilotBoats other = (PilotBoats) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getVesselCallSign() == null ? other.getVesselCallSign() == null : this.getVesselCallSign().equals(other.getVesselCallSign()))
            && (this.getBoatName() == null ? other.getBoatName() == null : this.getBoatName().equals(other.getBoatName()))
            && (this.getImoNumber() == null ? other.getImoNumber() == null : this.getImoNumber().equals(other.getImoNumber()))
            && (this.getOperationRange() == null ? other.getOperationRange() == null : this.getOperationRange().equals(other.getOperationRange()))
            && (this.getEngineModel() == null ? other.getEngineModel() == null : this.getEngineModel().equals(other.getEngineModel()))
            && (this.getEnginePower() == null ? other.getEnginePower() == null : this.getEnginePower().equals(other.getEnginePower()))
            && (this.getRpmThrustForward() == null ? other.getRpmThrustForward() == null : this.getRpmThrustForward().equals(other.getRpmThrustForward()))
            && (this.getRpmThrustReverse() == null ? other.getRpmThrustReverse() == null : this.getRpmThrustReverse().equals(other.getRpmThrustReverse()))
            && (this.getTowingForce() == null ? other.getTowingForce() == null : this.getTowingForce().equals(other.getTowingForce()))
            && (this.getNavigationEquipment() == null ? other.getNavigationEquipment() == null : this.getNavigationEquipment().equals(other.getNavigationEquipment()))
            && (this.getBuildDate() == null ? other.getBuildDate() == null : this.getBuildDate().equals(other.getBuildDate()))
            && (this.getShipyard() == null ? other.getShipyard() == null : this.getShipyard().equals(other.getShipyard()))
            && (this.getMaintenanceRecord() == null ? other.getMaintenanceRecord() == null : this.getMaintenanceRecord().equals(other.getMaintenanceRecord()))
            && (this.getCurrentStatus() == null ? other.getCurrentStatus() == null : this.getCurrentStatus().equals(other.getCurrentStatus()))
            && (this.getCurrentLocation() == null ? other.getCurrentLocation() == null : this.getCurrentLocation().equals(other.getCurrentLocation()))
            && (this.getMaxSpeed() == null ? other.getMaxSpeed() == null : this.getMaxSpeed().equals(other.getMaxSpeed()))
            && (this.getPassengerCapacity() == null ? other.getPassengerCapacity() == null : this.getPassengerCapacity().equals(other.getPassengerCapacity()))
            && (this.getLastMaintenanceDate() == null ? other.getLastMaintenanceDate() == null : this.getLastMaintenanceDate().equals(other.getLastMaintenanceDate()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getVesselCallSign() == null) ? 0 : getVesselCallSign().hashCode());
        result = prime * result + ((getBoatName() == null) ? 0 : getBoatName().hashCode());
        result = prime * result + ((getImoNumber() == null) ? 0 : getImoNumber().hashCode());
        result = prime * result + ((getOperationRange() == null) ? 0 : getOperationRange().hashCode());
        result = prime * result + ((getEngineModel() == null) ? 0 : getEngineModel().hashCode());
        result = prime * result + ((getEnginePower() == null) ? 0 : getEnginePower().hashCode());
        result = prime * result + ((getRpmThrustForward() == null) ? 0 : getRpmThrustForward().hashCode());
        result = prime * result + ((getRpmThrustReverse() == null) ? 0 : getRpmThrustReverse().hashCode());
        result = prime * result + ((getTowingForce() == null) ? 0 : getTowingForce().hashCode());
        result = prime * result + ((getNavigationEquipment() == null) ? 0 : getNavigationEquipment().hashCode());
        result = prime * result + ((getBuildDate() == null) ? 0 : getBuildDate().hashCode());
        result = prime * result + ((getShipyard() == null) ? 0 : getShipyard().hashCode());
        result = prime * result + ((getMaintenanceRecord() == null) ? 0 : getMaintenanceRecord().hashCode());
        result = prime * result + ((getCurrentStatus() == null) ? 0 : getCurrentStatus().hashCode());
        result = prime * result + ((getCurrentLocation() == null) ? 0 : getCurrentLocation().hashCode());
        result = prime * result + ((getMaxSpeed() == null) ? 0 : getMaxSpeed().hashCode());
        result = prime * result + ((getPassengerCapacity() == null) ? 0 : getPassengerCapacity().hashCode());
        result = prime * result + ((getLastMaintenanceDate() == null) ? 0 : getLastMaintenanceDate().hashCode());
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
        sb.append(", vesselCallSign=").append(vesselCallSign);
        sb.append(", boatName=").append(boatName);
        sb.append(", imoNumber=").append(imoNumber);
        sb.append(", operationRange=").append(operationRange);
        sb.append(", engineModel=").append(engineModel);
        sb.append(", enginePower=").append(enginePower);
        sb.append(", rpmThrustForward=").append(rpmThrustForward);
        sb.append(", rpmThrustReverse=").append(rpmThrustReverse);
        sb.append(", towingForce=").append(towingForce);
        sb.append(", navigationEquipment=").append(navigationEquipment);
        sb.append(", buildDate=").append(buildDate);
        sb.append(", shipyard=").append(shipyard);
        sb.append(", maintenanceRecord=").append(maintenanceRecord);
        sb.append(", currentStatus=").append(currentStatus);
        sb.append(", currentLocation=").append(currentLocation);
        sb.append(", maxSpeed=").append(maxSpeed);
        sb.append(", passengerCapacity=").append(passengerCapacity);
        sb.append(", lastMaintenanceDate=").append(lastMaintenanceDate);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}