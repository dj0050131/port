package syy.portPilotage.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.Result;
import syy.portPilotage.entity.Users;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.UsersService;
import syy.portPilotage.util.SecurityUtil;
import syy.portPilotage.vo.auth.UserInfoVO;
import syy.portPilotage.util.BeanCopyUtil;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户信息", description = "当前用户个人信息接口")
public class UserProfileController {

    private final UsersService usersService;

    @PutMapping("/profile")
    @Operation(summary = "更新个人信息")
    public Result<UserInfoVO> updateProfile(@RequestBody ProfileUpdateDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Users user = usersService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        if (dto.getName() != null) {
            user.setUsername(dto.getName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getDepartment() != null) {
            user.setDepartment(dto.getDepartment());
        }
        if (dto.getPosition() != null) {
            user.setPosition(dto.getPosition());
        }
        user.setUpdatedAt(new Date());
        usersService.updateById(user);

        UserInfoVO userInfoVO = BeanCopyUtil.copy(user, UserInfoVO.class);
        return Result.success(userInfoVO);
    }

    @PutMapping("/avatar")
    @Operation(summary = "更新头像")
    public Result<Void> updateAvatar(@RequestParam String avatar) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Users user = usersService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        user.setAvatar(avatar);
        user.setUpdatedAt(new Date());
        usersService.updateById(user);
        return Result.success();
    }

    @Data
    public static class ProfileUpdateDTO {
        private String name;
        private String phone;
        private String email;
        private String department;
        private String position;
    }
}
