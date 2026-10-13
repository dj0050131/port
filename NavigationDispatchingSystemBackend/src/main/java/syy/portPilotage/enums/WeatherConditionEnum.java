package syy.portPilotage.enums;

public enum WeatherConditionEnum {
    SUNNY,       // 晴
    CLOUDY,      // 多云
    OVERCAST,    // 阴
    SHOWER,      // 阵雨
    STORMY,      // 雷阵雨/暴雨
    HAIL,        // 雷阵雨伴有冰雹
    SLEET,       // 雨夹雪
    DRIZZLE,     // 小雨
    RAINY,       // 中雨/大雨
    SNOWY,       // 雪(阵雪/小雪/中雪/大雪/暴雪)
    FOGGY,       // 雾/轻雾
    FROZEN_RAIN, // 冻雨
    DUST_STORM,  // 沙尘暴/强沙尘暴/浮尘/扬沙
    HAZE,        // 霾
    STORM,       // 暴雨(用于isBadWeather判断)
    TYPHOON,     // 台风(用于isBadWeather判断)
    HEAVY_RAIN,  // 大雨(用于isBadWeather判断)
    HEAVY_SNOW;  // 大雪(用于isBadWeather判断)

    public String getDescription() {
        return switch (this) {
            case SUNNY -> "晴";
            case CLOUDY -> "多云";
            case OVERCAST -> "阴";
            case SHOWER -> "阵雨";
            case STORMY -> "雷阵雨";
            case HAIL -> "雷阵雨伴有冰雹";
            case SLEET -> "雨夹雪";
            case DRIZZLE -> "小雨";
            case RAINY -> "雨";
            case SNOWY -> "雪";
            case FOGGY -> "雾";
            case FROZEN_RAIN -> "冻雨";
            case DUST_STORM -> "沙尘暴";
            case HAZE -> "霾";
            case STORM -> "暴雨";
            case TYPHOON -> "台风";
            case HEAVY_RAIN -> "大雨";
            case HEAVY_SNOW -> "大雪";
        };
    }

    public static WeatherConditionEnum fromDescription(String description) {
        return switch (description) {
            case "晴" -> SUNNY;
            case "多云" -> CLOUDY;
            case "阴" -> OVERCAST;
            case "阵雨" -> SHOWER;
            case "雷阵雨" -> STORMY;
            case "雷阵雨伴有冰雹" -> HAIL;
            case "雨夹雪" -> SLEET;
            case "小雨" -> DRIZZLE;
            case "雨" -> RAINY;
            case "雪" -> SNOWY;
            case "雾" -> FOGGY;
            case "冻雨" -> FROZEN_RAIN;
            case "沙尘暴" -> DUST_STORM;
            case "霾" -> HAZE;
            case "暴雨" -> STORM;
            case "台风" -> TYPHOON;
            case "大雨" -> HEAVY_RAIN;
            case "大雪" -> HEAVY_SNOW;
            default -> SUNNY; // 默认返回晴
        };
    }
}