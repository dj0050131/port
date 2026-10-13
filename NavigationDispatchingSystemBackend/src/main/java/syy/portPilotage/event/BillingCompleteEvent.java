package syy.portPilotage.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class BillingCompleteEvent extends ApplicationEvent {
    
    private static final long serialVersionUID = 1L;
    

    private Long voucherId;

    private String eventType;
    
    public BillingCompleteEvent(Object source, Long voucherId, String eventType) {
        super(source);
        this.voucherId = voucherId;
        this.eventType = eventType;
    }
}
