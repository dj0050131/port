package syy.portPilotage.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Date;
import java.util.List;

@Getter
@Setter//解决循环依赖
public class PlanCreatedEvent extends ApplicationEvent {
    private Long planId;
    private Long pilotId;
    private Long driverId;
    private List<Long> tugboatIds;
    private List<Long> pilotBoatIds;
    private Date createdAt;

    public PlanCreatedEvent(Object source, Long planId, Long pilotId, Long driverId,
                            List<Long> tugboatIds, List<Long> pilotBoatIds, Date createdAt) {
        super(source);
        this.planId = planId;
        this.pilotId = pilotId;
        this.driverId = driverId;
        this.tugboatIds = tugboatIds;
        this.pilotBoatIds = pilotBoatIds;
        this.createdAt = createdAt;
    }
}