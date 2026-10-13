package syy.portPilotage.enums;

import lombok.Getter;

@Getter
public enum ConfirmationStatusEnum {
    ASSIGNED("ASSIGNED", "已分配"),
    PENDING("PENDING", "待确认"),
    CONFIRMED("CONFIRMED", "已确认"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    COMPLETED("COMPLETED", "已完成"),
    REJECTED("REJECTED", "已拒绝"),
    CANCELLED("CANCELLED", "已取消"),
    SYSTEM_CANCELLATION("SYSTEM_CANCELLATION", "系统取消");

    private final String code;
    private final String name;

    ConfirmationStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (ConfirmationStatusEnum status : ConfirmationStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getName();
            }
        }
        return code;
    }
}