package syy.portPilotage.service.screen;

import syy.portPilotage.common.Result;
import syy.portPilotage.vo.screen.WeatherTideVO;

public interface WeatherService {

    /**
     * 获取实时天气和潮汐数据
     * @return 天气潮汐视图对象
     */
    Result<WeatherTideVO> getWeatherAndTide();
}