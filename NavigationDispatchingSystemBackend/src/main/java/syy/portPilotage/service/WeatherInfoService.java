package syy.portPilotage.service;

import syy.portPilotage.entity.WeatherInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;

public interface WeatherInfoService extends IService<WeatherInfo> {
    // 获取当前时间的天气信息
    WeatherInfo getCurrentWeatherInfo(Date date);
}
