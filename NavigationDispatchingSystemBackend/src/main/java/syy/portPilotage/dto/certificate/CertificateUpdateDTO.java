package syy.portPilotage.dto.certificate;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CertificateUpdateDTO {
        private Long routeId;

    /**
     * 航线代码/名称：当 routeId 未提供时可手动补录
     */
    private String routeCode;
    private String routeName;

    private BigDecimal forwardDraft;
    private BigDecimal aftDraft;
    private BigDecimal horsepower;
    private Date pilotageStartTime;
    private Date pilotageEndTime;
    private Date boardingTime;
    private Date leavingTime;
    private Integer totalPassengers;
    private Integer totalCrew;
    private String detentionFlag;
    private BigDecimal detentionHours;
    private Integer detentionPersons;
    private String notes;
    private String specialSituationDescription;
    private String berthingAssistanceFlag;
    private BigDecimal berthingAssistanceCharge;
}