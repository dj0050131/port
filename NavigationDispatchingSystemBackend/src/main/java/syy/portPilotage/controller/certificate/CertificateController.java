package syy.portPilotage.controller.certificate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.certificate.*;
import syy.portPilotage.service.PilotageCertificatesService;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pilotage/certificate")
@RequiredArgsConstructor
@Tag(name = "签证单管理", description = "引航签证单相关接口")
public class CertificateController {

    private final PilotageCertificatesService pilotageCertificatesService;

    @PostMapping("/create")
    @Operation(summary = "创建签证单")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<CertificateResultDTO> createCertificate(@Valid @RequestBody CertificateCreateRequest request) {
        log.info("创建签证单: planId={}", request.getPlanId());

        CertificateResultDTO result = pilotageCertificatesService.createCertificateFromPlan(request);
        return Result.success(result);
    }

    @PostMapping("/create/auto/{planId}")
    @Operation(summary = "自动补生成签证单（用于已结束但缺失签证单的数据补救）")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public Result<CertificateResultDTO> createCertificateAuto(@PathVariable Long planId) {
        log.info("自动补生成签证单: planId={}", planId);
        CertificateResultDTO result = pilotageCertificatesService.createCertificateFromPlanAuto(planId);
        return Result.success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获取签证单列表")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER', 'PILOT', 'AGENT') or hasAuthority('PILOTAGE:VISA:VIEW') or hasAuthority('BILLING:VISA_LIST:VIEW') or hasAuthority('BILLING:CHARGING:MANAGE')")
    public Result<Map<String, Object>> getCertificateList(@Valid CertificateQueryDTO query) {
        log.info("获取签证单列表: page={}, size={}, vesselNameZh={}",
                query.getPage(), query.getSize(), query.getVesselNameZh());

        Map<String, Object> result = pilotageCertificatesService.getCertificateList(query);
        return Result.success(result);
    }

    @GetMapping("/export")
    @Operation(summary = "导出签证单")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','FINANCE') or hasAuthority('PILOTAGE:VISA:VIEW') or hasAuthority('BILLING:VISA_LIST:VIEW')")
    public void exportCertificates(@RequestParam(required = false) String ids,
                                   @Valid CertificateQueryDTO query,
                                   HttpServletResponse response) throws IOException {
        log.info("导出签证单: query={}", query);
        pilotageCertificatesService.exportCertifivates(ids,query,response);
    }

    @GetMapping("/detail/{certificateId}")
    @Operation(summary = "获取签证单详情")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER', 'PILOT', 'AGENT','FINANCE')")
    public Result<CertificateDetailDTO> getCertificateDetail(@PathVariable Long certificateId) {
        log.info("获取签证单详情: certificateId={}", certificateId);
        CertificateDetailDTO detail = pilotageCertificatesService.getCertificateDetail(certificateId);
        return Result.success(detail);
    }

    @PutMapping("/update/{certificateId}")
    @Operation(summary = "更新签证单信息")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER', 'PILOT')")
    public Result<Boolean> updateCertificate(@PathVariable Long certificateId,
                                             @Valid @RequestBody CertificateUpdateDTO dto) {
        log.info("更新签证单信息: certificateId={}", certificateId);
        boolean success = pilotageCertificatesService.updateCertificate(certificateId, dto);
        return success ? Result.success(true) : Result.error("更新签证单失败");
    }

    @PostMapping("/submit/{certificateId}")
    @Operation(summary = "提交签证单")
    @PreAuthorize("hasAnyRole('ADMIN','PILOT','FINANCE','DISPATCHER')")
    public Result<Boolean> submitCertificate(@PathVariable Long certificateId,
                                             @RequestBody(required = false) CertificateSubmitDTO dto) {
        log.info("提交签证单: certificateId={}", certificateId);

        boolean success = pilotageCertificatesService.submitCertificate(certificateId, dto);
        return success ? Result.success(true) : Result.error("提交签证单失败");
    }

    @PostMapping("/confirm/{certificateId}")
    @Operation(summary = "确认签证单")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','FINANCE')")
    public Result<Boolean> confirmCertificate(@PathVariable Long certificateId) {
        log.info("确认签证单: certificateId={}", certificateId);

        boolean success = pilotageCertificatesService.confirmCertificate(certificateId);
        return success ? Result.success(true) : Result.error("确认签证单失败");
    }

    @PostMapping("/sign/{certificateId}")
    @Operation(summary = "引航员签字")
    @PreAuthorize("hasAnyRole('ADMIN','PILOT')")
    public Result<Boolean> pilotSignCertificate(@PathVariable Long certificateId) {
        log.info("引航员签字: certificateId={}", certificateId);

        boolean success = pilotageCertificatesService.pilotSignCertificate(certificateId);
        return success ? Result.success(true) : Result.error("引航员签字失败");
    }

    @GetMapping("/plan/{planId}")
    @Operation(summary = "根据计划获取签证单")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER', 'PILOT', 'AGENT','FINANCE')")
    public Result<CertificateDetailDTO> getCertificateByPlanId(@PathVariable Long planId) {
        log.info("根据计划获取签证单: planId={}", planId);

        CertificateDetailDTO detail = pilotageCertificatesService.getCertificateByPlanId(planId);
        return Result.success(detail);
    }

     @DeleteMapping("/delete/{certificateId}")
     @Operation(summary = "删除签证单")
      @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','FINANCE')")
     public Result<Boolean> deleteCertificate(@PathVariable Long certificateId) {
         log.info("删除签证单: certificateId={}", certificateId);
         boolean success = pilotageCertificatesService.deleteCertificate(certificateId);
         return Result.success(success);
     }
}