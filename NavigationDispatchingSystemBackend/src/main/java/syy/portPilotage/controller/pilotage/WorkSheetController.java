package syy.portPilotage.controller.pilotage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.dto.pilotage.WorkSheetRecordQueryDTO;
import syy.portPilotage.entity.PilotagePlans;
import syy.portPilotage.entity.WorkSheets;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.PilotagePlansService;
import syy.portPilotage.service.PlanPilotBoatAssignmentsService;
import syy.portPilotage.service.PlanTugboatAssignmentsService;
import syy.portPilotage.service.PilotBoatsService;
import syy.portPilotage.service.TugboatsService;
import syy.portPilotage.mapper.WorkSheetRecordsMapper;
import syy.portPilotage.service.WorkSheetsService;
import syy.portPilotage.vo.pilotage.WorkSheetDriverRecordVO;
import syy.portPilotage.vo.pilotage.WorkSheetPilotRecordVO;
import syy.portPilotage.vo.pilotage.WorkSheetPilotBoatRecordVO;
import syy.portPilotage.vo.pilotage.WorkSheetTugboatRecordVO;
import syy.portPilotage.vo.pilotage.WorkSheetVehicleRecordVO;
import syy.portPilotage.util.CodeGeneratorUtil;
import syy.portPilotage.entity.PlanPilotBoatAssignments;
import syy.portPilotage.entity.PlanTugboatAssignments;
import syy.portPilotage.entity.PilotBoats;
import syy.portPilotage.entity.Tugboats;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/pilotage/worksheets")
@RequiredArgsConstructor
@Tag(name = "工作表管理", description = "工作表相关接口")
public class WorkSheetController {
    
    @Autowired
    WorkSheetsService workSheetsService;

    private final WorkSheetRecordsMapper workSheetRecordsMapper;
    private final CodeGeneratorUtil codeGeneratorUtil;
    private final PilotagePlansService pilotagePlansService;
    private final PlanTugboatAssignmentsService planTugboatAssignmentsService;
    private final PlanPilotBoatAssignmentsService planPilotBoatAssignmentsService;
    private final TugboatsService tugboatsService;
    private final PilotBoatsService pilotBoatsService;
    
    @PostMapping
    @Operation(summary = "创建工作表")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','PILOT')")
    public Result<Long> create(@RequestBody WorkSheets workSheet) {
        log.info("创建工作表: worksheetId={}", workSheet.getId());

        if (workSheet.getWorksheetCode() == null || workSheet.getWorksheetCode().trim().isEmpty()) {
            workSheet.setWorksheetCode(codeGeneratorUtil.generateWorksheetCode());
        }
        if (workSheet.getWorkStatus() == null || workSheet.getWorkStatus().trim().isEmpty()) {
            workSheet.setWorkStatus("NOT_STARTED");
        }
        // 自动计算工时（分钟）
        workSheet.setWorkDuration(calcWorkDurationMinutes(workSheet.getActualStartTime(), workSheet.getActualEndTime()));

        workSheet.setCreatedAt(new Date());
        workSheet.setUpdatedAt(new Date());
        
        workSheetsService.save(workSheet);
        
        log.info("工作表创建成功: id={}", workSheet.getId());
        return Result.success(workSheet.getId());
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新工作表")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','PILOT')")
    public Result<Void> update(@PathVariable Long id, @RequestBody WorkSheets workSheet) {
        log.info("更新工作表: id={}", id);
        
        WorkSheets existing = workSheetsService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工作表不存在");
        }
        
        workSheet.setId(id);
        workSheet.setUpdatedAt(new Date());
        workSheet.setWorkDuration(calcWorkDurationMinutes(workSheet.getActualStartTime(), workSheet.getActualEndTime()));
        
        workSheetsService.updateById(workSheet);
        
        log.info("工作表更新成功: id={}", id);
        return Result.success();
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取工作表详情")
    public Result<WorkSheets> getById(@PathVariable Long id) {
        WorkSheets workSheet = workSheetsService.getById(id);
        if (workSheet == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工作表不存在");
        }
        
        return Result.success(workSheet);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除工作表")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','PILOT')")
    public Result<Void> delete(@PathVariable Long id) {
        WorkSheets existing = workSheetsService.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "工作表不存在");
        }
        workSheetsService.removeById(id);
        return Result.success();
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询工作表")
    public Result<PageResult<WorkSheets>> page(PageQueryDTO query,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String workStatus,
                                              @RequestParam(required = false) String startDate,
                                              @RequestParam(required = false) String endDate) {
        Page<WorkSheets> page = new Page<>(query.getCurrent(), query.getSize());
        
        LambdaQueryWrapper<WorkSheets> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = "%" + keyword.trim() + "%";
            wrapper.and(w -> w
                    .like(WorkSheets::getWorksheetCode, k)
                    .or().like(WorkSheets::getStartLocation, k)
                    .or().like(WorkSheets::getEndLocation, k)
                    .or().like(WorkSheets::getAssignedTugboats, k)
                    .or().like(WorkSheets::getAssignedPilotBoats, k)
            );
        }
        if (workStatus != null && !workStatus.trim().isEmpty()) {
            wrapper.eq(WorkSheets::getWorkStatus, workStatus.trim());
        }
        Date start = parseFlexibleDate(startDate, true);
        Date end = parseFlexibleDate(endDate, false);
        if (start != null) {
            wrapper.ge(WorkSheets::getPlanStartTime, start);
        }
        if (end != null) {
            wrapper.le(WorkSheets::getPlanStartTime, end);
        }
        wrapper.orderByDesc(WorkSheets::getCreatedAt);
        
        IPage<WorkSheets> result = workSheetsService.page(page, wrapper);
        
        PageResult<WorkSheets> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );
        
        return Result.success(pageResult);
    }

    /**
     * 从引航计划同步生成工作表台账（work_sheets）
     * 说明：统计视图数据来自 pilotage_plans，而台账表需要同步/录入才会有数据。
     */
    @PostMapping("/ledger/sync")
    @Operation(summary = "同步生成工作表台账（从引航计划/分配表）")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<Map<String, Object>> syncLedger(@RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate) {
        Date start = parseFlexibleDate(startDate, true);
        Date end = parseFlexibleDate(endDate, false);

        if (start == null && end == null) {
            LocalDate d = LocalDate.now().minusDays(365);
            LocalDateTime dt = d.atStartOfDay();
            start = Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
        }

        List<PilotagePlans> plans = pilotagePlansService.lambdaQuery()
                .in(PilotagePlans::getPlanStatus, "ONGOING", "COMPLETED", "CERTIFICATED", "BILLED", "PAID")
                .ge(start != null, PilotagePlans::getPlanStartTime, start)
                .le(end != null, PilotagePlans::getPlanStartTime, end)
                .orderByDesc(PilotagePlans::getPlanStartTime)
                .list();

        int created = 0;
        int skipped = 0;

        for (PilotagePlans p : plans) {
            if (p.getPlanCode() == null || p.getPlanCode().trim().isEmpty()) {
                continue;
            }
            String worksheetCode = "WS-" + p.getPlanCode().trim();
            boolean exists = workSheetsService.lambdaQuery()
                    .eq(WorkSheets::getWorksheetCode, worksheetCode)
                    .exists();
            if (exists) {
                skipped++;
                continue;
            }

            WorkSheets ws = new WorkSheets();
            ws.setWorksheetCode(worksheetCode);
            ws.setPlanStartTime(p.getPlanStartTime());
            ws.setPlanEndTime(p.getPlanEndTime());
            ws.setStartLocation(p.getStartLocationName());
            ws.setEndLocation(p.getEndLocationName());
            ws.setAssignedPilotId(p.getAssignedPilotId());
            ws.setAssignedDriverId(p.getAssignedDriverId());
            ws.setBoardingTime(p.getBoardingTime());
            ws.setLeavingTime(p.getLeavingTime());
            ws.setActualStartTime(p.getActualStartTime());
            ws.setActualEndTime(p.getActualEndTime());
            ws.setOperationTypeFlag(p.getOperationTypeFlag());

            ws.setAssignedTugboats(resolveAssignedTugboats(p.getId()));
            ws.setAssignedPilotBoats(resolveAssignedPilotBoats(p.getId()));

            // 状态推断
            String status = inferWorkStatus(p);
            ws.setWorkStatus(status);
            ws.setWorkDuration(calcWorkDurationMinutes(ws.getActualStartTime(), ws.getActualEndTime()));

            ws.setCreatedAt(new Date());
            ws.setUpdatedAt(new Date());
            workSheetsService.save(ws);
            created++;
        }

        return Result.success(Map.of(
                "totalPlans", plans.size(),
                "created", created,
                "skipped", skipped
        ));
    }

    private String resolveAssignedTugboats(Long planId) {
        if (planId == null) return null;
        List<PlanTugboatAssignments> assigns = planTugboatAssignmentsService.lambdaQuery()
                .eq(PlanTugboatAssignments::getPlanId, planId)
                .list();
        if (assigns == null || assigns.isEmpty()) return null;
        List<Long> ids = assigns.stream().map(PlanTugboatAssignments::getTugboatId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return null;
        List<Tugboats> tugs = tugboatsService.listByIds(ids);
        if (tugs == null || tugs.isEmpty()) return null;
        return tugs.stream().map(Tugboats::getTugboatName).filter(Objects::nonNull).distinct().collect(Collectors.joining(","));
    }

    private String resolveAssignedPilotBoats(Long planId) {
        if (planId == null) return null;
        List<PlanPilotBoatAssignments> assigns = planPilotBoatAssignmentsService.lambdaQuery()
                .eq(PlanPilotBoatAssignments::getPlanId, planId)
                .list();
        if (assigns == null || assigns.isEmpty()) return null;
        List<Long> ids = assigns.stream().map(PlanPilotBoatAssignments::getPilotBoatId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return null;
        List<PilotBoats> boats = pilotBoatsService.listByIds(ids);
        if (boats == null || boats.isEmpty()) return null;
        return boats.stream().map(PilotBoats::getBoatName).filter(Objects::nonNull).distinct().collect(Collectors.joining(","));
    }

    private String inferWorkStatus(PilotagePlans p) {
        if (p == null) return "NOT_STARTED";
        if (p.getActualEndTime() != null) return "COMPLETED";
        if (p.getActualStartTime() != null) return "ONGOING";
        if ("COMPLETED".equalsIgnoreCase(p.getPlanStatus())
                || "CERTIFICATED".equalsIgnoreCase(p.getPlanStatus())
                || "BILLED".equalsIgnoreCase(p.getPlanStatus())
                || "PAID".equalsIgnoreCase(p.getPlanStatus())) {
            return "COMPLETED";
        }
        return "NOT_STARTED";
    }

    private Integer calcWorkDurationMinutes(Date start, Date end) {
        if (start == null || end == null) return null;
        long minutes = (end.getTime() - start.getTime()) / 60000L;
        return minutes < 0 ? null : (int) minutes;
    }

    private Date parseFlexibleDate(String input, boolean isStart) {
        if (input == null || input.trim().isEmpty()) return null;
        String s = input.trim();
        try {
            // yyyy-MM-dd HH:mm:ss
            LocalDateTime dt = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignore) {
            // fallthrough
        }
        try {
            // yyyy-MM-dd
            LocalDate d = LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDateTime dt = isStart ? d.atStartOfDay() : d.atTime(23, 59, 59);
            return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignore) {
            return null;
        }
    }

        @GetMapping("/records/pilot/page")
    @Operation(summary = "分页查询工作表记录-引航员")
    public Result<PageResult<WorkSheetPilotRecordVO>> pilotRecordsPage(PageQueryDTO pageQuery,
                                                                       WorkSheetRecordQueryDTO query) {
        int current = pageQuery.getCurrent();
        int size = pageQuery.getSize();
        int offset = (current - 1) * size;

        List<WorkSheetPilotRecordVO> records = workSheetRecordsMapper.selectPilotPage(
                offset, size,
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        Long total = workSheetRecordsMapper.countPilotPage(
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );

        return Result.success(PageResult.of(total == null ? 0 : total, current, size, records));
    }

    @GetMapping("/records/pilot/export")
    @Operation(summary = "导出工作表记录-引航员（CSV）")
    public void exportPilotRecords(WorkSheetRecordQueryDTO query, HttpServletResponse response) throws IOException {
        // 简单导出：按当前条件导出前 5000 条，避免一次性导出过大
        List<WorkSheetPilotRecordVO> rows = workSheetRecordsMapper.selectPilotPage(
                0, 5000,
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // UTF-8 BOM, 方便 Excel 正确识别中文
        sb.append("计划编号,中文船名,英文船名,引航员,时间/日期,泊位,动态,总吨,净吨,载重吨,船长,船宽,吃水,国籍,呼号,IMO\n");
        for (WorkSheetPilotRecordVO r : rows) {
            sb.append(csv(r.getPlanCode())).append(',')
              .append(csv(r.getShipName())).append(',')
              .append(csv(r.getEnglishName())).append(',')
              .append(csv(r.getPilotName())).append(',')
              .append(csv(r.getTime())).append(',')
              .append(csv(r.getBerth())).append(',')
              .append(csv(r.getDynamic())).append(',')
              .append(csv(r.getGrossTonnage())).append(',')
              .append(csv(r.getNetTonnage())).append(',')
              .append(csv(r.getDeadWeight())).append(',')
              .append(csv(r.getLength())).append(',')
              .append(csv(r.getWidth())).append(',')
              .append(csv(r.getDraft())).append(',')
              .append(csv(r.getNationality())).append(',')
              .append(csv(r.getCallSign())).append(',')
              .append(csv(r.getImoNumber())).append('\n');
        }

        String filename = "工作表_引航员_" + System.currentTimeMillis() + ".csv";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }

    @GetMapping("/records/driver/page")
    @Operation(summary = "分页查询工作表记录-车队司机")
    public Result<PageResult<WorkSheetDriverRecordVO>> driverRecordsPage(PageQueryDTO pageQuery,
                                                                         WorkSheetRecordQueryDTO query) {
        int current = pageQuery.getCurrent();
        int size = pageQuery.getSize();
        int offset = (current - 1) * size;

        List<WorkSheetDriverRecordVO> records = workSheetRecordsMapper.selectDriverPage(
                offset, size,
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        Long total = workSheetRecordsMapper.countDriverPage(
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        return Result.success(PageResult.of(total == null ? 0 : total, current, size, records));
    }

    @GetMapping("/records/driver/export")
    @Operation(summary = "导出工作表记录-车队司机（CSV）")
    public void exportDriverRecords(WorkSheetRecordQueryDTO query, HttpServletResponse response) throws IOException {
        List<WorkSheetDriverRecordVO> rows = workSheetRecordsMapper.selectDriverPage(
                0, 5000,
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("计划编号,中文船名,英文船名,驾驶员,车牌号,时间/日期,泊位,动态,总吨,净吨,载重吨,船长,船宽,吃水,国籍,呼号,IMO\n");
        for (WorkSheetDriverRecordVO r : rows) {
            sb.append(csv(r.getPlanCode())).append(',')
              .append(csv(r.getShipName())).append(',')
              .append(csv(r.getEnglishName())).append(',')
              .append(csv(r.getDriverName())).append(',')
              .append(csv(r.getLicensePlate())).append(',')
              .append(csv(r.getTime())).append(',')
              .append(csv(r.getBerth())).append(',')
              .append(csv(r.getDynamic())).append(',')
              .append(csv(r.getGrossTonnage())).append(',')
              .append(csv(r.getNetTonnage())).append(',')
              .append(csv(r.getDeadWeight())).append(',')
              .append(csv(r.getLength())).append(',')
              .append(csv(r.getWidth())).append(',')
              .append(csv(r.getDraft())).append(',')
              .append(csv(r.getNationality())).append(',')
              .append(csv(r.getCallSign())).append(',')
              .append(csv(r.getImoNumber())).append('\n');
        }
        String filename = "工作表_车队司机_" + System.currentTimeMillis() + ".csv";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }

    @GetMapping("/records/vehicle/page")
    @Operation(summary = "分页查询工作表记录-车辆")
    public Result<PageResult<WorkSheetVehicleRecordVO>> vehicleRecordsPage(PageQueryDTO pageQuery,
                                                                           WorkSheetRecordQueryDTO query) {
        int current = pageQuery.getCurrent();
        int size = pageQuery.getSize();
        int offset = (current - 1) * size;

        List<WorkSheetVehicleRecordVO> records = workSheetRecordsMapper.selectVehiclePage(
                offset, size,
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        Long total = workSheetRecordsMapper.countVehiclePage(
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        return Result.success(PageResult.of(total == null ? 0 : total, current, size, records));
    }

    @GetMapping("/records/vehicle/export")
    @Operation(summary = "导出工作表记录-车辆（CSV）")
    public void exportVehicleRecords(WorkSheetRecordQueryDTO query, HttpServletResponse response) throws IOException {
        List<WorkSheetVehicleRecordVO> rows = workSheetRecordsMapper.selectVehiclePage(
                0, 5000,
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("计划编号,中文船名,英文船名,车辆代码,车牌号,驾驶员,时间/日期,泊位,动态\n");
        for (WorkSheetVehicleRecordVO r : rows) {
            sb.append(csv(r.getPlanCode())).append(',')
              .append(csv(r.getShipName())).append(',')
              .append(csv(r.getEnglishName())).append(',')
              .append(csv(r.getVehicleCode())).append(',')
              .append(csv(r.getLicensePlate())).append(',')
              .append(csv(r.getDriverName())).append(',')
              .append(csv(r.getTime())).append(',')
              .append(csv(r.getBerth())).append(',')
              .append(csv(r.getDynamic())).append('\n');
        }
        String filename = "工作表_车辆_" + System.currentTimeMillis() + ".csv";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }

    @GetMapping("/records/tugboat/page")
    @Operation(summary = "分页查询工作表记录-拖轮")
    public Result<PageResult<WorkSheetTugboatRecordVO>> tugboatRecordsPage(PageQueryDTO pageQuery,
                                                                           WorkSheetRecordQueryDTO query) {
        int current = pageQuery.getCurrent();
        int size = pageQuery.getSize();
        int offset = (current - 1) * size;

        List<WorkSheetTugboatRecordVO> records = workSheetRecordsMapper.selectTugboatPage(
                offset, size,
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        Long total = workSheetRecordsMapper.countTugboatPage(
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        return Result.success(PageResult.of(total == null ? 0 : total, current, size, records));
    }

    @GetMapping("/records/tugboat/export")
    @Operation(summary = "导出工作表记录-拖轮（CSV）")
    public void exportTugboatRecords(WorkSheetRecordQueryDTO query, HttpServletResponse response) throws IOException {
        List<WorkSheetTugboatRecordVO> rows = workSheetRecordsMapper.selectTugboatPage(
                0, 5000,
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("计划编号,中文船名,英文船名,拖轮,呼号,拖力(吨),时间/日期,泊位,动态\n");
        for (WorkSheetTugboatRecordVO r : rows) {
            sb.append(csv(r.getPlanCode())).append(',')
              .append(csv(r.getShipName())).append(',')
              .append(csv(r.getEnglishName())).append(',')
              .append(csv(r.getTugboatName())).append(',')
              .append(csv(r.getTugboatCallSign())).append(',')
              .append(csv(r.getBollardPull())).append(',')
              .append(csv(r.getTime())).append(',')
              .append(csv(r.getBerth())).append(',')
              .append(csv(r.getDynamic())).append('\n');
        }
        String filename = "工作表_拖轮_" + System.currentTimeMillis() + ".csv";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }

    @GetMapping("/records/pilot-boat/page")
    @Operation(summary = "分页查询工作表记录-引航艇")
    public Result<PageResult<WorkSheetPilotBoatRecordVO>> pilotBoatRecordsPage(PageQueryDTO pageQuery,
                                                                               WorkSheetRecordQueryDTO query) {
        int current = pageQuery.getCurrent();
        int size = pageQuery.getSize();
        int offset = (current - 1) * size;

        List<WorkSheetPilotBoatRecordVO> records = workSheetRecordsMapper.selectPilotBoatPage(
                offset, size,
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        Long total = workSheetRecordsMapper.countPilotBoatPage(
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        return Result.success(PageResult.of(total == null ? 0 : total, current, size, records));
    }

    @GetMapping("/records/pilot-boat/export")
    @Operation(summary = "导出工作表记录-引航艇（CSV）")
    public void exportPilotBoatRecords(WorkSheetRecordQueryDTO query, HttpServletResponse response) throws IOException {
        List<WorkSheetPilotBoatRecordVO> rows = workSheetRecordsMapper.selectPilotBoatPage(
                0, 5000,
                query == null ? null : query.getKeyword(),
                query == null ? null : query.getStartDate(),
                query == null ? null : query.getEndDate()
        );
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("计划编号,中文船名,英文船名,引航艇,呼号,时间/日期,泊位,动态\n");
        for (WorkSheetPilotBoatRecordVO r : rows) {
            sb.append(csv(r.getPlanCode())).append(',')
              .append(csv(r.getShipName())).append(',')
              .append(csv(r.getEnglishName())).append(',')
              .append(csv(r.getBoatName())).append(',')
              .append(csv(r.getBoatCallSign())).append(',')
              .append(csv(r.getTime())).append(',')
              .append(csv(r.getBerth())).append(',')
              .append(csv(r.getDynamic())).append('\n');
        }
        String filename = "工作表_引航艇_" + System.currentTimeMillis() + ".csv";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }

    private String csv(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}