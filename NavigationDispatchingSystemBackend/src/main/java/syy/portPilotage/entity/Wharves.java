package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import syy.portPilotage.util.jackson.MultiFormatDateDeserializer;

/**
 * 码头信息表
 * @TableName wharves
 */
@TableName(value ="wharves")
@Data
public class Wharves implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属港区ID
     */
    @TableField(value = "port_area_id")
    private Long portAreaId;

    /**
     * 码头代码
     */
    @TableField(value = "wharf_code")
    private String wharfCode;

    /**
     * 码头中文名称
     */
    @TableField(value = "wharf_name_zh")
    private String wharfNameZh;

    /**
     * 码头英文名称
     */
    @TableField(value = "wharf_name_en")
    private String wharfNameEn;

    /**
     * 码头长度(米)
     */
    @TableField(value = "wharf_length")
    private BigDecimal wharfLength;

    /**
     * 码头水深(米)
     */
    @TableField(value = "wharf_depth")
    private BigDecimal wharfDepth;

    /**
     * 建成日期
     */
    @TableField(value = "construction_date")
    @JsonDeserialize(using = MultiFormatDateDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date constructionDate;

    /**
     * 码头类型
     */
    @TableField(value = "wharf_type")
    private Object wharfType;

    /**
     * 运营状态
     */
    @TableField(value = "operational_status")
    private Object operationalStatus;

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
        Wharves other = (Wharves) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPortAreaId() == null ? other.getPortAreaId() == null : this.getPortAreaId().equals(other.getPortAreaId()))
            && (this.getWharfCode() == null ? other.getWharfCode() == null : this.getWharfCode().equals(other.getWharfCode()))
            && (this.getWharfNameZh() == null ? other.getWharfNameZh() == null : this.getWharfNameZh().equals(other.getWharfNameZh()))
            && (this.getWharfNameEn() == null ? other.getWharfNameEn() == null : this.getWharfNameEn().equals(other.getWharfNameEn()))
            && (this.getWharfLength() == null ? other.getWharfLength() == null : this.getWharfLength().equals(other.getWharfLength()))
            && (this.getWharfDepth() == null ? other.getWharfDepth() == null : this.getWharfDepth().equals(other.getWharfDepth()))
            && (this.getConstructionDate() == null ? other.getConstructionDate() == null : this.getConstructionDate().equals(other.getConstructionDate()))
            && (this.getWharfType() == null ? other.getWharfType() == null : this.getWharfType().equals(other.getWharfType()))
            && (this.getOperationalStatus() == null ? other.getOperationalStatus() == null : this.getOperationalStatus().equals(other.getOperationalStatus()))
            && (this.getManagementCompany() == null ? other.getManagementCompany() == null : this.getManagementCompany().equals(other.getManagementCompany()))
            && (this.getContactPhone() == null ? other.getContactPhone() == null : this.getContactPhone().equals(other.getContactPhone()))
            && (this.getLongitude() == null ? other.getLongitude() == null : this.getLongitude().equals(other.getLongitude()))
            && (this.getLatitude() == null ? other.getLatitude() == null : this.getLatitude().equals(other.getLatitude()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPortAreaId() == null) ? 0 : getPortAreaId().hashCode());
        result = prime * result + ((getWharfCode() == null) ? 0 : getWharfCode().hashCode());
        result = prime * result + ((getWharfNameZh() == null) ? 0 : getWharfNameZh().hashCode());
        result = prime * result + ((getWharfNameEn() == null) ? 0 : getWharfNameEn().hashCode());
        result = prime * result + ((getWharfLength() == null) ? 0 : getWharfLength().hashCode());
        result = prime * result + ((getWharfDepth() == null) ? 0 : getWharfDepth().hashCode());
        result = prime * result + ((getConstructionDate() == null) ? 0 : getConstructionDate().hashCode());
        result = prime * result + ((getWharfType() == null) ? 0 : getWharfType().hashCode());
        result = prime * result + ((getOperationalStatus() == null) ? 0 : getOperationalStatus().hashCode());
        result = prime * result + ((getManagementCompany() == null) ? 0 : getManagementCompany().hashCode());
        result = prime * result + ((getContactPhone() == null) ? 0 : getContactPhone().hashCode());
        result = prime * result + ((getLongitude() == null) ? 0 : getLongitude().hashCode());
        result = prime * result + ((getLatitude() == null) ? 0 : getLatitude().hashCode());
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
        sb.append(", portAreaId=").append(portAreaId);
        sb.append(", wharfCode=").append(wharfCode);
        sb.append(", wharfNameZh=").append(wharfNameZh);
        sb.append(", wharfNameEn=").append(wharfNameEn);
        sb.append(", wharfLength=").append(wharfLength);
        sb.append(", wharfDepth=").append(wharfDepth);
        sb.append(", constructionDate=").append(constructionDate);
        sb.append(", wharfType=").append(wharfType);
        sb.append(", operationalStatus=").append(operationalStatus);
        sb.append(", managementCompany=").append(managementCompany);
        sb.append(", contactPhone=").append(contactPhone);
        sb.append(", longitude=").append(longitude);
        sb.append(", latitude=").append(latitude);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}