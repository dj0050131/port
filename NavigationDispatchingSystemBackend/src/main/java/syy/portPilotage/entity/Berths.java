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
 * 港口泊位数据表
 * @TableName berths
 */
@TableName(value ="berths")
@Data
public class Berths implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 泊位编号
     */
    @TableField(value = "berth_code")
    private String berthCode;

    /**
     * 港区名称
     */
    @TableField(value = "port_area")
    private String portArea;

    /**
     * 泊位类别
     */
    @TableField(value = "berth_type")
    private String berthType;

    /**
     * 泊位名称
     */
    @TableField(value = "berth_name")
    private String berthName;

    /**
     * 码头方向
     */
    @TableField(value = "wharf_direction")
    private String wharfDirection;

    /**
     * 经度
     */
    @TableField(value = "longitude")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @TableField(value = "latitude")
    private BigDecimal latitude;

    /**
     * 起始纬度
     */
    @TableField(value = "start_latitude")
    private BigDecimal startLatitude;

    /**
     * 起始经度
     */
    @TableField(value = "start_longitude")
    private BigDecimal startLongitude;

    /**
     * 终止纬度
     */
    @TableField(value = "end_latitude")
    private BigDecimal endLatitude;

    /**
     * 终止经度
     */
    @TableField(value = "end_longitude")
    private BigDecimal endLongitude;

    /**
     * 泊位方向
     */
    @TableField(value = "berth_direction")
    private String berthDirection;

    /**
     * 最大吃水
     */
    @TableField(value = "max_draft")
    private BigDecimal maxDraft;

    /**
     * 泊位长度
     */
    @TableField(value = "berth_length")
    private BigDecimal berthLength;

    /**
     * 泊位水深
     */
    @TableField(value = "berth_depth")
    private BigDecimal berthDepth;

    /**
     * 泊位状态
     */
    @TableField(value = "berth_status")
    private Object berthStatus;

    /**
     * 联系电话
     */
    @TableField(value = "contact_phone")
    private String contactPhone;

        @TableField(value = "created_at")
    private Date createdAt;

        @TableField(value = "updated_at")
    private Date updatedAt;

    /**
     * 所属港区ID
     */
    @TableField(value = "port_area_id")
    private Long portAreaId;

    /**
     * 所属码头ID
     */
    @TableField(value = "wharf_id")
    private Long wharfId;

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
        Berths other = (Berths) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBerthCode() == null ? other.getBerthCode() == null : this.getBerthCode().equals(other.getBerthCode()))
            && (this.getPortArea() == null ? other.getPortArea() == null : this.getPortArea().equals(other.getPortArea()))
            && (this.getBerthType() == null ? other.getBerthType() == null : this.getBerthType().equals(other.getBerthType()))
            && (this.getBerthName() == null ? other.getBerthName() == null : this.getBerthName().equals(other.getBerthName()))
            && (this.getWharfDirection() == null ? other.getWharfDirection() == null : this.getWharfDirection().equals(other.getWharfDirection()))
            && (this.getLongitude() == null ? other.getLongitude() == null : this.getLongitude().equals(other.getLongitude()))
            && (this.getLatitude() == null ? other.getLatitude() == null : this.getLatitude().equals(other.getLatitude()))
            && (this.getStartLatitude() == null ? other.getStartLatitude() == null : this.getStartLatitude().equals(other.getStartLatitude()))
            && (this.getStartLongitude() == null ? other.getStartLongitude() == null : this.getStartLongitude().equals(other.getStartLongitude()))
            && (this.getEndLatitude() == null ? other.getEndLatitude() == null : this.getEndLatitude().equals(other.getEndLatitude()))
            && (this.getEndLongitude() == null ? other.getEndLongitude() == null : this.getEndLongitude().equals(other.getEndLongitude()))
            && (this.getBerthDirection() == null ? other.getBerthDirection() == null : this.getBerthDirection().equals(other.getBerthDirection()))
            && (this.getMaxDraft() == null ? other.getMaxDraft() == null : this.getMaxDraft().equals(other.getMaxDraft()))
            && (this.getBerthLength() == null ? other.getBerthLength() == null : this.getBerthLength().equals(other.getBerthLength()))
            && (this.getBerthDepth() == null ? other.getBerthDepth() == null : this.getBerthDepth().equals(other.getBerthDepth()))
            && (this.getBerthStatus() == null ? other.getBerthStatus() == null : this.getBerthStatus().equals(other.getBerthStatus()))
            && (this.getContactPhone() == null ? other.getContactPhone() == null : this.getContactPhone().equals(other.getContactPhone()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()))
            && (this.getPortAreaId() == null ? other.getPortAreaId() == null : this.getPortAreaId().equals(other.getPortAreaId()))
            && (this.getWharfId() == null ? other.getWharfId() == null : this.getWharfId().equals(other.getWharfId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBerthCode() == null) ? 0 : getBerthCode().hashCode());
        result = prime * result + ((getPortArea() == null) ? 0 : getPortArea().hashCode());
        result = prime * result + ((getBerthType() == null) ? 0 : getBerthType().hashCode());
        result = prime * result + ((getBerthName() == null) ? 0 : getBerthName().hashCode());
        result = prime * result + ((getWharfDirection() == null) ? 0 : getWharfDirection().hashCode());
        result = prime * result + ((getLongitude() == null) ? 0 : getLongitude().hashCode());
        result = prime * result + ((getLatitude() == null) ? 0 : getLatitude().hashCode());
        result = prime * result + ((getStartLatitude() == null) ? 0 : getStartLatitude().hashCode());
        result = prime * result + ((getStartLongitude() == null) ? 0 : getStartLongitude().hashCode());
        result = prime * result + ((getEndLatitude() == null) ? 0 : getEndLatitude().hashCode());
        result = prime * result + ((getEndLongitude() == null) ? 0 : getEndLongitude().hashCode());
        result = prime * result + ((getBerthDirection() == null) ? 0 : getBerthDirection().hashCode());
        result = prime * result + ((getMaxDraft() == null) ? 0 : getMaxDraft().hashCode());
        result = prime * result + ((getBerthLength() == null) ? 0 : getBerthLength().hashCode());
        result = prime * result + ((getBerthDepth() == null) ? 0 : getBerthDepth().hashCode());
        result = prime * result + ((getBerthStatus() == null) ? 0 : getBerthStatus().hashCode());
        result = prime * result + ((getContactPhone() == null) ? 0 : getContactPhone().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getPortAreaId() == null) ? 0 : getPortAreaId().hashCode());
        result = prime * result + ((getWharfId() == null) ? 0 : getWharfId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", berthCode=").append(berthCode);
        sb.append(", portArea=").append(portArea);
        sb.append(", berthType=").append(berthType);
        sb.append(", berthName=").append(berthName);
        sb.append(", wharfDirection=").append(wharfDirection);
        sb.append(", longitude=").append(longitude);
        sb.append(", latitude=").append(latitude);
        sb.append(", startLatitude=").append(startLatitude);
        sb.append(", startLongitude=").append(startLongitude);
        sb.append(", endLatitude=").append(endLatitude);
        sb.append(", endLongitude=").append(endLongitude);
        sb.append(", berthDirection=").append(berthDirection);
        sb.append(", maxDraft=").append(maxDraft);
        sb.append(", berthLength=").append(berthLength);
        sb.append(", berthDepth=").append(berthDepth);
        sb.append(", berthStatus=").append(berthStatus);
        sb.append(", contactPhone=").append(contactPhone);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", portAreaId=").append(portAreaId);
        sb.append(", wharfId=").append(wharfId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}