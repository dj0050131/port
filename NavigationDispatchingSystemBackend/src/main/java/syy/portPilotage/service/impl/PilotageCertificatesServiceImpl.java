package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.certificate.*;
import syy.portPilotage.entity.*;
import syy.portPilotage.enums.LocationTypeEnum;
import syy.portPilotage.enums.PlanStatusEnum;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.mapper.PilotageCertificatesMapper;
import syy.portPilotage.mapper.PilotagePlansMapper;
import syy.portPilotage.service.*;
import syy.portPilotage.util.BeanCopyUtil;
import syy.portPilotage.util.CodeGeneratorUtil;
import syy.portPilotage.util.SecurityUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PilotageCertificatesServiceImpl extends ServiceImpl<PilotageCertificatesMapper, PilotageCertificates>
        implements PilotageCertificatesService {

    private final PilotagePlansMapper pilotagePlansMapper;
    private final VesselsService vesselsService;
    private final VoyagesService voyagesService;
    private final AgentsService agentsService;
    private final PilotsService pilotsService;
    private final BerthsService berthsService;
    private final AnchoragesService anchoragesService;
    private final WharvesService wharvesService;
    private final PortAreasService portAreasService;
    private final PortsService portsService;
    private final CodeGeneratorUtil codeGeneratorUtil;
    private final PilotageRoutesService pilotageRoutesService;
    private final PilotageCertificatesMapper pilotageCertificatesMapper;

    private static final String AUTO_ROUTE_CODE = "AUTO";

    private void assertRouteReadyForWorkflow(PilotageCertificates certificate, String actionName) {
        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }
        String code = certificate.getRouteCode();
        String name = certificate.getRouteName();
        boolean codeEmpty = (code == null || code.trim().isEmpty());
        boolean nameEmpty = (name == null || name.trim().isEmpty());
        boolean isAuto = (!codeEmpty && AUTO_ROUTE_CODE.equalsIgnoreCase(code.trim()));
        if (codeEmpty || nameEmpty || isAuto) {
            throw new BusinessException(
                    ErrorCode.OPERATION_NOT_ALLOWED,
                    "航线区间信息未补齐，禁止" + actionName + "。请先在签证单中选择/补录航线区间（routeId 或 routeCode/routeName）后再继续。"
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CertificateResultDTO createCertificateFromPlan(CertificateCreateRequest request) {
        log.info("创建签证单: planId={}", request.getPlanId());

        CertificateCreateDTO dto = request.getCertificateData();

        Long planId = request.getPlanId();

        PilotagePlans plan = pilotagePlansMapper.selectById(planId);

        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        if (!PlanStatusEnum.COMPLETED.getCode().equals(plan.getPlanStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID,
                    "只有已完成的计划才能生成签证单");
        }

        PilotageCertificates existingCertificate = this.lambdaQuery()
                .eq(PilotageCertificates::getPlanId, planId)
                .one();
        if (existingCertificate != null) {
            log.warn("计划{}已经存在签证单，签证单ID={}", planId, existingCertificate.getId());
            // 兼容：如果签证单已存在但计划状态仍停留在 COMPLETED，则推进到 CERTIFICATED
            if (!PlanStatusEnum.CERTIFICATED.getCode().equals(plan.getPlanStatus())) {
                plan.setPlanStatus(PlanStatusEnum.CERTIFICATED.getCode());
                plan.setUpdatedAt(new Date());
                pilotagePlansMapper.updateById(plan);
            }
            CertificateResultDTO result = BeanCopyUtil.copy(existingCertificate, CertificateResultDTO.class);
            result.setSuccess(true);
            result.setMessage("签证单已存在");
            return result;
        }

        Vessels vessel = vesselsService.getById(plan.getVesselId());
        if (vessel == null) {
            throw new BusinessException(ErrorCode.VESSEL_NOT_FOUND);
        }

        String startType = plan.getStartLocationType() == null ? null : plan.getStartLocationType().name();
        String endType = plan.getEndLocationType() == null ? null : plan.getEndLocationType().name();

        LambdaQueryWrapper<PilotageRoutes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PilotageRoutes::getStartLocationId, plan.getStartLocationId());
        queryWrapper.eq(PilotageRoutes::getStartLocationType, startType);
        queryWrapper.eq(PilotageRoutes::getEndLocationId, plan.getEndLocationId());
        queryWrapper.eq(PilotageRoutes::getEndLocationType, endType);
        PilotageRoutes pilotageRoutes = pilotageRoutesService.getOne(queryWrapper);
        if (pilotageRoutes == null) {
            log.warn(
                    "未匹配到航线，将使用计划起止位置作为回退: planId={}, start={}:{}, end={}:{}",
                    planId,
                    startType, plan.getStartLocationId(),
                    endType, plan.getEndLocationId()
            );
        }

        Voyages voyage = voyagesService.getById(plan.getVoyageId());
        if (voyage == null) {
            throw new BusinessException(ErrorCode.VOYAGE_NOT_FOUND);
        }

        PilotageCertificates certificate = new PilotageCertificates();

        String certificateCode = dto.getCertificateCode() != null ? dto.getCertificateCode() :
                codeGeneratorUtil.generateCertificateCode();

        certificate.setOperationId(codeGeneratorUtil.generateOperationId()); //作业编号
        certificate.setCertificateCode(certificateCode);

        certificate.setPlanId(planId);
        certificate.setVesselId(plan.getVesselId());
        certificate.setVoyageId(plan.getVoyageId());

        // 航线信息：优先使用匹配到的航线；否则用起止位置回退，避免空指针并保证可继续后续流程
        if (pilotageRoutes != null) {
            certificate.setRouteCode(pilotageRoutes.getRouteCode());
            certificate.setRouteName(pilotageRoutes.getRouteName());
        } else {
            certificate.setRouteCode(AUTO_ROUTE_CODE);
            String startName = plan.getStartLocationName() == null ? "" : plan.getStartLocationName();
            String endName = plan.getEndLocationName() == null ? "" : plan.getEndLocationName();
            String routeName = (startName.isEmpty() && endName.isEmpty()) ? "自动生成航线" : (startName + "→" + endName);
            certificate.setRouteName(routeName);
        }

        certificate.setVesselNameZh(vessel.getNameZh());
        certificate.setVesselNameEn(vessel.getNameEn());

        
        certificate.setInboundVoyage(voyage.getVoyageNumber());
        certificate.setOutboundVoyage(voyage.getVoyageNumber());

        certificate.setPilotageType(dto.getPilotageType());
        certificate.setForwardDraft(dto.getForwardDraft());
        certificate.setAftDraft(dto.getAftDraft());
        certificate.setHorsepower(dto.getHorsepower());

        certificate.setPilotageStartTime(dto.getPilotageStartTime() != null ? dto.getPilotageStartTime() : plan.getActualStartTime());
        certificate.setPilotageEndTime(dto.getPilotageEndTime() != null ? dto.getPilotageEndTime() : plan.getActualEndTime());
        certificate.setBoardingTime(dto.getBoardingTime() != null ? dto.getBoardingTime() : plan.getBoardingTime());
        certificate.setLeavingTime(dto.getLeavingTime() != null ? dto.getLeavingTime() : plan.getLeavingTime());

        certificate.setTotalPassengers(dto.getTotalPassengers() != null ? dto.getTotalPassengers() : 0);
        certificate.setTotalCrew(dto.getTotalCrew() != null ? dto.getTotalCrew() : 0);

        if (voyage.getAgentId() != null) {
            certificate.setInboundAgentId(voyage.getAgentId());
            certificate.setOutboundAgentId(voyage.getAgentId());
        }

        if (plan.getStartLocationId() != null) {
            certificate.setStartLocationId(plan.getStartLocationId());
            certificate.setStartLocationType(plan.getStartLocationType());
            certificate.setStartLocationName(plan.getStartLocationName());
        }
        if (plan.getEndLocationId() != null) {
            certificate.setEndLocationId(plan.getEndLocationId());
            certificate.setEndLocationType(plan.getEndLocationType());
            certificate.setEndLocationName(plan.getEndLocationName());
        }

        if (dto.getEnteredBy() == null || dto.getEnteredBy().isEmpty()){
            certificate.setEnteredBy("系统生成");
        }//录入人

        certificate.setBerthingAssistanceFlag(dto.getBerthingAssistanceFlag());
        certificate.setBerthingAssistanceCharge(dto.getBerthingAssistanceCharge());
        certificate.setDetentionFlag(dto.getDetentionFlag());
        certificate.setDetentionHours(dto.getDetentionHours());
        certificate.setDetentionPersons(dto.getDetentionPersons());
        certificate.setNotes(dto.getNotes());
        certificate.setSpecialSituationDescription(dto.getSpecialSituationDescription());

        
        certificate.setTradeType("外贸");//贸易类型
        certificate.setEscortFlag("N");//护航标记
        certificate.setBerthingAssistanceFlag("N");//引航助航标记
        certificate.setBerthingAssistanceCharge(BigDecimal.ZERO);//引航助航费
        certificate.setWithdrawalFlag("N");//撤回标记
        certificate.setHorsepower(BigDecimal.ZERO);//马力
        certificate.setDetentionFlag("N");//滞留标记
        certificate.setDetentionHours(BigDecimal.ZERO);//滞留小时数
        certificate.setDetentionPersons(0);//滞留人数
        certificate.setSpecialSituationDescription("暂无说明");//特殊情况说明

        certificate.setStatusCode("PENDING");
        certificate.setStatusName("待提交");
        certificate.setSubmitStatus("UNSUBMITTED");
        certificate.setBillingStatus("UNBILLED");

        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId != null) {
            certificate.setCreatedBy(currentUserId);
            certificate.setUpdatedBy(currentUserId);
        }

        certificate.setCreatedAt(new Date());
        certificate.setUpdatedAt(new Date());

        boolean success = this.save(certificate);

        CertificateResultDTO result = new CertificateResultDTO();

        if (success) {
            log.info("签证单生成成功: planId={}, certificateId={}, certificateCode={}",
                    planId, certificate.getId(), certificateCode);

            plan.setPlanStatus(PlanStatusEnum.CERTIFICATED.getCode());
            plan.setUpdatedAt(new Date());
            pilotagePlansMapper.updateById(plan);

            log.info("计划状态已更新为已签证: planId={}, planCode={}",
                    planId, plan.getPlanCode());

            result.setId(certificate.getId());
            result.setCertificateCode(certificateCode);
            result.setPlanId(planId);
            result.setVesselId(plan.getVesselId());
            result.setVoyageId(plan.getVoyageId());
            result.setSubmitStatus("UNSUBMITTED");
            result.setBillingStatus("UNBILLED");
            result.setCreatedAt(certificate.getCreatedAt());
            result.setSuccess(true);
            result.setMessage("签证单创建成功");
        } else {
            result.setSuccess(false);
            result.setMessage("签证单创建失败");
            throw new BusinessException(ErrorCode.CERTIFICATE_CREATE_FAILED, "签证单创建失败");
        }

        return result;
    }

    @Override
    public Map<String, Object> getCertificateList(CertificateQueryDTO query) {
        log.info("获取签证单列表: query={}", query);

        Map<String, Object> params = new HashMap<>();
        params.put("vesselNameZh", query.getVesselNameZh());
        params.put("vesselNameEn", query.getVesselNameEn());
        params.put("vesselCallSign", query.getVesselCallSign());
        params.put("imoNumber", query.getImoNumber());
        params.put("submitStatus", query.getSubmitStatus());
        params.put("billingStatus", query.getBillingStatus());
        params.put("pilotId", query.getPilotId());
        params.put("agentId", query.getAgentId());
        params.put("certificateCode", query.getCertificateCode());
        params.put("voyageNumber", query.getVoyageNumber());

        // 时间范围查询
        if (query.getStartDate() != null) {
            params.put("startDate", query.getStartDate());
        }
        if (query.getEndDate() != null) {
            params.put("endDate", query.getEndDate());
        }

        // 使用分页查询
        Page<CertificateDetailDTO> page = new Page<>(query.getPage(), query.getSize());
        IPage<CertificateDetailDTO> pageResult = baseMapper.selectCertificatePage(page, params);

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("pages", pageResult.getPages());

        return result;
    }

    @Override
    public CertificateDetailDTO getCertificateDetail(Long certificateId) {
        log.info("获取签证单详情: certificateId={}", certificateId);

        PilotageCertificates certificate = getById(certificateId);
        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }

        CertificateDetailDTO detail = BeanCopyUtil.copy(certificate, CertificateDetailDTO.class);
        if (detail != null) {
            Object submitStatusObj = certificate.getSubmitStatus();
            Object billingStatusObj = certificate.getBillingStatus();
            detail.setSubmitStatus(submitStatusObj == null ? null : String.valueOf(submitStatusObj));
            detail.setBillingStatus(billingStatusObj == null ? null : String.valueOf(billingStatusObj));
        }

        Vessels vessel = vesselsService.getById(certificate.getVesselId());
        if (vessel != null) {
            detail.setVesselNameZh(vessel.getNameZh());
            detail.setVesselNameEn(vessel.getNameEn());
            detail.setImoNumber(vessel.getImoNumber());
            detail.setVesselCallSign(vessel.getVesselCallSign());
            detail.setMmsiNumber(vessel.getMmsiNumber());
            detail.setVesselType(vessel.getVesselType() != null ? vessel.getVesselType().toString() : null);
            detail.setNationality(vessel.getNationality());
            detail.setVesselLength(vessel.getVesselLength());
            detail.setVesselWidth(vessel.getVesselWidth());
            detail.setGrossTonnage(vessel.getGrossTonnage());
            detail.setNetTonnage(vessel.getNetTonnage());
            detail.setDeadweight(vessel.getDeadweight());
            detail.setFullLoadDraft(vessel.getFullLoadDraft());
            detail.setAirDraft(vessel.getAirDraft());
            detail.setSpeed(vessel.getSpeed());
        }

        if (certificate.getVoyageId() != null) {
            Voyages voyage = voyagesService.getById(certificate.getVoyageId());
            if (voyage != null) {
                detail.setVoyageNumber(voyage.getVoyageNumber());
                detail.setVoyageType(String.valueOf(voyage.getVoyageType()));
                detail.setCargoName(voyage.getCargoName());
                detail.setCargoQuantity(voyage.getCargoQuantity());
                detail.setCargoUnit(voyage.getCargoUnit());
                detail.setDraftForward(voyage.getDraftForward());
                detail.setDraftAft(voyage.getDraftAft());
            }
        }

        if (certificate.getInboundAgentId() != null) {
            Agents inboundAgent = agentsService.getById(certificate.getInboundAgentId());
            if (inboundAgent != null) {
                detail.setInboundAgentId(inboundAgent.getId());
                detail.setInboundAgentCode(inboundAgent.getAgentCode());
                detail.setInboundAgentName(inboundAgent.getCompanyName());
            }
        }

        if (certificate.getOutboundAgentId() != null) {
            Agents outboundAgent = agentsService.getById(certificate.getOutboundAgentId());
            if (outboundAgent != null) {
                detail.setOutboundAgentId(outboundAgent.getId());
                detail.setOutboundAgentCode(outboundAgent.getAgentCode());
                detail.setOutboundAgentName(outboundAgent.getCompanyName());
            }
        }

        // 位置信息（支持 BERTH/ANCHORAGE/WHARF/PORT_AREA/PORT/OTHER）
        detail.setStartLocationId(certificate.getStartLocationId());
        detail.setEndLocationId(certificate.getEndLocationId());
        detail.setStartLocationType(certificate.getStartLocationType() != null ? certificate.getStartLocationType().name() : null);
        detail.setEndLocationType(certificate.getEndLocationType() != null ? certificate.getEndLocationType().name() : null);
        detail.setStartLocationName(certificate.getStartLocationName());
        detail.setEndLocationName(certificate.getEndLocationName());
        detail.setStartLocationFullName(resolveLocationFullName(certificate.getStartLocationType(), certificate.getStartLocationId(), certificate.getStartLocationName()));
        detail.setEndLocationFullName(resolveLocationFullName(certificate.getEndLocationType(), certificate.getEndLocationId(), certificate.getEndLocationName()));

        // 航线信息（签证单冗余字段）
        detail.setRouteCode(certificate.getRouteCode());
        detail.setRouteName(certificate.getRouteName());

        // 引航信息
        detail.setTradeType(certificate.getTradeType());

        if (certificate.getPlanId() != null) {
            PilotagePlans plan = pilotagePlansMapper.selectById(certificate.getPlanId());
            if (plan != null && plan.getAssignedPilotId() != null) {
                Pilots pilot = pilotsService.getById(plan.getAssignedPilotId());
                if (pilot != null) {
                    detail.setAssignedPilotId(pilot.getId());
                    detail.setAssignedPilotName(pilot.getPilotName());
                }
            }
        }

        return detail;
    }

    private String resolveLocationFullName(LocationTypeEnum type, Long id, String fallback) {
        if (type == null || id == null) return fallback;
        try {
            switch (type) {
                case BERTH -> {
                    Berths berth = berthsService.getById(id);
                    return berth != null ? berth.getBerthName() : fallback;
                }
                case ANCHORAGE -> {
                    Anchorages a = anchoragesService.getById(id);
                    return a != null ? a.getAnchorageNameZh() : fallback;
                }
                case WHARF -> {
                    Wharves w = wharvesService.getById(id);
                    return w != null ? w.getWharfNameZh() : fallback;
                }
                case PORT_AREA -> {
                    PortAreas pa = portAreasService.getById(id);
                    return pa != null ? pa.getAreaNameZh() : fallback;
                }
                default -> {
                    Ports p = portsService.getById(id);
                    return p != null ? p.getPortNameZh() : fallback;
                }
            }
        } catch (Exception e) {
            return fallback;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCertificate(Long certificateId, CertificateUpdateDTO dto) {
        log.info("更新签证单信息: certificateId={}, dto={}", certificateId, dto);

        PilotageCertificates certificate = getById(certificateId);
        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }

        if (!"UNSUBMITTED".equals(certificate.getSubmitStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID, "只有未提交的签证单才能修改");
        }

        // 航线信息补录：优先按 routeId 选择航线区间；否则允许直接补录 code/name
        if (dto.getRouteId() != null) {
            PilotageRoutes route = pilotageRoutesService.getById(dto.getRouteId());
            if (route == null) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "未找到航线区间信息");
            }
            certificate.setRouteCode(route.getRouteCode());
            certificate.setRouteName(route.getRouteName());
        } else {
            if (dto.getRouteCode() != null) {
                String v = dto.getRouteCode().trim();
                certificate.setRouteCode(v.isEmpty() ? null : v);
            }
            if (dto.getRouteName() != null) {
                String v = dto.getRouteName().trim();
                certificate.setRouteName(v.isEmpty() ? null : v);
            }
        }

        if (dto.getForwardDraft() != null) {
            certificate.setForwardDraft(dto.getForwardDraft());
        }
        if (dto.getAftDraft() != null) {
            certificate.setAftDraft(dto.getAftDraft());
        }
        if (dto.getHorsepower() != null) {
            certificate.setHorsepower(dto.getHorsepower());
        }
        if (dto.getPilotageStartTime() != null) {
            certificate.setPilotageStartTime(dto.getPilotageStartTime());
        }
        if (dto.getPilotageEndTime() != null) {
            certificate.setPilotageEndTime(dto.getPilotageEndTime());
        }
        if (dto.getBoardingTime() != null) {
            certificate.setBoardingTime(dto.getBoardingTime());
        }
        if (dto.getLeavingTime() != null) {
            certificate.setLeavingTime(dto.getLeavingTime());
        }
        if (dto.getTotalPassengers() != null) {
            certificate.setTotalPassengers(dto.getTotalPassengers());
        }
        if (dto.getTotalCrew() != null) {
            certificate.setTotalCrew(dto.getTotalCrew());
        }
        if (dto.getDetentionFlag() != null) {
            certificate.setDetentionFlag(dto.getDetentionFlag());
        }
        if (dto.getDetentionHours() != null) {
            certificate.setDetentionHours(dto.getDetentionHours());
        }
        if (dto.getDetentionPersons() != null) {
            certificate.setDetentionPersons(dto.getDetentionPersons());
        }
        if (dto.getNotes() != null) {
            certificate.setNotes(dto.getNotes());
        }
        if (dto.getSpecialSituationDescription() != null) {
            certificate.setSpecialSituationDescription(dto.getSpecialSituationDescription());
        }
        if (dto.getBerthingAssistanceFlag() != null) {
            certificate.setBerthingAssistanceFlag(dto.getBerthingAssistanceFlag());
        }
        if (dto.getBerthingAssistanceCharge() != null) {
            certificate.setBerthingAssistanceCharge(dto.getBerthingAssistanceCharge());
        }

        certificate.setUpdatedAt(new Date());
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId != null) {
            certificate.setUpdatedBy(currentUserId);
        }

        return updateById(certificate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitCertificate(Long certificateId, CertificateSubmitDTO dto) {
        log.info("提交签证单: certificateId={}", certificateId);

        PilotageCertificates certificate = getById(certificateId);
        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }

        if (!"UNSUBMITTED".equals(certificate.getSubmitStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID, "只有未提交的签证单才能提交");
        }

        // 最稳策略：允许无区间先生成签证单，但未补齐航线区间信息前禁止进入提交/计费流程
        assertRouteReadyForWorkflow(certificate, "提交");

        certificate.setSubmitStatus("SUBMITTED");
        certificate.setSubmittedAt(new Date());
        certificate.setStatusCode("SUBMITTED");
        certificate.setStatusName("已提交");

        if (dto != null && dto.getNotes() != null && !dto.getNotes().isEmpty()) {
            certificate.setNotes(dto.getNotes());
        }

        certificate.setUpdatedAt(new Date());
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId != null) {
            certificate.setUpdatedBy(currentUserId);
        }

        return updateById(certificate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmCertificate(Long certificateId) {
        log.info("确认签证单: certificateId={}", certificateId);

        PilotageCertificates certificate = getById(certificateId);
        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }

        if (!"SUBMITTED".equals(certificate.getSubmitStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID, "只有已提交的签证单才能确认");
        }

        assertRouteReadyForWorkflow(certificate, "确认");

        // 业务约束：必须先由引航员签字，代理才能确认
        if (certificate.getPilotSignTime() == null) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID, "引航员未签字，代理不能确认");
        }

        certificate.setSubmitStatus("CONFIRMED");
        certificate.setConfirmedAt(new Date());
        certificate.setStatusCode("CONFIRMED");
        certificate.setStatusName("已确认");
        certificate.setAgentConfirmTime(new Date());

        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId != null) {
            certificate.setConfirmedBy(currentUserId);
            certificate.setUpdatedBy(currentUserId);
        }

        certificate.setUpdatedAt(new Date());

        boolean result = updateById(certificate);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pilotSignCertificate(Long certificateId) {
        log.info("引航员签字: certificateId={}", certificateId);

        PilotageCertificates certificate = getById(certificateId);
        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }

        if (!"SUBMITTED".equals(certificate.getSubmitStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID, "只有已提交的签证单才能签字");
        }

        certificate.setPilotSignTime(new Date());

        certificate.setUpdatedAt(new Date());
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId != null) {
            certificate.setUpdatedBy(currentUserId);
        }

        return updateById(certificate);
    }

    @Override
    public CertificateDetailDTO getCertificateByPlanId(Long planId) {
        log.info("根据计划获取签证单: planId={}", planId);

        PilotageCertificates certificate = this.lambdaQuery()
                .eq(PilotageCertificates::getPlanId, planId)
                .one();

        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }

        return getCertificateDetail(certificate.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VoucherResultDTO createBillingVoucher(VoucherCreateRequest request) {
        log.info("创建计费凭证: certificateId={}", request.getCertificateId());

        Long certificateId = request.getCertificateId();
        VoucherCreateDTO dto = request.getVoucherData();

        PilotageCertificates certificate = getById(certificateId);
        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }

        if (!"CONFIRMED".equals(certificate.getSubmitStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID, "只有已确认的签证单才能创建计费凭证");
        }

        // 这里需要调用计费凭证服务，暂时省略

        // 这里需要调用计费凭证服务，暂时返回模拟数据
        VoucherResultDTO result = new VoucherResultDTO();
        result.setId(1L); // 模拟ID
        result.setVoucherCode(dto.getVoucherCode() != null ? dto.getVoucherCode() : "VOUCHER-001");
        result.setCertificateId(certificateId);
        result.setVoyageId(certificate.getVoyageId());
        result.setVesselId(certificate.getVesselId());
        result.setAuditStatus("PENDING");
        result.setPaymentStatus("UNPAID");
        result.setTotalAmount(null); // 实际需要计算
        result.setDueDate(null); // 实际需要设置
        result.setCreatedAt(new Date());
        result.setSuccess(true);
        result.setMessage("计费凭证创建成功");

        certificate.setBillingStatus("BILLED");
        certificate.setUpdatedAt(new Date());
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId != null) {
            certificate.setUpdatedBy(currentUserId);
        }
        updateById(certificate);

        log.info("计费凭证创建成功: certificateId={}, voucherCode={}", certificateId, result.getVoucherCode());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndConfirmDispatch(Long planId) {
        log.info("检查并确认调度: planId={}", planId);

        PilotagePlans plan = pilotagePlansMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        // 检查当前状态是否为"已分配"或类似状态
        if (!"PLANNED".equals(plan.getPlanStatus())) {
            throw new BusinessException(ErrorCode.PLAN_STATUS_INVALID,
                    "只有已计划的计划才能进行确认操作");
        }

        plan.setPlanStatus(PlanStatusEnum.CONFIRMED.getCode());
        plan.setUpdatedAt(new Date());

        boolean success = pilotagePlansMapper.updateById(plan) > 0;

        if (success) {
            log.info("调度确认成功: planId={}, planCode={}",
                    planId, plan.getPlanCode());
            return true;
        } else {
            log.error("调度确认失败: planId={}", planId);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CertificateResultDTO createCertificateFromPlanAuto(Long planId) {
        log.info("自动创建签证单: planId={}", planId);

        PilotagePlans plan = pilotagePlansMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.PLAN_NOT_FOUND);
        }

        Voyages voyage = voyagesService.getById(plan.getVoyageId());

        CertificateCreateRequest request = new CertificateCreateRequest();
        request.setPlanId(planId);

        CertificateCreateDTO certificateData = new CertificateCreateDTO();
        certificateData.setPlanId(planId);
        certificateData.setPilotageType(plan.getOperationTypeFlag() == null || plan.getOperationTypeFlag().trim().isEmpty()
                ? "STANDARD"
                : plan.getOperationTypeFlag().trim());
        certificateData.setPilotageStartTime(plan.getActualStartTime());
        certificateData.setPilotageEndTime(plan.getActualEndTime());
        certificateData.setBoardingTime(plan.getBoardingTime());
        certificateData.setLeavingTime(plan.getLeavingTime());

        if (voyage != null) {
            certificateData.setForwardDraft(voyage.getDraftForward() != null ?
                    BigDecimal.valueOf(voyage.getDraftForward().doubleValue()) : BigDecimal.valueOf(10.0));
            certificateData.setAftDraft(voyage.getDraftAft() != null ?
                    BigDecimal.valueOf(voyage.getDraftAft().doubleValue()) : BigDecimal.valueOf(10.0));
            certificateData.setTotalPassengers(voyage.getTotalPassengers());
            certificateData.setTotalCrew(voyage.getTotalCrew());
        } else {
            certificateData.setForwardDraft(BigDecimal.valueOf(10.0));
            certificateData.setAftDraft(BigDecimal.valueOf(10.0));
        }

        certificateData.setNotes("自动生成的签证单");

        request.setCertificateData(certificateData);

        return createCertificateFromPlan(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCertificate(Long certificateId) {
        log.info("删除签证单: certificateId={}", certificateId);

        PilotageCertificates certificate = getById(certificateId);
        if (certificate == null) {
            throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND);
        }

        if (!"UNSUBMITTED".equals(certificate.getSubmitStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只有未提交的签证单才能删除");
        }

        boolean result = removeById(certificateId);

        if (result) {
            log.info("签证单删除成功: certificateId={}", certificateId);
        } else {
            log.error("签证单删除失败: certificateId={}", certificateId);
        }

        return result;
    }

    @Override
    public void exportCertifivates(String ids, CertificateQueryDTO query, HttpServletResponse response) throws IOException {
        List<CertificateDetailDTO> records;
        if (ids != null && !ids.trim().isEmpty()) {
            records = new java.util.ArrayList<>();
            for (String s : ids.split(",")) {
                String t = s == null ? "" : s.trim();
                if (t.isEmpty()) continue;
                try {
                    Long id = Long.valueOf(t);
                    CertificateDetailDTO detail = this.getCertificateDetail(id);
                    if (detail != null) records.add(detail);
                } catch (Exception ignore) {
                    // ignore invalid id
                }
            }
        } else {
            // 导出默认取全量（上限 10000）
            query.setPage(1);
            query.setSize(10000);
            Map<String, Object> result = this.getCertificateList(query);
            @SuppressWarnings("unchecked")
            List<CertificateDetailDTO> list = (List<CertificateDetailDTO>) result.get("records");
            records = list;
        }

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("签证单");

        String[] headers = new String[] {
                "签证单号", "提交状态", "计费状态", "中文船名", "英文船名",
                "船舶呼号", "IMO编号", "航次号", "航次类型", "引航员",
                "起始位置", "结束位置", "创建时间"
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

        // 回填 submit/billing 状态：实体字段是 Object，部分场景下 DTO 映射会为空
        Map<Long, PilotageCertificates> entityMap = java.util.Collections.emptyMap();
        if (records != null && !records.isEmpty()) {
            List<Long> recordIds = records.stream().map(CertificateDetailDTO::getId).filter(java.util.Objects::nonNull).collect(Collectors.toList());
            if (!recordIds.isEmpty()) {
                entityMap = this.listByIds(recordIds).stream()
                        .collect(Collectors.toMap(PilotageCertificates::getId, e -> e, (a, b) -> a));
            }
        }

        if (records != null) {
            for (int i = 0; i < records.size(); i++) {
                CertificateDetailDTO c = records.get(i);
                Row row = sheet.createRow(i + 1);
                String submitStatus = c.getSubmitStatus();
                String billingStatus = c.getBillingStatus();
                if ((submitStatus == null || submitStatus.trim().isEmpty()) || (billingStatus == null || billingStatus.trim().isEmpty())) {
                    PilotageCertificates entity = c.getId() == null ? null : entityMap.get(c.getId());
                    if (entity != null) {
                        if (submitStatus == null || submitStatus.trim().isEmpty()) {
                            Object v = entity.getSubmitStatus();
                            submitStatus = (v == null ? "" : String.valueOf(v));
                        }
                        if (billingStatus == null || billingStatus.trim().isEmpty()) {
                            Object v = entity.getBillingStatus();
                            billingStatus = (v == null ? "" : String.valueOf(v));
                        }
                    }
                }

                row.createCell(0).setCellValue(c.getCertificateCode() == null ? "" : c.getCertificateCode());
                row.createCell(1).setCellValue(submitStatus == null ? "" : submitStatus);
                row.createCell(2).setCellValue(billingStatus == null ? "" : billingStatus);
                row.createCell(3).setCellValue(c.getVesselNameZh() == null ? "" : c.getVesselNameZh());
                row.createCell(4).setCellValue(c.getVesselNameEn() == null ? "" : c.getVesselNameEn());
                row.createCell(5).setCellValue(c.getVesselCallSign() == null ? "" : c.getVesselCallSign());
                row.createCell(6).setCellValue(c.getImoNumber() == null ? "" : c.getImoNumber());
                row.createCell(7).setCellValue(c.getVoyageNumber() == null ? "" : c.getVoyageNumber());
                row.createCell(8).setCellValue(c.getVoyageType() == null ? "" : c.getVoyageType());
                row.createCell(9).setCellValue(c.getAssignedPilotName() == null ? "" : c.getAssignedPilotName());
                row.createCell(10).setCellValue(c.getStartLocationFullName() == null ? (c.getStartLocationName() == null ? "" : c.getStartLocationName()) : c.getStartLocationFullName());
                row.createCell(11).setCellValue(c.getEndLocationFullName() == null ? (c.getEndLocationName() == null ? "" : c.getEndLocationName()) : c.getEndLocationFullName());
                row.createCell(12).setCellValue(c.getCreatedAt() == null ? "" : sdf.format(c.getCreatedAt()));
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();

        String filename = "签证单_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        response.getOutputStream().write(out.toByteArray());
        response.flushBuffer();
    }

    @Override
    public IPage<CertificateDetailDTO> selectCertificatePage(Page<CertificateDetailDTO> page, Map<String, Object> params) {
        return pilotageCertificatesMapper.selectCertificatePage(page,params);
    }
}
