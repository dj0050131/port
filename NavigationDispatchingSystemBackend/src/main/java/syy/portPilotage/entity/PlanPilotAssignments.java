package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 引航计划引航员分配表
 * @TableName plan_pilot_assignments
 */
@TableName(value ="plan_pilot_assignments")
@Data
public class PlanPilotAssignments implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 引航计划ID
     */
    @TableField(value = "plan_id")
    private Long planId;

    /**
     * 引航员ID
     */
    @TableField(value = "pilot_id")
    private Long pilotId;

    /**
     * 角色类型（MAIN-主引/ASSISTANT-副引/TRAINEE-实习/RESERVE-后备）
     */
    @TableField(value = "role_type")
    private String roleType;

    /**
     * 分配说明
     */
    @TableField(value = "assignment_notes")
    private String assignmentNotes;

        @TableField(value = "estimated_start_time")
    private Date estimatedStartTime;

        @TableField(value = "estimated_end_time")
    private Date estimatedEndTime;

        @TableField(value = "actual_start_time")
    private Date actualStartTime;

        @TableField(value = "actual_end_time")
    private Date actualEndTime;

    /**
     * 分配状态(ASSIGNED,CONFIRMED,CANCELLED,COMPLETED)
     */
    @TableField(value = "status")
    private String status;

    /**
     * 是否参与考核
     */
    @TableField(value = "assessment_flag")
    private Boolean assessmentFlag;

    /**
     * 考核评分
     */
    @TableField(value = "assessment_score")
    private Integer assessmentScore;

    /**
     * 考核备注
     */
    @TableField(value = "assessment_notes")
    private String assessmentNotes;

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
        PlanPilotAssignments other = (PlanPilotAssignments) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPlanId() == null ? other.getPlanId() == null : this.getPlanId().equals(other.getPlanId()))
            && (this.getPilotId() == null ? other.getPilotId() == null : this.getPilotId().equals(other.getPilotId()))
            && (this.getRoleType() == null ? other.getRoleType() == null : this.getRoleType().equals(other.getRoleType()))
            && (this.getAssignmentNotes() == null ? other.getAssignmentNotes() == null : this.getAssignmentNotes().equals(other.getAssignmentNotes()))
            && (this.getEstimatedStartTime() == null ? other.getEstimatedStartTime() == null : this.getEstimatedStartTime().equals(other.getEstimatedStartTime()))
            && (this.getEstimatedEndTime() == null ? other.getEstimatedEndTime() == null : this.getEstimatedEndTime().equals(other.getEstimatedEndTime()))
            && (this.getActualStartTime() == null ? other.getActualStartTime() == null : this.getActualStartTime().equals(other.getActualStartTime()))
            && (this.getActualEndTime() == null ? other.getActualEndTime() == null : this.getActualEndTime().equals(other.getActualEndTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getAssessmentFlag() == null ? other.getAssessmentFlag() == null : this.getAssessmentFlag().equals(other.getAssessmentFlag()))
            && (this.getAssessmentScore() == null ? other.getAssessmentScore() == null : this.getAssessmentScore().equals(other.getAssessmentScore()))
            && (this.getAssessmentNotes() == null ? other.getAssessmentNotes() == null : this.getAssessmentNotes().equals(other.getAssessmentNotes()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPlanId() == null) ? 0 : getPlanId().hashCode());
        result = prime * result + ((getPilotId() == null) ? 0 : getPilotId().hashCode());
        result = prime * result + ((getRoleType() == null) ? 0 : getRoleType().hashCode());
        result = prime * result + ((getAssignmentNotes() == null) ? 0 : getAssignmentNotes().hashCode());
        result = prime * result + ((getEstimatedStartTime() == null) ? 0 : getEstimatedStartTime().hashCode());
        result = prime * result + ((getEstimatedEndTime() == null) ? 0 : getEstimatedEndTime().hashCode());
        result = prime * result + ((getActualStartTime() == null) ? 0 : getActualStartTime().hashCode());
        result = prime * result + ((getActualEndTime() == null) ? 0 : getActualEndTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getAssessmentFlag() == null) ? 0 : getAssessmentFlag().hashCode());
        result = prime * result + ((getAssessmentScore() == null) ? 0 : getAssessmentScore().hashCode());
        result = prime * result + ((getAssessmentNotes() == null) ? 0 : getAssessmentNotes().hashCode());
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
        sb.append(", planId=").append(planId);
        sb.append(", pilotId=").append(pilotId);
        sb.append(", roleType=").append(roleType);
        sb.append(", assignmentNotes=").append(assignmentNotes);
        sb.append(", estimatedStartTime=").append(estimatedStartTime);
        sb.append(", estimatedEndTime=").append(estimatedEndTime);
        sb.append(", actualStartTime=").append(actualStartTime);
        sb.append(", actualEndTime=").append(actualEndTime);
        sb.append(", status=").append(status);
        sb.append(", assessmentFlag=").append(assessmentFlag);
        sb.append(", assessmentScore=").append(assessmentScore);
        sb.append(", assessmentNotes=").append(assessmentNotes);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}