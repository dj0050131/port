package syy.portPilotage.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterDTO {
    
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "账号不能为空")
    private String account;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    @NotBlank(message = "用户类型不能为空")
    private String userType;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String companyName;
}