package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName users
 */
@TableName(value ="users")
@Data
public class Users implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户代码
     */
    @TableField(value = "user_code")
    private String userCode;

    /**
     * 用户账号
     */
    @TableField(value = "account")
    private String account;

    /**
     * 用户类型
     */
    @TableField(value = "user_type")
    private Object userType;

    /**
     * 用户名称
     */
    @TableField(value = "username")
    private String username;

    /**
     * 口令
     */
    @TableField(value = "password")
    private String password;

    /**
     * 申请时间
     */
    @TableField(value = "apply_time")
    private Date applyTime;

    /**
     * 联系电话
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * E-mail
     */
    @TableField(value = "email")
    private String email;

    /**
     * 当前状态
     */
    @TableField(value = "current_status")
    private Object currentStatus;

    /**
     * 代理级别类型
     */
    @TableField(value = "agent_level_type")
    private String agentLevelType;

    /**
     * 所属代理主管ID
     */
    @TableField(value = "parent_agent_id")
    private Long parentAgentId;

    /**
     * 头像URL
     */
    @TableField(value = "avatar")
    private String avatar;

        @TableField(value = "last_login_at")
    private Date lastLoginAt;

    /**
     * 部门
     */
    @TableField(value = "department")
    private String department;

    /**
     * 职位
     */
    @TableField(value = "position")
    private String position;

        @TableField(value = "created_at")
    private Date createdAt;

        @TableField(value = "updated_at")
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Users other = (Users) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserCode() == null ? other.getUserCode() == null : this.getUserCode().equals(other.getUserCode()))
            && (this.getAccount() == null ? other.getAccount() == null : this.getAccount().equals(other.getAccount()))
            && (this.getUserType() == null ? other.getUserType() == null : this.getUserType().equals(other.getUserType()))
            && (this.getUsername() == null ? other.getUsername() == null : this.getUsername().equals(other.getUsername()))
            && (this.getPassword() == null ? other.getPassword() == null : this.getPassword().equals(other.getPassword()))
            && (this.getApplyTime() == null ? other.getApplyTime() == null : this.getApplyTime().equals(other.getApplyTime()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getCurrentStatus() == null ? other.getCurrentStatus() == null : this.getCurrentStatus().equals(other.getCurrentStatus()))
            && (this.getAgentLevelType() == null ? other.getAgentLevelType() == null : this.getAgentLevelType().equals(other.getAgentLevelType()))
            && (this.getParentAgentId() == null ? other.getParentAgentId() == null : this.getParentAgentId().equals(other.getParentAgentId()))
            && (this.getAvatar() == null ? other.getAvatar() == null : this.getAvatar().equals(other.getAvatar()))
            && (this.getLastLoginAt() == null ? other.getLastLoginAt() == null : this.getLastLoginAt().equals(other.getLastLoginAt()))
            && (this.getDepartment() == null ? other.getDepartment() == null : this.getDepartment().equals(other.getDepartment()))
            && (this.getPosition() == null ? other.getPosition() == null : this.getPosition().equals(other.getPosition()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserCode() == null) ? 0 : getUserCode().hashCode());
        result = prime * result + ((getAccount() == null) ? 0 : getAccount().hashCode());
        result = prime * result + ((getUserType() == null) ? 0 : getUserType().hashCode());
        result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
        result = prime * result + ((getPassword() == null) ? 0 : getPassword().hashCode());
        result = prime * result + ((getApplyTime() == null) ? 0 : getApplyTime().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getCurrentStatus() == null) ? 0 : getCurrentStatus().hashCode());
        result = prime * result + ((getAgentLevelType() == null) ? 0 : getAgentLevelType().hashCode());
        result = prime * result + ((getParentAgentId() == null) ? 0 : getParentAgentId().hashCode());
        result = prime * result + ((getAvatar() == null) ? 0 : getAvatar().hashCode());
        result = prime * result + ((getLastLoginAt() == null) ? 0 : getLastLoginAt().hashCode());
        result = prime * result + ((getDepartment() == null) ? 0 : getDepartment().hashCode());
        result = prime * result + ((getPosition() == null) ? 0 : getPosition().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userCode=").append(userCode);
        sb.append(", account=").append(account);
        sb.append(", userType=").append(userType);
        sb.append(", username=").append(username);
        sb.append(", password=***");
        sb.append(", applyTime=").append(applyTime);
        sb.append(", phone=").append(phone);
        sb.append(", email=").append(email);
        sb.append(", currentStatus=").append(currentStatus);
        sb.append(", agentLevelType=").append(agentLevelType);
        sb.append(", parentAgentId=").append(parentAgentId);
        sb.append(", avatar=").append(avatar);
        sb.append(", lastLoginAt=").append(lastLoginAt);
        sb.append(", department=").append(department);
        sb.append(", position=").append(position);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}