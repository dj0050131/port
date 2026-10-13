package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import syy.portPilotage.entity.Drivers;
import syy.portPilotage.entity.Pilots;
import syy.portPilotage.entity.Users;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.mapper.DriversMapper;
import syy.portPilotage.mapper.PilotsMapper;
import syy.portPilotage.mapper.UsersMapper;
import syy.portPilotage.service.UsersService;
import syy.portPilotage.util.BusinessCodeGenerator;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    @Autowired
    private PilotsMapper pilotsMapper;
    @Autowired
    private DriversMapper driversMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BusinessCodeGenerator businessCodeGenerator;

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUsername, username);
        return this.count(wrapper) > 0;
    }

    @Override
    public Users getByUsername(String username) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUsername, username);
        return this.getOne(wrapper);
    }

    /**
     * 根据账号获取用户
     * @param account 账号
     * @return 用户信息
     */
    public Users getByAccount(String account) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getAccount, account);
        return this.getOne(wrapper);
    }

    /**
     * 检查账号是否存在
     * @param account 账号
     * @return 是否存在
     */
    public boolean existsByAccount(String account) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getAccount, account);
        return this.count(wrapper) > 0;
    }

    @Override
    public List<Users> getByUserType(String userType) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserType, userType);
        return this.list(wrapper);
    }

    @Override
    public List<Users> getByStatus(String status) {
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getCurrentStatus, status);
        return this.list(wrapper);
    }

    @Override
    public boolean createUser(Users user) {
        user.setUserCode(normalizeBlank(user.getUserCode()));
        user.setPhone(normalizeBlank(user.getPhone()));
        user.setEmail(normalizeBlank(user.getEmail()));
        user.setAvatar(normalizeBlank(user.getAvatar()));
        user.setDepartment(normalizeBlank(user.getDepartment()));
        user.setPosition(normalizeBlank(user.getPosition()));
        user.setAgentLevelType(normalizeBlank(user.getAgentLevelType()));

        // user_code: NOT NULL + UNIQUE（数据库无默认值）——必须保证有值
        if (user.getUserCode() == null) {
            // 使用 business_code_sequences 生成规律编号，避免 UUID 过长
            // 规则：UT + 4位递增序号（全局递增，不带日期）
            Integer maxSeq = jdbcTemplate.queryForObject(
                    "SELECT MAX(CAST(SUBSTRING(user_code, 3) AS UNSIGNED)) " +
                            "FROM users WHERE user_code REGEXP '^UT[0-9]+$'",
                    Integer.class
            );
            int minNext = (maxSeq == null ? 0 : maxSeq) + 1;
            int seq = businessCodeGenerator.nextSeqAtLeast("USER_CODE", "00000000", minNext);
            user.setUserCode("UT" + String.format("%04d", seq));
        }

        // apply_time 允许为空；后台创建用户时给一个更合理的默认值，便于审计/展示
        if (user.getApplyTime() == null) {
            user.setApplyTime(new Date());
        }

        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        boolean result = this.save(user);
        if (result) {
            log.info("创建用户成功: id={}, username={}", user.getId(), user.getUsername());
        } else {
            log.error("创建用户失败: username={}", user.getUsername());
        }
        return result;
    }

    @Override
    public boolean updateUser(Users user) {
        user.setUpdatedAt(new Date());
        boolean result = this.updateById(user);
        if (result) {
            log.info("更新用户成功: id={}, username={}", user.getId(), user.getUsername());
        } else {
            log.error("更新用户失败: id={}", user.getId());
        }
        return result;
    }

    @Override
    public boolean deleteUser(Long id) {
        // 防止“删掉用户但资源仍存在/引用错乱”的问题：用户被资源关联时不允许删除
        Long pilotCnt = pilotsMapper.selectCount(new LambdaQueryWrapper<Pilots>().eq(Pilots::getUserId, id));
        if (pilotCnt != null && pilotCnt > 0) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE, "该用户已关联引航员，请先删除/解绑引航员后再删除用户");
        }
        Long driverCnt = driversMapper.selectCount(new LambdaQueryWrapper<Drivers>().eq(Drivers::getUserId, id));
        if (driverCnt != null && driverCnt > 0) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE, "该用户已关联驾驶员，请先删除/解绑驾驶员后再删除用户");
        }
        boolean result = this.removeById(id);
        if (result) {
            log.info("删除用户成功: id={}", id);
        } else {
            log.error("删除用户失败: id={}", id);
        }
        return result;
    }

    @Override
    public boolean updateLastLoginTime(Long userId) {
        Users user = new Users();
        user.setId(userId);
        user.setLastLoginAt(new Date());
        user.setUpdatedAt(new Date());
        boolean result = this.updateById(user);
        if (result) {
            log.info("更新用户最后登录时间成功: userId={}", userId);
        } else {
            log.error("更新用户最后登录时间失败: userId={}", userId);
        }
        return result;
    }

    private static String normalizeBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}