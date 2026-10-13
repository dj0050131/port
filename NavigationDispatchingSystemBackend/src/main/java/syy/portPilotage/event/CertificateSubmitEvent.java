package syy.portPilotage.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class CertificateSubmitEvent extends ApplicationEvent {
    
    private static final long serialVersionUID = 1L;
    

    private Long certificateId;

    private String eventType;
    
    public CertificateSubmitEvent(Object source, Long certificateId, String eventType) {
        super(source);
        this.certificateId = certificateId;
        this.eventType = eventType;
    }
}
