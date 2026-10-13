package syy.portPilotage.vo;

import lombok.Data;
import syy.portPilotage.entity.Users;

import java.util.Date;

@Data
public class UserOnlineVO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户代码
     */
    private String userCode;

    /**
     * 用户账号
     */
    private String account;

    /**
     * 用户类型
     */
    private Object userType;

    /**
     * 用户名称
     */
    private String username;

        private String password;

    /**
     * 申请时间
     */
    private Date applyTime;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * E-mail
     */
    private String email;

    /**
     * 当前状态
     */
    private Object currentStatus;

    /**
     * 代理级别类型
     */
    private String agentLevelType;

    /**
     * 所属代理主管ID
     */
    private Long parentAgentId;

    /**
     * 头像URL
     */
    private String avatar;

        private Date lastLoginAt;

    /**
     * 部门
     */
    private String department;

    /**
     * 职位
     */
    private String position;

        private Date createdAt;

        private Date updatedAt;

    /**
     * 在线状态
     */
    private String onlineStatus;

        public static UserOnlineVO fromEntity(Users user, String onlineStatus) {
        UserOnlineVO vo = new UserOnlineVO();
        vo.setId(user.getId());
        vo.setUserCode(user.getUserCode());
        vo.setAccount(user.getAccount());
        vo.setUserType(user.getUserType());
        vo.setUsername(user.getUsername());
                vo.setApplyTime(user.getApplyTime());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setCurrentStatus(user.getCurrentStatus());
        vo.setAgentLevelType(user.getAgentLevelType());
        vo.setParentAgentId(user.getParentAgentId());
        vo.setAvatar(user.getAvatar());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setDepartment(user.getDepartment());
        vo.setPosition(user.getPosition());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        vo.setOnlineStatus(onlineStatus);
        return vo;
    }
}