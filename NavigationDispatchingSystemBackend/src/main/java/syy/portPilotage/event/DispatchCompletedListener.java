package syy.portPilotage.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import syy.portPilotage.dto.certificate.CertificateResultDTO;
import syy.portPilotage.service.PilotageCertificatesService;

/**
 * 调度完成后的后置处理（AFTER_COMMIT）：自动生成签证单等。
 * 该逻辑不应影响“完成调度”接口本身的事务提交。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchCompletedListener {

    private final PilotageCertificatesService pilotageCertificatesService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void onDispatchCompleted(DispatchCompletedEvent event) {
        if (event == null || event.planId() == null) return;
        Long planId = event.planId();
        try {
            CertificateResultDTO result = pilotageCertificatesService.createCertificateFromPlanAuto(planId);
            if (result != null && result.getSuccess()) {
                log.info("AFTER_COMMIT 自动生成签证单成功: planId={}, certificateId={}, certificateCode={}",
                        planId, result.getId(), result.getCertificateCode());
            } else {
                log.warn("AFTER_COMMIT 自动生成签证单失败: planId={}, message={}",
                        planId, result == null ? "未知原因" : result.getMessage());
            }
        } catch (Exception e) {
            // 不向外抛出，避免影响事件分发链路
            log.error("AFTER_COMMIT 自动生成签证单异常: planId={}, error={}", planId, e.getMessage(), e);
        }
    }
}