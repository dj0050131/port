package syy.portPilotage.controller.statistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.statistics.HolidayDTO;
import syy.portPilotage.dto.statistics.WeekendDTO;
import syy.portPilotage.dto.statistics.NightDTO;
import syy.portPilotage.dto.statistics.SpecialDTO;
import syy.portPilotage.dto.statistics.MealDTO;
import syy.portPilotage.service.PilotageCertificateStatisticsService;
import syy.portPilotage.service.VesselsService;
import syy.portPilotage.service.PilotsService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/statistics/holidayWeekend")
@RequiredArgsConstructor
@Tag(name = "节假日和周末引航统计", description = "节假日和周末引航统计相关接口")
public class HolidayWeekendController {

    @Autowired
    private PilotageCertificateStatisticsService pilotageCertificateStatisticsService;
    
    @Autowired
    private VesselsService vesselsService;
    
    @Autowired
    private PilotsService pilotsService;

    @GetMapping("/holiday")
    @Operation(summary = "节假日引航统计")
    public Result<List<HolidayDTO>> getHoliday(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取节假日引航统计: startDate={}, endDate={}", startDate, endDate);

        // 设置默认日期范围
        LocalDate start = parseDateParam(startDate, LocalDate.of(1970, 1, 1));
        LocalDate end = parseDateParam(endDate, LocalDate.now().plusDays(1));

        List<HolidayDTO> result = pilotageCertificateStatisticsService.getHolidayReport(start, end);

        log.info("节假日引航统计获取成功");
        return Result.success(result);
    }

    @GetMapping("/weekend")
    @Operation(summary = "双休日引航统计")
    public Result<List<WeekendDTO>> getWeekend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取双休日引航统计: startDate={}, endDate={}", startDate, endDate);

        // 设置默认日期范围
        LocalDate start = parseDateParam(startDate, LocalDate.of(1970, 1, 1));
        LocalDate end = parseDateParam(endDate, LocalDate.now().plusDays(1));

        List<WeekendDTO> result = pilotageCertificateStatisticsService.getWeekendReport(start, end);

        log.info("双休日引航统计获取成功");
        return Result.success(result);
    }

    @GetMapping("/night")
    @Operation(summary = "夜班统计")
    public Result<List<NightDTO>> getNight(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取夜班统计: startDate={}, endDate={}", startDate, endDate);

        // 设置默认日期范围
        LocalDate start = parseDateParam(startDate, LocalDate.of(1970, 1, 1));
        LocalDate end = parseDateParam(endDate, LocalDate.now().plusDays(1));

        List<NightDTO> result = pilotageCertificateStatisticsService.getNightReport(start, end);

        log.info("夜班统计获取成功");
        return Result.success(result);
    }

    @GetMapping("/special")
    @Operation(summary = "特殊引航统计")
    public Result<List<SpecialDTO>> getSpecial(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String shipName) {
        log.info("获取特殊引航统计: startDate={}, endDate={}, shipName={}",
                startDate, endDate, shipName);

        // 设置默认日期范围
        LocalDate start = parseDateParam(startDate, LocalDate.of(1970, 1, 1));
        LocalDate end = parseDateParam(endDate, LocalDate.now().plusDays(1));

        List<SpecialDTO> result = pilotageCertificateStatisticsService.getSpecialReport(start, end, shipName);

        log.info("特殊引航统计获取成功");
        return Result.success(result);
    }

    @GetMapping("/meal")
    @Operation(summary = "餐补统计")
    public Result<List<MealDTO>> getMeal(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取餐补统计: startDate={}, endDate={}", startDate, endDate);

        // 设置默认日期范围
        LocalDate start = parseDateParam(startDate, LocalDate.of(1970, 1, 1));
        LocalDate end = parseDateParam(endDate, LocalDate.now().plusDays(1));

        List<MealDTO> result = pilotageCertificateStatisticsService.getMealReport(start, end);

        log.info("餐补统计获取成功");
        return Result.success(result);
    }

    private LocalDate parseDateParam(String value, LocalDate fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        String text = value.trim();
        try {
            if (text.length() >= 10 && text.contains("T")) {
                return LocalDate.parse(text.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception ignored) {
            return fallback;
        }
    }

}