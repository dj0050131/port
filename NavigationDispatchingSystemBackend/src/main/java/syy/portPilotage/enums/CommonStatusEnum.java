package syy.portPilotage.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CommonStatusEnum {
    ENABLED,
    DISABLED;

    /**
     * 兼容前端空字符串/中文/大小写：避免 JSON 解析阶段因 "" 直接失败。
     * - "" / null -> null
     * - "启用/禁用" -> 对应枚举
     * - "ENABLED/DISABLED"（大小写不敏感）-> 对应枚举
     */
    @JsonCreator
    public static CommonStatusEnum fromJson(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        if ("启用".equals(v) || "禁用".equals(v)) {
            return fromDescription(v);
        }
        return CommonStatusEnum.valueOf(v.toUpperCase());
    }

    public String getDescription() {
        return switch (this) {
            case ENABLED -> "启用";
            case DISABLED -> "禁用";
            default -> "";
        };
    }

    public static CommonStatusEnum fromDescription(String description) {
        return switch (description) {
            case "启用" -> ENABLED;
            case "禁用" -> DISABLED;
            default -> ENABLED; // 默认返回启用
        };
    }
}