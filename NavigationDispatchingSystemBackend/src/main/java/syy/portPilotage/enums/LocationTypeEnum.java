package syy.portPilotage.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum LocationTypeEnum {
    BERTH,      // 泊位
    ANCHORAGE,  // 锚地
    WHARF,      // 码头
    PORT_AREA,  // 港界
    PORT,       // 港口
    OTHER;      // 其他

    public String getDescription() {
        return switch (this) {
            case BERTH -> "泊位";
            case ANCHORAGE -> "锚地";
            case WHARF -> "码头";
            case PORT_AREA -> "港界";
            case PORT -> "港口";
            case OTHER -> "其他";
            default -> "";
        };
    }

    public static LocationTypeEnum fromDescription(String description) {
        return switch (description) {
            case "泊位" -> BERTH;
            case "锚地" -> ANCHORAGE;
            case "码头" -> WHARF;
            case "港界" -> PORT_AREA;
            case "港口" -> PORT;
            case "其他" -> OTHER;
            default -> OTHER; // 默认返回其他
        };
    }

    /**
     * 兼容前端/历史数据传入：
     * - "" / null -> null（避免 Jackson 空字符串转枚举直接报错）
     * - "BERTH" / "ANCHORAGE" / ...（不区分大小写）
     * - "泊位" / "锚地" / ...（中文描述）
     */
    @JsonCreator
    public static LocationTypeEnum fromJson(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        try {
            return LocationTypeEnum.valueOf(v.toUpperCase());
        } catch (Exception ignored) {
            return fromDescription(v);
        }
    }
}