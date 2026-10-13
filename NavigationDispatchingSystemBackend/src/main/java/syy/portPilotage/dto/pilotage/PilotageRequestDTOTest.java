package syy.portPilotage.dto.pilotage;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import syy.portPilotage.enums.RequestStatusEnum;
import syy.portPilotage.enums.PriorityEnum;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PilotageRequestDTOTest {
    
    @NotNull(message = "船舶ID不能为空")
    private Long vesselId; // 船舶ID

    // Basic Info
    private String applicationNo; // Request Code
    private RequestStatusEnum status;
    private Date createdAt;
    
    private String shipName; // 中文船名
    private String englishName; // 英文船名
    private String callSign; // 呼号
    private String imoNumber; // IMO
    private String mmsi; // MMSI
    private String nationality; // 国籍
    private String shipType; // 船舶种类
    private String company; // 船公司
    private BigDecimal length; // 船长
    private BigDecimal width; // 船宽
    private BigDecimal depth; // 型深
    private BigDecimal grossTonnage; // 总吨
    private BigDecimal netTonnage; // 净吨
    private BigDecimal deadWeight; // 载重吨
    private String engineType; // 主机种类
    private BigDecimal power; // 功率
    private BigDecimal speed; // 航速
    private BigDecimal thrusterPower; // 侧推功率
    private BigDecimal waterlineHeight; // 水面高度
    private String aisInterface; // AIS引航接口
    private Date buildDate; // 建造日期
    private String routeType; // 航线种类 (International/Domestic)

    @NotNull(message = "预到日期不能为空")
    private Date eta; // 预计到达时间
    private Date etd; // 预计离港时间
    private String wharf; // 码头
    private String berth; // 泊位
    private String anchorage; // 预抵锚地
    private String voyageStatus; // 动态 (Arrival/Departure)
    private BigDecimal foreDraft; // 艏吃水
    private BigDecimal aftDraft; // 艉吃水
    private String cargo; // 装货名称
    private BigDecimal cargoQty; // 数量
    private String etdEta; // ETD/ETA
    private String lastPort; // 上一港
    private String nextPort; // 下一港
    private Integer totalCrewRequest;
    
    private String ladderStatus; // 引航梯状态
    private String ladderType; // 引航梯类型
    private BigDecimal freeboardHeight; // 干舷高度

    private Long agentId;
    private String agentName; // 代理名称
    private String invoiceType; // 发票类型
    private String payerName; // 付款单位名称
    private String payerAddress; // 付款单位地址
    private String payerPhone; // 付款单位电话
    private String taxId; // 纳税人识别号
    private String bank; // 开户行
    private String account; // 账号
    
    private String remarks; // 备注
    
    private String departurePort; // 出发港口
    private Long expectedBerthId; // 预计泊位ID
    private String bargeNumber; // 驳船编号
    private BigDecimal inboundDraftForward; // 进港前吃水
    private BigDecimal inboundDraftAft; // 进港后吃水
    private String destinationPort; // 目的港

    private Integer inboundPassengerTotal; // 进港旅客总数

    private String inboundCargoName; // 进港货物名称
    private BigDecimal inboundCargoTotalWeight; // 进港货物总重量
    private String inboundGeneralCargoDetails; // 进港普通货物详情
    private String inboundDangerousCargoDetails; // 进港危险货物详情

    private String outboundCargoName; // 出港货物名称
    private BigDecimal outboundCargoTotalWeight; // 出港货物总重量
    private String outboundGeneralCargoDetails; // 出港普通货物详情
    private String outboundDangerousCargoDetails; // 出港危险货物详情

    private String requestNotes; // 申请备注
    private String applicant; // 申请人
    private Date applicationDate; // 申请日期
    private String requestSource; // 请求来源
    private PriorityEnum priority; // 优先级
    private String specialRequirements; // 特殊要求
}