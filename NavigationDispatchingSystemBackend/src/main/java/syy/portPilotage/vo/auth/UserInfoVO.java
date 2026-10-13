package syy.portPilotage.vo.auth;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserInfoVO {
    
    private Long id;
    private String account;
    private String userCode;
    private String userType;
    private String username;
    private String phone;
    private String email;
    private String currentStatus;
    private String avatar;
    private String department;
    private String position;
    private Date lastLoginAt;
    private Date createdAt;

        private List<String> permissions;
}