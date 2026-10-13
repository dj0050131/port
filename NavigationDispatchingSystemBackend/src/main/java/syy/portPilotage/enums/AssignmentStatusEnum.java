package syy.portPilotage.enums;

import lombok.Getter;

@Getter
public enum AssignmentStatusEnum {
    ASSIGNED("ASSIGNED", "已分配"),
    CONFIRMED("CONFIRMED", "已确认"),
    CANCELLED("CANCELLED", "已取消"),
    SYSTEM_CANCELLATION("SYSTEM_CANCELLATION", "系统取消"),
    COMPLETED("COMPLETED", "已完成");

    private final String code;
    private final String name;

    AssignmentStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (AssignmentStatusEnum status : AssignmentStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getName();
            }
        }
        return code;
    }

    public static boolean isValid(String code) {
        for (AssignmentStatusEnum status : AssignmentStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}