package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import syy.portPilotage.enums.RequestStatusEnum;
import syy.portPilotage.enums.PriorityEnum;
import syy.portPilotage.util.EnumColumn;
import syy.portPilotage.util.EnumTypeHandler;

/**
 * 引航申请表
 * @TableName pilotage_requests
 */
@TableName(value ="pilotage_requests")
@Data
public class PilotageRequests implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申请表编号
     */
    @TableField(value = "request_code")
    private String requestCode;

    /**
     * 船舶ID
     */
    @TableField(value = "vessel_id")
    private Long vesselId;

    /**
     * 航次ID
     */
    @TableField(value = "voyage_id")
    private Long voyageId;

    /**
     * 预计作业泊位ID
     */
    @TableField(value = "expected_berth_id")
    private Long expectedBerthId;

    /**
     * 驳船号
     */
    @TableField(value = "barge_number")
    private String bargeNumber;

    /**
     * 进口吃水前
     */
    @TableField(value = "inbound_draft_forward")
    private BigDecimal inboundDraftForward;

    /**
     * 进口吃水后
     */
    @TableField(value = "inbound_draft_aft")
    private BigDecimal inboundDraftAft;

    /**
     * 旅客总数
     */
    @TableField(value = "total_passengers")
    private Integer totalPassengers;

    /**
     * 船员总数
     */
    @TableField(value = "total_crew")
    private Integer totalCrew;

    /**
     * （进）载货名称
     */
    @TableField(value = "inbound_cargo_name")
    private String inboundCargoName;

    /**
     * （进）载货总吨数
     */
    @TableField(value = "inbound_cargo_total_weight")
    private BigDecimal inboundCargoTotalWeight;

    /**
     * （进）普通货物名称吨数
     */
    @TableField(value = "inbound_general_cargo_details")
    private String inboundGeneralCargoDetails;

    /**
     * （进）危险货物名称吨数
     */
    @TableField(value = "inbound_dangerous_cargo_details")
    private String inboundDangerousCargoDetails;

    /**
     * （出）载货名称
     */
    @TableField(value = "outbound_cargo_name")
    private String outboundCargoName;

    /**
     * （出）载货总吨数
     */
    @TableField(value = "outbound_cargo_total_weight")
    private BigDecimal outboundCargoTotalWeight;

    /**
     * （出）普通货物名称吨数
     */
    @TableField(value = "outbound_general_cargo_details")
    private String outboundGeneralCargoDetails;

    /**
     * （出）危险货物名称吨数
     */
    @TableField(value = "outbound_dangerous_cargo_details")
    private String outboundDangerousCargoDetails;

    /**
     * 申请表备注
     */
    @TableField(value = "request_notes")
    private String requestNotes;

    /**
     * 填报人
     */
    @TableField(value = "applicant")
    private String applicant;

    /**
     * 填报日期
     */
    @TableField(value = "application_date")
    private Date applicationDate;

    /**
     * 申请表信息来源
     */
    @TableField(value = "request_source")
    private String requestSource;

        @TableField(value = "visa_processing_result")
    private String visaProcessingResult;

    /**
     * 申请状态
     */
    @TableField(value = "request_status", typeHandler = EnumTypeHandler.class)
    private RequestStatusEnum requestStatus;

        @TableField(value = "reviewer_id")
    private Long reviewerId;

        @TableField(value = "reviewed_at")
    private Date reviewedAt;

    /**
     * 驳回原因
     */
    @TableField(value = "reject_reason")
    private String rejectReason;

        @TableField(value = "priority", typeHandler = EnumTypeHandler.class)
    private PriorityEnum priority;

    /**
     * 特殊要求
     */
    @TableField(value = "special_requirements")
    private String specialRequirements;

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
        PilotageRequests other = (PilotageRequests) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getRequestCode() == null ? other.getRequestCode() == null : this.getRequestCode().equals(other.getRequestCode()))
            && (this.getVesselId() == null ? other.getVesselId() == null : this.getVesselId().equals(other.getVesselId()))
            && (this.getVoyageId() == null ? other.getVoyageId() == null : this.getVoyageId().equals(other.getVoyageId()))
            && (this.getExpectedBerthId() == null ? other.getExpectedBerthId() == null : this.getExpectedBerthId().equals(other.getExpectedBerthId()))
            && (this.getBargeNumber() == null ? other.getBargeNumber() == null : this.getBargeNumber().equals(other.getBargeNumber()))
            && (this.getInboundDraftForward() == null ? other.getInboundDraftForward() == null : this.getInboundDraftForward().equals(other.getInboundDraftForward()))
            && (this.getInboundDraftAft() == null ? other.getInboundDraftAft() == null : this.getInboundDraftAft().equals(other.getInboundDraftAft()))
            && (this.getTotalPassengers() == null ? other.getTotalPassengers() == null : this.getTotalPassengers().equals(other.getTotalPassengers()))
            && (this.getTotalCrew() == null ? other.getTotalCrew() == null : this.getTotalCrew().equals(other.getTotalCrew()))
            && (this.getInboundCargoName() == null ? other.getInboundCargoName() == null : this.getInboundCargoName().equals(other.getInboundCargoName()))
            && (this.getInboundCargoTotalWeight() == null ? other.getInboundCargoTotalWeight() == null : this.getInboundCargoTotalWeight().equals(other.getInboundCargoTotalWeight()))
            && (this.getInboundGeneralCargoDetails() == null ? other.getInboundGeneralCargoDetails() == null : this.getInboundGeneralCargoDetails().equals(other.getInboundGeneralCargoDetails()))
            && (this.getInboundDangerousCargoDetails() == null ? other.getInboundDangerousCargoDetails() == null : this.getInboundDangerousCargoDetails().equals(other.getInboundDangerousCargoDetails()))
            && (this.getOutboundCargoName() == null ? other.getOutboundCargoName() == null : this.getOutboundCargoName().equals(other.getOutboundCargoName()))
            && (this.getOutboundCargoTotalWeight() == null ? other.getOutboundCargoTotalWeight() == null : this.getOutboundCargoTotalWeight().equals(other.getOutboundCargoTotalWeight()))
            && (this.getOutboundGeneralCargoDetails() == null ? other.getOutboundGeneralCargoDetails() == null : this.getOutboundGeneralCargoDetails().equals(other.getOutboundGeneralCargoDetails()))
            && (this.getOutboundDangerousCargoDetails() == null ? other.getOutboundDangerousCargoDetails() == null : this.getOutboundDangerousCargoDetails().equals(other.getOutboundDangerousCargoDetails()))
            && (this.getRequestNotes() == null ? other.getRequestNotes() == null : this.getRequestNotes().equals(other.getRequestNotes()))
            && (this.getApplicant() == null ? other.getApplicant() == null : this.getApplicant().equals(other.getApplicant()))
            && (this.getApplicationDate() == null ? other.getApplicationDate() == null : this.getApplicationDate().equals(other.getApplicationDate()))
            && (this.getRequestSource() == null ? other.getRequestSource() == null : this.getRequestSource().equals(other.getRequestSource()))
            && (this.getVisaProcessingResult() == null ? other.getVisaProcessingResult() == null : this.getVisaProcessingResult().equals(other.getVisaProcessingResult()))
            && (this.getRequestStatus() == null ? other.getRequestStatus() == null : this.getRequestStatus().equals(other.getRequestStatus()))
            && (this.getReviewerId() == null ? other.getReviewerId() == null : this.getReviewerId().equals(other.getReviewerId()))
            && (this.getReviewedAt() == null ? other.getReviewedAt() == null : this.getReviewedAt().equals(other.getReviewedAt()))
            && (this.getRejectReason() == null ? other.getRejectReason() == null : this.getRejectReason().equals(other.getRejectReason()))
            && (this.getPriority() == null ? other.getPriority() == null : this.getPriority().equals(other.getPriority()))
            && (this.getSpecialRequirements() == null ? other.getSpecialRequirements() == null : this.getSpecialRequirements().equals(other.getSpecialRequirements()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getRequestCode() == null) ? 0 : getRequestCode().hashCode());
        result = prime * result + ((getVesselId() == null) ? 0 : getVesselId().hashCode());
        result = prime * result + ((getVoyageId() == null) ? 0 : getVoyageId().hashCode());
        result = prime * result + ((getExpectedBerthId() == null) ? 0 : getExpectedBerthId().hashCode());
        result = prime * result + ((getBargeNumber() == null) ? 0 : getBargeNumber().hashCode());
        result = prime * result + ((getInboundDraftForward() == null) ? 0 : getInboundDraftForward().hashCode());
        result = prime * result + ((getInboundDraftAft() == null) ? 0 : getInboundDraftAft().hashCode());
        result = prime * result + ((getTotalPassengers() == null) ? 0 : getTotalPassengers().hashCode());
        result = prime * result + ((getTotalCrew() == null) ? 0 : getTotalCrew().hashCode());
        result = prime * result + ((getInboundCargoName() == null) ? 0 : getInboundCargoName().hashCode());
        result = prime * result + ((getInboundCargoTotalWeight() == null) ? 0 : getInboundCargoTotalWeight().hashCode());
        result = prime * result + ((getInboundGeneralCargoDetails() == null) ? 0 : getInboundGeneralCargoDetails().hashCode());
        result = prime * result + ((getInboundDangerousCargoDetails() == null) ? 0 : getInboundDangerousCargoDetails().hashCode());
        result = prime * result + ((getOutboundCargoName() == null) ? 0 : getOutboundCargoName().hashCode());
        result = prime * result + ((getOutboundCargoTotalWeight() == null) ? 0 : getOutboundCargoTotalWeight().hashCode());
        result = prime * result + ((getOutboundGeneralCargoDetails() == null) ? 0 : getOutboundGeneralCargoDetails().hashCode());
        result = prime * result + ((getOutboundDangerousCargoDetails() == null) ? 0 : getOutboundDangerousCargoDetails().hashCode());
        result = prime * result + ((getRequestNotes() == null) ? 0 : getRequestNotes().hashCode());
        result = prime * result + ((getApplicant() == null) ? 0 : getApplicant().hashCode());
        result = prime * result + ((getApplicationDate() == null) ? 0 : getApplicationDate().hashCode());
        result = prime * result + ((getRequestSource() == null) ? 0 : getRequestSource().hashCode());
        result = prime * result + ((getVisaProcessingResult() == null) ? 0 : getVisaProcessingResult().hashCode());
        result = prime * result + ((getRequestStatus() == null) ? 0 : getRequestStatus().hashCode());
        result = prime * result + ((getReviewerId() == null) ? 0 : getReviewerId().hashCode());
        result = prime * result + ((getReviewedAt() == null) ? 0 : getReviewedAt().hashCode());
        result = prime * result + ((getRejectReason() == null) ? 0 : getRejectReason().hashCode());
        result = prime * result + ((getPriority() == null) ? 0 : getPriority().hashCode());
        result = prime * result + ((getSpecialRequirements() == null) ? 0 : getSpecialRequirements().hashCode());
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
        sb.append(", requestCode=").append(requestCode);
        sb.append(", vesselId=").append(vesselId);
        sb.append(", voyageId=").append(voyageId);
        sb.append(", expectedBerthId=").append(expectedBerthId);
        sb.append(", bargeNumber=").append(bargeNumber);
        sb.append(", inboundDraftForward=").append(inboundDraftForward);
        sb.append(", inboundDraftAft=").append(inboundDraftAft);
        sb.append(", totalPassengers=").append(totalPassengers);
        sb.append(", totalCrew=").append(totalCrew);
        sb.append(", inboundCargoName=").append(inboundCargoName);
        sb.append(", inboundCargoTotalWeight=").append(inboundCargoTotalWeight);
        sb.append(", inboundGeneralCargoDetails=").append(inboundGeneralCargoDetails);
        sb.append(", inboundDangerousCargoDetails=").append(inboundDangerousCargoDetails);
        sb.append(", outboundCargoName=").append(outboundCargoName);
        sb.append(", outboundCargoTotalWeight=").append(outboundCargoTotalWeight);
        sb.append(", outboundGeneralCargoDetails=").append(outboundGeneralCargoDetails);
        sb.append(", outboundDangerousCargoDetails=").append(outboundDangerousCargoDetails);
        sb.append(", requestNotes=").append(requestNotes);
        sb.append(", applicant=").append(applicant);
        sb.append(", applicationDate=").append(applicationDate);
        sb.append(", requestSource=").append(requestSource);
        sb.append(", visaProcessingResult=").append(visaProcessingResult);
        sb.append(", requestStatus=").append(requestStatus);
        sb.append(", reviewerId=").append(reviewerId);
        sb.append(", reviewedAt=").append(reviewedAt);
        sb.append(", rejectReason=").append(rejectReason);
        sb.append(", priority=").append(priority);
        sb.append(", specialRequirements=").append(specialRequirements);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}