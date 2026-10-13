package syy.portPilotage.rabbitmq.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanNotifyMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long planId;
    private String planCode;
    private Long userId;
    private String userName;
    private String action; // CREATE, UPDATE, DELETE, CONFIRM
    private String message;
}