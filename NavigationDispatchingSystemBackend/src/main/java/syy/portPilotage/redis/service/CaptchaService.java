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
public class CaptchaService {

    @Autowired
    private RedisService redisService;

    /**
     * 存储验证码
     * @param captchaKey 验证码键
     * @param captchaCode 验证码值
     */
    public void storeCaptcha(String captchaKey, String captchaCode) {
        String key = RedisConstant.CAPTCHA + captchaKey;
        redisService.set(key, captchaCode, RedisConstant.CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("存储验证码: key={}", captchaKey);
    }

    /**
     * 获取验证码
     * @param captchaKey 验证码键
     * @return 验证码值
     */
    public String getCaptcha(String captchaKey) {
        String key = RedisConstant.CAPTCHA + captchaKey;
        return redisService.get(key, String.class);
    }

    /**
     * 删除验证码
     * @param captchaKey 验证码键
     */
    public void removeCaptcha(String captchaKey) {
        String key = RedisConstant.CAPTCHA + captchaKey;
        redisService.delete(key);
        log.info("删除验证码: key={}", captchaKey);
    }

    /**
     * 验证验证码是否正确
     * @param captchaKey 验证码键
     * @param inputCaptcha 用户输入的验证码
     * @return 是否正确
     */
    public boolean validateCaptcha(String captchaKey, String inputCaptcha) {
        String storedCaptcha = getCaptcha(captchaKey);
        if (storedCaptcha == null) {
            return false;
        }

        boolean isValid = storedCaptcha.equalsIgnoreCase(inputCaptcha);
        if (isValid) {
            // 验证成功后删除验证码，防止重复使用
            removeCaptcha(captchaKey);
        }
        return isValid;
    }
}