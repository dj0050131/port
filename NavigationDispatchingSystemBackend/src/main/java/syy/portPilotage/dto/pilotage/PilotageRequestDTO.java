package syy.portPilotage.dto.pilotage;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import syy.portPilotage.enums.LocationTypeEnum;
import syy.portPilotage.enums.PriorityEnum;
import syy.portPilotage.enums.RequestStatusEnum;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PilotageRequestDTO {

    // 来自 PilotageRequests
    private Long id;
    private String requestCode;
    private Long vesselId;
    private Long voyageId;
    private Long expectedBerthId;
    private String bargeNumber;
    private BigDecimal inboundDraftForward;
    private BigDecimal inboundDraftAft;
    private Integer totalPassengers;
    private Integer totalCrew;
    private String inboundCargoName;
    private BigDecimal inboundCargoTotalWeight;
    private String inboundGeneralCargoDetails;
    private String inboundDangerousCargoDetails;
    private String outboundCargoName;
    private BigDecimal outboundCargoTotalWeight;
    private String outboundGeneralCargoDetails;
    private String outboundDangerousCargoDetails;
    private String requestNotes;
    private String applicant;
    private Date applicationDate;
    private String requestSource;
    private String visaProcessingResult;
    private RequestStatusEnum requestStatus;
    private Long reviewerId;
    private Date reviewedAt;
    private String rejectReason;
    private PriorityEnum priority;
    private String specialRequirements;
    private Date createdAt;
    private Date updatedAt;

    // 来自 Vessels
    private String vesselCallSign;//
    private String imoNumber;//
    private String mmsiNumber;//
    private String vesselType;//
    private String nationality;//
    private String ownerZh;//
    private String ownerEn;//
    private String routeScope;//w
    private Object routeType;//航线种类
    private String nameZh;//w
    private String nameEn;//
    private BigDecimal vesselLength;//
    private BigDecimal vesselWidth;//
    private BigDecimal fullLoadDraft;//w
    private BigDecimal depth;//
    private BigDecimal maxHeight;//w
    private BigDecimal airDraft;//
    private BigDecimal speed;//
    private BigDecimal grossTonnage;//总吨
    private BigDecimal netTonnage;//净吨
    private BigDecimal deadweight;//载重吨
    private String mainEngineType;//主机种类
    private BigDecimal mainEnginePower;//主机功率
    private BigDecimal bowThrusterPower;// 侧推功率
    private String aisPilotInterface;//AIS引航接口
    private String vesselColor;//船舶颜色 //w
    private String funnelColor;//烟囱颜色 //w
    private String funnelMark;//w
    private Date buildDate;//建造日期
    private String blacklistRecord;//w //黑名单记录
    private String badStatusRecord;//w //不良异常记录
    private String otherAbnormalRecord;//w //其它异常记录
    private String portOfRegistry;//w //船籍港
    private String classificationSociety;//w // 船级社
    private String managementCompany; //管理公司
    private String insuranceCompany;//w //保险公司
    private String pniClub;//w //保赔协会

    //'付款信息表' PAYMENT_INFO
    private String payee_unit;
    private String payee_bank_account;
    private String payee_bank_name;

    private Long portId;
    private String portCode;
    private String portNameZh;
    private String portNameEn;
    private String portCountry;
    private String portRegion;
    private BigDecimal portLongitude;
    private BigDecimal portLatitude;
    private String portTimezone;
    private Object portStatus;

    // 来自 Voyages
    private String voyageNumber;
    private Object voyageType;
//    private String previousPortId; 
//    private String nextPortId; 
    private String previousPort;
    private String nextPort;
    private Long portOfDepartureId;//
    private Long portOfDestinationId;
    private Date eta;
    private Date etaAnchorage;
    private Date ata;
    private Date etd;
    private Date atd;
    private String cargoName;
    private String cargoType;
    private BigDecimal cargoQuantity;
    private String cargoUnit;
    private BigDecimal draftForward;
    private BigDecimal draftAft;
    private Object voyageStatus;
    private Long plannedStartId;
    private Long actualStartId;
    private Long plannedEndId;
    private Long actualEndId;
    private String voyageRemarks;
    
    // 位置类型
    private LocationTypeEnum startLocationType;
    private LocationTypeEnum endLocationType;

    //来自pilotage_plans
    private Long planId;
    private String planStatus;//计划状态
        private PriorityEnum planPriority;

    // 来自 PilotLadderInfo
    private String ladderStatus;
    private String ladderType;
    private BigDecimal freeboardHeight;
    private Date inspectionDate;
    private Object inspectionResult;
    private String inspector;
    private String ladderRemarks;

    // 来自 Agents
    // AgentInvoiceInfo 相关字段
    private Long agentId;
    private String agentName;
    private String payingUnitName;
    private String invoiceType;
    private String taxpayerId;
    private String payingUnitAddress;
    private String payingUnitPhone;
    private String payingUnitBank;
    private String payingUnitBankAccount;
    private Long billingVoucherId;
    private String agentCode;
    private String companyName;
    private String shortName;
    private String principal;
    private String contactPerson;
    private String agentPhone;
    private String agentAddress;
    private String agentEmail;
    private Object creditRating;
    private Object paymentStatus;
    private String businessLicense;
    private BigDecimal registeredCapital;
    private String defaultTaxpayerId;
    private String defaultBankName;
    private String defaultBankAccount;
    private String defaultAddress;

    private String reviewerUserCode;
    private String reviewerAccount;
    private Object reviewerUserType;
    private String reviewerUsername;
    private Date reviewerApplyTime;
    private String reviewerPhone;
    private String reviewerEmail;
    private Object reviewerCurrentStatus;
    private String reviewerAgentLevelType;
    private Long reviewerParentAgentId;
    private String reviewerAvatar;
    private Date reviewerLastLoginAt;
    private String reviewerDepartment;
    private String reviewerPosition;

    // 港区信息
    private Long portAreaId;
    private String portAreaCode;
    private String portAreaNameZh;
    private String portAreaNameEn;

    // 码头信息
    private Long wharfId;
    private String wharfCode;
    private String wharfNameZh;
    private String wharfNameEn;

    // 锚地信息
    private Long anchorageId;
    private String anchorageCode;
    private String anchorageNameEn;

    private String plannedStartName;  // 计划开始位置名称（可能是泊位名或锚地名）
    private String plannedEndName;    // 计划结束位置名称（可能是泊位名或锚地名）

    private String vesselName; // 船舶名称
    private String expectedBerthName; // 预计泊位名称
    private String reviewerName; // 审核人姓名
}