package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 工作表
 * @TableName work_sheets
 */
@TableName(value ="work_sheets")
@Data
public class WorkSheets implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工作表编号
     */
    @TableField(value = "worksheet_code")
    private String worksheetCode;

        @TableField(value = "plan_start_time")
    private Date planStartTime;

        @TableField(value = "plan_end_time")
    private Date planEndTime;

    /**
     * 引航起始地点
     */
    @TableField(value = "start_location")
    private String startLocation;

        @TableField(value = "end_location")
    private String endLocation;

        @TableField(value = "assigned_tugboats")
    private String assignedTugboats;

        @TableField(value = "assigned_pilot_boats")
    private String assignedPilotBoats;

        @TableField(value = "assigned_pilot_id")
    private Long assignedPilotId;

        @TableField(value = "assigned_driver_id")
    private Long assignedDriverId;

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

    /**
     * 工作状态
     */
    @TableField(value = "work_status")
    private String workStatus;

        @TableField(value = "completion_quality_score")
    private Integer completionQualityScore;

        @TableField(value = "special_situation_record")
    private String specialSituationRecord;

    /**
     * 客户反馈
     */
    @TableField(value = "customer_feedback")
    private String customerFeedback;

    /**
     * 工作时长（分钟）
     */
    @TableField(value = "work_duration")
    private Integer workDuration;

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
        WorkSheets other = (WorkSheets) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getWorksheetCode() == null ? other.getWorksheetCode() == null : this.getWorksheetCode().equals(other.getWorksheetCode()))
            && (this.getPlanStartTime() == null ? other.getPlanStartTime() == null : this.getPlanStartTime().equals(other.getPlanStartTime()))
            && (this.getPlanEndTime() == null ? other.getPlanEndTime() == null : this.getPlanEndTime().equals(other.getPlanEndTime()))
            && (this.getStartLocation() == null ? other.getStartLocation() == null : this.getStartLocation().equals(other.getStartLocation()))
            && (this.getEndLocation() == null ? other.getEndLocation() == null : this.getEndLocation().equals(other.getEndLocation()))
            && (this.getAssignedTugboats() == null ? other.getAssignedTugboats() == null : this.getAssignedTugboats().equals(other.getAssignedTugboats()))
            && (this.getAssignedPilotBoats() == null ? other.getAssignedPilotBoats() == null : this.getAssignedPilotBoats().equals(other.getAssignedPilotBoats()))
            && (this.getAssignedPilotId() == null ? other.getAssignedPilotId() == null : this.getAssignedPilotId().equals(other.getAssignedPilotId()))
            && (this.getAssignedDriverId() == null ? other.getAssignedDriverId() == null : this.getAssignedDriverId().equals(other.getAssignedDriverId()))
            && (this.getBoardingTime() == null ? other.getBoardingTime() == null : this.getBoardingTime().equals(other.getBoardingTime()))
            && (this.getLeavingTime() == null ? other.getLeavingTime() == null : this.getLeavingTime().equals(other.getLeavingTime()))
            && (this.getActualStartTime() == null ? other.getActualStartTime() == null : this.getActualStartTime().equals(other.getActualStartTime()))
            && (this.getActualEndTime() == null ? other.getActualEndTime() == null : this.getActualEndTime().equals(other.getActualEndTime()))
            && (this.getOperationTypeFlag() == null ? other.getOperationTypeFlag() == null : this.getOperationTypeFlag().equals(other.getOperationTypeFlag()))
            && (this.getWorkStatus() == null ? other.getWorkStatus() == null : this.getWorkStatus().equals(other.getWorkStatus()))
            && (this.getCompletionQualityScore() == null ? other.getCompletionQualityScore() == null : this.getCompletionQualityScore().equals(other.getCompletionQualityScore()))
            && (this.getSpecialSituationRecord() == null ? other.getSpecialSituationRecord() == null : this.getSpecialSituationRecord().equals(other.getSpecialSituationRecord()))
            && (this.getCustomerFeedback() == null ? other.getCustomerFeedback() == null : this.getCustomerFeedback().equals(other.getCustomerFeedback()))
            && (this.getWorkDuration() == null ? other.getWorkDuration() == null : this.getWorkDuration().equals(other.getWorkDuration()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getWorksheetCode() == null) ? 0 : getWorksheetCode().hashCode());
        result = prime * result + ((getPlanStartTime() == null) ? 0 : getPlanStartTime().hashCode());
        result = prime * result + ((getPlanEndTime() == null) ? 0 : getPlanEndTime().hashCode());
        result = prime * result + ((getStartLocation() == null) ? 0 : getStartLocation().hashCode());
        result = prime * result + ((getEndLocation() == null) ? 0 : getEndLocation().hashCode());
        result = prime * result + ((getAssignedTugboats() == null) ? 0 : getAssignedTugboats().hashCode());
        result = prime * result + ((getAssignedPilotBoats() == null) ? 0 : getAssignedPilotBoats().hashCode());
        result = prime * result + ((getAssignedPilotId() == null) ? 0 : getAssignedPilotId().hashCode());
        result = prime * result + ((getAssignedDriverId() == null) ? 0 : getAssignedDriverId().hashCode());
        result = prime * result + ((getBoardingTime() == null) ? 0 : getBoardingTime().hashCode());
        result = prime * result + ((getLeavingTime() == null) ? 0 : getLeavingTime().hashCode());
        result = prime * result + ((getActualStartTime() == null) ? 0 : getActualStartTime().hashCode());
        result = prime * result + ((getActualEndTime() == null) ? 0 : getActualEndTime().hashCode());
        result = prime * result + ((getOperationTypeFlag() == null) ? 0 : getOperationTypeFlag().hashCode());
        result = prime * result + ((getWorkStatus() == null) ? 0 : getWorkStatus().hashCode());
        result = prime * result + ((getCompletionQualityScore() == null) ? 0 : getCompletionQualityScore().hashCode());
        result = prime * result + ((getSpecialSituationRecord() == null) ? 0 : getSpecialSituationRecord().hashCode());
        result = prime * result + ((getCustomerFeedback() == null) ? 0 : getCustomerFeedback().hashCode());
        result = prime * result + ((getWorkDuration() == null) ? 0 : getWorkDuration().hashCode());
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
        sb.append(", worksheetCode=").append(worksheetCode);
        sb.append(", planStartTime=").append(planStartTime);
        sb.append(", planEndTime=").append(planEndTime);
        sb.append(", startLocation=").append(startLocation);
        sb.append(", endLocation=").append(endLocation);
        sb.append(", assignedTugboats=").append(assignedTugboats);
        sb.append(", assignedPilotBoats=").append(assignedPilotBoats);
        sb.append(", assignedPilotId=").append(assignedPilotId);
        sb.append(", assignedDriverId=").append(assignedDriverId);
        sb.append(", boardingTime=").append(boardingTime);
        sb.append(", leavingTime=").append(leavingTime);
        sb.append(", actualStartTime=").append(actualStartTime);
        sb.append(", actualEndTime=").append(actualEndTime);
        sb.append(", operationTypeFlag=").append(operationTypeFlag);
        sb.append(", workStatus=").append(workStatus);
        sb.append(", completionQualityScore=").append(completionQualityScore);
        sb.append(", specialSituationRecord=").append(specialSituationRecord);
        sb.append(", customerFeedback=").append(customerFeedback);
        sb.append(", workDuration=").append(workDuration);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}