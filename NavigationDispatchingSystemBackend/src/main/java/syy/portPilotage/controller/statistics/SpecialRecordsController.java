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
import syy.portPilotage.dto.statistics.RecordBreakDTO;
import syy.portPilotage.dto.statistics.NewWharfDTO;
import syy.portPilotage.dto.statistics.PlanChangeDTO;
import syy.portPilotage.dto.statistics.MealDTO;
import syy.portPilotage.service.PilotageCertificateStatisticsService;
import syy.portPilotage.service.VesselsService;
import syy.portPilotage.service.WharvesService;
import syy.portPilotage.service.PilotagePlansService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/statistics/specialRecords")
@RequiredArgsConstructor
@Tag(name = "特殊记录统计", description = "特殊记录统计相关接口")
public class SpecialRecordsController {

    @Autowired
    private PilotageCertificateStatisticsService pilotageCertificateStatisticsService;
    
    @Autowired
    private VesselsService vesselsService;
    
    @Autowired
    private WharvesService wharvesService;
    
    @Autowired
    private PilotagePlansService pilotagePlansService;
    @GetMapping("/recordBreak")
    @Operation(summary = "破纪录统计")
    public Result<List<RecordBreakDTO>> getRecordBreak(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取破纪录统计: startDate={}, endDate={}", startDate, endDate);

        // 设置默认日期范围
        LocalDate start = startDate != null ? LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.of(1970, 1, 1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().plusDays(1);

        List<RecordBreakDTO> result = pilotageCertificateStatisticsService.getRecordBreakReport(start, end);

        log.info("破纪录统计获取成功");
        return Result.success(result);
    }

    @GetMapping("/newWharf")
    @Operation(summary = "新码头靠泊统计")
    public Result<List<NewWharfDTO>> getNewWharf(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取新码头靠泊统计: startDate={}, endDate={}", startDate, endDate);

        // 设置默认日期范围
        LocalDate start = startDate != null ? LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.of(1970, 1, 1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().plusDays(1);

        List<NewWharfDTO> result = pilotageCertificateStatisticsService.getNewWharfReport(start, end);

        log.info("新码头靠泊统计获取成功");
        return Result.success(result);
    }

    @GetMapping("/planChange")
    @Operation(summary = "计划改变查询")
    public Result<List<PlanChangeDTO>> getPlanChange(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status) {
        log.info("获取计划改变查询: startDate={}, endDate={}, status={}",
                startDate, endDate, status);

        // 设置默认日期范围
        LocalDate start = startDate != null ? LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.of(1970, 1, 1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().plusDays(1);

        List<PlanChangeDTO> result = pilotagePlansService.getPlanChangeReport(start, end, status);

        log.info("计划改变查询获取成功");
        return Result.success(result);
    }

    @GetMapping("/meal")
    @Operation(summary = "餐补统计")
    public Result<List<MealDTO>> getMeal(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取餐补统计: startDate={}, endDate={}", startDate, endDate);

        // 设置默认日期范围
        LocalDate start = startDate != null ? LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.of(1970, 1, 1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().plusDays(1);

        List<MealDTO> result = pilotageCertificateStatisticsService.getMealReport(start, end);

        log.info("餐补统计获取成功");
        return Result.success(result);
    }

}