package syy.portPilotage.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.dto.statistics.*;
import syy.portPilotage.dto.statistics.MealDTO;
import syy.portPilotage.entity.*;
import syy.portPilotage.enums.RouteTypeEnum;
import syy.portPilotage.mapper.PilotageCertificatesMapper;
import syy.portPilotage.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PilotageCertificateStatisticsServiceImpl extends ServiceImpl<PilotageCertificatesMapper, PilotageCertificates>
        implements syy.portPilotage.service.PilotageCertificateStatisticsService {

    private final PilotagePlansService pilotagePlansService;
    private final VesselsService vesselsService;
    private final VoyagesService voyagesService;
    private final AgentsService agentsService;
    private final PilotsService pilotsService;
    private final BerthsService berthsService;
    private final TugboatsService tugboatsService;
    private final PlanTugboatAssignmentsService planTugboatAssignmentsService;
    private final PlanPilotAssignmentsService planPilotAssignmentsService;

    @Override
    public List<SafetyMonthlyDTO> getSafetyMonthlyReport(LocalDate startDate, LocalDate endDate) {
        log.info("获取船务安全科航行统计: startDate={}, endDate={}", startDate, endDate);
        
        LambdaQueryWrapper<PilotageCertificates> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotageCertificates::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        wrapper.orderByAsc(PilotageCertificates::getCreatedAt);
        
        List<PilotageCertificates> certificates = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();
        List<Pilots> pilots = pilotsService.list();
        
        List<SafetyMonthlyDTO> result = new ArrayList<>();
        for (PilotageCertificates cert : certificates) {
            SafetyMonthlyDTO dto = new SafetyMonthlyDTO();
            dto.setShipName(cert.getVesselNameZh());
            dto.setDate(DateUtil.formatDateTime(cert.getCreatedAt()));
            dto.setDynamicTime(DateUtil.formatDateTime(cert.getCreatedAt()));
            dto.setSailTime(DateUtil.formatDateTime(cert.getPilotageStartTime()));
            dto.setBerthTime(DateUtil.formatDateTime(cert.getPilotageEndTime()));
            dto.setTowedShip(cert.getVesselNameZh());
            dto.setDynamic(cert.getPilotageType());
            dto.setWaitTime("0小时");
            
            if (cert.getPlanId() != null) {
                PilotagePlans plan = pilotagePlansService.getById(cert.getPlanId());
                if (plan != null && plan.getAssignedPilotId() != null) {
                    Pilots pilot = pilots.stream()
                            .filter(p -> p.getId().equals(plan.getAssignedPilotId()))
                            .findFirst()
                            .orElse(null);
                    if (pilot != null) {
                        dto.setPilot(pilot.getPilotName());
                    }
                }
            }
            
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public List<PilotageMonthlyDTO> getPilotageMonthlyReport(LocalDate startDate, LocalDate endDate, String shipName, String pilot) {
        log.info("获取船舶引航统计(月份): startDate={}, endDate={}, shipName={}, pilot={}", startDate, endDate, shipName, pilot);
        
        LambdaQueryWrapper<PilotageCertificates> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotageCertificates::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        if (shipName != null && !shipName.isEmpty()) {
            wrapper.and(qw -> qw.like(PilotageCertificates::getVesselNameZh, shipName)
                    .or()
                    .like(PilotageCertificates::getVesselNameEn, shipName));
        }
        wrapper.orderByAsc(PilotageCertificates::getCreatedAt);
        
        List<PilotageCertificates> certificates = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();
        List<Pilots> pilots = pilotsService.list();
        
        List<PilotageMonthlyDTO> result = new ArrayList<>();
        for (PilotageCertificates cert : certificates) {
            PilotageMonthlyDTO dto = new PilotageMonthlyDTO();
            dto.setShipName(cert.getVesselNameZh());
            dto.setEnglishName(cert.getVesselNameEn());
            dto.setStartTime(DateUtil.formatDateTime(cert.getPilotageStartTime()));
            dto.setEndTime(DateUtil.formatDateTime(cert.getPilotageEndTime()));
            dto.setBoardingTime(DateUtil.formatDateTime(cert.getBoardingTime()));
            dto.setDynamic(cert.getPilotageType());
            dto.setNo(cert.getCertificateCode());
            dto.setRemarks(cert.getNotes());
            dto.setForeignFee(cert.getBerthingAssistanceCharge());
            
            Vessels vessel = vessels.stream()
                    .filter(v -> v.getId().equals(cert.getVesselId()))
                    .findFirst()
                    .orElse(null);
            if (vessel != null) {
                dto.setNationality(vessel.getNationality());
                dto.setRouteType(vessel.getRouteType() != null ? vessel.getRouteType().toString() : "");
                dto.setGrossTonnage(vessel.getGrossTonnage());
                dto.setNetTonnage(vessel.getNetTonnage());
                dto.setDeadWeight(vessel.getDeadweight());
                dto.setLength(vessel.getVesselLength());
                dto.setWidth(vessel.getVesselWidth());
                dto.setDraft(vessel.getFullLoadDraft());
                dto.setHeight(vessel.getAirDraft());
            }

            if (cert.getPlanId() != null) {
                LambdaQueryWrapper<PlanPilotAssignments> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(PlanPilotAssignments::getStatus, "ASSIGNED", "CONFIRMED", "COMPLETED");
                List<PlanPilotAssignments> list = planPilotAssignmentsService.list(queryWrapper);
                List<String> pilotNames = list.stream()
                        .map(PlanPilotAssignments::getPilotId)
                        .map(pilotId -> pilots.stream()
                                .filter(p -> p.getId().equals(pilotId))
                                .findFirst()
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .map(Pilots::getPilotName)
                        .collect(Collectors.toList());
                            dto.setPilotName(pilotNames);
            }
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public List<PilotageSummaryDTO> getPilotageSummaryReport(LocalDate startDate, LocalDate endDate) {
        log.info("获取船舶引航统计(汇总): startDate={}, endDate={}", startDate, endDate);

        LambdaQueryWrapper<PilotageCertificates> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotageCertificates::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        wrapper.orderByAsc(PilotageCertificates::getCreatedAt);

        List<PilotageCertificates> certificates = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();

        Map<Integer, List<PilotageCertificates>> certificatesByMonth = certificates.stream()
                .collect(Collectors.groupingBy(
                        cert -> cert.getCreatedAt().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                .getMonthValue()
                ));

        List<PilotageSummaryDTO> result = new ArrayList<>();
        for (Map.Entry<Integer, List<PilotageCertificates>> entry : certificatesByMonth.entrySet()) {
            Integer month = entry.getKey();
            List<PilotageCertificates> monthCerts = entry.getValue();

            PilotageSummaryDTO dto = new PilotageSummaryDTO();
            dto.setMonth(month);
            dto.setRemarks("引航统计汇总");

            long foreignCount = 0;
            long domesticCount = 0;
            long gt250Count = 0;
            long gt200_250Count = 0;
            long dangerousCount = 0;
            long containerCount = 0;
            long nightCount = 0;
            long restDayCount = 0;
            long holidayCount = 0;
            BigDecimal totalGrossTonnage = BigDecimal.ZERO;
            BigDecimal totalNetTonnage = BigDecimal.ZERO;
            BigDecimal totalDeadWeight = BigDecimal.ZERO;
            BigDecimal totalDistance = BigDecimal.ZERO;
            BigDecimal totalTime = BigDecimal.ZERO;

            for (PilotageCertificates cert : monthCerts) {
                Vessels vessel = vessels.stream()
                        .filter(v -> v.getId().equals(cert.getVesselId()))
                        .findFirst()
                        .orElse(null);
                if (vessel != null) {
                    if (RouteTypeEnum.INTERNATIONAL.equals(vessel.getRouteType())) {
                        foreignCount++;
                    } else {
                        domesticCount++;
                    }

                    if (vessel.getVesselLength() != null && vessel.getVesselLength().compareTo(BigDecimal.valueOf(250)) >= 0) {
                        gt250Count++;
                    } else if (vessel.getVesselLength() != null &&
                            vessel.getVesselLength().compareTo(BigDecimal.valueOf(200)) >= 0 &&
                            vessel.getVesselLength().compareTo(BigDecimal.valueOf(250)) < 0) {
                        gt200_250Count++;
                    }

                    totalGrossTonnage = totalGrossTonnage.add(vessel.getGrossTonnage() != null ? vessel.getGrossTonnage() : BigDecimal.ZERO);
                    totalNetTonnage = totalNetTonnage.add(vessel.getNetTonnage() != null ? vessel.getNetTonnage() : BigDecimal.ZERO);
                    totalDeadWeight = totalDeadWeight.add(vessel.getDeadweight() != null ? vessel.getDeadweight() : BigDecimal.ZERO);
                }

                if (cert.getPilotageStartTime() != null) {
                    int startHour = cert.getPilotageStartTime().toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime()
                            .getHour();
                    if (startHour < 6 || startHour > 22) {
                        nightCount++;
                    }
                }
            }

            dto.setForeign(foreignCount);
            dto.setDomestic(domesticCount);
            dto.setGt250(gt250Count);
            dto.setGt200_250(gt200_250Count);
            dto.setGrossTonnage(totalGrossTonnage);
            dto.setNetTonnage(totalNetTonnage);
            dto.setDeadWeight(totalDeadWeight);
            dto.setDangerous(dangerousCount);
            dto.setContainer(containerCount);
            dto.setNight(nightCount);
            dto.setRestDay(restDayCount);
            dto.setHoliday(holidayCount);
            dto.setDistance(totalDistance);
            dto.setTime(totalTime);

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<LengthGt250DTO> getLengthGt250Report(LocalDate startDate, LocalDate endDate, Double minLength) {
        log.info("获取船长统计(>{}m): startDate={}, endDate={}", minLength, startDate, endDate);
        
        LambdaQueryWrapper<PilotageCertificates> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotageCertificates::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        wrapper.orderByAsc(PilotageCertificates::getCreatedAt);
        
        List<PilotageCertificates> certificates = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();
        List<Pilots> pilots = pilotsService.list();
        
        List<LengthGt250DTO> result = new ArrayList<>();
        for (PilotageCertificates cert : certificates) {
            Vessels vessel = vessels.stream()
                    .filter(v -> v.getId().equals(cert.getVesselId()))
                    .findFirst()
                    .orElse(null);
            if (vessel != null && vessel.getVesselLength() != null && 
                vessel.getVesselLength().compareTo(BigDecimal.valueOf(minLength)) >= 0) {
                LengthGt250DTO dto = new LengthGt250DTO();
                dto.setShipName(cert.getVesselNameZh());
                dto.setEnglishName(cert.getVesselNameEn());
                dto.setNationality(vessel.getNationality());
                dto.setDynamic(cert.getPilotageType());
                dto.setTimeDate(DateUtil.formatDateTime(cert.getCreatedAt()));
                dto.setGrossTonnage(vessel.getGrossTonnage());
                dto.setNetTonnage(vessel.getNetTonnage());
                dto.setDeadWeight(vessel.getDeadweight());
                dto.setLength(vessel.getVesselLength());
                dto.setWidth(vessel.getVesselWidth());
                dto.setDraft(vessel.getAirDraft());
                dto.setBerth(vessel.getVesselType());
                dto.setRemarks(cert.getNotes());

                if (cert.getPlanId() != null) {
                    LambdaQueryWrapper<PlanPilotAssignments> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.in(PlanPilotAssignments::getStatus, "ASSIGNED", "CONFIRMED", "COMPLETED");
                    List<PlanPilotAssignments> list = planPilotAssignmentsService.list(queryWrapper);
                    List<String> pilotNames = list.stream()
                            .map(PlanPilotAssignments::getPilotId)
                            .map(pilotId -> pilots.stream()
                                    .filter(p -> p.getId().equals(pilotId))
                                    .findFirst()
                                    .orElse(null))
                            .filter(Objects::nonNull)
                            .map(Pilots::getPilotName)
                            .collect(Collectors.toList());
                    dto.setPilotName(pilotNames);
                }
                
                result.add(dto);
            }
        }
        
        return result;
    }

    @Override
    public List<HolidayDTO> getHolidayReport(LocalDate startDate, LocalDate endDate) {
        log.info("获取节假日引航统计: startDate={}, endDate={}", startDate, endDate);
        
        LambdaQueryWrapper<PilotageCertificates> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotageCertificates::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        wrapper.orderByAsc(PilotageCertificates::getCreatedAt);
        
        List<PilotageCertificates> certificates = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();
        List<Pilots> pilots = pilotsService.list();
        List<Tugboats> tugboats = tugboatsService.list();
        
        List<HolidayDTO> result = new ArrayList<>();
        for (PilotageCertificates cert : certificates) {
            HolidayDTO dto = new HolidayDTO();
            dto.setShipName(cert.getVesselNameZh());
            dto.setEnglishName(cert.getVesselNameEn());
            dto.setDynamic(cert.getPilotageType());
            dto.setTimeDate(DateUtil.formatDateTime(cert.getCreatedAt()));
            dto.setBoardingTime(DateUtil.formatDateTime(cert.getBoardingTime()));
            
            Vessels vessel = vessels.stream()
                    .filter(v -> v.getId().equals(cert.getVesselId()))
                    .findFirst()
                    .orElse(null);
            if (vessel != null) {
                dto.setNationality(vessel.getNationality());
                dto.setGrossTonnage(vessel.getGrossTonnage());
                dto.setNetTonnage(vessel.getNetTonnage());
                dto.setDeadWeight(vessel.getDeadweight());
                dto.setLength(vessel.getVesselLength());
                dto.setWidth(vessel.getVesselWidth());
                dto.setDraft(vessel.getAirDraft());
            }
            
            if (cert.getPlanId() != null) {
                PilotagePlans plan = pilotagePlansService.getById(cert.getPlanId());
                if (plan != null) {
                    dto.setBerth(plan.getEndLocationName() != null ? plan.getEndLocationName() : cert.getRouteName());
                    dto.setBoardingPlace(plan.getStartLocationName());

                    if (cert.getPlanId() != null) {
                        LambdaQueryWrapper<PlanPilotAssignments> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.in(PlanPilotAssignments::getStatus, "ASSIGNED", "CONFIRMED", "COMPLETED");
                        List<PlanPilotAssignments> list = planPilotAssignmentsService.list(queryWrapper);
                        List<String> pilotNames = list.stream()
                                .map(PlanPilotAssignments::getPilotId)
                                .map(pilotId -> pilots.stream()
                                        .filter(p -> p.getId().equals(pilotId))
                                        .findFirst()
                                        .orElse(null))
                                .filter(Objects::nonNull)
                                .map(Pilots::getPilotName)
                                .collect(Collectors.toList());
                        dto.setPilotName(pilotNames);
                    }
                    
                    List<PlanTugboatAssignments> tugboatAssignments = getTugboatAssignmentsByPlanId(plan.getId());
                    if (!tugboatAssignments.isEmpty()) {
                        List<String> tugboatNames = tugboatAssignments.stream()
                                .map(assignment -> {
                                    Tugboats tugboat = tugboats.stream()
                                            .filter(t -> t.getId().equals(assignment.getTugboatId()))
                                            .findFirst()
                                            .orElse(null);
                                    return tugboat != null ? tugboat.getTugboatName() : null;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        dto.setTug(String.join(",", tugboatNames));
                    }
                }
            } else {
                dto.setBerth(cert.getRouteName());
            }
            
            dto.setRemarks(cert.getNotes());
            
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public List<WeekendDTO> getWeekendReport(LocalDate startDate, LocalDate endDate) {
        log.info("获取双休日引航统计: startDate={}, endDate={}", startDate, endDate);
        
        LambdaQueryWrapper<PilotageCertificates> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotageCertificates::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        wrapper.orderByAsc(PilotageCertificates::getCreatedAt);
        
        List<PilotageCertificates> certificates = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();
        List<Pilots> pilots = pilotsService.list();
        List<Tugboats> tugboats = tugboatsService.list();
        
        List<WeekendDTO> result = new ArrayList<>();
        for (PilotageCertificates cert : certificates) {
            WeekendDTO dto = new WeekendDTO();
            dto.setShipName(cert.getVesselNameZh());
            dto.setEnglishName(cert.getVesselNameEn());
            dto.setDynamic(cert.getPilotageType());
            dto.setTimeDate(DateUtil.formatDateTime(cert.getCreatedAt()));
            dto.setBoardingTime(DateUtil.formatDateTime(cert.getBoardingTime()));
            
            Vessels vessel = vessels.stream()
                    .filter(v -> v.getId().equals(cert.getVesselId()))
                    .findFirst()
                    .orElse(null);
            if (vessel != null) {
                dto.setNationality(vessel.getNationality());
                dto.setGrossTonnage(vessel.getGrossTonnage());
                dto.setNetTonnage(vessel.getNetTonnage());
                dto.setDeadWeight(vessel.getDeadweight());
                dto.setLength(vessel.getVesselLength());
                dto.setWidth(vessel.getVesselWidth());
                dto.setDraft(vessel.getAirDraft());
            }
            
            if (cert.getPlanId() != null) {
                PilotagePlans plan = pilotagePlansService.getById(cert.getPlanId());
                if (plan != null) {
                    dto.setBerth(plan.getEndLocationName() != null ? plan.getEndLocationName() : cert.getRouteName());
                    dto.setBoardingPlace(plan.getStartLocationName());

                    if (cert.getPlanId() != null) {
                        LambdaQueryWrapper<PlanPilotAssignments> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.in(PlanPilotAssignments::getStatus, "ASSIGNED", "CONFIRMED", "COMPLETED");
                        List<PlanPilotAssignments> list = planPilotAssignmentsService.list(queryWrapper);
                        List<String> pilotNames = list.stream()
                                .map(PlanPilotAssignments::getPilotId)
                                .map(pilotId -> pilots.stream()
                                        .filter(p -> p.getId().equals(pilotId))
                                        .findFirst()
                                        .orElse(null))
                                .filter(Objects::nonNull)
                                .map(Pilots::getPilotName)
                                .collect(Collectors.toList());
                        dto.setPilotName(pilotNames);
                    }
                    
                    List<PlanTugboatAssignments> tugboatAssignments = getTugboatAssignmentsByPlanId(plan.getId());
                    if (!tugboatAssignments.isEmpty()) {
                        List<String> tugboatNames = tugboatAssignments.stream()
                                .map(assignment -> {
                                    Tugboats tugboat = tugboats.stream()
                                            .filter(t -> t.getId().equals(assignment.getTugboatId()))
                                            .findFirst()
                                            .orElse(null);
                                    return tugboat != null ? tugboat.getTugboatName() : null;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        dto.setTug(String.join(",", tugboatNames));
                    }
                }
            } else {
                dto.setBerth(cert.getRouteName());
            }
            
            dto.setRemarks(cert.getNotes());
            
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public List<NightDTO> getNightReport(LocalDate startDate, LocalDate endDate) {
        log.info("获取夜班统计: startDate={}, endDate={}", startDate, endDate);
        
        LambdaQueryWrapper<PilotageCertificates> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotageCertificates::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        wrapper.orderByAsc(PilotageCertificates::getCreatedAt);
        
        List<PilotageCertificates> certificates = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();
        List<Pilots> pilots = pilotsService.list();
        List<Tugboats> tugboats = tugboatsService.list();
        
        List<NightDTO> result = new ArrayList<>();
        for (PilotageCertificates cert : certificates) {
            if (cert.getPilotageStartTime() != null) {
                int startHour = cert.getPilotageStartTime().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
                        .getHour();
                int endHour = cert.getPilotageEndTime() != null ?
                        cert.getPilotageEndTime().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime()
                                .getHour() : startHour;
                if (startHour < 6 || startHour > 22 || endHour < 6 || endHour > 22) {
                    NightDTO dto = new NightDTO();
                    dto.setShipName(cert.getVesselNameZh());
                    dto.setEnglishName(cert.getVesselNameEn());
                    dto.setDynamic(cert.getPilotageType());
                    dto.setTimeDate(DateUtil.formatDateTime(cert.getCreatedAt()));
                    dto.setBoardingTime(DateUtil.formatDateTime(cert.getBoardingTime()));

                    Vessels vessel = vessels.stream()
                            .filter(v -> v.getId().equals(cert.getVesselId()))
                            .findFirst()
                            .orElse(null);
                    if (vessel != null) {
                        dto.setNationality(vessel.getNationality());
                        dto.setGrossTonnage(vessel.getGrossTonnage());
                        dto.setNetTonnage(vessel.getNetTonnage());
                        dto.setDeadWeight(vessel.getDeadweight());
                        dto.setLength(vessel.getVesselLength());
                        dto.setWidth(vessel.getVesselWidth());
                        dto.setDraft(vessel.getAirDraft());
                    }

                    if (cert.getPlanId() != null) {
                        PilotagePlans plan = pilotagePlansService.getById(cert.getPlanId());
                        if (plan != null) {
                            dto.setBerth(plan.getEndLocationName() != null ? plan.getEndLocationName() : cert.getRouteName());
                            dto.setBoardingPlace(plan.getStartLocationName());

                            if (cert.getPlanId() != null) {
                                LambdaQueryWrapper<PlanPilotAssignments> queryWrapper = new LambdaQueryWrapper<>();
                                queryWrapper.in(PlanPilotAssignments::getStatus, "ASSIGNED", "CONFIRMED", "COMPLETED");
                                List<PlanPilotAssignments> list = planPilotAssignmentsService.list(queryWrapper);
                                List<String> pilotNames = list.stream()
                                        .map(PlanPilotAssignments::getPilotId)
                                        .map(pilotId -> pilots.stream()
                                                .filter(p -> p.getId().equals(pilotId))
                                                .findFirst()
                                                .orElse(null))
                                        .filter(Objects::nonNull)
                                        .map(Pilots::getPilotName)
                                        .collect(Collectors.toList());
                                dto.setPilotName(pilotNames);
                            }

                            List<PlanTugboatAssignments> tugboatAssignments = getTugboatAssignmentsByPlanId(plan.getId());
                            if (!tugboatAssignments.isEmpty()) {
                                List<String> tugboatNames = tugboatAssignments.stream()
                                        .map(assignment -> {
                                            Tugboats tugboat = tugboats.stream()
                                                    .filter(t -> t.getId().equals(assignment.getTugboatId()))
                                                    .findFirst()
                                                    .orElse(null);
                                            return tugboat != null ? tugboat.getTugboatName() : null;
                                        })
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                                dto.setTug(String.join(",", tugboatNames));
                            }
                        }
                    } else {
                        dto.setBerth(cert.getRouteName());
                    }

                    dto.setRemarks("夜班作业");

                    result.add(dto);
                }
            }
        }
        
        return result;
    }

    @Override
    public List<SpecialDTO> getSpecialReport(LocalDate startDate, LocalDate endDate, String shipName) {
        log.info("获取特殊引航统计: startDate={}, endDate={}, shipName={}", startDate, endDate, shipName);
        
        LambdaQueryWrapper<PilotageCertificates> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotageCertificates::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        if (shipName != null && !shipName.isEmpty()) {
            wrapper.and(qw -> qw.like(PilotageCertificates::getVesselNameZh, shipName)
                    .or()
                    .like(PilotageCertificates::getVesselNameEn, shipName));
        }
        wrapper.orderByAsc(PilotageCertificates::getCreatedAt);
        
        List<PilotageCertificates> certificates = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();
        List<Pilots> pilots = pilotsService.list();
        
        List<SpecialDTO> result = new ArrayList<>();
        for (PilotageCertificates cert : certificates) {
            SpecialDTO dto = new SpecialDTO();
            dto.setShipName(cert.getVesselNameZh());
            dto.setEnglishName(cert.getVesselNameEn());
            dto.setSpecialOp("特殊作业");
            dto.setSpecialDesc(cert.getSpecialSituationDescription());
            dto.setDynamic(cert.getPilotageType());
            dto.setStartTime(DateUtil.formatDateTime(cert.getPilotageStartTime()));
            dto.setEndTime(DateUtil.formatDateTime(cert.getPilotageEndTime()));
            dto.setRemarks(cert.getNotes());
            dto.setForeignFee(cert.getBerthingAssistanceCharge());
            
            Vessels vessel = vessels.stream()
                    .filter(v -> v.getId().equals(cert.getVesselId()))
                    .findFirst()
                    .orElse(null);
            if (vessel != null) {
                dto.setNationality(vessel.getNationality());
                dto.setGrossTonnage(vessel.getGrossTonnage());
                dto.setNetTonnage(vessel.getNetTonnage());
            }
            
            if (cert.getPlanId() != null) {
                PilotagePlans plan = pilotagePlansService.getById(cert.getPlanId());
                if (plan != null && plan.getAssignedPilotId() != null) {
                    Pilots pilotName = pilots.stream()
                            .filter(p -> p.getId().equals(plan.getAssignedPilotId()))
                            .findFirst()
                            .orElse(null);
                    if (pilotName != null) {
                        dto.setPilotName(pilotName.getPilotName());
                    }
                }
            }
            
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public List<RecordBreakDTO> getRecordBreakReport(LocalDate startDate, LocalDate endDate) {
        log.info("获取破纪录统计: startDate={}, endDate={}", startDate, endDate);
        
        LambdaQueryWrapper<PilotageCertificates> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotageCertificates::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        wrapper.orderByAsc(PilotageCertificates::getCreatedAt);
        
        List<PilotageCertificates> certificates = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();
        List<Pilots> pilots = pilotsService.list();
        
        List<RecordBreakDTO> result = new ArrayList<>();
        for (PilotageCertificates cert : certificates) {
            // 这里可以添加破纪录的判断逻辑，例如最大吨位、最长船长等
            Vessels vessel = vessels.stream()
                    .filter(v -> v.getId().equals(cert.getVesselId()))
                    .findFirst()
                    .orElse(null);
            if (vessel != null && vessel.getGrossTonnage() != null && 
                vessel.getGrossTonnage().compareTo(BigDecimal.valueOf(100000)) > 0) { // 假设超过10万吨为破纪录
                RecordBreakDTO dto = new RecordBreakDTO();
                dto.setDesc("超大型船舶引航");
                dto.setShipName(cert.getVesselNameZh());
                dto.setEnglishName(cert.getVesselNameEn());
                dto.setNationality(vessel.getNationality());
                dto.setDynamic(cert.getPilotageType());
                dto.setTimeDate(DateUtil.formatDateTime(cert.getCreatedAt()));
                dto.setGrossTonnage(vessel.getGrossTonnage());
                dto.setLength(vessel.getVesselLength());
                dto.setDraft(vessel.getAirDraft());
                dto.setBerth(vessel.getVesselType());
                
                if (cert.getPlanId() != null) {
                    PilotagePlans plan = pilotagePlansService.getById(cert.getPlanId());
                    if (plan != null && plan.getAssignedPilotId() != null) {
                        Pilots pilotName = pilots.stream()
                                .filter(p -> p.getId().equals(plan.getAssignedPilotId()))
                                .findFirst()
                                .orElse(null);
                        if (pilotName != null) {
                            dto.setPilotName(pilotName.getPilotName());
                        }
                    }
                }
                
                result.add(dto);
            }
        }
        
        return result;
    }

    @Override
    public List<NewWharfDTO> getNewWharfReport(LocalDate startDate, LocalDate endDate) {
        log.info("获取新码头靠泊统计: startDate={}, endDate={}", startDate, endDate);
        
        LambdaQueryWrapper<PilotageCertificates> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotageCertificates::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        wrapper.orderByAsc(PilotageCertificates::getCreatedAt);
        
        List<PilotageCertificates> certificates = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();
        List<Pilots> pilots = pilotsService.list();
        List<Tugboats> tugboats = tugboatsService.list();
        
        List<NewWharfDTO> result = new ArrayList<>();
        for (PilotageCertificates cert : certificates) {
            NewWharfDTO dto = new NewWharfDTO();
            dto.setShipName(cert.getVesselNameZh());
            dto.setEnglishName(cert.getVesselNameEn());
            dto.setDynamic(cert.getPilotageType());
            dto.setTimeDate(DateUtil.formatDateTime(cert.getCreatedAt()));
            dto.setBoardingTime(DateUtil.formatDateTime(cert.getBoardingTime()));
            
            Vessels vessel = vessels.stream()
                    .filter(v -> v.getId().equals(cert.getVesselId()))
                    .findFirst()
                    .orElse(null);
            if (vessel != null) {
                dto.setNationality(vessel.getNationality());
                dto.setGrossTonnage(vessel.getGrossTonnage());
                dto.setNetTonnage(vessel.getNetTonnage());
                dto.setDeadWeight(vessel.getDeadweight());
                dto.setLength(vessel.getVesselLength());
                dto.setWidth(vessel.getVesselWidth());
                dto.setDraft(vessel.getAirDraft());
            }
            
            if (cert.getPlanId() != null) {
                PilotagePlans plan = pilotagePlansService.getById(cert.getPlanId());
                if (plan != null) {
                    dto.setBerth(plan.getEndLocationName() != null ? plan.getEndLocationName() : cert.getRouteName());
                    dto.setBoardingPlace(plan.getStartLocationName());

                    if (cert.getPlanId() != null) {
                        LambdaQueryWrapper<PlanPilotAssignments> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.in(PlanPilotAssignments::getStatus, "ASSIGNED", "CONFIRMED", "COMPLETED");
                        List<PlanPilotAssignments> list = planPilotAssignmentsService.list(queryWrapper);
                        List<String> pilotNames = list.stream()
                                .map(PlanPilotAssignments::getPilotId)
                                .map(pilotId -> pilots.stream()
                                        .filter(p -> p.getId().equals(pilotId))
                                        .findFirst()
                                        .orElse(null))
                                .filter(Objects::nonNull)
                                .map(Pilots::getPilotName)
                                .collect(Collectors.toList());
                        dto.setPilotName(pilotNames);
                    }
                    
                    List<PlanTugboatAssignments> tugboatAssignments = getTugboatAssignmentsByPlanId(plan.getId());
                    if (!tugboatAssignments.isEmpty()) {
                        List<String> tugboatNames = tugboatAssignments.stream()
                                .map(assignment -> {
                                    Tugboats tugboat = tugboats.stream()
                                            .filter(t -> t.getId().equals(assignment.getTugboatId()))
                                            .findFirst()
                                            .orElse(null);
                                    return tugboat != null ? tugboat.getTugboatName() : null;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        dto.setTug(String.join(",", tugboatNames));
                    }
                }
            } else {
                dto.setBerth(cert.getRouteName());
            }
            
            dto.setRemarks(cert.getNotes());
            
            result.add(dto);
        }
        
        return result;
    }
    
    @Override
    public List<MealDTO> getMealReport(LocalDate startDate, LocalDate endDate) {
        log.info("获取餐补统计: startDate={}, endDate={}", startDate, endDate);
        
        LambdaQueryWrapper<PilotageCertificates> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(PilotageCertificates::getCreatedAt, startDate.atStartOfDay(), endDate.atStartOfDay());
        wrapper.orderByAsc(PilotageCertificates::getCreatedAt);
        
        List<PilotageCertificates> certificates = this.list(wrapper);
        List<Vessels> vessels = vesselsService.list();
        List<Pilots> pilots = pilotsService.list();
        
        List<MealDTO> result = new ArrayList<>();
        for (PilotageCertificates cert : certificates) {
            MealDTO dto = new MealDTO();
            dto.setShipName(cert.getVesselNameZh());
            dto.setEnglishName(cert.getVesselNameEn());
            dto.setDynamic(cert.getPilotageType());
            dto.setTimeDate(DateUtil.formatDateTime(cert.getCreatedAt()));
            dto.setMealType("标准餐补");
            dto.setMealCount(1); // 默认餐补数量为1
            dto.setRemarks("餐补统计");
            
            Vessels vessel = vessels.stream()
                    .filter(v -> v.getId().equals(cert.getVesselId()))
                    .findFirst()
                    .orElse(null);
            if (vessel != null) {
                dto.setNationality(vessel.getNationality());
                dto.setGrossTonnage(vessel.getGrossTonnage());
                dto.setNetTonnage(vessel.getNetTonnage());
                dto.setDeadWeight(vessel.getDeadweight());
                dto.setLength(vessel.getVesselLength());
                dto.setWidth(vessel.getVesselWidth());
                dto.setDraft(vessel.getAirDraft());
            }

            if (cert.getPlanId() != null) {
                LambdaQueryWrapper<PlanPilotAssignments> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(PlanPilotAssignments::getStatus, "ASSIGNED", "CONFIRMED", "COMPLETED");
                List<PlanPilotAssignments> list = planPilotAssignmentsService.list(queryWrapper);
                List<String> pilotNames = list.stream()
                        .map(PlanPilotAssignments::getPilotId)
                        .map(pilotId -> pilots.stream()
                                .filter(p -> p.getId().equals(pilotId))
                                .findFirst()
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .map(Pilots::getPilotName)
                        .collect(Collectors.toList());
                dto.setPilotName(pilotNames);
            }
            
            result.add(dto);
        }
        
        return result;
    }
    
        private List<PlanTugboatAssignments> getTugboatAssignmentsByPlanId(Long planId) {
        LambdaQueryWrapper<PlanTugboatAssignments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanTugboatAssignments::getPlanId, planId);
        wrapper.eq(PlanTugboatAssignments::getStatus, "ASSIGNED");
        List<PlanTugboatAssignments> list = planTugboatAssignmentsService.list(wrapper);
        return new ArrayList<>(list);
    }
}