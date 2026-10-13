package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 驾驶员基本表
 * @TableName drivers
 */
@TableName(value ="drivers")
@Data
public class Drivers implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 驾驶员代码
     */
    @TableField(value = "driver_code")
    private String driverCode;

    /**
     * 姓名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 出生年月日
     */
    @TableField(value = "birth_date")
    private Date birthDate;

    /**
     * 级别
     */
    @TableField(value = "level")
    private String level;

    /**
     * 驾照号
     */
    @TableField(value = "license_number")
    private String licenseNumber;

    /**
     * 汽车ID
     */
    @TableField(value = "vehicle_id")
    private Long vehicleId;

    /**
     * 到车队年月日
     */
    @TableField(value = "join_date")
    private Date joinDate;

    /**
     * 工作经历
     */
    @TableField(value = "work_experience")
    private String workExperience;

    /**
     * 联系电话
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 手机号码
     */
    @TableField(value = "mobile")
    private String mobile;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 手机短号
     */
    @TableField(value = "short_number")
    private String shortNumber;

    /**
     * 在岗状态
     */
    @TableField(value = "on_duty_status")
    private Object onDutyStatus;

    /**
     * 紧急联系人
     */
    @TableField(value = "emergency_contact")
    private String emergencyContact;

    /**
     * 驾照类型
     */
    @TableField(value = "license_type")
    private String licenseType;

    /**
     * 驾照有效期
     */
    @TableField(value = "license_expiry")
    private Date licenseExpiry;

        @TableField(value = "created_at")
    private Date createdAt;

        @TableField(value = "updated_at")
    private Date updatedAt;

//    user_id	bigint	false	false	false		false		false	关联用户id
    @TableField(value = "user_id")
    private Long userId;

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
        Drivers other = (Drivers) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getDriverCode() == null ? other.getDriverCode() == null : this.getDriverCode().equals(other.getDriverCode()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getBirthDate() == null ? other.getBirthDate() == null : this.getBirthDate().equals(other.getBirthDate()))
            && (this.getLevel() == null ? other.getLevel() == null : this.getLevel().equals(other.getLevel()))
            && (this.getLicenseNumber() == null ? other.getLicenseNumber() == null : this.getLicenseNumber().equals(other.getLicenseNumber()))
            && (this.getVehicleId() == null ? other.getVehicleId() == null : this.getVehicleId().equals(other.getVehicleId()))
            && (this.getJoinDate() == null ? other.getJoinDate() == null : this.getJoinDate().equals(other.getJoinDate()))
            && (this.getWorkExperience() == null ? other.getWorkExperience() == null : this.getWorkExperience().equals(other.getWorkExperience()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getMobile() == null ? other.getMobile() == null : this.getMobile().equals(other.getMobile()))
            && (this.getShortNumber() == null ? other.getShortNumber() == null : this.getShortNumber().equals(other.getShortNumber()))
            && (this.getOnDutyStatus() == null ? other.getOnDutyStatus() == null : this.getOnDutyStatus().equals(other.getOnDutyStatus()))
            && (this.getEmergencyContact() == null ? other.getEmergencyContact() == null : this.getEmergencyContact().equals(other.getEmergencyContact()))
            && (this.getLicenseType() == null ? other.getLicenseType() == null : this.getLicenseType().equals(other.getLicenseType()))
            && (this.getLicenseExpiry() == null ? other.getLicenseExpiry() == null : this.getLicenseExpiry().equals(other.getLicenseExpiry()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
                && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getDriverCode() == null) ? 0 : getDriverCode().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getBirthDate() == null) ? 0 : getBirthDate().hashCode());
        result = prime * result + ((getLevel() == null) ? 0 : getLevel().hashCode());
        result = prime * result + ((getLicenseNumber() == null) ? 0 : getLicenseNumber().hashCode());
        result = prime * result + ((getVehicleId() == null) ? 0 : getVehicleId().hashCode());
        result = prime * result + ((getJoinDate() == null) ? 0 : getJoinDate().hashCode());
        result = prime * result + ((getWorkExperience() == null) ? 0 : getWorkExperience().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getMobile() == null) ? 0 : getMobile().hashCode());
        result = prime * result + ((getShortNumber() == null) ? 0 : getShortNumber().hashCode());
        result = prime * result + ((getOnDutyStatus() == null) ? 0 : getOnDutyStatus().hashCode());
        result = prime * result + ((getEmergencyContact() == null) ? 0 : getEmergencyContact().hashCode());
        result = prime * result + ((getLicenseType() == null) ? 0 : getLicenseType().hashCode());
        result = prime * result + ((getLicenseExpiry() == null) ? 0 : getLicenseExpiry().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
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
        sb.append(", driverCode=").append(driverCode);
        sb.append(", name=").append(name);
        sb.append(", birthDate=").append(birthDate);
        sb.append(", level=").append(level);
        sb.append(", licenseNumber=").append(licenseNumber);
        sb.append(", vehicleId=").append(vehicleId);
        sb.append(", joinDate=").append(joinDate);
        sb.append(", workExperience=").append(workExperience);
        sb.append(", phone=").append(phone);
        sb.append(", mobile=").append(mobile);
        sb.append(", shortNumber=").append(shortNumber);
        sb.append(", onDutyStatus=").append(onDutyStatus);
        sb.append(", emergencyContact=").append(emergencyContact);
        sb.append(", licenseType=").append(licenseType);
        sb.append(", licenseExpiry=").append(licenseExpiry);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", email=").append(email);
        sb.append(", userId=").append(userId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}