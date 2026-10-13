package syy.portPilotage.enums;

public enum VoyageStatusEnum {
    PLANNING,   // 计划中
    ON_ROUTE,   // 在途
    IN_PORT,    // 在港
    COMPLETED,  // 完成
    CANCELLED;  // 取消

    public String getDescription() {
        return switch (this) {
            case PLANNING -> "计划中";
            case ON_ROUTE -> "在途";
            case IN_PORT -> "在港";
            case COMPLETED -> "完成";
            case CANCELLED -> "取消";
            default -> "";
        };
    }

    public static VoyageStatusEnum fromDescription(String description) {
        return switch (description) {
            case "计划中" -> PLANNING;
            case "在途" -> ON_ROUTE;
            case "在港" -> IN_PORT;
            case "完成" -> COMPLETED;
            case "取消" -> CANCELLED;
            default -> PLANNING; // 默认返回计划中
        };
    }
}