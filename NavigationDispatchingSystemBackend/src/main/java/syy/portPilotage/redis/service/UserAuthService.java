package syy.portPilotage.redis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.constant.RedisConstant;
import syy.portPilotage.redis.RedisService;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

    @Autowired
    private RedisService redisService;

    /**
     * 存储用户登录Token
     * @param account 账号
     * @param token Token值
     */
    public void storeUserToken(String account, String token) {
        String key = RedisConstant.USER_TOKEN + account;
        redisService.set(key, token, RedisConstant.TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("存储用户Token: account={}, token={}", account, token);
    }

    /**
     * 获取用户Token
     * @param account 账号
     * @return Token值
     */
    public String getUserToken(String account) {
        String key = RedisConstant.USER_TOKEN + account;
        return redisService.get(key, String.class);
    }

    /**
     * 删除用户Token
     * @param account 账号
     */
    public void removeUserToken(String account) {
        String key = RedisConstant.USER_TOKEN + account;
        redisService.delete(key);
        log.info("删除用户Token: account={}", account);
    }

    /**
     * 增加用户登录失败次数
     * @param account 账号
     * @return 当前失败次数
     */
    public Long incrementLoginFailCount(String account) {
        String key = RedisConstant.LOGIN_FAIL_COUNT + account;
        Long count = redisService.increment(key);
        if (count == 1) {
            // 第一次失败时设置过期时间
            redisService.expire(key, RedisConstant.CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
        return count;
    }

    /**
     * 获取用户登录失败次数
     * @param account 账号
     * @return 失败次数
     */
    public Long getLoginFailCount(String account) {
        String key = RedisConstant.LOGIN_FAIL_COUNT + account;
        return redisService.get(key, Long.class);
    }

    /**
     * 清除用户登录失败次数
     * @param account 账号
     */
    public void clearLoginFailCount(String account) {
        String key = RedisConstant.LOGIN_FAIL_COUNT + account;
        redisService.delete(key);
    }

    /**
     * 锁定账户
     * @param account 账号
     */
    public void lockAccount(String account) {
        String key = RedisConstant.ACCOUNT_LOCK + account;
        redisService.set(key, true, RedisConstant.CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("锁定账户: account={}", account);
    }

    /**
     * 检查账户是否被锁定
     * @param account 账号
     * @return 是否被锁定
     */
    public boolean isAccountLocked(String account) {
        String key = RedisConstant.ACCOUNT_LOCK + account;
        return redisService.hasKey(key);
    }

    /**
     * 解锁账户
     * @param account 账号
     */
    public void unlockAccount(String account) {
        String key = RedisConstant.ACCOUNT_LOCK + account;
        redisService.delete(key);
        log.info("解锁账户: account={}", account);
    }
}