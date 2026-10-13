package syy.portPilotage.security.component;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import syy.portPilotage.entity.AuthPermissions;
import syy.portPilotage.entity.Users;
import syy.portPilotage.enums.UserStatus;
import syy.portPilotage.security.domain.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import syy.portPilotage.mapper.AuthPermissionsMapper;
import syy.portPilotage.service.UsersService;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsersService usersService;
    @Autowired
    private AuthzConfigService authzConfigService;
    @Autowired
    private AuthPermissionsMapper authPermissionsMapper;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        Users user = usersService.getByAccount(account);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + account);
        }

        if (user.getCurrentStatus() == UserStatus.DISABLED) {
            throw new UsernameNotFoundException("用户已被禁用: " + account);
        }

        if (user.getCurrentStatus() == UserStatus.PENDING) {
            throw new UsernameNotFoundException("用户待审核: " + account);
        }

        LoginUser loginUser = new LoginUser(user);

        String userType = loginUser.getUserType();
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (userType != null && !userType.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userType));
        }

        // 角色别名：让“新增身份”可以通过参数映射到既有 hasRole/hasAnyRole 注解
        for (String alias : authzConfigService.getRoleAliases(userType)) {
            if (alias != null && !alias.isBlank()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + alias.trim()));
            }
        }

        // 权限码：为后续细粒度鉴权（hasAuthority）做准备
        List<String> perms = authzConfigService.getPermissions(userType);
        boolean hasAll = perms.stream().anyMatch(p -> "*".equals(p));
        if (hasAll) {
            authorities.add(new SimpleGrantedAuthority("*"));
            List<AuthPermissions> all = authPermissionsMapper.selectList(null);
            for (AuthPermissions p : all) {
                if (p != null && p.getPermCode() != null && !p.getPermCode().trim().isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(p.getPermCode().trim()));
                }
            }
        }
        for (String perm : perms) {
            if (perm != null && !perm.isBlank()) {
                if (!"*".equals(perm.trim())) {
                    authorities.add(new SimpleGrantedAuthority(perm.trim()));
                }
            }
        }

        loginUser.setAuthorities(authorities);
        return loginUser;
    }
}