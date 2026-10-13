package syy.portPilotage.vo.screen;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShipDynamicVO {
    private Long id;
    private String name;
    private String imo;
    private String type;
    private String status;
    private BigDecimal speed;
    private Integer heading;
    private String destination;
    private String eta;
    private String etd;
    private GeoCoordinateVO position;
    private Boolean pilotRequired;
    private String berthId;
}