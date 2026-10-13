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
public class BillingMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long certificateId;//签证单id
    private Long voucherId;//船舶id
    private String action;
    private String message;
}
