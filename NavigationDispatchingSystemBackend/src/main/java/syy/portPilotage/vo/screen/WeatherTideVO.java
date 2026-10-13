package syy.portPilotage.vo.screen;

import lombok.Data;
import syy.portPilotage.enums.WeatherConditionEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

@Data
public class WeatherTideVO {
    private WeatherInfo weather;
    private TideInfo tide;
    private List<ForecastInfo> forecast;
    
    @Data
    public static class WeatherInfo {
        private Long id;
        private Date weatherDate;
        private BigDecimal temperature;
        private BigDecimal humidity;
        private BigDecimal windSpeed;
        private String windDirection;
        private BigDecimal visibility;
        private BigDecimal pressure;
        private String dataSource;
        private String Condition;
        private Date createdAt;
        private Date updatedAt;
    }
    
    @Data
    public static class TideInfo {
        private Long id;
        private Date tideDate;
        private String region;
        private Date highTideTime1;
        private BigDecimal highTideHeight1;
        private Date highTideTime2;
        private BigDecimal highTideHeight2;
        private Date lowTideTime1;
        private BigDecimal lowTideHeight1;
        private Date lowTideTime2;
        private BigDecimal lowTideHeight2;
        private Object tideType;
        private String dataSource;
        private Date createdAt;
        private Date updatedAt;
        private BigDecimal currentHeight;
        private String nextHighTide;
        private String nextLowTide;
        private Boolean surgeWarning;
    }
    
    @Data
    public static class ForecastInfo {
        private String time;
        private BigDecimal temperature;
        private BigDecimal tide;
        private String condition;
        private String wind;
        private Integer windLevel;
    }
}