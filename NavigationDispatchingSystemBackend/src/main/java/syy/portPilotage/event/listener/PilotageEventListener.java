package syy.portPilotage.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import syy.portPilotage.event.PilotagePlanEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class PilotageEventListener {
    
    @Async
    @EventListener
    public void handlePilotagePlanEvent(PilotagePlanEvent event) {
        log.info("接收到引航计划事件: planId={}, eventType={}", 
                event.getPlanId(), event.getEventType());
        
        try {
            switch (event.getEventType()) {
                case "CREATED" -> handlePlanCreated(event);
                case "PUBLISHED" -> handlePlanPublished(event);
                case "CONFIRMED" -> handlePlanConfirmed(event);
                case "COMPLETED" -> handlePlanCompleted(event);
                default -> log.warn("未知的事件类型: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("处理引航计划事件失败: {}", e.getMessage(), e);
        }
    }
    
    private void handlePlanCreated(PilotagePlanEvent event) {
        log.info("处理计划创建事件: planId={}", event.getPlanId());
    }
    
    private void handlePlanPublished(PilotagePlanEvent event) {
        log.info("处理计划发布事件: planId={}", event.getPlanId());
    }
    
    private void handlePlanConfirmed(PilotagePlanEvent event) {
        log.info("处理计划确认事件: planId={}", event.getPlanId());
    }
    
    private void handlePlanCompleted(PilotagePlanEvent event) {
        log.info("处理计划完成事件: planId={}", event.getPlanId());
    }
}