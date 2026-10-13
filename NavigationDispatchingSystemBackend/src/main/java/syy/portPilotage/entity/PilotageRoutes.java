package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import syy.portPilotage.enums.CommonStatusEnum;
import syy.portPilotage.enums.DangerLevelEnum;
import syy.portPilotage.enums.PilotageDirectionEnum;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 引航区间信息表
 * @TableName pilotage_routes
 */
@TableName(value ="pilotage_routes")
@Data
public class PilotageRoutes implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 区间编号
     */
    @TableField(value = "route_code")
    private String routeCode;

    /**
     * 区间类型
     */
    @TableField(value = "route_type")
    private String routeType;

    /**
     * 区间名称
     */
    @TableField(value = "route_name")
    private String routeName;

    /**
     * 区间备注
     */
    @TableField(value = "route_notes")
    private String routeNotes;

    /**
     * 距离_海里
     */
    @TableField(value = "distance_nautical")
    private BigDecimal distanceNautical;

        @TableField(value = "default_duration")
    private Integer defaultDuration;

    /**
     * 状态
     */
    @TableField(value = "status")
    private CommonStatusEnum status;

    /**
     * 危险等级
     */
    @TableField(value = "danger_level")
    private DangerLevelEnum dangerLevel;

    /**
     * 起始位置类型
     */
    @TableField(value = "start_location_type")
    private String startLocationType;

    /**
     * 起始位置ID
     */
    @TableField(value = "start_location_id")
    private Long startLocationId;

    
    @TableField(value = "start_location_name")
    private String startLocationName;

        @TableField(value = "end_location_type")
    private String endLocationType;

        @TableField(value = "end_location_id")
    private Long endLocationId;

        @TableField(value = "end_location_name")
    private String endLocationName;

    /**
     * 引航方向
     */
    @TableField(value = "pilotage_direction")
    private PilotageDirectionEnum pilotageDirection;

    /**
     * 适用船舶最小净吨
     */
    @TableField(value = "applicable_vessel_min_net_ton")
    private BigDecimal applicableVesselMinNetTon;

    /**
     * 适用船舶最大净吨
     */
    @TableField(value = "applicable_vessel_max_net_ton")
    private BigDecimal applicableVesselMaxNetTon;

    /**
     * 仅限白天
     */
    @TableField(value = "daylight_only")
    private Boolean daylightOnly;

    /**
     * 依赖潮汐
     */
    @TableField(value = "tide_dependent")
    private Boolean tideDependent;

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
        PilotageRoutes other = (PilotageRoutes) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getRouteCode() == null ? other.getRouteCode() == null : this.getRouteCode().equals(other.getRouteCode()))
            && (this.getRouteType() == null ? other.getRouteType() == null : this.getRouteType().equals(other.getRouteType()))
            && (this.getRouteName() == null ? other.getRouteName() == null : this.getRouteName().equals(other.getRouteName()))
            && (this.getRouteNotes() == null ? other.getRouteNotes() == null : this.getRouteNotes().equals(other.getRouteNotes()))
            && (this.getDistanceNautical() == null ? other.getDistanceNautical() == null : this.getDistanceNautical().equals(other.getDistanceNautical()))
            && (this.getDefaultDuration() == null ? other.getDefaultDuration() == null : this.getDefaultDuration().equals(other.getDefaultDuration()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getDangerLevel() == null ? other.getDangerLevel() == null : this.getDangerLevel().equals(other.getDangerLevel()))
            && (this.getStartLocationType() == null ? other.getStartLocationType() == null : this.getStartLocationType().equals(other.getStartLocationType()))
            && (this.getStartLocationId() == null ? other.getStartLocationId() == null : this.getStartLocationId().equals(other.getStartLocationId()))
            && (this.getStartLocationName() == null ? other.getStartLocationName() == null : this.getStartLocationName().equals(other.getStartLocationName()))
            && (this.getEndLocationType() == null ? other.getEndLocationType() == null : this.getEndLocationType().equals(other.getEndLocationType()))
            && (this.getEndLocationId() == null ? other.getEndLocationId() == null : this.getEndLocationId().equals(other.getEndLocationId()))
            && (this.getEndLocationName() == null ? other.getEndLocationName() == null : this.getEndLocationName().equals(other.getEndLocationName()))
            && (this.getPilotageDirection() == null ? other.getPilotageDirection() == null : this.getPilotageDirection().equals(other.getPilotageDirection()))
            && (this.getApplicableVesselMinNetTon() == null ? other.getApplicableVesselMinNetTon() == null : this.getApplicableVesselMinNetTon().equals(other.getApplicableVesselMinNetTon()))
            && (this.getApplicableVesselMaxNetTon() == null ? other.getApplicableVesselMaxNetTon() == null : this.getApplicableVesselMaxNetTon().equals(other.getApplicableVesselMaxNetTon()))
            && (this.getDaylightOnly() == null ? other.getDaylightOnly() == null : this.getDaylightOnly().equals(other.getDaylightOnly()))
            && (this.getTideDependent() == null ? other.getTideDependent() == null : this.getTideDependent().equals(other.getTideDependent()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getRouteCode() == null) ? 0 : getRouteCode().hashCode());
        result = prime * result + ((getRouteType() == null) ? 0 : getRouteType().hashCode());
        result = prime * result + ((getRouteName() == null) ? 0 : getRouteName().hashCode());
        result = prime * result + ((getRouteNotes() == null) ? 0 : getRouteNotes().hashCode());
        result = prime * result + ((getDistanceNautical() == null) ? 0 : getDistanceNautical().hashCode());
        result = prime * result + ((getDefaultDuration() == null) ? 0 : getDefaultDuration().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getDangerLevel() == null) ? 0 : getDangerLevel().hashCode());
        result = prime * result + ((getStartLocationType() == null) ? 0 : getStartLocationType().hashCode());
        result = prime * result + ((getStartLocationId() == null) ? 0 : getStartLocationId().hashCode());
        result = prime * result + ((getStartLocationName() == null) ? 0 : getStartLocationName().hashCode());
        result = prime * result + ((getEndLocationType() == null) ? 0 : getEndLocationType().hashCode());
        result = prime * result + ((getEndLocationId() == null) ? 0 : getEndLocationId().hashCode());
        result = prime * result + ((getEndLocationName() == null) ? 0 : getEndLocationName().hashCode());
        result = prime * result + ((getPilotageDirection() == null) ? 0 : getPilotageDirection().hashCode());
        result = prime * result + ((getApplicableVesselMinNetTon() == null) ? 0 : getApplicableVesselMinNetTon().hashCode());
        result = prime * result + ((getApplicableVesselMaxNetTon() == null) ? 0 : getApplicableVesselMaxNetTon().hashCode());
        result = prime * result + ((getDaylightOnly() == null) ? 0 : getDaylightOnly().hashCode());
        result = prime * result + ((getTideDependent() == null) ? 0 : getTideDependent().hashCode());
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
        sb.append(", routeCode=").append(routeCode);
        sb.append(", routeType=").append(routeType);
        sb.append(", routeName=").append(routeName);
        sb.append(", routeNotes=").append(routeNotes);
        sb.append(", distanceNautical=").append(distanceNautical);
        sb.append(", defaultDuration=").append(defaultDuration);
        sb.append(", status=").append(status);
        sb.append(", dangerLevel=").append(dangerLevel);
        sb.append(", startLocationType=").append(startLocationType);
        sb.append(", startLocationId=").append(startLocationId);
        sb.append(", startLocationName=").append(startLocationName);
        sb.append(", endLocationType=").append(endLocationType);
        sb.append(", endLocationId=").append(endLocationId);
        sb.append(", endLocationName=").append(endLocationName);
        sb.append(", pilotageDirection=").append(pilotageDirection);
        sb.append(", applicableVesselMinNetTon=").append(applicableVesselMinNetTon);
        sb.append(", applicableVesselMaxNetTon=").append(applicableVesselMaxNetTon);
        sb.append(", daylightOnly=").append(daylightOnly);
        sb.append(", tideDependent=").append(tideDependent);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}