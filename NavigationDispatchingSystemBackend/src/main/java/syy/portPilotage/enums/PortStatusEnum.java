package syy.portPilotage.enums;

public enum PortStatusEnum {
    ACTIVE,
    INACTIVE;

    public String getDescription() {
        return switch (this) {
            case ACTIVE -> "启用";
            case INACTIVE -> "禁用";
            default -> "";
        };
    }

    public static PortStatusEnum fromDescription(String description) {
        return switch (description) {
            case "启用" -> ACTIVE;
            case "禁用" -> INACTIVE;
            default -> ACTIVE; // 默认返回启用
        };
    }
}