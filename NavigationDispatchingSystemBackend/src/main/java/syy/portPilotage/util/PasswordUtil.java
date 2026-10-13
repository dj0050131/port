package syy.portPilotage.util;

import cn.hutool.crypto.SecureUtil;

import java.util.regex.Pattern;

public class PasswordUtil {
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$");
    
    public static boolean isStrongPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    public static String md5(String password) {
        return SecureUtil.md5(password);
    }
    
    public static String generateRandomPassword(int length) {
        return IdGenerator.uuid().substring(0, Math.min(length, 32));
    }
}