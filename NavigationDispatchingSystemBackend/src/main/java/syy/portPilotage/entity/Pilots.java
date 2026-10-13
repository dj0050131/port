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
 * 引航员基本信息表
 * @TableName pilots
 */
@TableName(value ="pilots")
@Data
public class Pilots implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 引航员代码
     */
    @TableField(value = "pilot_code")
    private String pilotCode;

    /**
     * 引航员名称
     */
    @TableField(value = "pilot_name")
    private String pilotName;

    /**
     * 引航员级别
     */
    @TableField(value = "pilot_level")
    private String pilotLevel;

    /**
     * 从事引航年月日
     */
    @TableField(value = "career_start_date")
    private Date careerStartDate;

    /**
     * 其他海上资历
     */
    @TableField(value = "other_experience")
    private String otherExperience;

    /**
     * 健康状况
     */
    @TableField(value = "health_status")
    private String healthStatus;

        @TableField(value = "accident_record")
    private String accidentRecord;

    /**
     * 主引已经引航的货物总吨数
     */
    @TableField(value = "total_cargo_tonnage")
    private BigDecimal totalCargoTonnage;

    /**
     * 主引已引航的次数
     */
    @TableField(value = "total_operations")
    private Integer totalOperations;

    /**
     * 联系电话
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 手机SIM卡号
     */
    @TableField(value = "sim_card")
    private String simCard;

    /**
     * 网卡SIM卡号
     */
    @TableField(value = "data_sim_card")
    private String dataSimCard;

    /**
     * 无线网卡IP
     */
    @TableField(value = "wireless_ip")
    private String wirelessIp;

    /**
     * 手机短号
     */
    @TableField(value = "short_number")
    private String shortNumber;

    /**
     * E-mail
     */
    @TableField(value = "email")
    private String email;

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
     * 紧急联系电话
     */
    @TableField(value = "emergency_phone")
    private String emergencyPhone;

    /**
     * 资质证书编号
     */
    @TableField(value = "certificate_no")
    private String certificateNo;

    /**
     * 资质有效期
     */
    @TableField(value = "certificate_expiry")
    private Date certificateExpiry;

    /**
     * 关联用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

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
        Pilots other = (Pilots) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPilotCode() == null ? other.getPilotCode() == null : this.getPilotCode().equals(other.getPilotCode()))
            && (this.getPilotName() == null ? other.getPilotName() == null : this.getPilotName().equals(other.getPilotName()))
            && (this.getPilotLevel() == null ? other.getPilotLevel() == null : this.getPilotLevel().equals(other.getPilotLevel()))
            && (this.getCareerStartDate() == null ? other.getCareerStartDate() == null : this.getCareerStartDate().equals(other.getCareerStartDate()))
            && (this.getOtherExperience() == null ? other.getOtherExperience() == null : this.getOtherExperience().equals(other.getOtherExperience()))
            && (this.getHealthStatus() == null ? other.getHealthStatus() == null : this.getHealthStatus().equals(other.getHealthStatus()))
            && (this.getAccidentRecord() == null ? other.getAccidentRecord() == null : this.getAccidentRecord().equals(other.getAccidentRecord()))
            && (this.getTotalCargoTonnage() == null ? other.getTotalCargoTonnage() == null : this.getTotalCargoTonnage().equals(other.getTotalCargoTonnage()))
            && (this.getTotalOperations() == null ? other.getTotalOperations() == null : this.getTotalOperations().equals(other.getTotalOperations()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getSimCard() == null ? other.getSimCard() == null : this.getSimCard().equals(other.getSimCard()))
            && (this.getDataSimCard() == null ? other.getDataSimCard() == null : this.getDataSimCard().equals(other.getDataSimCard()))
            && (this.getWirelessIp() == null ? other.getWirelessIp() == null : this.getWirelessIp().equals(other.getWirelessIp()))
            && (this.getShortNumber() == null ? other.getShortNumber() == null : this.getShortNumber().equals(other.getShortNumber()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getOnDutyStatus() == null ? other.getOnDutyStatus() == null : this.getOnDutyStatus().equals(other.getOnDutyStatus()))
            && (this.getEmergencyContact() == null ? other.getEmergencyContact() == null : this.getEmergencyContact().equals(other.getEmergencyContact()))
            && (this.getEmergencyPhone() == null ? other.getEmergencyPhone() == null : this.getEmergencyPhone().equals(other.getEmergencyPhone()))
            && (this.getCertificateNo() == null ? other.getCertificateNo() == null : this.getCertificateNo().equals(other.getCertificateNo()))
            && (this.getCertificateExpiry() == null ? other.getCertificateExpiry() == null : this.getCertificateExpiry().equals(other.getCertificateExpiry()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPilotCode() == null) ? 0 : getPilotCode().hashCode());
        result = prime * result + ((getPilotName() == null) ? 0 : getPilotName().hashCode());
        result = prime * result + ((getPilotLevel() == null) ? 0 : getPilotLevel().hashCode());
        result = prime * result + ((getCareerStartDate() == null) ? 0 : getCareerStartDate().hashCode());
        result = prime * result + ((getOtherExperience() == null) ? 0 : getOtherExperience().hashCode());
        result = prime * result + ((getHealthStatus() == null) ? 0 : getHealthStatus().hashCode());
        result = prime * result + ((getAccidentRecord() == null) ? 0 : getAccidentRecord().hashCode());
        result = prime * result + ((getTotalCargoTonnage() == null) ? 0 : getTotalCargoTonnage().hashCode());
        result = prime * result + ((getTotalOperations() == null) ? 0 : getTotalOperations().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getSimCard() == null) ? 0 : getSimCard().hashCode());
        result = prime * result + ((getDataSimCard() == null) ? 0 : getDataSimCard().hashCode());
        result = prime * result + ((getWirelessIp() == null) ? 0 : getWirelessIp().hashCode());
        result = prime * result + ((getShortNumber() == null) ? 0 : getShortNumber().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getOnDutyStatus() == null) ? 0 : getOnDutyStatus().hashCode());
        result = prime * result + ((getEmergencyContact() == null) ? 0 : getEmergencyContact().hashCode());
        result = prime * result + ((getEmergencyPhone() == null) ? 0 : getEmergencyPhone().hashCode());
        result = prime * result + ((getCertificateNo() == null) ? 0 : getCertificateNo().hashCode());
        result = prime * result + ((getCertificateExpiry() == null) ? 0 : getCertificateExpiry().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
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
        sb.append(", pilotCode=").append(pilotCode);
        sb.append(", pilotName=").append(pilotName);
        sb.append(", pilotLevel=").append(pilotLevel);
        sb.append(", careerStartDate=").append(careerStartDate);
        sb.append(", otherExperience=").append(otherExperience);
        sb.append(", healthStatus=").append(healthStatus);
        sb.append(", accidentRecord=").append(accidentRecord);
        sb.append(", totalCargoTonnage=").append(totalCargoTonnage);
        sb.append(", totalOperations=").append(totalOperations);
        sb.append(", phone=").append(phone);
        sb.append(", simCard=").append(simCard);
        sb.append(", dataSimCard=").append(dataSimCard);
        sb.append(", wirelessIp=").append(wirelessIp);
        sb.append(", shortNumber=").append(shortNumber);
        sb.append(", email=").append(email);
        sb.append(", onDutyStatus=").append(onDutyStatus);
        sb.append(", emergencyContact=").append(emergencyContact);
        sb.append(", emergencyPhone=").append(emergencyPhone);
        sb.append(", certificateNo=").append(certificateNo);
        sb.append(", certificateExpiry=").append(certificateExpiry);
        sb.append(", userId=").append(userId);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}