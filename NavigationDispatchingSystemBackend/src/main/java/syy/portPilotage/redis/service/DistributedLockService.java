package syy.portPilotage.redis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.redis.RedisService;
import syy.portPilotage.util.IdGenerator;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    @Autowired
    private RedisService redisService;

    public String tryLockBlocking(String lockKey, long expireTime, long timeout) {
        String lockValue = IdGenerator.uuid();
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeout) {
            if (redisService.tryLock(lockKey, lockValue, expireTime)) {
                return lockValue;
            }
            // 短暂休眠避免忙等待
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    public String tryLockNonBlocking(String lockKey, long expireTime) {
        String lockValue = IdGenerator.uuid();
        if (redisService.tryLock(lockKey, lockValue, expireTime)) {
            return lockValue;
        }
        return null;
    }

    public boolean releaseLock(String lockKey, String lockValue) {
        if (lockValue == null) {
            return false;
        }
        return redisService.releaseLock(lockKey, lockValue);
    }

    public boolean canSubmitForm(Long userId, String formId, long expireTime) {
        String lockKey = "form_submit_lock:" + userId + ":" + formId;
        String lockValue = tryLockNonBlocking(lockKey, expireTime);
        return lockValue != null;
    }

    public boolean canProcessBilling(String billId, long expireTime) {
        String lockKey = "billing_process_lock:" + billId;
        String lockValue = tryLockNonBlocking(lockKey, expireTime);
        return lockValue != null;
    }
}