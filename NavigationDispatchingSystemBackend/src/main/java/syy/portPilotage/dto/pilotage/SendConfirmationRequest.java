package syy.portPilotage.dto.pilotage;

import lombok.Data;

import java.io.Serializable;

@Data
public class SendConfirmationRequest implements Serializable {
    /**
     * 计划ID
     */
    private Long planId;

    /**
     * 资源ID（引航员/驾驶员/泊位等的ID）
     */
    private Long resourceId;

    /**
     * 资源类型（PILOT/DRIVER/BERTH等）
     */
    private String resourceType;

    private static final long serialVersionUID = 1L;
}