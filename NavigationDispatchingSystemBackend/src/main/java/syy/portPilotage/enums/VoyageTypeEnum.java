package syy.portPilotage.enums;

import java.util.List;

public enum VoyageTypeEnum {
    IMPORT,   // 进港
    EXPORT,   // 出港
    TRANSIT;  // 移泊

    public String getDescription() {
        return switch (this) {
            case IMPORT -> "进港";
            case EXPORT -> "出港";
            case TRANSIT -> "移泊";
            default -> "";
        };
    }

    public static VoyageTypeEnum fromDescription(String description) {
        return switch (description) {
            case "进港" -> IMPORT;
            case "出港" -> EXPORT;
            case "移泊" -> TRANSIT;
            default -> IMPORT; // 默认返回进港
        };
    }
}