package syy.portPilotage.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmsUtil {
    
    public static boolean sendSms(String phone, String content) {
        log.info("发送短信: phone={}, content={}", phone, content);
        try {
            return true;
        } catch (Exception e) {
            log.error("发送短信失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public static boolean sendTemplateSms(String phone, String templateCode, String params) {
        log.info("发送模板短信: phone={}, template={}", phone, templateCode);
        try {
            return true;
        } catch (Exception e) {
            log.error("发送模板短信失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
