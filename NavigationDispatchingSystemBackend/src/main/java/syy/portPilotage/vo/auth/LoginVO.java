package syy.portPilotage.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    
    private String token;
    private Long userId;
    private String account;
    private String username;
    private String userType;
    private String status;
    private String phone;
    private String email;
    private String avatar;

        private List<String> permissions;
}