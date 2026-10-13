package syy.portPilotage.vo.screen;

import lombok.Data;

@Data
public class AlertItemVO {
    private String id;
    private String level;
    private String message;
    private String detail;
    private String timestamp;
    private Boolean acknowledged;//是否处理
    //告警类型
    private String alertType;
    //告警类型中文
    private String alertTypeName;
}