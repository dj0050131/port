package syy.portPilotage.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateSubmitTask {
    
    @Scheduled(cron = "0 0 9 * * ?")
    public void remindCertificateSubmit() {
        log.info("开始执行签证单提交提醒任务");
        try {
            log.info("签证单提交提醒任务执行成功");
        } catch (Exception e) {
            log.error("签证单提交提醒任务执行失败: {}", e.getMessage(), e);
        }
    }
}
