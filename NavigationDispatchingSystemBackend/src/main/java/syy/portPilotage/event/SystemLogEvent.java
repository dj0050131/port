package syy.portPilotage.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
public class SystemLogEvent extends ApplicationEvent {
    
    private static final long serialVersionUID = 1L;

    private String action;
    private String details;

    public SystemLogEvent(Object source, String action, String details) {
        super(source);
        this.action = action;
        this.details = details;
    }
}
