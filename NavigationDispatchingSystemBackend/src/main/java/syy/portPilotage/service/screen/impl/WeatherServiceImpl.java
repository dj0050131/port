package syy.portPilotage.service.screen.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syy.portPilotage.common.Result;
import syy.portPilotage.entity.TideInfo;
import syy.portPilotage.entity.WeatherInfo;
import syy.portPilotage.service.TideInfoService;
import syy.portPilotage.service.WeatherInfoService;
import syy.portPilotage.service.screen.WeatherService;
import syy.portPilotage.vo.screen.WeatherTideVO;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class WeatherServiceImpl implements WeatherService {

    @Autowired
    private WeatherInfoService weatherInfoService;
    @Autowired
    private TideInfoService tideInfoService;

    @Override
    public Result<WeatherTideVO> getWeatherAndTide() {
        WeatherTideVO weatherTide = new WeatherTideVO();

        List<WeatherInfo> weatherInfos = weatherInfoService.list(
                new LambdaQueryWrapper<WeatherInfo>()
                        .orderByDesc(WeatherInfo::getCreatedAt)
                        .last("LIMIT 1")
        );
        if (!weatherInfos.isEmpty()) {
            WeatherInfo latestWeather = weatherInfos.get(0);

            // 天气信息
            WeatherTideVO.WeatherInfo weather = new WeatherTideVO.WeatherInfo();
            weather.setTemperature(latestWeather.getTemperature());
            weather.setHumidity(latestWeather.getHumidity());
            weather.setVisibility(latestWeather.getVisibility());
            weather.setWindSpeed(latestWeather.getWindSpeed());
            weather.setWindDirection(latestWeather.getWindDirection());
            weather.setCondition(latestWeather.getWeatherCondition().getDescription());
            weather.setPressure(latestWeather.getPressure());
            weatherTide.setWeather(weather);
        }

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        Date today = todayCal.getTime();
        
        Calendar tomorrowCal = Calendar.getInstance();
        tomorrowCal.setTime(today);
        tomorrowCal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = tomorrowCal.getTime();
        
        List<TideInfo> tideInfos = tideInfoService.list(
                new LambdaQueryWrapper<TideInfo>()
                        .ge(TideInfo::getTideDate, today)  // 从今天开始
                        .lt(TideInfo::getTideDate, tomorrow)  // 到明天之前
                        .orderByDesc(TideInfo::getTideDate)
                        .last("LIMIT 1")
        );
        if (!tideInfos.isEmpty()) {
            TideInfo todayTide = tideInfos.get(0);

            // 潮汐信息
            WeatherTideVO.TideInfo tide = new WeatherTideVO.TideInfo();

            // 计算当前潮高 - 基于当前时间判断是高潮还是低潮
            tide.setCurrentHeight(calculateCurrentTideHeight(todayTide));

            tide.setNextHighTide(formatTideTime(todayTide.getTideDate(), todayTide.getHighTideTime1()));
            tide.setNextLowTide(formatTideTime(todayTide.getTideDate(), todayTide.getLowTideTime1()));

            tide.setSurgeWarning(todayTide.getHighTideHeight1() != null &&
                    todayTide.getHighTideHeight1().compareTo(new BigDecimal("4.0")) > 0);

            weatherTide.setTide(tide);
        }

        // 预报信息（生成未来24小时每小时的预报数据）
        List<WeatherTideVO.ForecastInfo> forecast = generateForecastData();
        weatherTide.setForecast(forecast);

        return Result.success(weatherTide);
    }

    private String formatTideTime(Date tideDate, Date tideTime) {
        if (tideTime == null) {
            return "";
        }

        // 如果tideTime只有时间部分（如数据库中的time类型），需要结合tideDate
        if (tideDate != null) {
            // 将tideDate的日期部分与tideTime的时间部分结合
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(tideDate);

            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime(tideTime);

            Calendar resultCal = Calendar.getInstance();
            resultCal.set(Calendar.YEAR, dateCal.get(Calendar.YEAR));
            resultCal.set(Calendar.MONTH, dateCal.get(Calendar.MONTH));
            resultCal.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
            resultCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            resultCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            resultCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
            resultCal.set(Calendar.MILLISECOND, timeCal.get(Calendar.MILLISECOND));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.format(resultCal.getTime());
        } else {
            // 如果没有tideDate，只返回时间部分
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return sdf.format(tideTime);
        }
    }

    // 计算当前潮高
    private BigDecimal calculateCurrentTideHeight(TideInfo tideInfo) {
        // 使用当前时间计算实时潮高
        Date currentTime = new Date();
        return calculateTideValueByTime(currentTime);
    }

    private List<WeatherTideVO.ForecastInfo> generateForecastData() {
        List<WeatherTideVO.ForecastInfo> forecast = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date today = cal.getTime();

        List<WeatherInfo> allWeatherData = weatherInfoService.list(
                new LambdaQueryWrapper<WeatherInfo>()
                        .ge(WeatherInfo::getWeatherDate, today)  // 从今天开始
                        .orderByAsc(WeatherInfo::getWeatherDate)
        );

        List<TideInfo> allTideData = tideInfoService.list(
                new LambdaQueryWrapper<TideInfo>()
                        .ge(TideInfo::getTideDate, today)  // 从今天开始
                        .orderByAsc(TideInfo::getTideDate)
        );

        // 按日期分组天气数据
        java.util.Map<String, List<WeatherInfo>> weatherByDate = new java.util.HashMap<>();
        for (WeatherInfo weather : allWeatherData) {
            if (weather.getWeatherDate() != null) {
                String dateKey = formatDate(weather.getWeatherDate());
                weatherByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(weather);
            }
        }

        // 按日期分组潮汐数据
        java.util.Map<String, List<TideInfo>> tideByDate = new java.util.HashMap<>();
        for (TideInfo tide : allTideData) {
            if (tide.getTideDate() != null) {
                String dateKey = formatDate(tide.getTideDate());
                tideByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(tide);
            }
        }

        for (int day = 0; day < 7; day++) {
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_MONTH, day);
            Date targetDate = cal.getTime();
            String dateKey = formatDate(targetDate);

            // 获取当天的天气数据
            List<WeatherInfo> dailyWeather = weatherByDate.get(dateKey);
            // 获取当天的潮汐数据
            List<TideInfo> dailyTide = tideByDate.get(dateKey);

            // 如果有天气数据，为每个天气时间点生成预报
            if (dailyWeather != null && !dailyWeather.isEmpty()) {
                // 按时间排序天气数据
                dailyWeather.sort((w1, w2) -> w1.getWeatherDate().compareTo(w2.getWeatherDate()));
                
                // 确保每天都有24小时的数据
                Calendar dayStart = Calendar.getInstance();
                dayStart.setTime(targetDate);
                dayStart.set(Calendar.HOUR_OF_DAY, 0);
                dayStart.set(Calendar.MINUTE, 0);
                dayStart.set(Calendar.SECOND, 0);
                dayStart.set(Calendar.MILLISECOND, 0);
                Date dayStartTime = dayStart.getTime();
                
                Calendar dayEnd = Calendar.getInstance();
                dayEnd.setTime(targetDate);
                dayEnd.add(Calendar.DAY_OF_MONTH, 1); // 下一天的0点
                dayEnd.set(Calendar.HOUR_OF_DAY, 0);
                dayEnd.set(Calendar.MINUTE, 0);
                dayEnd.set(Calendar.SECOND, 0);
                dayEnd.set(Calendar.MILLISECOND, 0);
                Date dayEndTime = dayEnd.getTime();
                
                java.util.Map<Date, WeatherInfo> hourWeatherMap = new java.util.HashMap<>();
                
                // 为每个小时分配天气数据
                int currentHourIndex = 0;
                for (int hour = 0; hour < 24; hour++) {
                    Calendar hourCal = Calendar.getInstance();
                    hourCal.setTime(dayStartTime);
                    hourCal.add(Calendar.HOUR_OF_DAY, hour);
                    Date hourTime = hourCal.getTime();
                    
                    // 找到当前小时应该使用的天气数据
                    WeatherInfo currentWeather = null;
                    
                    // 从当前小时索引开始查找合适的天气数据
                    while (currentHourIndex < dailyWeather.size()) {
                        WeatherInfo weather = dailyWeather.get(currentHourIndex);
                        
                        // 如果当前天气时间小于等于当前小时，使用该天气数据
                        if (weather.getWeatherDate().getTime() <= hourTime.getTime()) {
                            currentWeather = weather;
                            
                            if (currentHourIndex + 1 < dailyWeather.size()) {
                                WeatherInfo nextWeather = dailyWeather.get(currentHourIndex + 1);
                                // 如果下一个天气时间小于等于当前小时，继续前进
                                if (nextWeather.getWeatherDate().getTime() <= hourTime.getTime()) {
                                    currentHourIndex++;
                                    continue; // 继续循环，继续查找
                                }
                            }
                            
                            break; // 找到合适的天气数据，跳出循环
                        } else {
                            // 当前天气时间大于当前小时，使用前一个天气数据
                            if (currentHourIndex > 0) {
                                currentWeather = dailyWeather.get(currentHourIndex - 1);
                            } else {
                                // 如果是第一个天气数据但时间还早，使用第一个
                                currentWeather = dailyWeather.get(0);
                            }
                            break;
                        }
                    }
                    
                    // 如果没有找到天气数据，使用当天的第一个
                    if (currentWeather == null && !dailyWeather.isEmpty()) {
                        currentWeather = dailyWeather.get(0);
                    }
                    
                    hourWeatherMap.put(hourTime, currentWeather);
                }
                
                // 为每天的24小时生成预报数据
                for (int hour = 0; hour < 24; hour++) {
                    Calendar hourCal = Calendar.getInstance();
                    hourCal.setTime(dayStartTime);
                    hourCal.add(Calendar.HOUR_OF_DAY, hour);
                    Date hourTime = hourCal.getTime();
                    
                    WeatherInfo currentWeather = hourWeatherMap.get(hourTime);
                    
                    WeatherTideVO.ForecastInfo forecastItem = new WeatherTideVO.ForecastInfo();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    forecastItem.setTime(sdf.format(hourTime));

                    if (currentWeather != null) {
                        // 使用当前天气数据的信息
                        forecastItem.setTemperature(currentWeather.getTemperature());
                        forecastItem.setCondition(currentWeather.getWeatherContent() != null ? 
                                currentWeather.getWeatherContent() : 
                                (currentWeather.getWeatherCondition() != null ? 
                                        currentWeather.getWeatherCondition().name() : "晴"));
                        forecastItem.setWind(currentWeather.getWindDirection() != null ? 
                                currentWeather.getWindDirection() : "北风");
                        forecastItem.setWindLevel(currentWeather.getWindSpeed() != null ? 
                                currentWeather.getWindSpeed().intValue() : 2);
                    } else {
                        // 如果没有天气数据，使用默认值
                        forecastItem.setTemperature(BigDecimal.ZERO);
                        forecastItem.setCondition("晴");
                        forecastItem.setWind("北风");
                        forecastItem.setWindLevel(2);
                    }

                    TideInfo closestTide = findClosestTide(hourTime, dailyTide);
                    BigDecimal tideValue = calculateTideValueByTime(hourTime);
                    forecastItem.setTide(tideValue);

                    forecast.add(forecastItem);
                }
            } else {
                // 如果没有天气数据，生成当天24小时的默认数据
                for (int hour = 0; hour < 24; hour++) {
                    Calendar hourCal = Calendar.getInstance();
                    hourCal.setTime(targetDate);
                    hourCal.add(Calendar.HOUR_OF_DAY, hour);

                    Date hourTime = hourCal.getTime();
                    WeatherTideVO.ForecastInfo forecastItem = new WeatherTideVO.ForecastInfo();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    forecastItem.setTime(sdf.format(hourTime));

                    forecastItem.setTemperature(BigDecimal.ZERO);
                    forecastItem.setCondition("晴");
                    forecastItem.setWind("北风");
                    forecastItem.setWindLevel(2);

                    TideInfo closestTide = findClosestTide(hourTime, dailyTide);
                    BigDecimal tideValue = calculateTideValueByTime(hourTime);
                    forecastItem.setTide(tideValue);

                    forecast.add(forecastItem);
                }
            }
        }

        return forecast;
    }

    private TideInfo findClosestTide(Date targetTime, List<TideInfo> tideList) {
        if (tideList == null || tideList.isEmpty()) {
            return null;
        }

        TideInfo closestTide = tideList.get(0);
        long minDiff = Math.abs(targetTime.getTime() - closestTide.getTideDate().getTime());

        for (TideInfo tide : tideList) {
            long diff = Math.abs(targetTime.getTime() - tide.getTideDate().getTime());
            if (diff < minDiff) {
                minDiff = diff;
                closestTide = tide;
            }
        }

        return closestTide;
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    private BigDecimal calculateTideValueByTime(Date time) {
        List<TideInfo> tideInfos = tideInfoService.list(
                new LambdaQueryWrapper<TideInfo>()
                        .orderByDesc(TideInfo::getCreatedAt)
                        .last("LIMIT 1")
        );

        if (tideInfos.isEmpty()) {
            // 如果没有基准数据，返回一个合理的默认值
            return new BigDecimal(1.5 + Math.random() * 2.0);
        }

        TideInfo baseTide = tideInfos.get(0);

        // 使用正弦函数模拟潮汐周期变化（简化模型）
        // 潮汐周期约为12小时25分钟，这里简化为12小时周期
        long currentTimeMillis = time.getTime();
        long highTideTime = baseTide.getHighTideTime1() != null ?
                baseTide.getHighTideTime1().getTime() : System.currentTimeMillis();

        double timeDiffHours = (currentTimeMillis - highTideTime) / (1000.0 * 60 * 60);
        // 潮汐周期约为12.4小时
        double tideCycle = 12.4;

        // 使用正弦函数计算潮汐高度
        BigDecimal maxTideHeight = baseTide.getHighTideHeight1() != null ?
                baseTide.getHighTideHeight1() : new BigDecimal("2.5");
        BigDecimal minTideHeight = baseTide.getLowTideHeight1() != null ?
                baseTide.getLowTideHeight1() : maxTideHeight.multiply(new BigDecimal("0.5"));

        BigDecimal avgTide = maxTideHeight.add(minTideHeight).divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal amplitude = maxTideHeight.subtract(avgTide);

        // 正弦函数计算当前潮位
        double tideValue = avgTide.doubleValue() +
                amplitude.doubleValue() * Math.sin(2 * Math.PI * timeDiffHours / tideCycle);

        // 确保结果在合理范围内
        BigDecimal result = new BigDecimal(tideValue);
        if (result.compareTo(minTideHeight) < 0) {
            result = minTideHeight;
        }
        if (result.compareTo(maxTideHeight) > 0) {
            result = maxTideHeight;
        }

        return result.setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal getTideValueForDate(Date weatherDate, List<TideInfo> tideInfos) {
        for (TideInfo tide : tideInfos) {
            if (tide.getTideDate() != null && weatherDate != null) {
                LocalDate weatherLocalDate = Instant.ofEpochMilli(weatherDate.getTime())
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                LocalDate tideLocalDate = Instant.ofEpochMilli(tide.getTideDate().getTime())
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

                if (weatherLocalDate.equals(tideLocalDate)) {
                    if (tide.getHighTideHeight1() != null && tide.getLowTideHeight1() != null) {
                        return tide.getHighTideHeight1().add(tide.getLowTideHeight1())
                                .divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
                    } else if (tide.getHighTideHeight1() != null) {
                        return tide.getHighTideHeight1();
                    } else if (tide.getLowTideHeight1() != null) {
                        return tide.getLowTideHeight1();
                    }
                }
            }
        }

        // 如果没有找到匹配的潮汐数据，返回随机值或默认值
        return new BigDecimal(1.5 + Math.random() * 2.0); // 1.5-3.5之间的随机值
    }
}