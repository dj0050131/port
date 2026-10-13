package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import syy.portPilotage.enums.LocationTypeEnum;
import syy.portPilotage.enums.PriorityEnum;
import org.apache.ibatis.type.EnumTypeHandler;

/**
 * 引航计划表
 * @TableName pilotage_plans
 */
@TableName(value ="pilotage_plans")
@Data
public class PilotagePlans implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 引航计划编号
     */
    @TableField(value = "plan_code")
    private String planCode;

        @TableField(value = "plan_start_time")
    private Date planStartTime;

        @TableField(value = "plan_end_time")
    private Date planEndTime;

    /**
     * 引航起始地点名称
     */
    @TableField(value = "start_location_name")
    private String startLocationName;

        @TableField(value = "end_location_name")
    private String endLocationName;

    /**
     *  起始位置ID
     */
    @TableField(value = "start_location_id")
    private Long startLocationId;

    /**
     * 起始位置类型
     */
    @TableField(value = "start_location_type")
    private LocationTypeEnum startLocationType;

        @TableField(value = "end_location_id")
    private Long endLocationId;

        @TableField(value = "end_location_type")
    private LocationTypeEnum endLocationType;

        @TableField(value = "assigned_pilot_id")
    private Long assignedPilotId;

        @TableField(value = "assigned_driver_id")
    private Long assignedDriverId;

        @TableField(value = "assigned_vehicle_id")
    private Long assignedVehicleId;

        @TableField(value = "vessel_id")
    private Long vesselId;

        @TableField(value = "voyage_id")
    private Long voyageId;

    /**
     * 登轮时间
     */
    @TableField(value = "boarding_time")
    private Date boardingTime;

    /**
     * 离轮时间
     */
    @TableField(value = "leaving_time")
    private Date leavingTime;

        @TableField(value = "actual_start_time")
    private Date actualStartTime;

        @TableField(value = "actual_end_time")
    private Date actualEndTime;

    /**
     * 引航作业类型标志
     */
    @TableField(value = "operation_type_flag")
    private String operationTypeFlag;

        @TableField(value = "priority", typeHandler = EnumTypeHandler.class)
    private PriorityEnum priority;

    /**
     * 船期状态标识
     */
    @TableField(value = "vessel_schedule_status")
    private String vesselScheduleStatus;

    /**
     * 靠离泊计划状态标识
     */
    @TableField(value = "berthing_plan_status")
    private String berthingPlanStatus;

    /**
     * 引航计划状态标识
     */
    @TableField(value = "pilotage_plan_status")
    private String pilotagePlanStatus;

    /**
     * 此表类别标识
     */
    @TableField(value = "table_category_flag")
    private String tableCategoryFlag;

    /**
     * 计划状态 - 这里应该是String类型
     */
    @TableField(value = "plan_status")
    private String planStatus;

        @TableField(value = "cancel_reason")
    private String cancelReason;

        @TableField(value = "replan_required_reason")
    private String replanRequiredReason;

    
    @TableField(value = "replan_required_resource_type")
    private String replanRequiredResourceType;

    
    @TableField(value = "replan_required_resource_id")
    private Long replanRequiredResourceId;

    
    @TableField(value = "replan_required_at")
    private Date replanRequiredAt;

        @TableField(value = "created_by")
    private Long createdBy;

    /**
     * 发布时间
     */
    @TableField(value = "published_at")
    private Date publishedAt;

        @TableField(value = "actual_completion_time")
    private Date actualCompletionTime;

    /**
     * 延误原因
     */
    @TableField(value = "delay_reason")
    private String delayReason;

    /**
     * 天气影响说明
     */
    @TableField(value = "weather_impact")
    private String weatherImpact;

        @TableField(value = "notes")
    private String notes;

        @TableField(value = "created_at")
    private Date createdAt;

        @TableField(value = "updated_at")
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    // 注意：assigned_tugboats和assigned_pilot_boats字段在实体类中已移除
    // 这些信息现在通过分配表来管理
}