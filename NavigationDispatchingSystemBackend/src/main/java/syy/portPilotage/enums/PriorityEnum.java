package syy.portPilotage.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PriorityEnum {
    LOW,     // 低
    MEDIUM,  // 中
    HIGH,    // 高
    URGENT;  // 紧急

    public String getDescription() {
        return switch (this) {
            case LOW -> "低";
            case MEDIUM -> "中";
            case HIGH -> "高";
            case URGENT -> "紧急";
            default -> "";
        };
    }

    public static PriorityEnum fromDescription(String description) {
        return switch (description) {
            case "低" -> LOW;
            case "中" -> MEDIUM;
            case "高" -> HIGH;
            case "紧急" -> URGENT;
            default -> MEDIUM; // 默认返回中
        };
    }

        @JsonCreator
    public static PriorityEnum fromJson(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        try {
            return PriorityEnum.valueOf(v.toUpperCase());
        } catch (Exception ignored) {
            return fromDescription(v);
        }
    }
}