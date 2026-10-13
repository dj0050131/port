package syy.portPilotage.service;

import syy.portPilotage.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UsersService extends IService<Users> {
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户信息
     */
    Users getByUsername(String username);

    /**
     * 根据账号获取用户
     * @param account 账号
     * @return 用户信息
     */
    Users getByAccount(String account);

    /**
     * 检查账号是否存在
     * @param account 账号
     * @return 是否存在
     */
    boolean existsByAccount(String account);

    /**
     * 根据用户类型查询用户
     * @param userType 用户类型
     * @return 用户列表
     */
    List<Users> getByUserType(String userType);

    /**
     * 根据状态查询用户
     * @param status 状态
     * @return 用户列表
     */
    List<Users> getByStatus(String status);

    /**
     * 创建用户
     * @param user 用户信息
     * @return 是否创建成功
     */
    boolean createUser(Users user);

    /**
     * 更新用户
     * @param user 用户信息
     * @return 是否更新成功
     */
    boolean updateUser(Users user);

    /**
     * 删除用户
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(Long id);

    /**
     * 更新用户最后登录时间
     * @param userId 用户ID
     * @return 是否更新成功
     */
    boolean updateLastLoginTime(Long userId);
}