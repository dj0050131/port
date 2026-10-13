package syy.portPilotage.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshTask {
    
    @Scheduled(cron = "0 0 */1 * * ?")
    public void refreshCache() {
        log.info("开始执行缓存刷新任务");
        try {
            log.info("缓存刷新任务执行成功");
        } catch (Exception e) {
            log.error("缓存刷新任务执行失败: {}", e.getMessage(), e);
        }
    }
}
