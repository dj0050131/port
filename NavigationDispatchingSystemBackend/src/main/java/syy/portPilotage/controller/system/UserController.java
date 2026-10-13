package syy.portPilotage.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.constant.SecurityConstant;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Users;
import syy.portPilotage.enums.UserStatus;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.UsersService;
import syy.portPilotage.util.BeanCopyUtil;
import syy.portPilotage.vo.auth.UserInfoVO;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "系统用户管理接口")
public class UserController {
    
    @Autowired
    UsersService usersService;
    @Autowired
    PasswordEncoder passwordEncoder;
    
    @PostMapping
    @Operation(summary = "创建用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@RequestBody Users user) {
        log.info("创建用户: username={}", user.getUsername());
        
        if (usersService.existsByUsername(user.getUsername())) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "用户名已存在");
        }
        
        user.setPassword(passwordEncoder.encode(SecurityConstant.DEFAULT_PASSWORD));
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        
        usersService.save(user);
        
        log.info("用户创建成功: id={}", user.getId());
        return Result.success(user.getId());
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Users user) {
        log.info("更新用户: id={}", id);
        
        Users existing = usersService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        
        user.setId(id);
        user.setUpdatedAt(new Date());
        
        usersService.updateById(user);
        
        log.info("用户更新成功: id={}", id);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除用户: id={}", id);
        
        usersService.removeById(id);
        
        log.info("用户删除成功: id={}", id);
        return Result.success();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<UserInfoVO> getById(@PathVariable Long id) {
        Users user = usersService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        
        UserInfoVO userInfoVO = BeanCopyUtil.copy(user, UserInfoVO.class);
        return Result.success(userInfoVO);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<UserInfoVO>> page(PageQueryDTO query) {
        Page<Users> page = new Page<>(query.getCurrent(), query.getSize());
        
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Users::getCreatedAt);
        
        IPage<Users> result = usersService.page(page, wrapper);
        
        List<UserInfoVO> voList = BeanCopyUtil.copyList(result.getRecords(), UserInfoVO.class);
        PageResult<UserInfoVO> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                voList
        );
        
        return Result.success(pageResult);
    }
    
    @PostMapping("/{id}/reset-password")
    @Operation(summary = "重置密码")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id) {
        log.info("重置用户密码: id={}", id);
        
        Users user = usersService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(SecurityConstant.DEFAULT_PASSWORD));
        user.setUpdatedAt(new Date());
        
        usersService.updateById(user);
        
        log.info("用户密码重置成功: id={}", id);
        return Result.success();
    }
    
    @PostMapping("/{id}/status")
    @Operation(summary = "更新用户状态")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新用户状态: id={}, status={}", id, status);

        Users user = usersService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        try {
            UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
            user.setCurrentStatus(userStatus);
            user.setUpdatedAt(new Date());

            usersService.updateById(user);

            log.info("用户状态更新成功: id={}", id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无效的状态值: " + status);
        }
    }
}
