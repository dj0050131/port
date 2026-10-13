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
public class EmailMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String to;
    private String subject;
    private String content;
    private Boolean html;
}