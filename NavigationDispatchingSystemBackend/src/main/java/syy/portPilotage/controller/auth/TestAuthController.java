package syy.portPilotage.controller.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.entity.Users;
import syy.portPilotage.security.domain.LoginUser;
import syy.portPilotage.service.UsersService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class TestAuthController {

    @Autowired
    private UsersService usersService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/test-login")
    public Result<Map<String, Object>> testLogin(@RequestParam String account, @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        Users user = usersService.getByAccount(account);
        if (user == null) {
            result.put("userExists", false);
            return Result.success(result);
        }
        result.put("userExists", true);
        result.put("dbPassword", user.getPassword());
        result.put("inputPassword", password);
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        result.put("matches", matches);

        try {
            LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(account);
            result.put("userDetailsPassword", loginUser.getPassword());
            result.put("userDetailsMatches", passwordEncoder.matches(password, loginUser.getPassword()));
        } catch (Exception e) {
            result.put("userDetailsError", e.getMessage());
        }

        return Result.success(result);
    }
}
