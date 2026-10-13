package syy.portPilotage.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.auth.LoginDTO;
import syy.portPilotage.dto.auth.RegisterDTO;
import syy.portPilotage.entity.Users;
import syy.portPilotage.enums.UserStatus;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.jwt.JwtTokenUtil;
import syy.portPilotage.redis.cache.UserOnlineStatusService;
import syy.portPilotage.redis.service.UserAuthService;
import syy.portPilotage.security.domain.LoginUser;
import syy.portPilotage.security.component.AuthzConfigService;
import syy.portPilotage.service.UsersService;
import syy.portPilotage.util.BeanCopyUtil;
import syy.portPilotage.util.SecurityUtil;
import syy.portPilotage.vo.auth.LoginVO;
import syy.portPilotage.vo.auth.UserInfoVO;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {
    
    @Autowired
    UsersService usersService;
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    UserAuthService userAuthService;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private UserOnlineStatusService userOnlineStatusService;
    @Autowired
    private AuthzConfigService authzConfigService;
    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getAccount());
        
        // 检查账户是否被锁定
        if (userAuthService.isAccountLocked(loginDTO.getAccount())) {
            log.warn("账户已被锁定: {}", loginDTO.getAccount());
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "账户已被锁定，请稍后再试");
        }
        
        try {
            LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(loginDTO.getAccount());
            if (!passwordEncoder.matches(loginDTO.getPassword(), loginUser.getPassword())) {
                throw new RuntimeException("密码不匹配");
            }
            String token = jwtTokenUtil.generateToken(loginUser);
            
            userAuthService.storeUserToken(loginUser.getAccount(), token);
            userOnlineStatusService.setUserOnline(loginUser.getUserId());
            // 清除登录失败次数
            userAuthService.clearLoginFailCount(loginUser.getAccount());
            
            LoginVO loginVO = LoginVO.builder()
                    .token(token)
                    .userId(loginUser.getUserId())
                    .username(loginUser.getUsername())
                    .account(loginUser.getAccount())
                    .userType(loginUser.getUserType())
                    .status(loginUser.getStatus())
                    .phone(loginUser.getPhone())
                    .email(loginUser.getEmail())
                    .avatar(loginUser.getAvatar())
                    .permissions(extractPermissions(loginUser))
                    .build();
            
            log.info("用户登录成功: {}", loginUser.getAccount());
            return Result.success(loginVO);
            
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            
            // 增加登录失败次数
            Long failCount = userAuthService.incrementLoginFailCount(loginDTO.getAccount());
            
            // 如果失败次数超过5次，则锁定账户
            if (failCount >= 5) {
                userAuthService.lockAccount(loginDTO.getAccount());
                throw new BusinessException(ErrorCode.LOGIN_FAILED, "登录失败次数过多，账户已被锁定");
            }
            
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "账号或密码错误，您还有" + (5 - failCount) + "次机会");
        }
    }
    
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());
        
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次密码输入不一致");
        }
        
        if (usersService.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "用户名已存在");
        }
        
        Users user = new Users();
        user.setUsername(registerDTO.getUsername());
        user.setAccount(registerDTO.getAccount() != null ? registerDTO.getAccount() : registerDTO.getUsername()); // 设置账号，默认为用户名
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setUserType(registerDTO.getUserType());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setUserCode(UUID.randomUUID().toString().replace("-", ""));
        user.setCurrentStatus(UserStatus.PENDING);
        
        usersService.save(user);
        
        log.info("用户注册成功: {}", registerDTO.getUsername());
        return Result.success();
    }
    
    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息")
    public Result<UserInfoVO> getCurrentUser() {
        LoginUser loginUser = SecurityUtil.getCurrentUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        Users user = usersService.getById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        
        UserInfoVO userInfoVO = BeanCopyUtil.copy(user, UserInfoVO.class);
        userInfoVO.setUserType(loginUser.getUserType());
        userInfoVO.setPermissions(extractPermissions(loginUser));
        return Result.success(userInfoVO);
    }

    private List<String> extractPermissions(LoginUser loginUser) {
        if (loginUser == null || loginUser.getAuthorities() == null) return List.of();
        return loginUser.getAuthorities().stream()
                .map(a -> a == null ? null : a.getAuthority())
                .filter(s -> s != null && !s.trim().isEmpty())
                .filter(s -> !s.startsWith("ROLE_"))
                .distinct()
                .collect(Collectors.toList());
    }
    
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout() {
        LoginUser loginUser = SecurityUtil.getCurrentUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        log.info("用户登出: {}", loginUser.getAccount());
        
        userAuthService.removeUserToken(loginUser.getAccount());
        
        userOnlineStatusService.removeUserOnlineStatus(loginUser.getUserId());
        return Result.success();
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@RequestParam String oldPassword, 
                                       @RequestParam String newPassword) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        Users user = usersService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        usersService.updateById(user);
        
        userAuthService.removeUserToken(user.getAccount());
        
        log.info("用户修改密码成功: {}", user.getAccount());
        return Result.success();
    }
}