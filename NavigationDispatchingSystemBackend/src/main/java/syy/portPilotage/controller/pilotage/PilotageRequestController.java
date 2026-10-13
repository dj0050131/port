package syy.portPilotage.controller.pilotage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.dto.pilotage.UpdateStatusDTO;
import syy.portPilotage.entity.PilotageRequests;
import syy.portPilotage.entity.Users;
import syy.portPilotage.entity.Vessels;
import syy.portPilotage.entity.Voyages;
import syy.portPilotage.enums.LocationTypeEnum;
import syy.portPilotage.enums.PlanStatusEnum;
import syy.portPilotage.enums.RequestStatusEnum;
import syy.portPilotage.enums.VoyageTypeEnum;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.PilotageRequestsService;
import syy.portPilotage.util.BeanCopyUtil;
import syy.portPilotage.util.SecurityUtil;
import syy.portPilotage.dto.pilotage.PilotageRequestDTO;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RestController
@RequestMapping("/api/pilotage/requests")
@RequiredArgsConstructor
@Tag(name = "引航申请管理", description = "引航申请相关接口")
public class PilotageRequestController {
    
    @Autowired
    PilotageRequestsService pilotageRequestsService;
    
    @PostMapping
    @Operation(summary = "创建引航申请")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PILOTAGE:APPLICATIONS:CREATE')")
    public Result<Long> create(@Valid @RequestBody PilotageRequestDTO dto) {
        log.info("创建引航申请: vesselId={}", dto.getVesselId());
        PilotageRequests pilotageRequests = pilotageRequestsService.savePilotAgeRequestData(dto);
        log.info("引航申请创建成功: id={}, code={}", pilotageRequests.getId(), pilotageRequests.getRequestCode());
        return Result.success(pilotageRequests.getId());
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "更新引航申请")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PILOTAGE:APPLICATIONS:EDIT')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PilotageRequestDTO dto) {
        log.info("更新引航申请: id={}", id);
        
        PilotageRequests request = pilotageRequestsService.getById(id);
        if (request == null) {
            throw new BusinessException(ErrorCode.REQUEST_NOT_FOUND);
        }
        
        if (!RequestStatusEnum.PENDING.equals(request.getRequestStatus())) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY, "只有待审核状态才能修改");
        }

        PilotageRequests pilotAgeData = pilotageRequestsService.updatePilotAgeData(dto);

        log.info("引航申请更新成功: id={}", id);
        return Result.success();
    }
    

    
    @GetMapping("/{id}")
    @Operation(summary = "获取引航申请详情")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PILOTAGE:APPLICATIONS:VIEW')")
    public Result<PilotageRequestDTO> getById(@PathVariable Long id) {
        PilotageRequestDTO request = pilotageRequestsService.getPilotAgeRequestById(id);
        return Result.success(request);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询引航申请")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PILOTAGE:APPLICATIONS:VIEW')")
    public Result<PageResult<PilotageRequestDTO>> page(
            PageQueryDTO query,
            @RequestParam(required = false) String nameZh,
            @RequestParam(required = false) String nameEn,
            @RequestParam(required = false) String requestStatus,
            @RequestParam(required = false) String planStatus) {
        if (query.getParams() == null) {
            query.setParams(new java.util.HashMap<>());
        }
        Map<String, Object> params = query.getParams();
        if (nameZh != null && !nameZh.trim().isEmpty()) {
            params.put("nameZh", nameZh.trim());
        }
        if (nameEn != null && !nameEn.trim().isEmpty()) {
            params.put("nameEn", nameEn.trim());
        }
        if (requestStatus != null && !requestStatus.trim().isEmpty()) {
            params.put("requestStatus", requestStatus.trim());
        }
        if (planStatus != null && !planStatus.trim().isEmpty()) {
            params.put("planStatus", planStatus.trim());
        }
        IPage<PilotageRequestDTO> result = pilotageRequestsService.getPageWithJoin(query);

        PageResult<PilotageRequestDTO> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );

        return Result.success(pageResult);
    }

    @GetMapping("/export")
    @Operation(summary = "导出引航申请（动态预报）")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PILOTAGE:APPLICATIONS:EXPORT')")
    public void export(@RequestParam(required = false) String ids, HttpServletResponse response) throws IOException {
        log.info("导出引航申请: ids={}", ids);

        List<PilotageRequestDTO> rows;
        if (ids != null && !ids.trim().isEmpty()) {
            List<Long> idList = new ArrayList<>();
            for (String s : ids.split(",")) {
                String t = s == null ? "" : s.trim();
                if (t.isEmpty()) continue;
                try {
                    idList.add(Long.valueOf(t));
                } catch (Exception ignore) {
                    // ignore invalid id
                }
            }
            // 导出必须走联表查询，否则计划状态/港区/码头/位置名称等字段会为空
            rows = pilotageRequestsService.getListWithJoinByIds(idList);
        } else {
            PageQueryDTO q = new PageQueryDTO();
            q.setCurrent(1);
            q.setSize(5000);
            IPage<PilotageRequestDTO> page = pilotageRequestsService.getPageWithJoin(q);
            rows = page.getRecords();
        }

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("引航申请");

        String[] headers = new String[]{
                "申请编号", "中文船名", "英文船名", "呼号", "IMO",
                "航次号", "航次类型", "ETA", "申请状态", "计划状态",
                "港区", "码头", "开始位置", "结束位置", "创建时间"
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
        for (int i = 0; i < (rows == null ? 0 : rows.size()); i++) {
            PilotageRequestDTO r = rows.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(r.getRequestCode() == null ? "" : r.getRequestCode());
            row.createCell(1).setCellValue(r.getNameZh() == null ? "" : r.getNameZh());
            row.createCell(2).setCellValue(r.getNameEn() == null ? "" : r.getNameEn());
            row.createCell(3).setCellValue(r.getVesselCallSign() == null ? "" : r.getVesselCallSign());
            row.createCell(4).setCellValue(r.getImoNumber() == null ? "" : r.getImoNumber());
            row.createCell(5).setCellValue(r.getVoyageNumber() == null ? "" : r.getVoyageNumber());
            row.createCell(6).setCellValue(toVoyageTypeText(r.getVoyageType()));
            row.createCell(7).setCellValue(r.getEta() == null ? "" : sdf.format(r.getEta()));
            row.createCell(8).setCellValue(toRequestStatusText(r.getRequestStatus()));
            row.createCell(9).setCellValue(toPlanStatusText(r.getPlanStatus()));
            row.createCell(10).setCellValue(r.getPortAreaNameZh() == null ? "" : r.getPortAreaNameZh());
            row.createCell(11).setCellValue(r.getWharfNameZh() == null ? "" : r.getWharfNameZh());
            row.createCell(12).setCellValue(toLocationDisplay(r.getStartLocationType(), r.getPlannedStartName()));
            row.createCell(13).setCellValue(toLocationDisplay(r.getEndLocationType(), r.getPlannedEndName()));
            row.createCell(14).setCellValue(r.getCreatedAt() == null ? "" : sdf.format(r.getCreatedAt()));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();

        String filename = "引航申请_" + System.currentTimeMillis() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        response.getOutputStream().write(out.toByteArray());
        response.flushBuffer();
    }

    private String toRequestStatusText(RequestStatusEnum status) {
        return status == null ? "" : status.getDescription();
    }

    private String toVoyageTypeText(Object voyageType) {
        if (voyageType == null) return "";
        if (voyageType instanceof VoyageTypeEnum e) return e.getDescription();
        String s = String.valueOf(voyageType).trim();
        if (s.isEmpty()) return "";
        if ("进港".equals(s) || "出港".equals(s) || "移泊".equals(s)) return s;
        try {
            return VoyageTypeEnum.valueOf(s.toUpperCase()).getDescription();
        } catch (Exception ignored) {
            return s;
        }
    }

    private String toPlanStatusText(String planStatusCode) {
        if (planStatusCode == null || planStatusCode.trim().isEmpty()) return "";
        PlanStatusEnum e = PlanStatusEnum.fromCode(planStatusCode.trim());
        return e == null ? planStatusCode : e.getName();
    }

    private String toLocationDisplay(LocationTypeEnum type, String name) {
        if (name == null || name.trim().isEmpty()) return "";
        if (type == null) return name;
        return type.getDescription() + "-" + name;
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除引航申请")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PILOTAGE:APPLICATIONS:DELETE')")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        log.info("批量删除引航申请: ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要删除的申请");
        }

        pilotageRequestsService.deleteBatchPilotAgeData(ids);

        log.info("批量引航申请删除成功: ids={}", ids);
        return Result.success();
    }

    @PostMapping("/batch/review")
    @Operation(summary = "批量审核引航申请")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PILOTAGE:APPLICATIONS:REVIEW')")
    public Result<Void> batchReview(@RequestBody List<Long> ids,
                                    @RequestParam Boolean approved,
                                    @RequestParam(required = false) String rejectReason) {
        log.info("批量审核引航申请: ids={}, approved={}", ids, approved);

        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要审核的申请");
        }

        RequestStatusEnum status = approved ? RequestStatusEnum.APPROVED : RequestStatusEnum.REJECTED;
        Long reviewerId = SecurityUtil.getCurrentUserId();
        Date reviewedAt = new Date();

        pilotageRequestsService.batchReviewRequests(ids, status, reviewerId, reviewedAt, rejectReason);

        log.info("批量引航申请审核完成: ids={}, status={}", ids, status);
        return Result.success();
    }

        @PutMapping("/status")
    @Operation(summary = "更新引航申请申请状态")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PILOTAGE:APPLICATIONS:REVIEW')")
    public Result<Void> updatePilotageRequestStatus(@RequestBody UpdateStatusDTO updateStatusDTO) {
        log.info("更新引航申请状态: ids={}, status={}", updateStatusDTO.getIds(), updateStatusDTO.getStatus());
        
        if (updateStatusDTO.getIds() == null || updateStatusDTO.getIds().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要更新状态的申请");
        }
        
        RequestStatusEnum status = RequestStatusEnum.fromDescription(updateStatusDTO.getStatus());
        pilotageRequestsService.updatePilotageRequestStatus(updateStatusDTO.getIds(), status);
        
        log.info("引航申请状态更新成功: ids={}, status={}", updateStatusDTO.getIds(), updateStatusDTO.getStatus());
        return Result.success();
    }
//
//        // 注意：这些字段在PilotageRequests实体中需要存在才能映射
//        // 如果PilotageRequests中没有这些字段，则需要通过vesselId关联查询
//
//        // 注意：这些字段在PilotageRequests实体中需要存在才能映射
//        // 如果PilotageRequests中没有这些字段，则需要通过voyageId关联查询
//
//        // 注意：这些字段在PilotageRequests实体中需要存在才能映射
//        // 如果PilotageRequests中没有这些字段，则需要通过关联查询
//
//        // 注意：这些字段在PilotageRequests实体中需要存在才能映射
//        // 如果PilotageRequests中没有这些字段，则需要通过关联查询
//
//        // 注意：这些字段在PilotageRequests实体中需要存在才能映射
//        // 如果PilotageRequests中没有这些字段，则需要通过关联查询
//    }

    /**
     * 映射实体类到VO
     * @param request
     * @param vo
     */
    private void mapEntityToVo(PilotageRequests request, PilotageRequestDTO vo) {
        vo.setId(request.getId());
        vo.setRequestCode(request.getRequestCode());
        vo.setVesselId(request.getVesselId());
        vo.setExpectedBerthId(request.getExpectedBerthId());
        vo.setBargeNumber(request.getBargeNumber());
        vo.setInboundDraftForward(request.getInboundDraftForward());
        vo.setInboundDraftAft(request.getInboundDraftAft());
        vo.setInboundCargoName(request.getInboundCargoName());
        vo.setInboundCargoTotalWeight(request.getInboundCargoTotalWeight());
        vo.setOutboundCargoName(request.getOutboundCargoName());
        vo.setOutboundCargoTotalWeight(request.getOutboundCargoTotalWeight());
        vo.setRequestNotes(request.getRequestNotes());
        vo.setApplicant(request.getApplicant());
        vo.setApplicationDate(request.getApplicationDate());
        vo.setRequestSource(request.getRequestSource());
        vo.setRequestStatus(request.getRequestStatus() != null ? request.getRequestStatus() : null);
        vo.setReviewerId(request.getReviewerId());
        vo.setReviewedAt(request.getReviewedAt());
        vo.setRejectReason(request.getRejectReason());
        vo.setPriority(request.getPriority() != null ? request.getPriority(): null);
        vo.setSpecialRequirements(request.getSpecialRequirements());
        vo.setCreatedAt(request.getCreatedAt());
        vo.setUpdatedAt(request.getUpdatedAt());

        // 注意：这些字段在PilotageRequests实体中需要存在才能映射
        // 如果PilotageRequests中没有这些字段，则需要通过vesselId关联查询

        // 注意：这些字段在PilotageRequests实体中需要存在才能映射
        // 如果PilotageRequests中没有这些字段，则需要通过voyageId关联查询

        // 注意：这些字段在PilotageRequests实体中需要存在才能映射
        // 如果PilotageRequests中没有这些字段，则需要通过关联查询

        // 注意：这些字段在PilotageRequests实体中需要存在才能映射
        // 如果PilotageRequests中没有这些字段，则需要通过关联查询

        // 注意：这些字段在PilotageRequests实体中需要存在才能映射
        // 如果PilotageRequests中没有这些字段，则需要通过关联查询
    }
}