package syy.portPilotage.dto.certificate;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class CertificateQueryDTO {
    private Integer page = 1;
    private Integer size = 20;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
    private String vesselNameZh;
    private String vesselNameEn;
    private String vesselCallSign;
    private String imoNumber;
    private String submitStatus;
    private String billingStatus;
    private Long pilotId;
    private Long agentId;
    private String certificateCode;
    private String voyageNumber;
}