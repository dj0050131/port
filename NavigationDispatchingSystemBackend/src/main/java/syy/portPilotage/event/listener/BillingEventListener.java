package syy.portPilotage.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import syy.portPilotage.event.BillingCompleteEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingEventListener {
    
    @Async
    @EventListener
    public void handleBillingCompleteEvent(BillingCompleteEvent event) {
        log.info("接收到计费完成事件: voucherId={}, eventType={}", 
                event.getVoucherId(), event.getEventType());
        
        try {
            log.info("计费完成事件处理成功");
        } catch (Exception e) {
            log.error("计费完成事件处理失败: {}", e.getMessage(), e);
        }
    }
}
