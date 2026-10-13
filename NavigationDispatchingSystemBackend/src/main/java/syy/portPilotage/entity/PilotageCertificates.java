package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import syy.portPilotage.enums.LocationTypeEnum;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 引航签证单表
 * @TableName pilotage_certificates
 */
@TableName(value ="pilotage_certificates")
@Data
public class PilotageCertificates implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 签证单编号
     */
    @TableField(value = "certificate_code")
    private String certificateCode;

    /**
     * 作业编号
     */
    @TableField(value = "operation_id")
    private String operationId;

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
     * 进口船代ID
     */
    @TableField(value = "inbound_agent_id")
    private Long inboundAgentId;

    /**
     * 出口船代ID
     */
    @TableField(value = "outbound_agent_id")
    private Long outboundAgentId;

        @TableField(value = "start_location_id")
    private Long startLocationId;

        @TableField(value = "end_location_id")
    private Long endLocationId;

    /**
     * 起始位置类型
     */
    @TableField(value = "start_location_type")
    private LocationTypeEnum startLocationType;

        @TableField(value = "start_location_name")
    private String startLocationName;

        @TableField(value = "end_location_type")
    private LocationTypeEnum endLocationType;

        @TableField(value = "end_location_name")
    private String endLocationName;

    /**
     * 贸易类型
     */
    @TableField(value = "trade_type")
    private String tradeType;

    /**
     * 船舶名称(中文)
     */
    @TableField(value = "vessel_name_zh")
    private String vesselNameZh;

    /**
     * 护航标记
     */
    @TableField(value = "escort_flag")
    private String escortFlag;

    /**
     * 录入人
     */
    @TableField(value = "entered_by")
    private String enteredBy;

    /**
     * 交接单编号
     */
    @TableField(value = "handover_sheet_id")
    private String handoverSheetId;
    /**
     * 引航计划id
     */
    @TableField(value = "plan_id")
    private Long planId;

    /**
     * 航线代码
     */
    @TableField(value = "route_code")
    private String routeCode;

    /**
     * 航线名称
     */
    @TableField(value = "route_name")
    private String routeName;

    /**
     * 进口航次
     */
    @TableField(value = "inbound_voyage")
    private String inboundVoyage;

    /**
     * 出口航次
     */
    @TableField(value = "outbound_voyage")
    private String outboundVoyage;

    /**
     * 助泊标记
     */
    @TableField(value = "berthing_assistance_flag")
    private String berthingAssistanceFlag;

    /**
     * 助泊(护航)计费
     */
    @TableField(value = "berthing_assistance_charge")
    private BigDecimal berthingAssistanceCharge;

    /**
     * 船舶名称(英文)
     */
    @TableField(value = "vessel_name_en")
    private String vesselNameEn;

    /**
     * 撤回标记
     */
    @TableField(value = "withdrawal_flag")
    private String withdrawalFlag;

    /**
     * 引航类型
     */
    @TableField(value = "pilotage_type")
    private String pilotageType;

    /**
     * 前吃水
     */
    @TableField(value = "forward_draft")
    private BigDecimal forwardDraft;

    /**
     * 后吃水
     */
    @TableField(value = "aft_draft")
    private BigDecimal aftDraft;

    /**
     * 马力
     */
    @TableField(value = "horsepower")
    private BigDecimal horsepower;

    /**
     * 滞留标记
     */
    @TableField(value = "detention_flag")
    private String detentionFlag;

    /**
     * 滞留小时数
     */
    @TableField(value = "detention_hours")
    private BigDecimal detentionHours;

    /**
     * 滞留人数
     */
    @TableField(value = "detention_persons")
    private Integer detentionPersons;

        @TableField(value = "pilotage_start_time")
    private Date pilotageStartTime;

        @TableField(value = "pilotage_end_time")
    private Date pilotageEndTime;

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
     * 状态编号
     */
    @TableField(value = "status_code")
    private String statusCode;

    /**
     * 状态名称
     */
    @TableField(value = "status_name")
    private String statusName;

    /**
     * 登船时间
     */
    @TableField(value = "boarding_time")
    private Date boardingTime;

    /**
     * 离船时间
     */
    @TableField(value = "leaving_time")
    private Date leavingTime;

    /**
     * 备注
     */
    @TableField(value = "notes")
    private String notes;

        @TableField(value = "submit_status")
    private Object submitStatus;

        @TableField(value = "submitted_at")
    private Date submittedAt;

        @TableField(value = "confirmed_by")
    private Long confirmedBy;

        @TableField(value = "confirmed_at")
    private Date confirmedAt;

    /**
     * 计费状态
     */
    @TableField(value = "billing_status")
    private Object billingStatus;

    /**
     * 特殊情况说明
     */
    @TableField(value = "special_situation_description")
    private String specialSituationDescription;

    /**
     * 引航员签字时间
     */
    @TableField(value = "pilot_sign_time")
    private Date pilotSignTime;

        @TableField(value = "agent_confirm_time")
    private Date agentConfirmTime;

        @TableField(value = "created_by")
    private Long createdBy;

        @TableField(value = "updated_by")
    private Long updatedBy;

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
        PilotageCertificates other = (PilotageCertificates) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getCertificateCode() == null ? other.getCertificateCode() == null : this.getCertificateCode().equals(other.getCertificateCode()))
            && (this.getOperationId() == null ? other.getOperationId() == null : this.getOperationId().equals(other.getOperationId()))
            && (this.getVesselId() == null ? other.getVesselId() == null : this.getVesselId().equals(other.getVesselId()))
            && (this.getVoyageId() == null ? other.getVoyageId() == null : this.getVoyageId().equals(other.getVoyageId()))
            && (this.getInboundAgentId() == null ? other.getInboundAgentId() == null : this.getInboundAgentId().equals(other.getInboundAgentId()))
            && (this.getOutboundAgentId() == null ? other.getOutboundAgentId() == null : this.getOutboundAgentId().equals(other.getOutboundAgentId()))
            && (this.getStartLocationId() == null ? other.getStartLocationId() == null : this.getStartLocationId().equals(other.getStartLocationId()))
            && (this.getEndLocationId() == null ? other.getEndLocationId() == null : this.getEndLocationId().equals(other.getEndLocationId()))
            && (this.getStartLocationType() == null ? other.getStartLocationType() == null : this.getStartLocationType().equals(other.getStartLocationType()))
            && (this.getStartLocationName() == null ? other.getStartLocationName() == null : this.getStartLocationName().equals(other.getStartLocationName()))
            && (this.getEndLocationType() == null ? other.getEndLocationType() == null : this.getEndLocationType().equals(other.getEndLocationType()))
            && (this.getEndLocationName() == null ? other.getEndLocationName() == null : this.getEndLocationName().equals(other.getEndLocationName()))
            && (this.getTradeType() == null ? other.getTradeType() == null : this.getTradeType().equals(other.getTradeType()))
            && (this.getVesselNameZh() == null ? other.getVesselNameZh() == null : this.getVesselNameZh().equals(other.getVesselNameZh()))
            && (this.getEscortFlag() == null ? other.getEscortFlag() == null : this.getEscortFlag().equals(other.getEscortFlag()))
            && (this.getEnteredBy() == null ? other.getEnteredBy() == null : this.getEnteredBy().equals(other.getEnteredBy()))
            && (this.getHandoverSheetId() == null ? other.getHandoverSheetId() == null : this.getHandoverSheetId().equals(other.getHandoverSheetId()))
            && (this.getRouteCode() == null ? other.getRouteCode() == null : this.getRouteCode().equals(other.getRouteCode()))
            && (this.getRouteName() == null ? other.getRouteName() == null : this.getRouteName().equals(other.getRouteName()))
            && (this.getInboundVoyage() == null ? other.getInboundVoyage() == null : this.getInboundVoyage().equals(other.getInboundVoyage()))
            && (this.getOutboundVoyage() == null ? other.getOutboundVoyage() == null : this.getOutboundVoyage().equals(other.getOutboundVoyage()))
            && (this.getBerthingAssistanceFlag() == null ? other.getBerthingAssistanceFlag() == null : this.getBerthingAssistanceFlag().equals(other.getBerthingAssistanceFlag()))
            && (this.getBerthingAssistanceCharge() == null ? other.getBerthingAssistanceCharge() == null : this.getBerthingAssistanceCharge().equals(other.getBerthingAssistanceCharge()))
            && (this.getVesselNameEn() == null ? other.getVesselNameEn() == null : this.getVesselNameEn().equals(other.getVesselNameEn()))
            && (this.getWithdrawalFlag() == null ? other.getWithdrawalFlag() == null : this.getWithdrawalFlag().equals(other.getWithdrawalFlag()))
            && (this.getPilotageType() == null ? other.getPilotageType() == null : this.getPilotageType().equals(other.getPilotageType()))
            && (this.getForwardDraft() == null ? other.getForwardDraft() == null : this.getForwardDraft().equals(other.getForwardDraft()))
            && (this.getAftDraft() == null ? other.getAftDraft() == null : this.getAftDraft().equals(other.getAftDraft()))
            && (this.getHorsepower() == null ? other.getHorsepower() == null : this.getHorsepower().equals(other.getHorsepower()))
            && (this.getDetentionFlag() == null ? other.getDetentionFlag() == null : this.getDetentionFlag().equals(other.getDetentionFlag()))
            && (this.getDetentionHours() == null ? other.getDetentionHours() == null : this.getDetentionHours().equals(other.getDetentionHours()))
            && (this.getDetentionPersons() == null ? other.getDetentionPersons() == null : this.getDetentionPersons().equals(other.getDetentionPersons()))
            && (this.getPilotageStartTime() == null ? other.getPilotageStartTime() == null : this.getPilotageStartTime().equals(other.getPilotageStartTime()))
            && (this.getPilotageEndTime() == null ? other.getPilotageEndTime() == null : this.getPilotageEndTime().equals(other.getPilotageEndTime()))
            && (this.getTotalPassengers() == null ? other.getTotalPassengers() == null : this.getTotalPassengers().equals(other.getTotalPassengers()))
            && (this.getTotalCrew() == null ? other.getTotalCrew() == null : this.getTotalCrew().equals(other.getTotalCrew()))
            && (this.getStatusCode() == null ? other.getStatusCode() == null : this.getStatusCode().equals(other.getStatusCode()))
            && (this.getStatusName() == null ? other.getStatusName() == null : this.getStatusName().equals(other.getStatusName()))
            && (this.getBoardingTime() == null ? other.getBoardingTime() == null : this.getBoardingTime().equals(other.getBoardingTime()))
            && (this.getLeavingTime() == null ? other.getLeavingTime() == null : this.getLeavingTime().equals(other.getLeavingTime()))
            && (this.getNotes() == null ? other.getNotes() == null : this.getNotes().equals(other.getNotes()))
            && (this.getSubmitStatus() == null ? other.getSubmitStatus() == null : this.getSubmitStatus().equals(other.getSubmitStatus()))
            && (this.getSubmittedAt() == null ? other.getSubmittedAt() == null : this.getSubmittedAt().equals(other.getSubmittedAt()))
            && (this.getConfirmedBy() == null ? other.getConfirmedBy() == null : this.getConfirmedBy().equals(other.getConfirmedBy()))
            && (this.getConfirmedAt() == null ? other.getConfirmedAt() == null : this.getConfirmedAt().equals(other.getConfirmedAt()))
            && (this.getBillingStatus() == null ? other.getBillingStatus() == null : this.getBillingStatus().equals(other.getBillingStatus()))
            && (this.getSpecialSituationDescription() == null ? other.getSpecialSituationDescription() == null : this.getSpecialSituationDescription().equals(other.getSpecialSituationDescription()))
            && (this.getPilotSignTime() == null ? other.getPilotSignTime() == null : this.getPilotSignTime().equals(other.getPilotSignTime()))
            && (this.getAgentConfirmTime() == null ? other.getAgentConfirmTime() == null : this.getAgentConfirmTime().equals(other.getAgentConfirmTime()))
            && (this.getCreatedBy() == null ? other.getCreatedBy() == null : this.getCreatedBy().equals(other.getCreatedBy()))
            && (this.getUpdatedBy() == null ? other.getUpdatedBy() == null : this.getUpdatedBy().equals(other.getUpdatedBy()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getPlanId() == null ? other.getPlanId() == null : this.getPlanId().equals(other.getPlanId()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getCertificateCode() == null) ? 0 : getCertificateCode().hashCode());
        result = prime * result + ((getOperationId() == null) ? 0 : getOperationId().hashCode());
        result = prime * result + ((getVesselId() == null) ? 0 : getVesselId().hashCode());
        result = prime * result + ((getVoyageId() == null) ? 0 : getVoyageId().hashCode());
        result = prime * result + ((getInboundAgentId() == null) ? 0 : getInboundAgentId().hashCode());
        result = prime * result + ((getOutboundAgentId() == null) ? 0 : getOutboundAgentId().hashCode());
        result = prime * result + ((getStartLocationId() == null) ? 0 : getStartLocationId().hashCode());
        result = prime * result + ((getEndLocationId() == null) ? 0 : getEndLocationId().hashCode());
        result = prime * result + ((getStartLocationType() == null) ? 0 : getStartLocationType().hashCode());
        result = prime * result + ((getStartLocationName() == null) ? 0 : getStartLocationName().hashCode());
        result = prime * result + ((getEndLocationType() == null) ? 0 : getEndLocationType().hashCode());
        result = prime * result + ((getEndLocationName() == null) ? 0 : getEndLocationName().hashCode());
        result = prime * result + ((getTradeType() == null) ? 0 : getTradeType().hashCode());
        result = prime * result + ((getVesselNameZh() == null) ? 0 : getVesselNameZh().hashCode());
        result = prime * result + ((getEscortFlag() == null) ? 0 : getEscortFlag().hashCode());
        result = prime * result + ((getEnteredBy() == null) ? 0 : getEnteredBy().hashCode());
        result = prime * result + ((getHandoverSheetId() == null) ? 0 : getHandoverSheetId().hashCode());
        result = prime * result + ((getRouteCode() == null) ? 0 : getRouteCode().hashCode());
        result = prime * result + ((getRouteName() == null) ? 0 : getRouteName().hashCode());
        result = prime * result + ((getInboundVoyage() == null) ? 0 : getInboundVoyage().hashCode());
        result = prime * result + ((getOutboundVoyage() == null) ? 0 : getOutboundVoyage().hashCode());
        result = prime * result + ((getBerthingAssistanceFlag() == null) ? 0 : getBerthingAssistanceFlag().hashCode());
        result = prime * result + ((getBerthingAssistanceCharge() == null) ? 0 : getBerthingAssistanceCharge().hashCode());
        result = prime * result + ((getVesselNameEn() == null) ? 0 : getVesselNameEn().hashCode());
        result = prime * result + ((getWithdrawalFlag() == null) ? 0 : getWithdrawalFlag().hashCode());
        result = prime * result + ((getPilotageType() == null) ? 0 : getPilotageType().hashCode());
        result = prime * result + ((getForwardDraft() == null) ? 0 : getForwardDraft().hashCode());
        result = prime * result + ((getAftDraft() == null) ? 0 : getAftDraft().hashCode());
        result = prime * result + ((getHorsepower() == null) ? 0 : getHorsepower().hashCode());
        result = prime * result + ((getDetentionFlag() == null) ? 0 : getDetentionFlag().hashCode());
        result = prime * result + ((getDetentionHours() == null) ? 0 : getDetentionHours().hashCode());
        result = prime * result + ((getDetentionPersons() == null) ? 0 : getDetentionPersons().hashCode());
        result = prime * result + ((getPilotageStartTime() == null) ? 0 : getPilotageStartTime().hashCode());
        result = prime * result + ((getPilotageEndTime() == null) ? 0 : getPilotageEndTime().hashCode());
        result = prime * result + ((getTotalPassengers() == null) ? 0 : getTotalPassengers().hashCode());
        result = prime * result + ((getTotalCrew() == null) ? 0 : getTotalCrew().hashCode());
        result = prime * result + ((getStatusCode() == null) ? 0 : getStatusCode().hashCode());
        result = prime * result + ((getStatusName() == null) ? 0 : getStatusName().hashCode());
        result = prime * result + ((getBoardingTime() == null) ? 0 : getBoardingTime().hashCode());
        result = prime * result + ((getLeavingTime() == null) ? 0 : getLeavingTime().hashCode());
        result = prime * result + ((getNotes() == null) ? 0 : getNotes().hashCode());
        result = prime * result + ((getSubmitStatus() == null) ? 0 : getSubmitStatus().hashCode());
        result = prime * result + ((getSubmittedAt() == null) ? 0 : getSubmittedAt().hashCode());
        result = prime * result + ((getConfirmedBy() == null) ? 0 : getConfirmedBy().hashCode());
        result = prime * result + ((getConfirmedAt() == null) ? 0 : getConfirmedAt().hashCode());
        result = prime * result + ((getBillingStatus() == null) ? 0 : getBillingStatus().hashCode());
        result = prime * result + ((getSpecialSituationDescription() == null) ? 0 : getSpecialSituationDescription().hashCode());
        result = prime * result + ((getPilotSignTime() == null) ? 0 : getPilotSignTime().hashCode());
        result = prime * result + ((getAgentConfirmTime() == null) ? 0 : getAgentConfirmTime().hashCode());
        result = prime * result + ((getCreatedBy() == null) ? 0 : getCreatedBy().hashCode());
        result = prime * result + ((getUpdatedBy() == null) ? 0 : getUpdatedBy().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getPlanId() == null) ? 0 : getPlanId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", certificateCode=").append(certificateCode);
        sb.append(", operationId=").append(operationId);
        sb.append(", vesselId=").append(vesselId);
        sb.append(", voyageId=").append(voyageId);
        sb.append(", inboundAgentId=").append(inboundAgentId);
        sb.append(", outboundAgentId=").append(outboundAgentId);
        sb.append(", startBerthId=").append(startLocationId);
        sb.append(", endBerthId=").append(endLocationId);
        sb.append(", startLocationType=").append(startLocationType);
        sb.append(", startLocationName=").append(startLocationName);
        sb.append(", endLocationType=").append(endLocationType);
        sb.append(", endLocationName=").append(endLocationName);
        sb.append(", tradeType=").append(tradeType);
        sb.append(", vesselNameZh=").append(vesselNameZh);
        sb.append(", escortFlag=").append(escortFlag);
        sb.append(", enteredBy=").append(enteredBy);
        sb.append(", handoverSheetId=").append(handoverSheetId);
        sb.append(", routeCode=").append(routeCode);
        sb.append(", routeName=").append(routeName);
        sb.append(", inboundVoyage=").append(inboundVoyage);
        sb.append(", outboundVoyage=").append(outboundVoyage);
        sb.append(", berthingAssistanceFlag=").append(berthingAssistanceFlag);
        sb.append(", berthingAssistanceCharge=").append(berthingAssistanceCharge);
        sb.append(", vesselNameEn=").append(vesselNameEn);
        sb.append(", withdrawalFlag=").append(withdrawalFlag);
        sb.append(", pilotageType=").append(pilotageType);
        sb.append(", forwardDraft=").append(forwardDraft);
        sb.append(", aftDraft=").append(aftDraft);
        sb.append(", horsepower=").append(horsepower);
        sb.append(", detentionFlag=").append(detentionFlag);
        sb.append(", detentionHours=").append(detentionHours);
        sb.append(", detentionPersons=").append(detentionPersons);
        sb.append(", pilotageStartTime=").append(pilotageStartTime);
        sb.append(", pilotageEndTime=").append(pilotageEndTime);
        sb.append(", totalPassengers=").append(totalPassengers);
        sb.append(", totalCrew=").append(totalCrew);
        sb.append(", statusCode=").append(statusCode);
        sb.append(", statusName=").append(statusName);
        sb.append(", boardingTime=").append(boardingTime);
        sb.append(", leavingTime=").append(leavingTime);
        sb.append(", notes=").append(notes);
        sb.append(", submitStatus=").append(submitStatus);
        sb.append(", submittedAt=").append(submittedAt);
        sb.append(", confirmedBy=").append(confirmedBy);
        sb.append(", confirmedAt=").append(confirmedAt);
        sb.append(", billingStatus=").append(billingStatus);
        sb.append(", specialSituationDescription=").append(specialSituationDescription);
        sb.append(", pilotSignTime=").append(pilotSignTime);
        sb.append(", agentConfirmTime=").append(agentConfirmTime);
        sb.append(", createdBy=").append(createdBy);
        sb.append(", updatedBy=").append(updatedBy);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", planId=").append(planId);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}