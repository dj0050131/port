package syy.portPilotage.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import syy.portPilotage.entity.TideInfo;
import syy.portPilotage.entity.WeatherInfo;
import syy.portPilotage.enums.WeatherConditionEnum;
import syy.portPilotage.service.TideInfoService;
import syy.portPilotage.service.WeatherInfoService;
import syy.portPilotage.util.WeatherUtil;

import javax.net.ssl.*;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherSyncTask {

    private final WeatherInfoService weatherInfoService;
    private final TideInfoService tideInfoService;

    @Value("${weather.fetch.url:}")
    private String weatherFetchUrl;

    // 正则表达式模式定义
    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{2}:\\d{2}|\\d+日\\d{2}:\\d{2}");
    private static final Pattern TEMP_PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)℃");
    private static final Pattern WIND_SPEED_PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)m/s");
    private static final Pattern WIND_DIR_PATTERN = Pattern.compile("东风|南风|西风|北风|东北风|东南风|西北风|西南风");
    private static final Pattern HUMIDITY_PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)%");
    private static final Pattern RAIN_PATTERN = Pattern.compile("-|\\d+(\\.\\d+)?mm");
    private static final Pattern PRESSURE_PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)hPa");
    private static final Pattern VISIBILITY_PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)km");

//    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    @Scheduled(cron = "0 0 */3 * * ?") // 每3小时执行一次
    public void syncWeatherData() {
        log.info("开始执行气象数据同步任务");
        try {
            fetchAndSaveWeatherData();
            log.info("气象数据同步任务执行成功");
            parseAndSaveTideData();
            log.info("潮汐数据同步任务执行成功");
        } catch (Exception e) {
            log.error("气象数据同步任务执行失败: {}", e.getMessage(), e);
        }
    }

    private void fetchAndSaveWeatherData() throws Exception {
        if (weatherFetchUrl == null || weatherFetchUrl.isBlank()) {
            log.warn("未配置 weather.fetch.url，跳过气象数据同步");
            return;
        }
        trustAllCertificates();

        Document doc = Jsoup.connect(weatherFetchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get();

        parseAndSaveWeatherData(doc);
    }

    private void parseAndSaveWeatherData(Document doc) {
        Date fetchTime = new Date();

        String publishTimeText = doc.select("div.container div:contains(发布时间)").text();
        Date publishDate = parsePublishDate(publishTimeText, fetchTime);
        if (publishDate == null) {
            publishDate = fetchTime;
        }

        log.info("基准发布时间: {}, 当前时间: {}", publishDate, fetchTime);

        List<WeatherInfo> weatherInfoList = new ArrayList<>();

        for (int day = 0; day <= 6; day++) {
            String dayId = "day" + day;
            Element dayContainer = doc.getElementById(dayId);
            if (dayContainer == null) {
                log.warn("未找到元素: {}", dayId);
                continue;
            }

            Elements hourContainers = dayContainer.select(".hour3");
            if (hourContainers.isEmpty()) {
                log.warn("day {} 中没有找到小时数据", day);
                continue;
            }

            log.info("day {} 找到 {} 个小时数据", day, hourContainers.size());

            // 基准日期：发布时间加上天数偏移
            Calendar baseCalendar = Calendar.getInstance();
            baseCalendar.setTime(publishDate);
            baseCalendar.add(Calendar.DAY_OF_MONTH, day);
            Date baseDate = baseCalendar.getTime();

            Calendar cal = Calendar.getInstance();
            cal.setTime(baseDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            baseDate = cal.getTime();

            Date previousTime = null;

            for (Element hourContainer : hourContainers) {
                Elements divs = hourContainer.select("div");

                if (divs.size() < 9) {
                    log.warn("数据列数不足: {}", divs.size());
                    continue;
                }

                String firstDivText = divs.get(0).text().trim();

                Matcher timeMatcher = TIME_PATTERN.matcher(firstDivText);
                String time = timeMatcher.find() ? timeMatcher.group() : "";

                // 如果无法解析时间，跳过这条记录
                if (time.isEmpty()) {
                    log.warn("无法提取时间: {}", firstDivText);
                    continue;
                }

                Date weatherDate = parseDateTimeWithBaseDate(time, baseDate, previousTime);

                // 如果无法解析时间，跳过这条记录
                if (weatherDate == null) {
                    log.warn("时间解析失败: {}, baseDate: {}", time, baseDate);
                    continue;
                }

                previousTime = weatherDate;

                String tempText = divs.get(3).text().trim();
                Matcher tempMatcher = TEMP_PATTERN.matcher(tempText);
                String temp = tempMatcher.find() ? tempMatcher.group() : "";
                BigDecimal temperature = extractNumber(temp, TEMP_PATTERN);

                if (temperature == null) {
                    tempMatcher = TEMP_PATTERN.matcher(firstDivText);
                    temp = tempMatcher.find() ? tempMatcher.group() : "";
                    temperature = extractNumber(temp, TEMP_PATTERN);
                }

                String windSpeedText = divs.get(4).text().trim();
                Matcher windSpeedMatcher = WIND_SPEED_PATTERN.matcher(windSpeedText);
                String windSpeed = windSpeedMatcher.find() ? windSpeedMatcher.group() : "";
                BigDecimal windSpeedValue = extractNumber(windSpeed, WIND_SPEED_PATTERN);

                if (windSpeedValue == null) {
                    windSpeedMatcher = WIND_SPEED_PATTERN.matcher(firstDivText);
                    windSpeed = windSpeedMatcher.find() ? windSpeedMatcher.group() : "";
                    windSpeedValue = extractNumber(windSpeed, WIND_SPEED_PATTERN);
                }

                String windDirText = divs.get(5).text().trim();
                Matcher windDirMatcher = WIND_DIR_PATTERN.matcher(windDirText);
                String windDir = windDirMatcher.find() ? windDirMatcher.group() : "";

                if (windDir.isEmpty()) {
                    windDirMatcher = WIND_DIR_PATTERN.matcher(firstDivText);
                    windDir = windDirMatcher.find() ? windDirMatcher.group() : "";
                }

                String rain = "";
                if (divs.size() >= 3) {
                    rain = divs.get(2).text().trim();
                }
                if (rain.isEmpty() || "-".equals(rain)) {
                    Matcher rainMatcher = RAIN_PATTERN.matcher(firstDivText);
                    rain = rainMatcher.find() ? rainMatcher.group() : "-";
                }
                rain = rain.isEmpty() ? "-" : rain;

                String humidityText = divs.get(7).text().trim();
                Matcher humidityMatcher = HUMIDITY_PATTERN.matcher(humidityText);
                String humidity = humidityMatcher.find() ? humidityMatcher.group() : "";
                BigDecimal humidityValue = extractNumber(humidity, HUMIDITY_PATTERN);

                if (humidityValue == null) {
                    humidityMatcher = HUMIDITY_PATTERN.matcher(firstDivText);
                    humidity = humidityMatcher.find() ? humidityMatcher.group() : "";
                    humidityValue = extractNumber(humidity, HUMIDITY_PATTERN);
                }

                String pressureStr = "";
                if (divs.size() >= 7) {
                    pressureStr = divs.get(6).text().trim();
                }
                BigDecimal pressure = extractNumber(pressureStr, PRESSURE_PATTERN);

                if (pressure == null) {
                    Matcher pressureMatcher = PRESSURE_PATTERN.matcher(firstDivText);
                    pressureStr = pressureMatcher.find() ? pressureMatcher.group() : "";
                    pressure = extractNumber(pressureStr, PRESSURE_PATTERN);
                }

                String cloud = "";
                if (divs.size() >= 9) {
                    cloud = divs.get(8).text().trim();
                }

                String weatherCondition = extractWeatherCondition(hourContainer);

                WeatherInfo weatherInfo = new WeatherInfo();
                weatherInfo.setWeatherDate(weatherDate);
                weatherInfo.setWeatherContent(rain);
                weatherInfo.setTemperature(temperature);
                weatherInfo.setWindSpeed(windSpeedValue);
                weatherInfo.setWindDirection(windDir);
                weatherInfo.setPressure(pressure);
                weatherInfo.setHumidity(humidityValue);
                weatherInfo.setWeatherCondition(WeatherConditionEnum.fromDescription(weatherCondition));
                weatherInfo.setDataSource("国家气象中心");
                weatherInfo.setCreatedAt(new Date());
                weatherInfo.setUpdatedAt(new Date());

                WeatherInfo existingWeather = weatherInfoService.getCurrentWeatherInfo(weatherDate);
                if (existingWeather != null) {
                    // 如果存在，则更新
                    weatherInfo.setId(existingWeather.getId());
                    weatherInfo.setCreatedAt(existingWeather.getCreatedAt());
                }
                
                weatherInfoList.add(weatherInfo);
            }
        }
        
        if (!weatherInfoList.isEmpty()) {
            weatherInfoService.saveOrUpdateBatch(weatherInfoList);
            log.info("批量保存天气数据: 数量={}", weatherInfoList.size());
        }
    }

        private String extractWeatherCondition(Element hourContainer) {
        Element imgElement = hourContainer.select("div.hourimg img").first();
        if (imgElement != null) {
            String imgSrc = imgElement.attr("src");
            Pattern pattern = Pattern.compile("/(\\d+)\\.png$");
            Matcher matcher = pattern.matcher(imgSrc);
            if (matcher.find()) {
                String weatherCode = matcher.group(1);
                return WeatherUtil.mapWeatherCodeToEnum(weatherCode);
            }
        }

        // 在某些结构中，天气描述可能在特定的div中
        Element descElement = hourContainer.select("div.desc").first();
        if (descElement != null) {
            String weatherText = descElement.text().trim();
            return WeatherUtil.mapWeatherTextToEnum(weatherText);
        }

        // 如果都提取不到，返回默认值
        return "CLOUDY"; // 默认多云
    }

        private Date parsePublishDate(String publishTimeText, Date referenceDate) {
        try {
            Pattern pattern = Pattern.compile("\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{2}");
            Matcher matcher = pattern.matcher(publishTimeText);
            if (matcher.find()) {
                String timeStr = matcher.group();
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
                Date publishTime = sdf.parse(timeStr);

                // 设置年份为当前年份
                Calendar publishCal = Calendar.getInstance();
                publishCal.setTime(publishTime);

                Calendar refCal = Calendar.getInstance();
                refCal.setTime(referenceDate);

                publishCal.set(Calendar.YEAR, refCal.get(Calendar.YEAR));

                // 如果发布时间月份比当前月份小，说明是明年
                if (publishCal.get(Calendar.MONTH) < refCal.get(Calendar.MONTH)) {
                    publishCal.add(Calendar.YEAR, 1);
                }

                return publishCal.getTime();
            }
        } catch (ParseException e) {
            log.warn("发布时间解析失败: {}", publishTimeText);
        }
        return null;
    }

        private Date parseDateTimeWithBaseDate(String timeStr, Date baseDate, Date previousTime) {
        try {
            boolean containsDay = timeStr.contains("日");

            String extractedTime = extractPureTime(timeStr);
            if (extractedTime == null) {
                log.warn("无法提取纯时间: {}", timeStr);
                return null;
            }

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            Date time = timeFormat.parse(extractedTime);

            Calendar cal = Calendar.getInstance();
            cal.setTime(baseDate);

            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(time);
            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            Date result = cal.getTime();

            // 如果有前一个时间点，需要判断是否跨天
            if (previousTime != null) {
                // 如果当前时间小于前一个时间，说明跨天了，需要加一天
                if (result.before(previousTime) || containsDay) {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    result = cal.getTime();
                }
            }

            return result;
        } catch (ParseException e) {
            log.warn("时间解析失败: {}, baseDate: {}", timeStr, baseDate);
            return null;
        }
    }

        private String extractPureTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }

        // 如果包含"日"，提取"日"之后的部分
        if (timeStr.contains("日")) {
            String[] parts = timeStr.split("日");
            if (parts.length > 1) {
                timeStr = parts[1].trim();
            }
        }

        // 使用正则提取HH:mm格式的时间
        Pattern pattern = Pattern.compile("(\\d{1,2}):(\\d{2})");
        Matcher matcher = pattern.matcher(timeStr);
        if (matcher.find()) {
            String hour = matcher.group(1);
            String minute = matcher.group(2);

            // 确保两位数格式
            hour = hour.length() == 1 ? "0" + hour : hour;
            minute = minute.length() == 1 ? "0" + minute : minute;

            return hour + ":" + minute;
        }

        log.warn("无法从字符串中提取纯时间: {}", timeStr);
        return null;
    }

    private BigDecimal extractNumber(String text, Pattern pattern) {
        if (text == null || text.isEmpty() || "-".equals(text)) {
            return null;
        }

        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String numberStr = matcher.group(1);
            try {
                return new BigDecimal(numberStr);
            } catch (NumberFormatException e) {
                log.warn("数字解析失败: {}", numberStr);
                return null;
            }
        }
        return null;
    }
    private void parseAndSaveTideData() {
        try {
            Date fetchTime = new Date();
            List<Date> weatherDates = getWeatherDateRange(fetchTime);
            
            List<TideInfo> tideInfoList = new ArrayList<>();
            
            // 为每个天气日期生成24小时的潮汐数据
            for (Date weatherDate : weatherDates) {
                for (int hour = 0; hour < 24; hour++) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(weatherDate);
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    Date targetTime = cal.getTime();
                    
                    // 检查是否存在该小时的数据
                    TideInfo existingTide = tideInfoService.getHourlyTideInfo(
                            targetTime,
                            hour,
                            "温州"
                    );
                    
                    TideInfo tideInfo;
                    if (existingTide != null) {
                        // 如果存在，则更新
                        tideInfo = existingTide;
                        tideInfo.setTideDate(targetTime);
                        tideInfo.setRegion("温州");
                        tideInfo.setDataSource("小时级潮汐数据");
                        tideInfo.setUpdatedAt(new Date());
                    } else {
                        // 如果不存在，则创建新记录
                        tideInfo = new TideInfo();
                        tideInfo.setTideDate(targetTime);
                        tideInfo.setRegion("温州");
                        tideInfo.setDataSource("小时级潮汐数据");
                        tideInfo.setCreatedAt(new Date());
                        tideInfo.setUpdatedAt(new Date());
                    }
                    
                    setTideDataForHour(tideInfo, hour, targetTime);
                    tideInfoList.add(tideInfo);
                }
            }
            
            if (!tideInfoList.isEmpty()) {
                tideInfoService.saveOrUpdateBatch(tideInfoList);
                log.info("批量保存潮汐数据: 数量={}", tideInfoList.size());
            }
            
            log.info("潮汐数据同步完成");
        } catch (Exception e) {
            log.error("生成潮汐数据失败: {}", e.getMessage(), e);
        }
    }

        private void setTideDataForHour(TideInfo tideInfo, int hour, Date targetTime) {
        long daysSinceEpoch = targetTime.getTime() / (24 * 60 * 60 * 1000);
        if (daysSinceEpoch % 15 == 0 || daysSinceEpoch % 15 == 7) {
            tideInfo.setTideType("SPRING"); // 大潮
        } else if (daysSinceEpoch % 15 == 4 || daysSinceEpoch % 15 == 11) {
            tideInfo.setTideType("NEAP"); // 小潮
        } else {
            tideInfo.setTideType("MEDIUM"); // 中等潮
        }

        // 根据小时设置潮汐高度，这里假设每6小时一个潮汐周期
        BigDecimal tideHeight = generateTideHeightForHour(targetTime, hour);

        // 根据小时设置潮汐高度
        setTideHeightByHour(tideInfo, hour, tideHeight);
    }
    
        private void setTideHeightByHour(TideInfo tideInfo, int hour, BigDecimal tideHeight) {
        // 根据小时设置潮汐高度，这里假设每6小时一个潮汐周期
        int tidePeriod = hour % 6;

        long daysSinceEpoch = tideInfo.getTideDate().getTime() / (24 * 60 * 60 * 1000);
        if (daysSinceEpoch % 15 == 0 || daysSinceEpoch % 15 == 7) {
            tideInfo.setTideType("SPRING"); // 大潮
        } else if (daysSinceEpoch % 15 == 4 || daysSinceEpoch % 15 == 11) {
            tideInfo.setTideType("NEAP"); // 小潮
        } else {
            tideInfo.setTideType("MEDIUM"); // 中等潮
        }

        switch (tidePeriod) {
            case 0:
                tideInfo.setHighTideHeight1(tideHeight);
                tideInfo.setHighTideTime1(tideInfo.getTideDate());
                break;
            case 1:
                tideInfo.setHighTideHeight2(tideHeight);
                tideInfo.setHighTideTime2(tideInfo.getTideDate());
                break;
            case 2:
                tideInfo.setLowTideHeight1(tideHeight);
                tideInfo.setLowTideTime1(tideInfo.getTideDate());
                break;
            case 3:
                tideInfo.setLowTideHeight2(tideHeight);
                tideInfo.setLowTideTime2(tideInfo.getTideDate());
                break;
            default:
                // 对于其他小时，可以设置为中间值或者根据具体算法计算
                if (tideInfo.getHighTideHeight1() == null) {
                    tideInfo.setHighTideHeight1(tideHeight);
                    tideInfo.setHighTideTime1(tideInfo.getTideDate());
                } else if (tideInfo.getHighTideHeight2() == null) {
                    tideInfo.setHighTideHeight2(tideHeight);
                    tideInfo.setHighTideTime2(tideInfo.getTideDate());
                } else if (tideInfo.getLowTideHeight1() == null) {
                    tideInfo.setLowTideHeight1(tideHeight);
                    tideInfo.setLowTideTime1(tideInfo.getTideDate());
                } else {
                    tideInfo.setLowTideHeight2(tideHeight);
                    tideInfo.setLowTideTime2(tideInfo.getTideDate());
                }
                break;
        }
    }

    
    private BigDecimal generateTideHeightForHour(Date targetTime, int hourOffset) {
        // 获取当前时间作为参考点，当前时间对应第7个位置
        long currentTimeMillis = new Date().getTime();
        long targetTimeMillis = targetTime.getTime();

        long hourDiff = (targetTimeMillis - currentTimeMillis) / (1000 * 60 * 60);

        // 当前时间对应第7个位置，潮汐按正弦波形变化
        double tideCycle = 12.4; // 潮汐周期约为12.4小时
        double positionInCycle = (hourDiff + 7) % tideCycle; // +7表示当前时间是第7个位置

        // 使用正弦函数模拟潮汐变化
        // 潮汐范围在1.0到3.0之间
        double tideValue = 2.0 + 1.0 * Math.sin(2 * Math.PI * positionInCycle / tideCycle);

        // 确保值在合理范围内
        if (tideValue < 1.0) tideValue = 1.0;
        if (tideValue > 3.0) tideValue = 3.0;

        return new BigDecimal(tideValue).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private TideInfo generateTideDataForDate(Date date) {
        TideInfo tideInfo = new TideInfo();
        tideInfo.setTideDate(date);
        tideInfo.setRegion("温州");
        tideInfo.setDataSource("模拟数据生成");
        tideInfo.setCreatedAt(new Date());
        tideInfo.setUpdatedAt(new Date());

        long daysSinceEpoch = date.getTime() / (24 * 60 * 60 * 1000);
        if (daysSinceEpoch % 15 == 0 || daysSinceEpoch % 15 == 7) {
            tideInfo.setTideType("SPRING"); // 大潮
        } else if (daysSinceEpoch % 15 == 4 || daysSinceEpoch % 15 == 11) {
            tideInfo.setTideType("NEAP"); // 小潮
        } else {
            tideInfo.setTideType("MEDIUM"); // 中等潮
        }

        BigDecimal baseHighTide = BigDecimal.ZERO;
        BigDecimal baseLowTide = BigDecimal.ZERO;

        if ("SPRING".equals(tideInfo.getTideType())) {
            baseHighTide = new BigDecimal("3.5");
            baseLowTide = new BigDecimal("0.5");
        } else if ("NEAP".equals(tideInfo.getTideType())) {
            baseHighTide = new BigDecimal("2.5");
            baseLowTide = new BigDecimal("1.5");
        } else {
            baseHighTide = new BigDecimal("3.0");
            baseLowTide = new BigDecimal("1.0");
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // 第一次高潮：上午6点左右
        cal.set(Calendar.HOUR_OF_DAY, 6);
        cal.set(Calendar.MINUTE, (int)(Math.random() * 30)); // 随机分钟
        tideInfo.setHighTideTime1(cal.getTime());
        tideInfo.setHighTideHeight1(baseHighTide.add(new BigDecimal(Math.random() * 0.3 - 0.15)).setScale(2, BigDecimal.ROUND_HALF_UP));

        // 第一次低潮：上午12点左右
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, (int)(Math.random() * 30)); // 随机分钟
        tideInfo.setLowTideTime1(cal.getTime());
        tideInfo.setLowTideHeight1(baseLowTide.add(new BigDecimal(Math.random() * 0.2 - 0.1)).setScale(2, BigDecimal.ROUND_HALF_UP));

        // 第二次高潮：下午6点左右
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, (int)(Math.random() * 30)); // 随机分钟
        tideInfo.setHighTideTime2(cal.getTime());
        tideInfo.setHighTideHeight2(baseHighTide.add(new BigDecimal(Math.random() * 0.3 - 0.15)).setScale(2, BigDecimal.ROUND_HALF_UP));

        // 第二次低潮：晚上12点左右
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, (int)(Math.random() * 30)); // 随机分钟
        cal.add(Calendar.DAY_OF_MONTH, 1); // 跨天
        tideInfo.setLowTideTime2(cal.getTime());
        tideInfo.setLowTideHeight2(baseLowTide.add(new BigDecimal(Math.random() * 0.2 - 0.1)).setScale(2, BigDecimal.ROUND_HALF_UP));

        return tideInfo;
    }
    
    private void setTideDataForDate(TideInfo tideInfo, Date date) {
        long daysSinceEpoch = date.getTime() / (24 * 60 * 60 * 1000);
        if (daysSinceEpoch % 15 == 0 || daysSinceEpoch % 15 == 7) {
            tideInfo.setTideType("SPRING"); // 大潮
        } else if (daysSinceEpoch % 15 == 4 || daysSinceEpoch % 15 == 11) {
            tideInfo.setTideType("NEAP"); // 小潮
        } else {
            tideInfo.setTideType("MEDIUM"); // 中等潮
        }

        BigDecimal baseHighTide = BigDecimal.ZERO;
        BigDecimal baseLowTide = BigDecimal.ZERO;

        if ("SPRING".equals(tideInfo.getTideType())) {
            baseHighTide = new BigDecimal("3.5");
            baseLowTide = new BigDecimal("0.5");
        } else if ("NEAP".equals(tideInfo.getTideType())) {
            baseHighTide = new BigDecimal("2.5");
            baseLowTide = new BigDecimal("1.5");
        } else {
            baseHighTide = new BigDecimal("3.0");
            baseLowTide = new BigDecimal("1.0");
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // 第一次高潮：上午6点左右
        cal.set(Calendar.HOUR_OF_DAY, 6);
        cal.set(Calendar.MINUTE, (int)(Math.random() * 30)); // 随机分钟
        tideInfo.setHighTideTime1(cal.getTime());
        tideInfo.setHighTideHeight1(baseHighTide.add(new BigDecimal(Math.random() * 0.3 - 0.15)).setScale(2, BigDecimal.ROUND_HALF_UP));

        // 第一次低潮：上午12点左右
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, (int)(Math.random() * 30)); // 随机分钟
        tideInfo.setLowTideTime1(cal.getTime());
        tideInfo.setLowTideHeight1(baseLowTide.add(new BigDecimal(Math.random() * 0.2 - 0.1)).setScale(2, BigDecimal.ROUND_HALF_UP));

        // 第二次高潮：下午6点左右
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, (int)(Math.random() * 30)); // 随机分钟
        tideInfo.setHighTideTime2(cal.getTime());
        tideInfo.setHighTideHeight2(baseHighTide.add(new BigDecimal(Math.random() * 0.3 - 0.15)).setScale(2, BigDecimal.ROUND_HALF_UP));

        // 第二次低潮：晚上12点左右
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, (int)(Math.random() * 30)); // 随机分钟
        cal.add(Calendar.DAY_OF_MONTH, 1); // 跨天
        tideInfo.setLowTideTime2(cal.getTime());
        tideInfo.setLowTideHeight2(baseLowTide.add(new BigDecimal(Math.random() * 0.2 - 0.1)).setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    private BigDecimal calculateTideHeightForTime(Date time, TideInfo baseTideInfo) {
        // 使用正弦函数模拟潮汐周期变化
        long timeInMillis = time.getTime();

        // 参考第一次高潮的时间和高度
        Date highTideTime = baseTideInfo.getHighTideTime1();
        BigDecimal highTideHeight = baseTideInfo.getHighTideHeight1();
        BigDecimal lowTideHeight = baseTideInfo.getLowTideHeight1();

        if (highTideTime == null || highTideHeight == null || lowTideHeight == null) {
            // 如果基准数据不完整，使用默认值
            highTideHeight = new BigDecimal("3.0");
            lowTideHeight = new BigDecimal("1.0");
            highTideTime = time;
        }

        double timeDiffHours = (timeInMillis - highTideTime.getTime()) / (1000.0 * 60 * 60);
        // 潮汐周期约为12.4小时
        double tideCycle = 12.4;

        BigDecimal avgTide = highTideHeight.add(lowTideHeight).divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal amplitude = highTideHeight.subtract(avgTide);

        // 使用正弦函数计算当前潮位
        double tideValue = avgTide.doubleValue() +
                amplitude.doubleValue() * Math.sin(2 * Math.PI * timeDiffHours / tideCycle);

        // 确保结果在合理范围内
        BigDecimal result = new BigDecimal(tideValue);
        if (result.compareTo(lowTideHeight) < 0) {
            result = lowTideHeight;
        }
        if (result.compareTo(highTideHeight) > 0) {
            result = highTideHeight;
        }

        return result.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    private Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    private Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    private static void trustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        HostnameVerifier allHostsValid = (hostname, session) -> true;

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
    
    private Date parseTime(String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date date = sdf.parse(timeStr);
            return date;
        } catch (ParseException e) {
            log.warn("时间解析失败: {}", timeStr);
            return null;
        }
    }
    
    private List<Date> getWeatherDateRange(Date fetchTime) {
        List<Date> weatherDates = new ArrayList<>();

        if (weatherFetchUrl == null || weatherFetchUrl.isBlank()) {
            return weatherDates;
        }

        try {
            trustAllCertificates();

            Document doc = Jsoup.connect(weatherFetchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            String publishTimeText = doc.select("div.container div:contains(发布时间)").text();
            Date publishDate = parsePublishDate(publishTimeText, fetchTime);
            if (publishDate == null) {
                publishDate = fetchTime;
            }
            
            for (int day = 0; day <= 6; day++) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(publishDate);
                cal.add(Calendar.DAY_OF_MONTH, day);
                
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                
                weatherDates.add(cal.getTime());
            }
        } catch (Exception e) {
            log.error("获取天气日期范围失败: {}", e.getMessage(), e);
            // 如果无法获取网络数据，则使用默认的未来7天
            for (int day = 0; day <= 6; day++) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(fetchTime);
                cal.add(Calendar.DAY_OF_MONTH, day);
                
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                
                weatherDates.add(cal.getTime());
            }
        }
        
        return weatherDates;
    }
}
