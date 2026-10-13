package syy.portPilotage.vo.pilotapp;

import lombok.Data;

@Data
public class AppProfileVO {
    /**
     * 资源类型：PILOT / DRIVER
     */
    private String resourceType;

    /**
     * 资源ID（pilots.id / drivers.id）
     */
    private Long resourceId;

        private String resourceCode;

    /**
     * 资源姓名（pilot_name / name）
     */
    private String resourceName;

    /**
     * 在岗状态：ON_DUTY / RESTING / LEAVE / ASSIGNED / OPERATING ...
     */
    private String onDutyStatus;
}
