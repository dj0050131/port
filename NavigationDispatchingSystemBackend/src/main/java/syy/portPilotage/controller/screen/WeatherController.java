package syy.portPilotage.controller.screen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.Result;
import syy.portPilotage.service.screen.WeatherService;
import syy.portPilotage.vo.screen.WeatherTideVO;

@Tag(name = "天气潮汐数据接口", description = "天气潮汐数据相关接口")
@RestController
@RequestMapping("/api/public")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Operation(summary = "获取实时天气和潮汐数据")
    @GetMapping("/weather/realtime")
    public Result<WeatherTideVO> getWeatherAndTide() {
        return weatherService.getWeatherAndTide();
    }
}