package syy.portPilotage.vo.screen;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoCoordinateVO {
    private BigDecimal lng;
    private BigDecimal lat;

}