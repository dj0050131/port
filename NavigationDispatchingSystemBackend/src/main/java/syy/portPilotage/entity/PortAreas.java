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
 * 港区信息表
 * @TableName port_areas
 */
@TableName(value ="port_areas")
@Data
public class PortAreas implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属港口ID
     */
    @TableField(value = "port_id")
    private Long portId;

    /**
     * 港区代码
     */
    @TableField(value = "area_code")
    private String areaCode;

    /**
     * 港区中文名称
     */
    @TableField(value = "area_name_zh")
    private String areaNameZh;

    /**
     * 港区英文名称
     */
    @TableField(value = "area_name_en")
    private String areaNameEn;

    /**
     * 港区类型
     */
    @TableField(value = "area_type")
    private Object areaType;

        @TableField(value = "management_company")
    private String managementCompany;

    /**
     * 联系电话
     */
    @TableField(value = "contact_phone")
    private String contactPhone;

    /**
     * 中心经度
     */
    @TableField(value = "longitude")
    private BigDecimal longitude;

    /**
     * 中心纬度
     */
    @TableField(value = "latitude")
    private BigDecimal latitude;

    /**
     * 边界坐标
     */
    @TableField(value = "boundary_coordinates")
    private Object boundaryCoordinates;

    /**
     * 状态
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
        PortAreas other = (PortAreas) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPortId() == null ? other.getPortId() == null : this.getPortId().equals(other.getPortId()))
            && (this.getAreaCode() == null ? other.getAreaCode() == null : this.getAreaCode().equals(other.getAreaCode()))
            && (this.getAreaNameZh() == null ? other.getAreaNameZh() == null : this.getAreaNameZh().equals(other.getAreaNameZh()))
            && (this.getAreaNameEn() == null ? other.getAreaNameEn() == null : this.getAreaNameEn().equals(other.getAreaNameEn()))
            && (this.getAreaType() == null ? other.getAreaType() == null : this.getAreaType().equals(other.getAreaType()))
            && (this.getManagementCompany() == null ? other.getManagementCompany() == null : this.getManagementCompany().equals(other.getManagementCompany()))
            && (this.getContactPhone() == null ? other.getContactPhone() == null : this.getContactPhone().equals(other.getContactPhone()))
            && (this.getLongitude() == null ? other.getLongitude() == null : this.getLongitude().equals(other.getLongitude()))
            && (this.getLatitude() == null ? other.getLatitude() == null : this.getLatitude().equals(other.getLatitude()))
            && (this.getBoundaryCoordinates() == null ? other.getBoundaryCoordinates() == null : this.getBoundaryCoordinates().equals(other.getBoundaryCoordinates()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPortId() == null) ? 0 : getPortId().hashCode());
        result = prime * result + ((getAreaCode() == null) ? 0 : getAreaCode().hashCode());
        result = prime * result + ((getAreaNameZh() == null) ? 0 : getAreaNameZh().hashCode());
        result = prime * result + ((getAreaNameEn() == null) ? 0 : getAreaNameEn().hashCode());
        result = prime * result + ((getAreaType() == null) ? 0 : getAreaType().hashCode());
        result = prime * result + ((getManagementCompany() == null) ? 0 : getManagementCompany().hashCode());
        result = prime * result + ((getContactPhone() == null) ? 0 : getContactPhone().hashCode());
        result = prime * result + ((getLongitude() == null) ? 0 : getLongitude().hashCode());
        result = prime * result + ((getLatitude() == null) ? 0 : getLatitude().hashCode());
        result = prime * result + ((getBoundaryCoordinates() == null) ? 0 : getBoundaryCoordinates().hashCode());
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
        sb.append(", portId=").append(portId);
        sb.append(", areaCode=").append(areaCode);
        sb.append(", areaNameZh=").append(areaNameZh);
        sb.append(", areaNameEn=").append(areaNameEn);
        sb.append(", areaType=").append(areaType);
        sb.append(", managementCompany=").append(managementCompany);
        sb.append(", contactPhone=").append(contactPhone);
        sb.append(", longitude=").append(longitude);
        sb.append(", latitude=").append(latitude);
        sb.append(", boundaryCoordinates=").append(boundaryCoordinates);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}