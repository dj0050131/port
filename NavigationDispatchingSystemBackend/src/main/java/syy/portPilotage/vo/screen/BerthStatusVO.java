package syy.portPilotage.vo.screen;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BerthStatusVO {
    private String id;
    private String name;
    private GeoCoordinateVO position;
    private String status;
    private String shipId;
    private String berthCode;
    private String portArea;
    private String berthType;
    private String wharfDirection;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal startLatitude;
    private BigDecimal startLongitude;
    private BigDecimal endLatitude;
    private BigDecimal endLongitude;
    private String berthDirection;
    private BigDecimal maxDraft;
    private BigDecimal berthLength;
    private BigDecimal berthDepth;
    private String contactPhone;
    private Long portAreaId;
    private Long wharfId;
    private Long vesselId;
}