package syy.portPilotage.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PilotageReminderTask {
    
    @Scheduled(cron = "0 0 8 * * ?")
    public void remindPendingRequests() {
        log.info("开始执行引航申请提醒任务");
        try {
            log.info("引航申请提醒任务执行成功");
        } catch (Exception e) {
            log.error("引航申请提醒任务执行失败: {}", e.getMessage(), e);
        }
    }
}
