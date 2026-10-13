package syy.portPilotage.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import syy.portPilotage.entity.WeatherInfo;
import syy.portPilotage.entity.TideInfo;

import javax.net.ssl.*;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

@Slf4j
public class WeatherUtil {
    
    public static boolean isBadWeather(String weatherCode) {
        return "STORM".equals(weatherCode) || "TYPHOON".equals(weatherCode) 
                || "HEAVY_RAIN".equals(weatherCode) || "HEAVY_SNOW".equals(weatherCode);
    }

    
    public static String mapWeatherCodeToEnum(String weatherCode) {
        // 根据国家气象中心天气图标代码映射
        switch (weatherCode) {
            case "0": // 晴
                return "SUNNY";
            case "1": // 多云
                return "CLOUDY";
            case "2": // 阴
                return "OVERCAST";
            case "3": // 阵雨
                return "SHOWER";
            case "4": // 雷阵雨
                return "STORMY";
            case "5": // 雷阵雨伴有冰雹
                return "HAIL";
            case "6": // 雨夹雪
                return "SLEET";
            case "7": // 小雨
                return "DRIZZLE";
            case "8": // 中雨
                return "RAINY";
            case "9": // 大雨
                return "RAINY";
            case "10": // 暴雨
                return "STORMY";
            case "11": // 大暴雨
                return "STORMY";
            case "12": // 特大暴雨
                return "STORMY";
            case "13": // 阵雪
                return "SNOWY";
            case "14": // 小雪
                return "SNOWY";
            case "15": // 中雪
                return "SNOWY";
            case "16": // 大雪
                return "SNOWY";
            case "17": // 暴雪
                return "SNOWY";
            case "18": // 雾
                return "FOGGY";
            case "19": // 冻雨
                return "FROZEN_RAIN";
            case "20": // 沙尘暴
                return "DUST_STORM";
            case "21": // 强沙尘暴
                return "DUST_STORM";
            case "22": // 轻雾
                return "FOGGY";
            case "23": // 霾
                return "HAZE";
            case "24": // 浮尘
                return "DUST_STORM";
            case "25": // 扬沙
                return "DUST_STORM";
            default:
                return "CLOUDY"; // 默认多云
        }
    }

    
    public static String mapWeatherTextToEnum(String weatherText) {
        if (weatherText == null || weatherText.isEmpty()) {
            return "CLOUDY";
        }

        weatherText = weatherText.toLowerCase();

        if (weatherText.contains("晴")) {
            return "SUNNY";
        } else if (weatherText.contains("多云")) {
            return "CLOUDY";
        } else if (weatherText.contains("阴")) {
            return "OVERCAST";
        } else if (weatherText.contains("阵雨")) {
            return "SHOWER";
        } else if (weatherText.contains("雷阵雨")) {
            return "STORMY";
        } else if (weatherText.contains("雷阵雨伴有冰雹") || weatherText.contains("冰雹")) {
            return "HAIL";
        } else if (weatherText.contains("雨夹雪")) {
            return "SLEET";
        } else if (weatherText.contains("小雨")) {
            return "DRIZZLE";
        } else if (weatherText.contains("中雨")) {
            return "RAINY";
        } else if (weatherText.contains("大雨")) {
            return "RAINY";
        } else if (weatherText.contains("暴雨")) {
            return "STORMY";
        } else if (weatherText.contains("大暴雨")) {
            return "STORMY";
        } else if (weatherText.contains("特大暴雨")) {
            return "STORMY";
        } else if (weatherText.contains("阵雪")) {
            return "SNOWY";
        } else if (weatherText.contains("小雪")) {
            return "SNOWY";
        } else if (weatherText.contains("中雪")) {
            return "SNOWY";
        } else if (weatherText.contains("大雪")) {
            return "SNOWY";
        } else if (weatherText.contains("暴雪")) {
            return "SNOWY";
        } else if (weatherText.contains("雾")) {
            return "FOGGY";
        } else if (weatherText.contains("冻雨")) {
            return "FROZEN_RAIN";
        } else if (weatherText.contains("沙尘暴")) {
            return "DUST_STORM";
        } else if (weatherText.contains("强沙尘暴")) {
            return "DUST_STORM";
        } else if (weatherText.contains("轻雾")) {
            return "FOGGY";
        } else if (weatherText.contains("霾")) {
            return "HAZE";
        } else if (weatherText.contains("浮尘")) {
            return "DUST_STORM";
        } else if (weatherText.contains("扬沙")) {
            return "DUST_STORM";
        } else {
            return "CLOUDY";
        }
    }
}
