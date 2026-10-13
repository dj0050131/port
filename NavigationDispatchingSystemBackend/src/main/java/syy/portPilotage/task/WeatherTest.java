package syy.portPilotage.task;

import com.alibaba.fastjson.JSON;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import syy.portPilotage.entity.WeatherInfo;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class WeatherTest {
    // 正则表达式模式定义
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{2}:\\d{2}");
    private static final Pattern TEMP_PATTERN = Pattern.compile("\\d+(\\.\\d+)?℃");
    private static final Pattern WIND_SPEED_PATTERN = Pattern.compile("\\d+(\\.\\d+)?m/s");
    private static final Pattern WIND_DIR_PATTERN = Pattern.compile("东风|南风|西风|北风|东北风|东南风|西北风|西南风");
    private static final Pattern HUMIDITY_PATTERN = Pattern.compile("\\d+(\\.\\d+)?%");
    private static final Pattern RAIN_PATTERN = Pattern.compile("-|\\d+(\\.\\d+)?mm");
    private static final Pattern DAY_PATTERN = Pattern.compile("(\\d{1,2})/(\\d{1,2})");

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException, IOException, ParseException {
        System.out.println("开始爬取苏州气象数据...");

        // 注意：由于main方法无法自动注入，这里需要手动创建Spring上下文或直接使用Mapper

        Map<String, Object> weatherData = fetchWeatherData();

        System.out.println("===== 解析到的气象数据 =====");
        System.out.println(JSON.toJSONString(weatherData, true));

        System.out.println("\n===== 数据库存储分析 =====");
        // 因为Spring的依赖注入在main方法中不生效
        analyzeAndStoreDataStandalone(weatherData);

        System.out.println("\n===== 完成 =====");
    }

    private static Map<String, Object> fetchWeatherData() throws NoSuchAlgorithmException, KeyManagementException, IOException {
        Map<String, Object> result = new HashMap<>();
        String url = System.getenv("WEATHER_FETCH_URL");
        if (url == null || url.isBlank()) {
            System.out.println("请设置环境变量 WEATHER_FETCH_URL");
            return result;
        }
        trustAllCertificates();

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(10000)
                .get();

        Map<String, Object> weatherData = parseWeatherData(doc);

        Map<String, Object> otherInfo = parseOtherWeatherInfo(doc);

        result.put("forecast7Days", weatherData);
        result.put("otherInfo", otherInfo);
        result.put("city", "苏州");
        result.put("province", "江苏省");
        result.put("dataSource", "weather");
        result.put("fetchTime", new Date());

        return result;
    }
        private static Map<String, Object> parseWeatherData(Document doc) {
        List<Map<String, Object>> allDaysRawData = new ArrayList<>();
        List<Map<String, Object>> sevenDayForecast = new ArrayList<>();

        Elements dayForecasts = doc.select(".7days .weather");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        for (int i = 0; i < dayForecasts.size(); i++) {
            Element dayElement = dayForecasts.get(i);
            Map<String, Object> dayForecast = new HashMap<>();

            String dateText = dayElement.select(".date").text();
            String[] dateParts = dateText.split("\n");
            String monthDay = dateParts[0].trim(); // 如 "12/24"

            dayForecast.put("date", monthDay);
            dayForecast.put("week", dateParts.length > 1 ? dateParts[1].trim() : "");
            dayForecast.put("dayWeather", dayElement.select(".desc").first().text());
            dayForecast.put("nightWeather", dayElement.select(".desc").last().text());
            dayForecast.put("highTemp", dayElement.select(".tmp").first().text());
            dayForecast.put("lowTemp", dayElement.select(".tmp").last().text());
            dayForecast.put("dayWind", dayElement.select(".windd").first().text());
            dayForecast.put("nightWind", dayElement.select(".windd").last().text());

            sevenDayForecast.add(dayForecast);
        }

        for (int day = 0; day <= 6; day++) {
            String dayId = "day" + day;
            Element dayContainer = doc.getElementById(dayId);
            if (dayContainer == null) continue;

            List<Map<String, String>> hourDataList = new ArrayList<>();
            Elements hourContainers = dayContainer.select(".hour3.hbg");

            for (Element hourContainer : hourContainers) {
                Map<String, String> hourData = new HashMap<>();
                Elements divs = hourContainer.select("div");

                if (divs.size() < 9) continue;

                // 时间
                String timeText = divs.get(0).text().trim();
                hourData.put("time", timeText);

                // 天气图标（可推断天气状况）
                String imgSrc = hourContainer.select("img").attr("src");
                hourData.put("weatherIcon", imgSrc);

                // 降水量
                String rain = divs.get(2).text().trim();
                hourData.put("rain", rain.isEmpty() ? "-" : rain);

                // 温度
                String temp = divs.get(3).text().trim();
                hourData.put("temp", temp);

                // 风速
                String windSpeed = divs.get(4).text().trim();
                hourData.put("windSpeed", windSpeed);

                // 风向
                String windDir = divs.get(5).text().trim();
                hourData.put("windDir", windDir);

                String pressure = divs.get(6).text().trim();
                hourData.put("pressure", pressure);

                // 湿度
                String humidity = divs.get(7).text().trim();
                hourData.put("humidity", humidity);

                String cloud = divs.get(8).text().trim();
                hourData.put("cloud", cloud);

                hourDataList.add(hourData);
            }

            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("day", dayId);
            dayMap.put("hourData", hourDataList);
            allDaysRawData.add(dayMap);
        }

        Map<String, String> realTimeData = new HashMap<>();
        Element realTempElement = doc.getElementById("realTemperature");
        if (realTempElement != null) {
            realTimeData.put("temperature", realTempElement.text());
        }

        Map<String, List<Map<String, String>>> formattedData = new HashMap<>();
        for (Map<String, Object> dayMap : allDaysRawData) {
            String dayKey = (String) dayMap.get("day");
            List<Map<String, String>> hourData = (List<Map<String, String>>) dayMap.get("hourData");
            formattedData.put(dayKey, hourData);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sevenDayForecast", sevenDayForecast);
        resultMap.put("hourlyForecast", formattedData);
        resultMap.put("realTimeData", realTimeData);
        return resultMap;
    }

        private static Map<String, Object> parseOtherWeatherInfo(Document doc) {
        Map<String, Object> otherInfo = new HashMap<>();

        Element radarElement = doc.getElementById("radarImage");
        if (radarElement != null) {
            otherInfo.put("radarUrl", radarElement.attr("href"));
            otherInfo.put("radarImage", radarElement.select("img").attr("src"));
        }

        Elements publishElements = doc.select("div:containsOwn(发布时间：)");
        if (!publishElements.isEmpty()) {
            otherInfo.put("publishTime", publishElements.text().replace("发布时间：", "").trim());
        }

        Element publishTimeElement = doc.getElementById("realPublishTime");
        if (publishTimeElement != null) {
            otherInfo.put("realPublishTime", publishTimeElement.text());
        }

        return otherInfo;
    }
    private static void trustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        HostnameVerifier allHostsValid = (hostname, session) -> true;

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
    private static void analyzeAndStoreDataStandalone(Map<String, Object> weatherData) throws ParseException {
        System.out.println("1. WeatherInfo 表可存储的数据:");

        Map<String, Object> forecastData = (Map<String, Object>) weatherData.get("forecast7Days");
        if (forecastData == null) {
            System.out.println("错误: forecast7Days 数据为 null");
            return;
        }
        Map<String, List<Map<String, String>>> formattedData = (Map<String, List<Map<String, String>>>) forecastData.get("formattedData");
        if (formattedData == null) {
            System.out.println("错误: formattedData 数据为 null");
            return;
        }

        int totalRecords = 0;

        for (Map.Entry<String, List<Map<String, String>>> entry : formattedData.entrySet()) {
            String dayKey = entry.getKey();
            List<Map<String, String>> hourDataList = entry.getValue();

            if (hourDataList == null) {
                continue;
            }

            for (Map<String, String> hourData : hourDataList) {
                if (hourData == null) {
                    continue;
                }

                totalRecords++;

                WeatherInfo weatherInfo = new WeatherInfo();

                String time = hourData.get("time");
                String temp = hourData.get("temp");
                String windSpeed = hourData.get("windSpeed");
                String windDir = hourData.get("windDir");
                String humidity = hourData.get("humidity");
                String rain = hourData.get("rain");

                System.out.println("记录 " + totalRecords + " (" + (dayKey != null ? dayKey : "未知") + " " + (time != null ? time : "未知") + "):");
                System.out.println("  - 温度: " + (temp != null ? temp : "无") + " -> temperature字段");
                System.out.println("  - 湿度: " + (humidity != null ? humidity : "无") + " -> humidity字段");
                System.out.println("  - 风速: " + (windSpeed != null ? windSpeed : "无") + " -> windSpeed字段");
                System.out.println("  - 风向: " + (windDir != null ? windDir : "无") + " -> windDirection字段");
                System.out.println("  - 降水: " + (rain != null ? rain : "无") + " -> weatherContent字段");

                System.out.println("  - 【注意】以下信息在WeatherInfo表中没有对应字段：");
                System.out.println("    * 天气现象（晴/多云/小雨等）");
                System.out.println("    * 雷达图URL");
                System.out.println("    * 日出日落时间");
                System.out.println("    * 体感温度");
                System.out.println("    * AQI空气质量");
                System.out.println("    * 舒适度指数");
            }
        }

        System.out.println("\n总共可存储 " + totalRecords + " 条逐小时记录到WeatherInfo表");

        System.out.println("\n2. 7天天气预报信息：");
        // 这些信息在WeatherInfo表中也没有直接对应字段
        System.out.println("【注意】7天预报的以下信息需要额外处理：");
        System.out.println("  * 白天/夜间天气状况（晴/多云/小雨等）");
        System.out.println("  * 白天/夜间温度范围（如8℃~4℃）");
        System.out.println("  * 白天/夜间风向风力");
        System.out.println("建议：创建专门的7天预报表或扩展WeatherInfo表");

        System.out.println("\n3. 其他气象信息：");
        Map<String, Object> otherInfo = (Map<String, Object>) weatherData.get("otherInfo");
        System.out.println("【注意】以下信息无法存入现有表结构：");
        System.out.println("  * 雷达图信息");
        System.out.println("  * 相关产品推荐（天气公报、环境公报等）");
        System.out.println("  * 网站菜单导航信息");

        System.out.println("\n4. TideInfo 表分析：");
        System.out.println("【注意】当前网页没有潮汐信息，TideInfo表无法填充");
        System.out.println("建议：从其他数据源获取潮汐信息，如国家海洋局网站");

        System.out.println("\n5. 建议的表结构改进：");
        System.out.println("  1. 创建DailyForecast表存储7天预报");
        System.out.println("  2. 创建WeatherDetail表存储实况数据");
        System.out.println("  3. 在WeatherInfo表中添加字段：");
        System.out.println("     - weather_phenomenon 天气现象");
        System.out.println("     - feels_like 体感温度");
        System.out.println("     - sunrise_time 日出时间");
        System.out.println("     - sunset_time 日落时间");
        System.out.println("     - aqi 空气质量指数");
    }
}