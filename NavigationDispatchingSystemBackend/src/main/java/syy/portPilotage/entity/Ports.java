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
 * 港口信息表
 * @TableName ports
 */
@TableName(value ="ports")
@Data
public class Ports implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 港口代码
     */
    @TableField(value = "port_code")
    private String portCode;

    /**
     * 港口中文名称
     */
    @TableField(value = "port_name_zh")
    private String portNameZh;

    /**
     * 港口英文名称
     */
    @TableField(value = "port_name_en")
    private String portNameEn;

    /**
     * 所属国家
     */
    @TableField(value = "country")
    private String country;

    /**
     * 所属区域
     */
    @TableField(value = "region")
    private String region;

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
     * 时区
     */
    @TableField(value = "timezone")
    private String timezone;

    /**
     * 状态
     */
    @TableField(value = "status")
    private Object status;

    /**
     * 
     */
    @TableField(value = "created_at")
    private Date createdAt;

    /**
     * 
     */
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
        Ports other = (Ports) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPortCode() == null ? other.getPortCode() == null : this.getPortCode().equals(other.getPortCode()))
            && (this.getPortNameZh() == null ? other.getPortNameZh() == null : this.getPortNameZh().equals(other.getPortNameZh()))
            && (this.getPortNameEn() == null ? other.getPortNameEn() == null : this.getPortNameEn().equals(other.getPortNameEn()))
            && (this.getCountry() == null ? other.getCountry() == null : this.getCountry().equals(other.getCountry()))
            && (this.getRegion() == null ? other.getRegion() == null : this.getRegion().equals(other.getRegion()))
            && (this.getLongitude() == null ? other.getLongitude() == null : this.getLongitude().equals(other.getLongitude()))
            && (this.getLatitude() == null ? other.getLatitude() == null : this.getLatitude().equals(other.getLatitude()))
            && (this.getTimezone() == null ? other.getTimezone() == null : this.getTimezone().equals(other.getTimezone()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPortCode() == null) ? 0 : getPortCode().hashCode());
        result = prime * result + ((getPortNameZh() == null) ? 0 : getPortNameZh().hashCode());
        result = prime * result + ((getPortNameEn() == null) ? 0 : getPortNameEn().hashCode());
        result = prime * result + ((getCountry() == null) ? 0 : getCountry().hashCode());
        result = prime * result + ((getRegion() == null) ? 0 : getRegion().hashCode());
        result = prime * result + ((getLongitude() == null) ? 0 : getLongitude().hashCode());
        result = prime * result + ((getLatitude() == null) ? 0 : getLatitude().hashCode());
        result = prime * result + ((getTimezone() == null) ? 0 : getTimezone().hashCode());
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
        sb.append(", portCode=").append(portCode);
        sb.append(", portNameZh=").append(portNameZh);
        sb.append(", portNameEn=").append(portNameEn);
        sb.append(", country=").append(country);
        sb.append(", region=").append(region);
        sb.append(", longitude=").append(longitude);
        sb.append(", latitude=").append(latitude);
        sb.append(", timezone=").append(timezone);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}