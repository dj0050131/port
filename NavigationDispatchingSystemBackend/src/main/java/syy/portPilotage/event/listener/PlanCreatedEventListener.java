package syy.portPilotage.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.plan.PlanConfirmationDTO;
import syy.portPilotage.event.PlanCreatedEvent;
import syy.portPilotage.service.PlanConfirmationsService;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanCreatedEventListener {

    private final PlanConfirmationsService planConfirmationsService;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePlanCreatedEvent(PlanCreatedEvent event) {
        // 真实业务：计划确认应由资源在“计划已发布(PUBLISHED)”后手动确认；
        // 这里不再在计划创建时自动确认，避免未发布计划也可确认/自动进入已确认状态的问题。
        log.info("收到计划创建事件(不自动确认): planId={}", event.getPlanId());
    }
}