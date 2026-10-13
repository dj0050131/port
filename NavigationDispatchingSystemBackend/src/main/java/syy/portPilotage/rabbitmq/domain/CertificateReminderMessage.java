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
public class CertificateReminderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long certificateId;
    private String certificateCode;//签证单编号
    private Long agentId;//船代id
    private String agentName;
    private String reminderType; // SUBMIT_REMINDER, APPROVE_REMINDER, EXPIRE_REMINDER
    private String message;
}