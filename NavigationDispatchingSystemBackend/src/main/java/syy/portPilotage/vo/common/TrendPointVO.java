package syy.portPilotage.vo.common;

import lombok.Data;

@Data
public class TrendPointVO {
    private String time;
    private Number value;

    public TrendPointVO() {}

    public TrendPointVO(String time, Number value) {
        this.time = time;
        this.value = value;
    }
}