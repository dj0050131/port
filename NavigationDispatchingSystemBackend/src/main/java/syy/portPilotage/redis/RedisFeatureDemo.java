package syy.portPilotage.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.redis.service.DistributedLockService;
import syy.portPilotage.redis.service.RateLimitService;
import syy.portPilotage.redis.service.UserAuthService;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisFeatureDemo {

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private DistributedLockService distributedLockService;

        public void demoUserAuthFeatures() {
        String username = "testUser";
        String token = "your-jwt-token";

        userAuthService.storeUserToken(username, token);

        String storedToken = userAuthService.getUserToken(username);
        log.info("获取到的Token: {}", storedToken);

        for (int i = 1; i <= 3; i++) {
            Long failCount = userAuthService.incrementLoginFailCount(username);
            log.info("登录失败次数: {}", failCount);
        }

        Long failCount = userAuthService.getLoginFailCount(username);
        log.info("当前登录失败次数: {}", failCount);

        if (failCount >= 3) {
            userAuthService.lockAccount(username);
            log.info("账户已被锁定");
        }

        boolean isLocked = userAuthService.isAccountLocked(username);
        log.info("账户锁定状态: {}", isLocked);

        userAuthService.unlockAccount(username);
        log.info("账户已解锁");

        userAuthService.clearLoginFailCount(username);
        log.info("登录失败次数已清除");
    }

    
    public void demoRateLimitFeatures() {
        String clientId = "client-id";
        String apiKey = "submit_form";
        int maxRequests = 5;
        int windowSeconds = 60; // 1分钟

        for (int i = 1; i <= 7; i++) {
            boolean isLimited = rateLimitService.isApiRateLimited(apiKey, clientId, maxRequests, windowSeconds);
            if (isLimited) {
                log.warn("第{}次请求被限流", i);
            } else {
                log.info("第{}次请求通过", i);
            }

            try {
                Thread.sleep(100); // 模拟请求间隔
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 演示分布式锁功能
     */
    public void demoDistributedLockFeatures() {
        String resourceId = "resource_123";
        long expireTime = 30; // 30秒

        String lockValue = distributedLockService.tryLockNonBlocking(resourceId, expireTime);
        if (lockValue != null) {
            log.info("成功获取分布式锁: {}", lockValue);

            try {
                log.info("正在处理需要互斥访问的业务...");
                Thread.sleep(5000);
                log.info("业务处理完成");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // 释放锁
                boolean released = distributedLockService.releaseLock(resourceId, lockValue);
                log.info("分布式锁释放结果: {}", released);
            }
        } else {
            log.warn("获取分布式锁失败");
        }

        Long userId = 1001L;
        String formId = "form_abc";
        boolean canSubmit = distributedLockService.canSubmitForm(userId, formId, 60);
        log.info("表单提交许可: {}", canSubmit);

        String billId = "bill_xyz";
        boolean canProcess = distributedLockService.canProcessBilling(billId, 60);
        log.info("计费处理许可: {}", canProcess);
    }
}