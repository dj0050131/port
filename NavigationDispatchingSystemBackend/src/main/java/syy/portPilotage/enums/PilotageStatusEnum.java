package syy.portPilotage.enums;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

public enum PilotageStatusEnum {
    PENDING,      // 待审核
    APPROVED,     // 已批准
    REJECTED,     // 已驳回
    PLANNED,      // 已排班
    IN_PROGRESS,
    COMPLETED,    // 已完成
    CANCELLED;    // 已取消

    public String getDescription() {
        return switch (this) {
            case PENDING -> "待审核";
            case APPROVED -> "已批准";
            case REJECTED -> "已驳回";
            case PLANNED -> "已排班";
            case IN_PROGRESS -> "执行中";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
            default -> "";
        };
    }

    public static PilotageStatusEnum fromDescription(String description) {
        return switch (description) {
            case "待审核" -> PENDING;
            case "已批准" -> APPROVED;
            case "已驳回" -> REJECTED;
            case "已排班" -> PLANNED;
            case "执行中" -> IN_PROGRESS;
            case "已完成" -> COMPLETED;
            case "已取消" -> CANCELLED;
            default -> PENDING; // 默认返回待审核
        };
    }
}