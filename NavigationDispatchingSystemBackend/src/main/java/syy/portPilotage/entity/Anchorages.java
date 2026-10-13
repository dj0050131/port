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
 * 锚地信息表
 * @TableName anchorages
 */
@TableName(value ="anchorages")
@Data
public class Anchorages implements Serializable {
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
     * 锚地代码
     */
    @TableField(value = "anchorage_code")
    private String anchorageCode;

    /**
     * 锚地中文名称
     */
    @TableField(value = "anchorage_name_zh")
    private String anchorageNameZh;

    /**
     * 锚地英文名称
     */
    @TableField(value = "anchorage_name_en")
    private String anchorageNameEn;

    /**
     * 锚地类型
     */
    @TableField(value = "anchorage_type")
    private Object anchorageType;

    /**
     * 容纳船舶数量
     */
    @TableField(value = "capacity")
    private Integer capacity;

    /**
     * 最大水深(米)
     */
    @TableField(value = "max_draft")
    private BigDecimal maxDraft;

    /**
     * 最小水深(米)
     */
    @TableField(value = "min_draft")
    private BigDecimal minDraft;

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
     * 半径(海里)
     */
    @TableField(value = "radius")
    private BigDecimal radius;

    /**
     * 边界坐标
     */
    @TableField(value = "boundary_coordinates")
    private Object boundaryCoordinates;

    /**
     * 潮汐限制说明
     */
    @TableField(value = "tide_restriction")
    private String tideRestriction;

    /**
     * 气象限制说明
     */
    @TableField(value = "weather_restriction")
    private String weatherRestriction;

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
        Anchorages other = (Anchorages) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPortAreaId() == null ? other.getPortAreaId() == null : this.getPortAreaId().equals(other.getPortAreaId()))
            && (this.getAnchorageCode() == null ? other.getAnchorageCode() == null : this.getAnchorageCode().equals(other.getAnchorageCode()))
            && (this.getAnchorageNameZh() == null ? other.getAnchorageNameZh() == null : this.getAnchorageNameZh().equals(other.getAnchorageNameZh()))
            && (this.getAnchorageNameEn() == null ? other.getAnchorageNameEn() == null : this.getAnchorageNameEn().equals(other.getAnchorageNameEn()))
            && (this.getAnchorageType() == null ? other.getAnchorageType() == null : this.getAnchorageType().equals(other.getAnchorageType()))
            && (this.getCapacity() == null ? other.getCapacity() == null : this.getCapacity().equals(other.getCapacity()))
            && (this.getMaxDraft() == null ? other.getMaxDraft() == null : this.getMaxDraft().equals(other.getMaxDraft()))
            && (this.getMinDraft() == null ? other.getMinDraft() == null : this.getMinDraft().equals(other.getMinDraft()))
            && (this.getLongitude() == null ? other.getLongitude() == null : this.getLongitude().equals(other.getLongitude()))
            && (this.getLatitude() == null ? other.getLatitude() == null : this.getLatitude().equals(other.getLatitude()))
            && (this.getRadius() == null ? other.getRadius() == null : this.getRadius().equals(other.getRadius()))
            && (this.getBoundaryCoordinates() == null ? other.getBoundaryCoordinates() == null : this.getBoundaryCoordinates().equals(other.getBoundaryCoordinates()))
            && (this.getTideRestriction() == null ? other.getTideRestriction() == null : this.getTideRestriction().equals(other.getTideRestriction()))
            && (this.getWeatherRestriction() == null ? other.getWeatherRestriction() == null : this.getWeatherRestriction().equals(other.getWeatherRestriction()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPortAreaId() == null) ? 0 : getPortAreaId().hashCode());
        result = prime * result + ((getAnchorageCode() == null) ? 0 : getAnchorageCode().hashCode());
        result = prime * result + ((getAnchorageNameZh() == null) ? 0 : getAnchorageNameZh().hashCode());
        result = prime * result + ((getAnchorageNameEn() == null) ? 0 : getAnchorageNameEn().hashCode());
        result = prime * result + ((getAnchorageType() == null) ? 0 : getAnchorageType().hashCode());
        result = prime * result + ((getCapacity() == null) ? 0 : getCapacity().hashCode());
        result = prime * result + ((getMaxDraft() == null) ? 0 : getMaxDraft().hashCode());
        result = prime * result + ((getMinDraft() == null) ? 0 : getMinDraft().hashCode());
        result = prime * result + ((getLongitude() == null) ? 0 : getLongitude().hashCode());
        result = prime * result + ((getLatitude() == null) ? 0 : getLatitude().hashCode());
        result = prime * result + ((getRadius() == null) ? 0 : getRadius().hashCode());
        result = prime * result + ((getBoundaryCoordinates() == null) ? 0 : getBoundaryCoordinates().hashCode());
        result = prime * result + ((getTideRestriction() == null) ? 0 : getTideRestriction().hashCode());
        result = prime * result + ((getWeatherRestriction() == null) ? 0 : getWeatherRestriction().hashCode());
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
        sb.append(", portAreaId=").append(portAreaId);
        sb.append(", anchorageCode=").append(anchorageCode);
        sb.append(", anchorageNameZh=").append(anchorageNameZh);
        sb.append(", anchorageNameEn=").append(anchorageNameEn);
        sb.append(", anchorageType=").append(anchorageType);
        sb.append(", capacity=").append(capacity);
        sb.append(", maxDraft=").append(maxDraft);
        sb.append(", minDraft=").append(minDraft);
        sb.append(", longitude=").append(longitude);
        sb.append(", latitude=").append(latitude);
        sb.append(", radius=").append(radius);
        sb.append(", boundaryCoordinates=").append(boundaryCoordinates);
        sb.append(", tideRestriction=").append(tideRestriction);
        sb.append(", weatherRestriction=").append(weatherRestriction);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}