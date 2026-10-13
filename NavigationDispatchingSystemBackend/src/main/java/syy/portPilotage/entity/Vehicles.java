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
 * 车辆基本信息表
 * @TableName vehicles
 */
@TableName(value ="vehicles")
@Data
public class Vehicles implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 汽车代码
     */
    @TableField(value = "vehicle_code")
    private String vehicleCode;

    /**
     * 牌照号
     */
    @TableField(value = "license_plate")
    private String licensePlate;

    /**
     * 汽车型号
     */
    @TableField(value = "vehicle_model")
    private String vehicleModel;

    /**
     * 购置年月日
     */
    @TableField(value = "purchase_date")
    private Date purchaseDate;

    /**
     * 载客数
     */
    @TableField(value = "passenger_capacity")
    private Integer passengerCapacity;

    /**
     * 主要性能
     */
    @TableField(value = "main_performance")
    private String mainPerformance;

    /**
     * 累计公里数
     */
    @TableField(value = "total_kilometers")
    private BigDecimal totalKilometers;

    /**
     * 车辆状态
     */
    @TableField(value = "vehicle_status")
    private Object vehicleStatus;

    /**
     * 当前位置
     */
    @TableField(value = "current_location")
    private String currentLocation;

    /**
     * 颜色
     */
    @TableField(value = "color")
    private String color;

    /**
     * 最近保养日期
     */
    @TableField(value = "last_maintenance_date")
    private Date lastMaintenanceDate;

    /**
     * 保养周期
     */
    @TableField(value = "maintenance_interval")
    private Integer maintenanceInterval;

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
        Vehicles other = (Vehicles) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getVehicleCode() == null ? other.getVehicleCode() == null : this.getVehicleCode().equals(other.getVehicleCode()))
            && (this.getLicensePlate() == null ? other.getLicensePlate() == null : this.getLicensePlate().equals(other.getLicensePlate()))
            && (this.getVehicleModel() == null ? other.getVehicleModel() == null : this.getVehicleModel().equals(other.getVehicleModel()))
            && (this.getPurchaseDate() == null ? other.getPurchaseDate() == null : this.getPurchaseDate().equals(other.getPurchaseDate()))
            && (this.getPassengerCapacity() == null ? other.getPassengerCapacity() == null : this.getPassengerCapacity().equals(other.getPassengerCapacity()))
            && (this.getMainPerformance() == null ? other.getMainPerformance() == null : this.getMainPerformance().equals(other.getMainPerformance()))
            && (this.getTotalKilometers() == null ? other.getTotalKilometers() == null : this.getTotalKilometers().equals(other.getTotalKilometers()))
            && (this.getVehicleStatus() == null ? other.getVehicleStatus() == null : this.getVehicleStatus().equals(other.getVehicleStatus()))
            && (this.getCurrentLocation() == null ? other.getCurrentLocation() == null : this.getCurrentLocation().equals(other.getCurrentLocation()))
            && (this.getColor() == null ? other.getColor() == null : this.getColor().equals(other.getColor()))
            && (this.getLastMaintenanceDate() == null ? other.getLastMaintenanceDate() == null : this.getLastMaintenanceDate().equals(other.getLastMaintenanceDate()))
            && (this.getMaintenanceInterval() == null ? other.getMaintenanceInterval() == null : this.getMaintenanceInterval().equals(other.getMaintenanceInterval()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getVehicleCode() == null) ? 0 : getVehicleCode().hashCode());
        result = prime * result + ((getLicensePlate() == null) ? 0 : getLicensePlate().hashCode());
        result = prime * result + ((getVehicleModel() == null) ? 0 : getVehicleModel().hashCode());
        result = prime * result + ((getPurchaseDate() == null) ? 0 : getPurchaseDate().hashCode());
        result = prime * result + ((getPassengerCapacity() == null) ? 0 : getPassengerCapacity().hashCode());
        result = prime * result + ((getMainPerformance() == null) ? 0 : getMainPerformance().hashCode());
        result = prime * result + ((getTotalKilometers() == null) ? 0 : getTotalKilometers().hashCode());
        result = prime * result + ((getVehicleStatus() == null) ? 0 : getVehicleStatus().hashCode());
        result = prime * result + ((getCurrentLocation() == null) ? 0 : getCurrentLocation().hashCode());
        result = prime * result + ((getColor() == null) ? 0 : getColor().hashCode());
        result = prime * result + ((getLastMaintenanceDate() == null) ? 0 : getLastMaintenanceDate().hashCode());
        result = prime * result + ((getMaintenanceInterval() == null) ? 0 : getMaintenanceInterval().hashCode());
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
        sb.append(", vehicleCode=").append(vehicleCode);
        sb.append(", licensePlate=").append(licensePlate);
        sb.append(", vehicleModel=").append(vehicleModel);
        sb.append(", purchaseDate=").append(purchaseDate);
        sb.append(", passengerCapacity=").append(passengerCapacity);
        sb.append(", mainPerformance=").append(mainPerformance);
        sb.append(", totalKilometers=").append(totalKilometers);
        sb.append(", vehicleStatus=").append(vehicleStatus);
        sb.append(", currentLocation=").append(currentLocation);
        sb.append(", color=").append(color);
        sb.append(", lastMaintenanceDate=").append(lastMaintenanceDate);
        sb.append(", maintenanceInterval=").append(maintenanceInterval);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}