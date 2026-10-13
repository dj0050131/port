package syy.portPilotage.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

@Getter
public class PilotagePlanEvent extends ApplicationEvent {
    
    private static final long serialVersionUID = 1L;
    

    private Long planId;

    private String eventType;
    
    public PilotagePlanEvent(Object source, Long planId, String eventType) {
        super(source);
        this.planId = planId;
        this.eventType = eventType;
    }
}