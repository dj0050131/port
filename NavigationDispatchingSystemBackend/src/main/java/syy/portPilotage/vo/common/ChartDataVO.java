package syy.portPilotage.vo.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataVO {
    
    private List<String> labels;
    private List<SeriesData> series;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeriesData {
        private String name;
        private List<Object> data;
    }
}
