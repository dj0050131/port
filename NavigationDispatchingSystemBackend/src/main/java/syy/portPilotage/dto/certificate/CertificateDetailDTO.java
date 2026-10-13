package syy.portPilotage.dto.certificate;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CertificateDetailDTO {
    private Long id;
    private String certificateCode;
    private String operationId;
    private Long planId;
    private Long vesselId;
    private Long voyageId;

    // 船舶信息
    private String vesselNameZh;
    private String vesselNameEn;
    private String mmsiNumber;
    private String imoNumber;
    private String vesselCallSign;
    private String vesselType;
    private String nationality;
    private BigDecimal vesselLength;
    private BigDecimal vesselWidth;
    private BigDecimal grossTonnage;
    private BigDecimal netTonnage;
    private BigDecimal deadweight;
    private BigDecimal fullLoadDraft;
    private BigDecimal airDraft;
    private BigDecimal speed;

    // 航次信息
    private String voyageNumber;
    private String voyageType;
    private String cargoName;
    private BigDecimal cargoQuantity;
    private String cargoUnit;
    private BigDecimal draftForward;
    private BigDecimal draftAft;

    // 代理信息
    private Long inboundAgentId;
    private Long outboundAgentId;
    private String inboundAgentName;
    private String outboundAgentName;
    private String inboundAgentCode;
    private String outboundAgentCode;

    // 位置信息
    private String startLocationType;
    private Long startLocationId;
    private String startLocationName;
    private String startLocationFullName; // 动态获取的完整名称

    private String endLocationType;
    private Long endLocationId;
    private String endLocationName;
    private String endLocationFullName; // 动态获取的完整名称

    private String routeCode;
    private String routeName;

    // 引航信息
    private String pilotageType;
    private BigDecimal forwardDraft;
    private BigDecimal aftDraft;
    private BigDecimal horsepower;
    private String tradeType;

    // 时间信息
    private Date pilotageStartTime;
    private Date pilotageEndTime;
    private Date boardingTime;
    private Date leavingTime;

    // 人员信息
    private Integer totalPassengers;
    private Integer totalCrew;
    private String detentionFlag;
    private BigDecimal detentionHours;
    private Integer detentionPersons;

    // 状态信息
    private String submitStatus;
    private String billingStatus;
    private Date submittedAt;
    private Date confirmedAt;
    private Date pilotSignTime;
    private Date agentConfirmTime;

    // 引航员信息
    private Long assignedPilotId;
    private String assignedPilotName;

    // 其他
    private String notes;
    private String specialSituationDescription;
    private String berthingAssistanceFlag;
    private BigDecimal berthingAssistanceCharge;

    private Date createdAt;
    private Date updatedAt;

    private Long latestBillingVoucherId;
}