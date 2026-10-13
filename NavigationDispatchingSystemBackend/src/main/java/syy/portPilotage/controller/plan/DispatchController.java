package syy.portPilotage.controller.plan;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.dispatch.*;
import syy.portPilotage.service.DispatchService;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pilotage/dispatch")
@RequiredArgsConstructor
@Tag(name = "引航调度管理", description = "引航调度相关接口")
public class DispatchController {

    private final DispatchService dispatchService;

    @GetMapping("/list")
    @Operation(summary = "获取调度列表")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER', 'PILOT')")
    public Result<Map<String, Object>> getDispatchList(@Valid DispatchQueryDTO query) {
        log.info("获取调度列表: page={}, size={}, planStatus={}",
                query.getPage(), query.getSize(), query.getPlanStatus());

        Map<String, Object> result = dispatchService.getDispatchList(query);
        return Result.success(result);
    }

    @GetMapping("/detail/{planId}")
    @Operation(summary = "获取调度详情")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER', 'PILOT')")
    public Result<DispatchDetailDTO> getDispatchDetail(@PathVariable Long planId) {
        log.info("获取调度详情: planId={}", planId);

        DispatchDetailDTO detail = dispatchService.getDispatchDetail(planId);
        return Result.success(detail);
    }

    @PostMapping("/publish/{planId}")
    @Operation(summary = "发布调度")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Boolean> publishDispatch(@PathVariable Long planId) {
        log.info("发布调度: planId={}", planId);

        boolean success = dispatchService.publishDispatch(planId);
        return success ? Result.success(true) : Result.error("发布调度失败");
    }

    @PostMapping("/start/{planId}")
    @Operation(summary = "开始调度")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Boolean> startDispatch(@PathVariable Long planId,
                                         @RequestBody DispatchTimeUpdateDTO dto) {
        log.info("开始调度: planId={}, dto={}", planId, dto);

        boolean success = dispatchService.startDispatch(planId, dto);
        return success ? Result.success(true) : Result.error("开始调度失败");
    }

    @PostMapping("/complete/{planId}")
    @Operation(summary = "完成调度")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Boolean> completeDispatch(@PathVariable Long planId,
                                            @RequestBody DispatchTimeUpdateDTO dto) {
        log.info("完成调度: planId={}, dto={}", planId, dto);

        boolean success = dispatchService.completeDispatch(planId, dto);
        return success ? Result.success(true) : Result.error("完成调度失败");
    }

    @PostMapping("/cancel/{planId}")
    @Operation(summary = "取消调度")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Boolean> cancelDispatch(@PathVariable Long planId,
                                          @RequestParam(required = false) String reason) {
        log.info("取消调度: planId={}, reason={}", planId, reason);

        boolean success = dispatchService.cancelDispatch(planId, reason);
        return success ? Result.success(true) : Result.error("取消调度失败");
    }

    @PutMapping("/time/{planId}")
    @Operation(summary = "更新调度时间")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Boolean> updateDispatchTime(@PathVariable Long planId,
                                              @Valid @RequestBody DispatchTimeUpdateDTO dto) {
        log.info("更新调度时间: planId={}, dto={}", planId, dto);

        boolean success = dispatchService.updateDispatchTime(planId, dto);
        return success ? Result.success(true) : Result.error("更新调度时间失败");
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取调度统计信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Map<String, Object>> getDispatchStatistics(@Valid DispatchStatisticsQueryDTO query) {
        log.info("获取调度统计信息: query={}", query);

        Map<String, Object> statistics = dispatchService.getDispatchStatistics(query);
        return Result.success(statistics);
    }

    @GetMapping("/export")
    @Operation(summary = "导出调度列表")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public void exportDispatch(@Valid DispatchQueryDTO query, HttpServletResponse response) throws IOException {
        log.info("导出调度列表: query={}", query);

        // 导出默认取全量（上限 10000，避免一次性过大）
        query.setPage(1);
        query.setSize(10000);

        Map<String, Object> result = dispatchService.getDispatchList(query);
        @SuppressWarnings("unchecked")
        List<DispatchPlanDTO> list = (List<DispatchPlanDTO>) result.get("list");

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("调度列表");

        String[] headers = new String[] {
                "计划编号", "船名(中)", "航次类型", "计划开始时间", "计划结束时间",
                "状态", "引航员", "驾驶员", "车辆", "起始位置", "结束位置", "确认进度(%)"
        };

        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 18 * 256);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                DispatchPlanDTO d = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(d.getPlanCode() == null ? "" : d.getPlanCode());
                row.createCell(1).setCellValue(d.getVesselNameZh() == null ? "" : d.getVesselNameZh());
                row.createCell(2).setCellValue(d.getVoyageType() == null ? "" : d.getVoyageType());
                row.createCell(3).setCellValue(d.getPlanStartTime() == null ? "" : sdf.format(d.getPlanStartTime()));
                row.createCell(4).setCellValue(d.getPlanEndTime() == null ? "" : sdf.format(d.getPlanEndTime()));
                row.createCell(5).setCellValue(d.getDispatchStatus() == null ? (d.getPlanStatus() == null ? "" : d.getPlanStatus()) : d.getDispatchStatus());
                row.createCell(6).setCellValue(d.getMainPilot() == null ? "" : d.getMainPilot());
                row.createCell(7).setCellValue(d.getDriverName() == null ? "" : d.getDriverName());
                String vehicle = "";
                if (d.getLicensePlate() != null) vehicle += d.getLicensePlate();
                if (d.getVehicleModel() != null) vehicle = (vehicle + " " + d.getVehicleModel()).trim();
                row.createCell(8).setCellValue(vehicle);
                row.createCell(9).setCellValue(d.getStartLocationName() == null ? "" : d.getStartLocationName());
                row.createCell(10).setCellValue(d.getEndLocationName() == null ? "" : d.getEndLocationName());
                row.createCell(11).setCellValue(d.getConfirmationProgress() == null ? "" : String.valueOf(d.getConfirmationProgress()));
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();

        String filename = "引航调度_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        response.getOutputStream().write(out.toByteArray());
        response.flushBuffer();
    }
}