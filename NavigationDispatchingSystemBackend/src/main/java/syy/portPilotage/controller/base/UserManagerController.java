package syy.portPilotage.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Users;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.redis.cache.UserOnlineStatusService;
import syy.portPilotage.redis.service.UserAuthService;
import syy.portPilotage.service.UsersService;
import syy.portPilotage.vo.UserOnlineVO;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/base/userManager")
@RequiredArgsConstructor
@Tag(name = "用户信息管理", description = "用户信息相关接口")
public class UserManagerController {

    @Autowired
    private UsersService usersService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserAuthService userAuthService;
    @Autowired
    private UserOnlineStatusService userOnlineStatusService;

    @PostMapping
    @Operation(summary = "创建用户信息")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:USERS:MANAGE')")
    public Result<Long> create(@Valid @RequestBody Users user) {
        log.info("创建用户信息: username={}, account={}", user.getUsername(), user.getAccount());
        
        if (usersService.existsByUsername(user.getUsername())) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "用户名已存在");
        }
        
        if (usersService.existsByAccount(user.getAccount())) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "账号已存在");
        }

        // 后台创建用户时，密码通常是明文输入；这里统一做一次加密，避免无法登录/明文落库
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码不能为空");
        }
        String pwd = user.getPassword().trim();
        // 如果前端已传入 BCrypt 哈希（例如 $2a$...），则不重复加密
        if (!(pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$2y$"))) {
            user.setPassword(passwordEncoder.encode(pwd));
        } else {
            user.setPassword(pwd);
        }
        
        boolean result = usersService.createUser(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "创建用户失败");
        }
        
        log.info("用户信息创建成功: id={}, username={}", user.getId(), user.getUsername());
        return Result.success(user.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:USERS:MANAGE')")
    public Result<Users> update(@PathVariable Long id, @Valid @RequestBody Users user) {
        log.info("更新用户信息: id={}", id);

        Users existingUser = usersService.getById(id);
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "用户信息不存在");
        }

        user.setId(id);
        boolean result = usersService.updateUser(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新用户失败");
        }
        
        Users updatedUser = usersService.getById(id);
        log.info("用户信息更新成功: id={}", id);
        return Result.success(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户信息")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:USERS:MANAGE')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除用户信息: id={}", id);

        Users user = usersService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "用户信息不存在");
        }

        boolean result = usersService.deleteUser(id);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "删除用户失败");
        }
        
        log.info("用户信息删除成功: id={}", id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息详情")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:USERS:MANAGE')")
    public Result<UserOnlineVO> getById(@PathVariable Long id) {
        Users user = usersService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "用户信息不存在");
        }
        
        String onlineStatus = userOnlineStatusService.getUserOnlineStatus(user.getId());
        UserOnlineVO userOnlineVO = UserOnlineVO.fromEntity(user, onlineStatus);
        
        return Result.success(userOnlineVO);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询用户信息")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:USERS:MANAGE')")
    public Result<PageResult<UserOnlineVO>> page(PageQueryDTO query) {
        IPage<Users> result = usersService.page(new Page<>(query.getCurrent(), query.getSize()));

        List<UserOnlineVO> userOnlineVOs = result.getRecords().stream()
                .map(user -> {
                    String onlineStatus = userOnlineStatusService.getUserOnlineStatus(user.getId());
                    return UserOnlineVO.fromEntity(user, onlineStatus);
                })
                .toList();

        PageResult<UserOnlineVO> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                userOnlineVOs
        );

        return Result.success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获取用户列表")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:USERS:MANAGE')")
    public Result<List<UserOnlineVO>> list(@RequestParam(required = false) String userType,
                                    @RequestParam(required = false) String status) {
        log.info("获取用户列表: userType={}, status={}", userType, status);
        
        List<Users> users;
        if (userType != null && status != null) {
            users = usersService.list(new LambdaQueryWrapper<Users>()
                    .eq(Users::getUserType, userType)
                    .eq(Users::getCurrentStatus, status));
        } else if (userType != null) {
            users = usersService.getByUserType(userType);
        } else if (status != null) {
            users = usersService.getByStatus(status);
        } else {
            users = usersService.list();
        }
        
        List<UserOnlineVO> userOnlineVOs = users.stream()
                .map(user -> {
                    String onlineStatus = userOnlineStatusService.getUserOnlineStatus(user.getId());
                    return UserOnlineVO.fromEntity(user, onlineStatus);
                })
                .toList();
        
        return Result.success(userOnlineVOs);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索用户")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:USERS:MANAGE')")
    public Result<List<UserOnlineVO>> search(@RequestParam String keyword) {
        log.info("搜索用户: keyword={}", keyword);
        
        List<Users> users = usersService.list(new LambdaQueryWrapper<Users>()
                .like(Users::getUsername, keyword)
                .or()
                .like(Users::getAccount, keyword));
        
        List<UserOnlineVO> userOnlineVOs = users.stream()
                .map(user -> {
                    String onlineStatus = userOnlineStatusService.getUserOnlineStatus(user.getId());
                    return UserOnlineVO.fromEntity(user, onlineStatus);
                })
                .toList();
        
        return Result.success(userOnlineVOs);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新用户状态")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:USERS:MANAGE')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Object status) {
        log.info("更新用户状态: id={}, status={}", id, status);

        Users user = usersService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "用户信息不存在");
        }

        user.setCurrentStatus(status);
        boolean result = usersService.updateUser(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新用户状态失败");
        }
        
        log.info("用户状态更新成功: id={}, status={}", id, status);
        return Result.success();
    }

    @PatchMapping("/{id}/password/reset")
    @Operation(summary = "重置用户密码")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SYSTEM:USERS:MANAGE')")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        log.info("重置用户密码: id={}", id);
        
        Users user = usersService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "用户信息不存在");
        }
        
        String newPassword = requestBody.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码不能为空");
        }
        
        // 使用密码加密器加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        boolean result = usersService.updateUser(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "重置用户密码失败");
        }
        
        log.info("用户密码重置成功: id={}", id);
        return Result.success();
    }

    @PatchMapping("/{id}/disable")
    @Operation(summary = "禁用用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> disableUser(@PathVariable Long id) {
        log.info("禁用用户: id={}", id);
        
        Users user = usersService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "用户信息不存在");
        }
        
        user.setCurrentStatus("DISABLED");
        boolean result = usersService.updateUser(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "禁用用户失败");
        }
        
        log.info("用户禁用成功: id={}", id);
        return Result.success();
    }

    @PatchMapping("/{id}/offline")
    @Operation(summary = "下线用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> offlineUser(@PathVariable Long id) {
        log.info("下线用户: id={}", id);
        
        Users user = usersService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "用户信息不存在");
        }
        
        userAuthService.removeUserToken(user.getAccount());
        
        userOnlineStatusService.removeUserOnlineStatus(user.getId());
        
        log.info("用户下线成功: id={}", id);
        return Result.success();
    }
}