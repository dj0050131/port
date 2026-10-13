package syy.portPilotage.enums;

public enum RouteTypeEnum {
    INTERNATIONAL, // 国际航线
    DOMESTIC;      // 国内航线

    public String getDescription() {
        return switch (this) {
            case INTERNATIONAL -> "国际航线";
            case DOMESTIC      -> "国内航线";
            default -> "";
        };
    }

    public static RouteTypeEnum fromDescription(String description) {
        return switch (description) {
            case "国际航线" -> INTERNATIONAL;
            case "国内航线" -> DOMESTIC;
            default -> DOMESTIC;
        };
    }
}