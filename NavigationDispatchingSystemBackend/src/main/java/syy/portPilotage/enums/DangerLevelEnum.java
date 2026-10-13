package syy.portPilotage.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DangerLevelEnum {
    LOW,     // 低
    MEDIUM,  // 中
    HIGH;    // 高

    /**
     * 兼容前端空字符串/中文/大小写：避免 JSON 解析阶段因 "" 直接失败。
     * - "" / null -> null
     * - "低/中/高" -> 对应枚举
     * - "LOW/MEDIUM/HIGH"（大小写不敏感）-> 对应枚举
     */
    @JsonCreator
    public static DangerLevelEnum fromJson(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        // 中文
        if ("低".equals(v) || "中".equals(v) || "高".equals(v)) {
            return fromDescription(v);
        }
        // 枚举名（大小写不敏感）
        return DangerLevelEnum.valueOf(v.toUpperCase());
    }

    public String getDescription() {
        return switch (this) {
            case LOW -> "低";
            case MEDIUM -> "中";
            case HIGH -> "高";
            default -> "";
        };
    }

    public static DangerLevelEnum fromDescription(String description) {
        return switch (description) {
            case "低" -> LOW;
            case "中" -> MEDIUM;
            case "高" -> HIGH;
            default -> LOW; // 默认返回低
        };
    }
}