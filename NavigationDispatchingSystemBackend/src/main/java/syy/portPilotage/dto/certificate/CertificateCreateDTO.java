package syy.portPilotage.dto.certificate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "创建签证单请求参数")
public class CertificateCreateDTO {

    @Schema(description = "计划ID", required = true)
    @NotNull(message = "计划ID不能为空")
    private Long planId;

    @Schema(description = "签证单编号（自动生成时可空）")
    private String certificateCode;

    @Schema(description = "引航类型", required = true)
    @NotNull(message = "引航类型不能为空")
    private String pilotageType;

    @Schema(description = "前吃水", required = true)
    @NotNull(message = "前吃水不能为空")
    private BigDecimal forwardDraft;

    @Schema(description = "后吃水", required = true)
    @NotNull(message = "后吃水不能为空")
    private BigDecimal aftDraft;

    @Schema(description = "开始引航时间", required = true)
    @NotNull(message = "开始引航时间不能为空")
    private Date pilotageStartTime;

    @Schema(description = "结束引航时间", required = true)
    @NotNull(message = "结束引航时间不能为空")
    private Date pilotageEndTime;

    @Schema(description = "登船时间")
    private Date boardingTime;

    @Schema(description = "离船时间")
    private Date leavingTime;

    @Schema(description = "旅客总数", defaultValue = "0")
    private Integer totalPassengers = 0;

    @Schema(description = "船员总数", defaultValue = "0")
    private Integer totalCrew = 0;

    @Schema(description = "备注")
    private String notes;

    @Schema(description = "特殊情况说明")
    private String specialSituationDescription;

    @Schema(description = "马力")
    private BigDecimal horsepower;

    @Schema(description = "助泊标记")
    private String berthingAssistanceFlag;

    @Schema(description = "助泊计费")
    private BigDecimal berthingAssistanceCharge;

    @Schema(description = "滞留标记")
    private String detentionFlag;

    @Schema(description = "滞留小时数")
    private BigDecimal detentionHours;

    @Schema(description = "滞留人数")
    private Integer detentionPersons;

    @Schema(description = "录入人")
    private String enteredBy;

}