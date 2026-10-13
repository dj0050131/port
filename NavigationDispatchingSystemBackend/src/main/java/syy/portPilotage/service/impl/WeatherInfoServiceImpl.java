package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import syy.portPilotage.entity.WeatherInfo;
import syy.portPilotage.service.WeatherInfoService;
import syy.portPilotage.mapper.WeatherInfoMapper;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class WeatherInfoServiceImpl extends ServiceImpl<WeatherInfoMapper, WeatherInfo>
    implements WeatherInfoService{
    @Override
    public WeatherInfo getCurrentWeatherInfo(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfHour = cal.getTime();

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(startOfHour);
        calEnd.add(Calendar.HOUR_OF_DAY, 1);
        Date endOfHour = calEnd.getTime();

        return this.getOne(
                new LambdaQueryWrapper<WeatherInfo>()
                        .ge(WeatherInfo::getWeatherDate, startOfHour)  // >= 小时开始时间
                        .lt(WeatherInfo::getWeatherDate, endOfHour)    // < 小时结束时间
        );
    }

}
