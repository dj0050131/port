package syy.portPilotage.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PilotageDirectionEnum {
    IMPORT,     // 进港
    EXPORT,     // 出港
    SHIFTING,   // 移泊
    OTHER;      // 其他

    /**
     * 兼容前端空字符串/中文/大小写：避免 JSON 解析阶段因 "" 直接失败。
     * - "" / null -> null
     * - "进港/出港/移泊/其他" -> 对应枚举
     * - "IMPORT/EXPORT/SHIFTING/OTHER"（大小写不敏感）-> 对应枚举
     */
    @JsonCreator
    public static PilotageDirectionEnum fromJson(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        if ("进港".equals(v) || "出港".equals(v) || "移泊".equals(v) || "其他".equals(v)) {
            return fromDescription(v);
        }
        return PilotageDirectionEnum.valueOf(v.toUpperCase());
    }

    public String getDescription() {
        return switch (this) {
            case IMPORT -> "进港";
            case EXPORT -> "出港";
            case SHIFTING -> "移泊";
            case OTHER -> "其他";
            default -> "";
        };
    }

    public static PilotageDirectionEnum fromDescription(String description) {
        return switch (description) {
            case "进港" -> IMPORT;
            case "出港" -> EXPORT;
            case "移泊" -> SHIFTING;
            case "其他" -> OTHER;
            default -> OTHER; // 默认返回其他
        };
    }
}