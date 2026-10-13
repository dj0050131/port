package syy.portPilotage.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    
    @NotBlank(message = "账号不能为空")
    private String account;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String captcha;
    private String captchaKey;

    public @NotBlank(message = "账号不能为空") String getAccount() {
        return account;
    }

    public void setAccount(@NotBlank(message = "账号不能为空") String account) {
        this.account = account;
    }

    public @NotBlank(message = "密码不能为空") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "密码不能为空") String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getCaptchaKey() {
        return captchaKey;
    }

    public void setCaptchaKey(String captchaKey) {
        this.captchaKey = captchaKey;
    }
}