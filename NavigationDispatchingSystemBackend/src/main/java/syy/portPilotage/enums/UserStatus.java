package syy.portPilotage.enums;

import com.baomidou.mybatisplus.annotation.TableField;

public enum UserStatus {
    NORMAL,   // 正常
    DISABLED,
    PENDING;  // 待审核

    public String getDescription() {
        return switch (this) {
            case NORMAL -> "正常";
            case DISABLED -> "禁用";
            case PENDING -> "待审核";
            default -> "";
        };
    }

    public static UserStatus fromDescription(String description) {
        return switch (description) {
            case "正常" -> NORMAL;
            case "禁用" -> DISABLED;
            case "待审核" -> PENDING;
            default -> PENDING; // 默认返回待审核
        };
    }
}