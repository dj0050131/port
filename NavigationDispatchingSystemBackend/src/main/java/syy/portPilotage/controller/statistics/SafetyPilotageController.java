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
import syy.portPilotage.dto.statistics.SafetyMonthlyDTO;
import syy.portPilotage.dto.statistics.PilotageMonthlyDTO;
import syy.portPilotage.dto.statistics.PilotageSummaryDTO;
import syy.portPilotage.dto.statistics.LengthGt250DTO;
import syy.portPilotage.service.PilotageCertificateStatisticsService;
import syy.portPilotage.service.VesselsService;
import syy.portPilotage.service.PilotsService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/statistics/safetyPilotage")
@RequiredArgsConstructor
@Tag(name = "安全引航统计", description = "安全引航统计相关接口")
public class SafetyPilotageController {

    @Autowired
    private PilotageCertificateStatisticsService pilotageCertificateStatisticsService;
    
    @Autowired
    private VesselsService vesselsService;
    
    @Autowired
    private PilotsService pilotsService;
    @GetMapping("/safetyMonthly")
    @Operation(summary = "船务安全科航行(月份)统计")
    public Result<List<SafetyMonthlyDTO>> getSafetyMonthly(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取船务安全科航行统计: startDate={}, endDate={}", startDate, endDate);

        // 设置默认日期范围
        LocalDate start = parseDateParam(startDate, LocalDate.of(1970, 1, 1));
        LocalDate end = parseDateParam(endDate, LocalDate.now().plusDays(1));

        List<SafetyMonthlyDTO> result = pilotageCertificateStatisticsService.getSafetyMonthlyReport(start, end);

        log.info("船务安全科航行统计获取成功");
        return Result.success(result);
    }

    @GetMapping("/pilotageMonthly")
    @Operation(summary = "船舶引航统计(月份)")
    public Result<List<PilotageMonthlyDTO>> getPilotageMonthly(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String shipName,
            @RequestParam(required = false) String pilot) {
        log.info("获取船舶引航统计: startDate={}, endDate={}, shipName={}, pilot={}",
                startDate, endDate, shipName, pilot);

        // 设置默认日期范围
        LocalDate start = parseDateParam(startDate, LocalDate.of(1970, 1, 1));
        LocalDate end = parseDateParam(endDate, LocalDate.now().plusDays(1));

        List<PilotageMonthlyDTO> result = pilotageCertificateStatisticsService.getPilotageMonthlyReport(start, end, shipName, pilot);

        log.info("船舶引航统计获取成功");
        return Result.success(result);
    }

    @GetMapping("/pilotageSummary")
    @Operation(summary = "船舶引航统计(汇总)")
    public Result<List<PilotageSummaryDTO>> getPilotageSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取船舶引航统计汇总: startDate={}, endDate={}", startDate, endDate);

        // 设置默认日期范围
        LocalDate start = parseDateParam(startDate, LocalDate.of(1970, 1, 1));
        LocalDate end = parseDateParam(endDate, LocalDate.now().plusDays(1));

        List<PilotageSummaryDTO> result = pilotageCertificateStatisticsService.getPilotageSummaryReport(start, end);

        log.info("船舶引航统计汇总获取成功");
        return Result.success(result);
    }

    @GetMapping("/lengthGt250")
    @Operation(summary = "船长统计(>250m)")
    public Result<List<LengthGt250DTO>> getLengthGt250(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "250") Double minLength) {
        log.info("获取船长统计(>250m): startDate={}, endDate={}, minLength={}",
                startDate, endDate, minLength);

        // 设置默认日期范围
        LocalDate start = parseDateParam(startDate, LocalDate.of(1970, 1, 1));
        LocalDate end = parseDateParam(endDate, LocalDate.now().plusDays(1));

        List<LengthGt250DTO> result = pilotageCertificateStatisticsService.getLengthGt250Report(start, end, minLength);

        log.info("船长统计(>250m)获取成功");
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