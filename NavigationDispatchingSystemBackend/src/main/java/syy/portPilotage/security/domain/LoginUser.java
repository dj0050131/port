package syy.portPilotage.security.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import syy.portPilotage.entity.Users;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {
    
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String userCode;
    private String username;
    private String account;
    private String password;
    private String userType;
    private String status;
    private String phone;
    private String email;
    private String avatar;
    private boolean enabled; // 是否启用
    private boolean locked;// 是否锁定

    private Collection<? extends GrantedAuthority> authorities;
    
    public LoginUser(Users user) {
        this.userId = user.getId();
        this.userCode = user.getUserCode();
        this.username = user.getUsername();
        this.account = user.getAccount();
        this.password = user.getPassword();
        this.userType = user.getUserType() != null ? user.getUserType().toString() : null;
        this.status = user.getCurrentStatus() != null ? user.getCurrentStatus().toString() : null;
        this.phone = user.getPhone();
        this.email = user.getEmail();
        this.avatar = user.getAvatar();
        this.enabled = !"DISABLED".equals(this.status);
        this.locked = "LOCKED".equals(this.status);
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities != null) return authorities;
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userType));
    }

//    private Collection<GrantedAuthority> authorities; // TODO:  获取用户权限方案
//    }
    
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return account != null ? account : username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}