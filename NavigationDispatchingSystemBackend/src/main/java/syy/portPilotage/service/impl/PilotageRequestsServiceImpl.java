package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.dto.pilotage.PilotageRequestDTO;
import syy.portPilotage.entity.*;
import syy.portPilotage.enums.RequestStatusEnum;
import syy.portPilotage.enums.RouteTypeEnum;
import syy.portPilotage.enums.VoyageStatusEnum;
import syy.portPilotage.enums.VoyageTypeEnum;
import syy.portPilotage.enums.PriorityEnum;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.mapper.*;
import syy.portPilotage.rabbitmq.service.MessageService;
import syy.portPilotage.service.PilotageRequestsService;
import syy.portPilotage.util.IdGenerator;
import syy.portPilotage.util.VoyageNumberUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PilotageRequestsServiceImpl extends ServiceImpl<PilotageRequestsMapper, PilotageRequests>
    implements PilotageRequestsService{

    @Autowired
    private VesselsMapper vesselsMapper;

    @Autowired
    private VoyagesMapper voyagesMapper;

    @Autowired
    private AgentInvoiceInfoMapper agentInvoiceInfoMapper;

    @Autowired
    private PilotageRequestsMapper pilotageRequestsMapper;

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private PilotLadderInfoMapper pilotLadderInfoMapper;
    @Autowired
    private AgentsMapper agentsMapper;
    @Autowired
    private BillingVouchersMapper billingVouchersMapper;
    @Autowired
    private PilotageCertificatesMapper pilotageCertificatesMapper;
    @Autowired
    private PilotagePlansMapper pilotagePlansMapper;
    
    @Autowired
    private MessageService messageService;

    public PilotageRequests getDetailedRequest(Long id) {
        PilotageRequests request = this.getById(id);
        if (request != null && request.getVesselId() != null) {
            Vessels vessel = vesselsMapper.selectById(request.getVesselId());
            // 具体实现取决于业务需求
        }
        return request;
    }

    @Override
    @Transactional
    public PilotageRequests savePilotAgeRequestData(PilotageRequestDTO pilotageRequestDTO) {
        Vessels existingVessel = null;
        
        if (pilotageRequestDTO.getVesselCallSign() != null && !pilotageRequestDTO.getVesselCallSign().trim().isEmpty()) {
            existingVessel = vesselsMapper.selectOne(
                new LambdaQueryWrapper<Vessels>().eq(Vessels::getVesselCallSign, pilotageRequestDTO.getVesselCallSign())
            );
        }
        
        if (existingVessel == null && pilotageRequestDTO.getImoNumber() != null && !pilotageRequestDTO.getImoNumber().trim().isEmpty()) {
            existingVessel = vesselsMapper.selectOne(
                new LambdaQueryWrapper<Vessels>().eq(Vessels::getImoNumber, pilotageRequestDTO.getImoNumber())
            );
        }
        
        if (existingVessel == null && pilotageRequestDTO.getMmsiNumber() != null && !pilotageRequestDTO.getMmsiNumber().trim().isEmpty()) {
            existingVessel = vesselsMapper.selectOne(
                new LambdaQueryWrapper<Vessels>().eq(Vessels::getMmsiNumber, pilotageRequestDTO.getMmsiNumber())
            );
        }
        
        Vessels vessels;
        if (existingVessel != null) {
            // 如果存在，更新现有船舶信息
            vessels = existingVessel;
            mapVesselData(pilotageRequestDTO, vessels);
            vessels.setUpdatedAt(new Date());
            vesselsMapper.updateById(vessels);
        } else {
            // 如果不存在，创建新船舶
            vessels = new Vessels();
            mapVesselData(pilotageRequestDTO, vessels);
            vessels.setCreatedAt(new Date());
            vessels.setUpdatedAt(new Date());
            vesselsMapper.insert(vessels);
        }

        Long vesselId = vessels.getId();

        Voyages voyages = new Voyages();
        mapVoyageData(pilotageRequestDTO, voyages);
        voyages.setVesselId(vesselId);
        // 使用带唯一性检查的航次编号生成方法
        voyages.setVoyageNumber(VoyageNumberUtil.generateVoyageNumber(vessels));
        voyages.setCreatedAt(new Date());
        voyages.setUpdatedAt(new Date());
        if (pilotageRequestDTO.getStartLocationType() != null) {
            voyages.setStartLocationType(pilotageRequestDTO.getStartLocationType());
        }
        if (pilotageRequestDTO.getEndLocationType() != null) {
            voyages.setEndLocationType(pilotageRequestDTO.getEndLocationType());
        }
        voyagesMapper.insert(voyages);

        Long voyageId = voyages.getId();

        AgentInvoiceInfo agentInvoiceInfo = new AgentInvoiceInfo();
        mapAgentInvoiceInfoData(pilotageRequestDTO, agentInvoiceInfo);
        agentInvoiceInfo.setVoyageId(voyageId);
        agentInvoiceInfo.setCreatedAt(new Date());
        agentInvoiceInfo.setUpdatedAt(new Date());
        agentInvoiceInfoMapper.insert(agentInvoiceInfo);

        PilotageRequests pilotageRequests = new PilotageRequests();
        mapPilotageRequestsData(pilotageRequestDTO, pilotageRequests);
        // 若未来前端/外部系统显式传入 priority，则尊重传入值
        PriorityEnum priority = pilotageRequestDTO.getPriority();
        if (priority == null) {
            priority = calculateRequestPriority(pilotageRequestDTO);
        }
        pilotageRequests.setPriority(priority == null ? PriorityEnum.MEDIUM : priority);
        // 使用带唯一性检查的申请编号生成方法
        pilotageRequests.setRequestCode(IdGenerator.generateRequestCode());
        pilotageRequests.setRequestStatus(RequestStatusEnum.PENDING);
        pilotageRequests.setCreatedAt(new Date());
        pilotageRequests.setUpdatedAt(new Date());
        pilotageRequests.setVesselId(vesselId);
        pilotageRequests.setVoyageId(voyageId);
        pilotageRequestsMapper.insert(pilotageRequests);

        voyages.setPilotageRequestsId(pilotageRequests.getId());
        voyagesMapper.updateById(voyages);

        PilotLadderInfo pilotLadderInfo = new PilotLadderInfo();
        mapPilotLadderInfoData(pilotageRequestDTO, pilotLadderInfo);
        pilotLadderInfo.setVesselId(vesselId);
        pilotLadderInfo.setVoyageId(voyageId);
        pilotLadderInfo.setCreatedAt(new Date());
        pilotLadderInfo.setUpdatedAt(new Date());
        pilotLadderInfoMapper.insert(pilotLadderInfo);

        //'付款信息表' PAYMENT_INFO
        PaymentInfo paymentInfo = new PaymentInfo();
        mapPaymentInfoData(pilotageRequestDTO, paymentInfo);

        return pilotageRequests;
    }

    private PriorityEnum calculateRequestPriority(PilotageRequestDTO dto) {
        String notes = safeLower(dto.getRequestNotes());
        if (containsAny(notes, "紧急", "加急", "urgent", "emergency", "优先")) {
            return PriorityEnum.URGENT;
        }

        int score = 20; // MEDIUM 基线

        Date eta = dto.getEta();
        if (eta != null) {
            long diffMinutes = (eta.getTime() - System.currentTimeMillis()) / 60000L;
            if (diffMinutes <= 0) {
                return PriorityEnum.URGENT;
            }
            if (diffMinutes <= 120) {         // 2h 内
                return PriorityEnum.URGENT;
            } else if (diffMinutes <= 360) {  // 6h 内
                score += 50;
            } else if (diffMinutes <= 1440) { // 24h 内
                score += 25;
            } else if (diffMinutes <= 4320) { // 72h 内
                score += 5;
            } else {                          // 72h 以上
                score -= 15;
            }
        }

        String cargoType = safeUpper(dto.getCargoType());
        if ("DANGEROUS".equals(cargoType)) {
            score += 40;
        }
        String vesselType = safeUpper(dto.getVesselType());
        if (containsAny(vesselType, "LNG", "CHEMICAL", "TANKER", "GAS")) {
            score += 15;
        }

        String ladderStatus = safeLower(dto.getLadderStatus());
        if (containsAny(ladderStatus, "故障", "fault", "abnormal")) {
            score += 20;
        }

        String voyageType = safeUpper(String.valueOf(dto.getVoyageType() == null ? "" : dto.getVoyageType()));
        if (containsAny(voyageType, "IMPORT", "进港")) {
            score += 5;
        }

        if (score >= 90) return PriorityEnum.URGENT;
        if (score >= 60) return PriorityEnum.HIGH;
        if (score >= 20) return PriorityEnum.MEDIUM;
        return PriorityEnum.LOW;
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static String safeUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    private static boolean containsAny(String haystack, String... needles) {
        if (haystack == null || haystack.isEmpty()) return false;
        for (String n : needles) {
            if (n != null && !n.isEmpty() && haystack.contains(n)) return true;
        }
        return false;
    }

    @Override
    public PilotageRequestDTO getPilotAgeRequestById(Long pilotageId) {
        PilotageRequests pilotageRequests = pilotageRequestsMapper.selectById(pilotageId);
        if (pilotageRequests == null) {
            throw new BusinessException(ErrorCode.REQUEST_NOT_FOUND);
        }
        PilotageRequestDTO pilotageRequestDTO = new PilotageRequestDTO();

        mapPilotageRequestsToDTO(pilotageRequests, pilotageRequestDTO);

        Long vesselId = pilotageRequests.getVesselId();
        if (vesselId != null) {
            Vessels vessels = vesselsMapper.selectById(vesselId);
            if (vessels != null) {
                mapVesselToDTO(vessels, pilotageRequestDTO);
            } else {
                log.warn("引航申请{}关联船舶不存在: vesselId={}", pilotageId, vesselId);
            }
        }

        // 航次信息：优先按 voyageId（更准确），否则按 vesselId 兜底
        Voyages voyages = null;
        if (pilotageRequests.getVoyageId() != null) {
            voyages = voyagesMapper.selectById(pilotageRequests.getVoyageId());
        } else if (vesselId != null) {
            LambdaQueryWrapper<Voyages> voyageWrapper = new LambdaQueryWrapper<>();
            voyageWrapper.eq(Voyages::getVesselId, vesselId);
            voyages = voyagesMapper.selectOne(voyageWrapper);
        }

        if (voyages != null) {
            mapVoyageToDTO(voyages, pilotageRequestDTO);

            LambdaQueryWrapper<AgentInvoiceInfo> agentInvoiceWrapper = new LambdaQueryWrapper<>();
            agentInvoiceWrapper.eq(AgentInvoiceInfo::getVoyageId, voyages.getId());
            AgentInvoiceInfo agentInvoiceInfo = agentInvoiceInfoMapper.selectOne(agentInvoiceWrapper);
            if (agentInvoiceInfo != null) {
                mapAgentInvoiceInfoToDTO(agentInvoiceInfo, pilotageRequestDTO);
            }

            LambdaQueryWrapper<PilotLadderInfo> pilotLadderWrapper = new LambdaQueryWrapper<>();
            if (vesselId != null) {
                pilotLadderWrapper.eq(PilotLadderInfo::getVesselId, vesselId);
            }
            pilotLadderWrapper.eq(PilotLadderInfo::getVoyageId, voyages.getId());
            PilotLadderInfo pilotLadderInfo = pilotLadderInfoMapper.selectOne(pilotLadderWrapper);
            if (pilotLadderInfo != null) {
                mapPilotLadderInfoToDTO(pilotLadderInfo, pilotageRequestDTO);
            }
        }

        

        return pilotageRequestDTO;
    }

    @Override
    @Transactional
    public PilotageRequests updatePilotAgeData(PilotageRequestDTO pilotageRequestDTO) {
        Vessels existingVessel = null;
        
        // 按呼号查找（排除当前船舶自身）
        if (pilotageRequestDTO.getVesselCallSign() != null && !pilotageRequestDTO.getVesselCallSign().trim().isEmpty()) {
            existingVessel = vesselsMapper.selectOne(
                new LambdaQueryWrapper<Vessels>()
                    .eq(Vessels::getVesselCallSign, pilotageRequestDTO.getVesselCallSign())
                    .ne(Vessels::getId, pilotageRequestDTO.getVesselId())
            );
        }
        
        if (existingVessel == null && pilotageRequestDTO.getImoNumber() != null && !pilotageRequestDTO.getImoNumber().trim().isEmpty()) {
            existingVessel = vesselsMapper.selectOne(
                new LambdaQueryWrapper<Vessels>()
                    .eq(Vessels::getImoNumber, pilotageRequestDTO.getImoNumber())
                    .ne(Vessels::getId, pilotageRequestDTO.getVesselId()) // 排除当前船舶自身
            );
        }
        
        if (existingVessel == null && pilotageRequestDTO.getMmsiNumber() != null && !pilotageRequestDTO.getMmsiNumber().trim().isEmpty()) {
            existingVessel = vesselsMapper.selectOne(
                new LambdaQueryWrapper<Vessels>()
                    .eq(Vessels::getMmsiNumber, pilotageRequestDTO.getMmsiNumber())
                    .ne(Vessels::getId, pilotageRequestDTO.getVesselId()) // 排除当前船舶自身
            );
        }
        
        if (existingVessel != null) {
            // 如果发现重复的船舶，抛出异常或合并数据
            throw new BusinessException(ErrorCode.DATA_DUPLICATION, "船舶信息与其他已存在船舶重复");
        }
        
        Vessels vessels = vesselsMapper.selectById(pilotageRequestDTO.getVesselId());
        if (vessels == null) {
            return new PilotageRequests();
        }

        mapVesselData(pilotageRequestDTO, vessels);
        vessels.setUpdatedAt(new Date());
        vesselsMapper.updateById(vessels);

        LambdaQueryWrapper<Voyages> voyageWrapper = new LambdaQueryWrapper<>();
        voyageWrapper.eq(Voyages::getVesselId, pilotageRequestDTO.getVesselId());
        Voyages voyages = voyagesMapper.selectOne(voyageWrapper);
        if (voyages != null) {
            mapVoyageData(pilotageRequestDTO, voyages);
            if (pilotageRequestDTO.getStartLocationType() != null) {
                voyages.setStartLocationType(pilotageRequestDTO.getStartLocationType());
            }
            if (pilotageRequestDTO.getEndLocationType() != null) {
                voyages.setEndLocationType(pilotageRequestDTO.getEndLocationType());
            }
            voyages.setUpdatedAt(new Date());
            voyagesMapper.updateById(voyages);

            LambdaQueryWrapper<AgentInvoiceInfo> agentInvoiceWrapper = new LambdaQueryWrapper<>();
            agentInvoiceWrapper.eq(AgentInvoiceInfo::getVoyageId, voyages.getId());
            AgentInvoiceInfo agentInvoiceInfo = agentInvoiceInfoMapper.selectOne(agentInvoiceWrapper);
            if (agentInvoiceInfo != null) {
                mapAgentInvoiceInfoData(pilotageRequestDTO, agentInvoiceInfo);
                agentInvoiceInfo.setUpdatedAt(new Date());
                agentInvoiceInfoMapper.updateById(agentInvoiceInfo);
            }

            LambdaQueryWrapper<PilotageRequests> pilotageRequestWrapper = new LambdaQueryWrapper<>();
            pilotageRequestWrapper.eq(PilotageRequests::getVesselId, pilotageRequestDTO.getVesselId())
                    .eq(PilotageRequests::getVoyageId, voyages.getId());
            PilotageRequests pilotageRequests = pilotageRequestsMapper.selectOne(pilotageRequestWrapper);
            if (pilotageRequests != null) {
                mapPilotageRequestsData(pilotageRequestDTO, pilotageRequests);
                pilotageRequests.setUpdatedAt(new Date());
                pilotageRequestsMapper.updateById(pilotageRequests);

                voyages.setPilotageRequestsId(pilotageRequests.getId());
                voyagesMapper.updateById(voyages);
            }

            LambdaQueryWrapper<PilotLadderInfo> pilotLadderWrapper = new LambdaQueryWrapper<>();
            pilotLadderWrapper.eq(PilotLadderInfo::getVesselId, pilotageRequestDTO.getVesselId())
                    .eq(PilotLadderInfo::getVoyageId, voyages.getId());
            PilotLadderInfo pilotLadderInfo = pilotLadderInfoMapper.selectOne(pilotLadderWrapper);
            if (pilotLadderInfo != null) {
                mapPilotLadderInfoData(pilotageRequestDTO, pilotLadderInfo);
                pilotLadderInfo.setUpdatedAt(new Date());
                pilotLadderInfoMapper.updateById(pilotLadderInfo);
            }
        }

        return new PilotageRequests();
    }

    @Override
    @Transactional
    public boolean deletePilotAgeData(Long requestId) {
        try {
            PilotageRequests pilotageRequest = pilotageRequestsMapper.selectById(requestId);
            if (pilotageRequest == null) {
                return false;
            }

            Long vesselId = pilotageRequest.getVesselId();
            Long voyageId = pilotageRequest.getVoyageId();

            if (hasRelatedData(voyageId)) {
                throw new RuntimeException("该申请已有后续流程数据，无法删除");
            }

            pilotageRequestsMapper.deleteById(requestId);

            QueryWrapper<PilotLadderInfo> pilotLadderWrapper = new QueryWrapper<>();
            pilotLadderWrapper.eq("voyage_id", voyageId);
            pilotLadderInfoMapper.delete(pilotLadderWrapper);

            QueryWrapper<AgentInvoiceInfo> agentInvoiceWrapper = new QueryWrapper<>();
            agentInvoiceWrapper.eq("voyage_id", voyageId);
            agentInvoiceInfoMapper.delete(agentInvoiceWrapper);

            voyagesMapper.deleteById(voyageId);

            QueryWrapper<Voyages> vesselVoyageWrapper = new QueryWrapper<>();
            vesselVoyageWrapper.eq("vessel_id", vesselId);
            Long voyageCount = voyagesMapper.selectCount(vesselVoyageWrapper);
            if (voyageCount == 0) {
                vesselsMapper.deleteById(vesselId);
            }

            return true;
        } catch (Exception e) {
            log.error("删除引航申请数据失败，申请ID: {}", requestId, e);
            throw new RuntimeException("删除引航申请数据失败: " + e.getMessage(), e);
        }
    }

    private boolean hasRelatedData(Long voyageId) {
        QueryWrapper<PilotageCertificates> certificateWrapper = new QueryWrapper<>();
        certificateWrapper.eq("voyage_id", voyageId);
        Long certificateCount = pilotageCertificatesMapper.selectCount(certificateWrapper);
        if (certificateCount > 0) {
            return true;
        }

        // 检查是否有计费凭证
        QueryWrapper<BillingVouchers> billingWrapper = new QueryWrapper<>();
        billingWrapper.eq("voyage_id", voyageId);
        Long billingCount = billingVouchersMapper.selectCount(billingWrapper);
        if (billingCount > 0) {
            return true;
        }

        return false;
    }

    @Override
    public <DTO> IPage<DTO> selectJoinPage(Page<DTO> page, Class<DTO> clazz, MPJLambdaWrapper<PilotageRequests> wrapper) {
        return pilotageRequestsMapper.selectJoinPage(page, clazz, wrapper);
    }

    @Override
    public IPage<PilotageRequestDTO> getPageWithJoin(PageQueryDTO query) {
        MPJLambdaWrapper<PilotageRequests> wrapper = buildPageQueryWrapper(query);
        Page<PilotageRequestDTO> page = new Page<>(query.getCurrent(), query.getSize());
        return this.selectJoinPage(page, PilotageRequestDTO.class, wrapper);
    }

    @Override
    public List<PilotageRequestDTO> getListWithJoinByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        MPJLambdaWrapper<PilotageRequests> wrapper = buildPageQueryWrapper(new PageQueryDTO())
                .in(PilotageRequests::getId, ids);
        // 直接用 page 拉全量（导出场景）
        Page<PilotageRequestDTO> page = new Page<>(1, Math.max(ids.size(), 1));
        List<PilotageRequestDTO> records = this.selectJoinPage(page, PilotageRequestDTO.class, wrapper).getRecords();

        // 补齐计划状态：MPJ 中 voyages 使用了别名（v），可能导致 pilotage_plans 的 join 在部分场景下失效。
        // 为避免导出“计划状态”为空，按 voyageId 批量回查一次 plan_status。
        List<Long> voyageIds = records.stream()
                .map(PilotageRequestDTO::getVoyageId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!voyageIds.isEmpty()) {
            Map<Long, PilotagePlans> planByVoyageId = pilotagePlansMapper.selectList(
                            new LambdaQueryWrapper<PilotagePlans>().in(PilotagePlans::getVoyageId, voyageIds)
                    ).stream()
                    .filter(p -> p.getVoyageId() != null)
                    .collect(Collectors.toMap(PilotagePlans::getVoyageId, Function.identity(), (a, b) -> a));

            for (PilotageRequestDTO dto : records) {
                if ((dto.getPlanStatus() == null || dto.getPlanStatus().trim().isEmpty()) && dto.getVoyageId() != null) {
                    PilotagePlans p = planByVoyageId.get(dto.getVoyageId());
                    if (p != null && p.getPlanStatus() != null) {
                        dto.setPlanStatus(p.getPlanStatus());
                    }else {
                        dto.setPlanStatus("未计划");
                    }
                }
            }
        }

        return records;
    }

    @Override
    @Transactional
    public boolean deleteBatchPilotAgeData(List<Long> requestIds) {
        try {
            List<PilotageRequests> requests = pilotageRequestsMapper.selectBatchIds(requestIds);

            if (requests.size() != requestIds.size()) {
                throw new BusinessException(ErrorCode.REQUEST_NOT_FOUND, "部分申请不存在");
            }

            for (PilotageRequests request : requests) {
                if (!RequestStatusEnum.PENDING.equals(request.getRequestStatus())) {
                    throw new BusinessException(ErrorCode.CANNOT_DELETE,
                            "申请ID " + request.getId() + " 不是待审核状态，无法删除");
                }
            }

            List<Long> voyageIds = requests.stream()
                    .map(PilotageRequests::getVoyageId)
                    .distinct()
                    .collect(Collectors.toList());

            List<Long> vesselIds = requests.stream()
                    .map(PilotageRequests::getVesselId)
                    .distinct()
                    .collect(Collectors.toList());

            for (Long voyageId : voyageIds) {
                if (hasRelatedData(voyageId)) {
                    throw new BusinessException(ErrorCode.CANNOT_DELETE,
                            "某些申请已有后续流程数据，无法删除");
                }
            }

            pilotageRequestsMapper.deleteBatchIds(requestIds);

            QueryWrapper<PilotLadderInfo> pilotLadderWrapper = new QueryWrapper<>();
            pilotLadderWrapper.in("voyage_id", voyageIds);
            pilotLadderInfoMapper.delete(pilotLadderWrapper);

            QueryWrapper<AgentInvoiceInfo> agentInvoiceWrapper = new QueryWrapper<>();
            agentInvoiceWrapper.in("voyage_id", voyageIds);
            agentInvoiceInfoMapper.delete(agentInvoiceWrapper);

            voyagesMapper.deleteBatchIds(voyageIds);

            QueryWrapper<Voyages> vesselVoyageWrapper = new QueryWrapper<>();
            vesselVoyageWrapper.notIn("id", voyageIds) // 不在删除列表中的航次
                    .in("vessel_id", vesselIds); // 但属于被删除申请涉及的船舶

            List<Voyages> remainingVoyages = voyagesMapper.selectList(vesselVoyageWrapper);
            Set<Long> remainingVesselIds = remainingVoyages.stream()
                    .map(Voyages::getVesselId)
                    .collect(Collectors.toSet());

            List<Long> vesselsToDelete = vesselIds.stream()
                    .filter(vesselId -> !remainingVesselIds.contains(vesselId))
                    .collect(Collectors.toList());

            if (!vesselsToDelete.isEmpty()) {
                vesselsMapper.deleteBatchIds(vesselsToDelete);
            }

            return true;
        } catch (BusinessException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            log.error("批量删除引航申请数据失败，申请IDs: {}", requestIds, e);
            throw new RuntimeException("批量删除引航申请数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean batchReviewRequests(List<Long> requestIds, RequestStatusEnum status, Long reviewerId,
                                       Date reviewedAt, String rejectReason) {
        try {
            List<PilotageRequests> requests = pilotageRequestsMapper.selectBatchIds(requestIds);

            if (requests.size() != requestIds.size()) {
                throw new BusinessException(ErrorCode.REQUEST_NOT_FOUND, "部分申请不存在");
            }

            for (PilotageRequests request : requests) {
                if (!RequestStatusEnum.PENDING.equals(request.getRequestStatus())) {
                    throw new BusinessException(ErrorCode.INVALID_STATUS,
                            "申请ID " + request.getId() + " 状态不正确");
                }
            }

            LambdaQueryWrapper<PilotageRequests> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.in(PilotageRequests::getId, requestIds);

            PilotageRequests updateEntity = new PilotageRequests();
            updateEntity.setRequestStatus(status);
            updateEntity.setReviewerId(reviewerId);
            updateEntity.setReviewedAt(reviewedAt);
            if (!RequestStatusEnum.APPROVED.equals(status)) {
                updateEntity.setRejectReason(rejectReason);
            }
            updateEntity.setUpdatedAt(new Date());

            int updatedCount = pilotageRequestsMapper.update(updateEntity, updateWrapper);
            
            if (updatedCount > 0) {
                for (PilotageRequests request : requests) {
                    try {
                        Voyages voyage = voyagesMapper.selectById(request.getVoyageId());
                        if (voyage != null && voyage.getAgentId() != null) {
                            // 根据voyage中的agent_id获取代理公司信息，包括email字段
                            Agents agent = agentsMapper.selectById(voyage.getAgentId());
                            if (agent != null && agent.getEmail() != null && !agent.getEmail().isEmpty()) {
                                Vessels vessel = vesselsMapper.selectById(request.getVesselId());
                                String subject = "引航申请审核结果通知";
                                String content = buildEmailContent(request, vessel, agent, status, rejectReason);
                                
                                messageService.sendEmail(agent.getEmail(), subject, content, true);
                                log.info("已发送审核结果邮件至: {} (申请ID: {})", agent.getEmail(), request.getId());
                            }
                        }
                    } catch (Exception e) {
                        log.error("发送审核结果邮件失败，申请ID: {}，错误: {}", request.getId(), e.getMessage(), e);
                        // 邮件发送失败不影响主业务流程
                    }
                }
            }

            return updatedCount > 0;
        } catch (BusinessException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            log.error("批量审核引航申请失败，申请IDs: {}", requestIds, e);
            throw new RuntimeException("批量审核引航申请失败: " + e.getMessage(), e);
        }
    }
    
        private String buildEmailContent(PilotageRequests request, Vessels vessel, Agents agent, 
                                   RequestStatusEnum status, String rejectReason) {
        StringBuilder content = new StringBuilder();
        content.append("<h2>引航申请审核结果通知</h2>");
        content.append("<p><strong>申请编号:</strong> ").append(request.getRequestCode() != null ? request.getRequestCode() : "N/A").append("</p>");
        content.append("<p><strong>船舶名称:</strong> ").append(vessel.getNameZh() != null ? vessel.getNameZh() : "N/A").append("</p>");
        content.append("<p><strong>审核结果:</strong> ");
        if (RequestStatusEnum.APPROVED.equals(status)) {
            content.append("<span style=\"color: green;\">通过</span>");
        } else {
            content.append("<span style=\"color: red;\">驳回</span>");
        }
        content.append("</p>");
        
        if (!RequestStatusEnum.APPROVED.equals(status) && rejectReason != null) {
            content.append("<p><strong>驳回原因:</strong> ").append(rejectReason).append("</p>");
        }
        
        content.append("<p><strong>审核时间:</strong> ").append(request.getReviewedAt() != null ? request.getReviewedAt() : new Date()).append("</p>");
        content.append("<p><strong>代理公司:</strong> ").append(agent.getCompanyName() != null ? agent.getCompanyName() : "N/A").append("</p>");
        content.append("<p>如有疑问，请及时联系相关部门。</p>");
        
        return content.toString();
    }

    @Override
    @Transactional
    public boolean updatePilotageRequestStatus(List<Long> requestIds, RequestStatusEnum status) {
        try {
            List<PilotageRequests> requests = pilotageRequestsMapper.selectBatchIds(requestIds);

            if (requests.size() != requestIds.size()) {
                throw new BusinessException(ErrorCode.REQUEST_NOT_FOUND, "部分申请不存在");
            }

            LambdaQueryWrapper<PilotageRequests> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.in(PilotageRequests::getId, requestIds);

            PilotageRequests updateEntity = new PilotageRequests();
            updateEntity.setRequestStatus(status);
            updateEntity.setUpdatedAt(new Date());

            int updatedCount = pilotageRequestsMapper.update(updateEntity, updateWrapper);

            return updatedCount > 0;
        } catch (BusinessException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            log.error("批量更新引航申请状态失败，申请IDs: {}", requestIds, e);
            throw new RuntimeException("批量更新引航申请状态失败: " + e.getMessage(), e);
        }
    }

    private MPJLambdaWrapper<PilotageRequests> buildPageQueryWrapper(PageQueryDTO query) {
        MPJLambdaWrapper<PilotageRequests> wrapper = new MPJLambdaWrapper<PilotageRequests>()
                // PilotageRequests 字段映射
                .selectAs(PilotageRequests::getId, PilotageRequestDTO::getId)
                .selectAs(PilotageRequests::getRequestCode, PilotageRequestDTO::getRequestCode)
                .selectAs(PilotageRequests::getVesselId, PilotageRequestDTO::getVesselId)
                .selectAs(PilotageRequests::getVoyageId, PilotageRequestDTO::getVoyageId)
                .selectAs(PilotageRequests::getExpectedBerthId, PilotageRequestDTO::getExpectedBerthId)
                .selectAs(PilotageRequests::getBargeNumber, PilotageRequestDTO::getBargeNumber)
                .selectAs(PilotageRequests::getInboundDraftForward, PilotageRequestDTO::getInboundDraftForward)
                .selectAs(PilotageRequests::getInboundDraftAft, PilotageRequestDTO::getInboundDraftAft)
                .selectAs(PilotageRequests::getTotalPassengers, PilotageRequestDTO::getTotalPassengers)
                .selectAs(PilotageRequests::getTotalCrew, PilotageRequestDTO::getTotalCrew)
                .selectAs(PilotageRequests::getInboundCargoName, PilotageRequestDTO::getInboundCargoName)
                .selectAs(PilotageRequests::getInboundCargoTotalWeight, PilotageRequestDTO::getInboundCargoTotalWeight)
                .selectAs(PilotageRequests::getInboundGeneralCargoDetails, PilotageRequestDTO::getInboundGeneralCargoDetails)
                .selectAs(PilotageRequests::getInboundDangerousCargoDetails, PilotageRequestDTO::getInboundDangerousCargoDetails)
                .selectAs(PilotageRequests::getOutboundCargoName, PilotageRequestDTO::getOutboundCargoName)
                .selectAs(PilotageRequests::getOutboundCargoTotalWeight, PilotageRequestDTO::getOutboundCargoTotalWeight)
                .selectAs(PilotageRequests::getOutboundGeneralCargoDetails, PilotageRequestDTO::getOutboundGeneralCargoDetails)
                .selectAs(PilotageRequests::getOutboundDangerousCargoDetails, PilotageRequestDTO::getOutboundDangerousCargoDetails)
                .selectAs(PilotageRequests::getRequestNotes, PilotageRequestDTO::getRequestNotes)
                .selectAs(PilotageRequests::getApplicant, PilotageRequestDTO::getApplicant)
                .selectAs(PilotageRequests::getApplicationDate, PilotageRequestDTO::getApplicationDate)
                .selectAs(PilotageRequests::getRequestSource, PilotageRequestDTO::getRequestSource)
                .selectAs(PilotageRequests::getVisaProcessingResult, PilotageRequestDTO::getVisaProcessingResult)
                .selectAs(PilotageRequests::getRequestStatus, PilotageRequestDTO::getRequestStatus)
                .selectAs(PilotageRequests::getReviewerId, PilotageRequestDTO::getReviewerId)
                .selectAs(PilotageRequests::getReviewedAt, PilotageRequestDTO::getReviewedAt)
                .selectAs(PilotageRequests::getRejectReason, PilotageRequestDTO::getRejectReason)
                .selectAs(PilotageRequests::getPriority, PilotageRequestDTO::getPriority)
                .selectAs(PilotageRequests::getSpecialRequirements, PilotageRequestDTO::getSpecialRequirements)
                .selectAs(PilotageRequests::getCreatedAt, PilotageRequestDTO::getCreatedAt)
                .selectAs(PilotageRequests::getUpdatedAt, PilotageRequestDTO::getUpdatedAt)

                // Vessels 字段映射
                .selectAs(Vessels::getVesselCallSign, PilotageRequestDTO::getVesselCallSign)
                .selectAs(Vessels::getImoNumber, PilotageRequestDTO::getImoNumber)
                .selectAs(Vessels::getMmsiNumber, PilotageRequestDTO::getMmsiNumber)
                .selectAs(Vessels::getVesselType, PilotageRequestDTO::getVesselType)
                .selectAs(Vessels::getNationality, PilotageRequestDTO::getNationality)
                .selectAs(Vessels::getNameZh, PilotageRequestDTO::getNameZh)
                .selectAs(Vessels::getNameEn, PilotageRequestDTO::getNameEn)
                .selectAs(Vessels::getVesselLength, PilotageRequestDTO::getVesselLength)
                .selectAs(Vessels::getVesselWidth, PilotageRequestDTO::getVesselWidth)
                .selectAs(Vessels::getFullLoadDraft, PilotageRequestDTO::getFullLoadDraft)
                .selectAs(Vessels::getDepth, PilotageRequestDTO::getDepth)
                .selectAs(Vessels::getMaxHeight, PilotageRequestDTO::getMaxHeight)
                .selectAs(Vessels::getAirDraft, PilotageRequestDTO::getAirDraft)
                .selectAs(Vessels::getSpeed, PilotageRequestDTO::getSpeed)
                .selectAs(Vessels::getGrossTonnage, PilotageRequestDTO::getGrossTonnage)
                .selectAs(Vessels::getNetTonnage, PilotageRequestDTO::getNetTonnage)
                .selectAs(Vessels::getDeadweight, PilotageRequestDTO::getDeadweight)
                .selectAs(Vessels::getMainEngineType, PilotageRequestDTO::getMainEngineType)
                .selectAs(Vessels::getMainEnginePower, PilotageRequestDTO::getMainEnginePower)
                .selectAs(Vessels::getBowThrusterPower, PilotageRequestDTO::getBowThrusterPower)
                .selectAs(Vessels::getAisPilotInterface, PilotageRequestDTO::getAisPilotInterface)
                .selectAs(Vessels::getVesselColor, PilotageRequestDTO::getVesselColor)
                .selectAs(Vessels::getFunnelColor, PilotageRequestDTO::getFunnelColor)
                .selectAs(Vessels::getFunnelMark, PilotageRequestDTO::getFunnelMark)
                .selectAs(Vessels::getBuildDate, PilotageRequestDTO::getBuildDate)
                .selectAs(Vessels::getBlacklistRecord, PilotageRequestDTO::getBlacklistRecord)
                .selectAs(Vessels::getBadStatusRecord, PilotageRequestDTO::getBadStatusRecord)
                .selectAs(Vessels::getOtherAbnormalRecord, PilotageRequestDTO::getOtherAbnormalRecord)
                .selectAs(Vessels::getPortOfRegistry, PilotageRequestDTO::getPortOfRegistry)
                .selectAs(Vessels::getClassificationSociety, PilotageRequestDTO::getClassificationSociety)
                .selectAs(Vessels::getManagementCompany, PilotageRequestDTO::getManagementCompany)
                .selectAs(Vessels::getInsuranceCompany, PilotageRequestDTO::getInsuranceCompany)
                .selectAs(Vessels::getPniClub, PilotageRequestDTO::getPniClub)
                .selectAs(Vessels::getOwnerZh, PilotageRequestDTO::getOwnerZh)
                .selectAs(Vessels::getOwnerEn, PilotageRequestDTO::getOwnerEn)
                .selectAs(Vessels::getRouteScope, PilotageRequestDTO::getRouteScope)
                .selectAs(Vessels::getRouteType, PilotageRequestDTO::getRouteType)

            // Voyages 字段映射
            .selectAs(Voyages::getVoyageNumber, PilotageRequestDTO::getVoyageNumber)
            .selectAs(Voyages::getVoyageType, PilotageRequestDTO::getVoyageType)
            .selectAs(Voyages::getPreviousPort, PilotageRequestDTO::getPreviousPort)
            .selectAs(Voyages::getNextPort, PilotageRequestDTO::getNextPort)
            .selectAs(Voyages::getPortOfDepartureId, PilotageRequestDTO::getPortOfDepartureId)
            .selectAs(Voyages::getPortOfDestinationId, PilotageRequestDTO::getPortOfDestinationId)
            .selectAs(Voyages::getEta, PilotageRequestDTO::getEta)
            .selectAs(Voyages::getEtaAnchorage, PilotageRequestDTO::getEtaAnchorage)
            .selectAs(Voyages::getAta, PilotageRequestDTO::getAta)
            .selectAs(Voyages::getEtd, PilotageRequestDTO::getEtd)
            .selectAs(Voyages::getAtd, PilotageRequestDTO::getAtd)
            .selectAs(Voyages::getCargoName, PilotageRequestDTO::getCargoName)
            .selectAs(Voyages::getCargoType, PilotageRequestDTO::getCargoType)
            .selectAs(Voyages::getCargoQuantity, PilotageRequestDTO::getCargoQuantity)
            .selectAs(Voyages::getCargoUnit, PilotageRequestDTO::getCargoUnit)
            .selectAs(Voyages::getDraftForward, PilotageRequestDTO::getDraftForward)
            .selectAs(Voyages::getDraftAft, PilotageRequestDTO::getDraftAft)
            .selectAs(Voyages::getVoyageStatus, PilotageRequestDTO::getVoyageStatus)
            .selectAs(Voyages::getAgentId, PilotageRequestDTO::getAgentId)
            .selectAs(Voyages::getRemarks, PilotageRequestDTO::getVoyageRemarks)
            .selectAs(Voyages::getStartLocationType, PilotageRequestDTO::getStartLocationType)
            .selectAs(Voyages::getEndLocationType, PilotageRequestDTO::getEndLocationType)
            .selectAs(Voyages::getPlannedStartId, PilotageRequestDTO::getPlannedStartId)
            .selectAs(Voyages::getPlannedEndId, PilotageRequestDTO::getPlannedEndId)

            // Agents 字段映射
            .selectAs(Agents::getAgentCode, PilotageRequestDTO::getAgentCode)
            .selectAs(Agents::getCompanyName, PilotageRequestDTO::getAgentName)
            .selectAs(Agents::getShortName, PilotageRequestDTO::getShortName)
            .selectAs(Agents::getPrincipal, PilotageRequestDTO::getPrincipal)
            .selectAs(Agents::getContactPerson, PilotageRequestDTO::getContactPerson)
            .selectAs(Agents::getPhone, PilotageRequestDTO::getAgentPhone)
            .selectAs(Agents::getAddress, PilotageRequestDTO::getAgentAddress)
            .selectAs(Agents::getEmail, PilotageRequestDTO::getAgentEmail)
            .selectAs(Agents::getCreditRating, PilotageRequestDTO::getCreditRating)
            .selectAs(Agents::getPaymentStatus, PilotageRequestDTO::getPaymentStatus)
            .selectAs(Agents::getBusinessLicense, PilotageRequestDTO::getBusinessLicense)
            .selectAs(Agents::getRegisteredCapital, PilotageRequestDTO::getRegisteredCapital)
            .selectAs(Agents::getDefaultTaxpayerId, PilotageRequestDTO::getDefaultTaxpayerId)
            .selectAs(Agents::getDefaultBankName, PilotageRequestDTO::getDefaultBankName)
            .selectAs(Agents::getDefaultBankAccount, PilotageRequestDTO::getDefaultBankAccount)
            .selectAs(Agents::getDefaultAddress, PilotageRequestDTO::getDefaultAddress)

            // Users 字段映射（审核人信息）
            .selectAs(Users::getUserCode, PilotageRequestDTO::getReviewerUserCode)
            .selectAs(Users::getAccount, PilotageRequestDTO::getReviewerAccount)
            .selectAs(Users::getUserType, PilotageRequestDTO::getReviewerUserType)
            .selectAs(Users::getUsername, PilotageRequestDTO::getReviewerUsername)
            .selectAs(Users::getApplyTime, PilotageRequestDTO::getReviewerApplyTime)
            .selectAs(Users::getPhone, PilotageRequestDTO::getReviewerPhone)
            .selectAs(Users::getEmail, PilotageRequestDTO::getReviewerEmail)
            .selectAs(Users::getCurrentStatus, PilotageRequestDTO::getReviewerCurrentStatus)
            .selectAs(Users::getAgentLevelType, PilotageRequestDTO::getReviewerAgentLevelType)
            .selectAs(Users::getParentAgentId, PilotageRequestDTO::getReviewerParentAgentId)
            .selectAs(Users::getAvatar, PilotageRequestDTO::getReviewerAvatar)
            .selectAs(Users::getLastLoginAt, PilotageRequestDTO::getReviewerLastLoginAt)
            .selectAs(Users::getDepartment, PilotageRequestDTO::getReviewerDepartment)
            .selectAs(Users::getPosition, PilotageRequestDTO::getReviewerPosition)

            // PilotLadderInfo 字段映射
            .selectAs(PilotLadderInfo::getLadderStatus, PilotageRequestDTO::getLadderStatus)
            .selectAs(PilotLadderInfo::getLadderType, PilotageRequestDTO::getLadderType)
            .selectAs(PilotLadderInfo::getFreeboardHeight, PilotageRequestDTO::getFreeboardHeight)
            .selectAs(PilotLadderInfo::getInspectionDate, PilotageRequestDTO::getInspectionDate)
            .selectAs(PilotLadderInfo::getInspectionResult, PilotageRequestDTO::getInspectionResult)
            .selectAs(PilotLadderInfo::getInspector, PilotageRequestDTO::getInspector)
            .selectAs(PilotLadderInfo::getRemarks, PilotageRequestDTO::getLadderRemarks)

            // AgentInvoiceInfo 字段映射
            .selectAs(AgentInvoiceInfo::getAgentId, PilotageRequestDTO::getAgentId)
            .selectAs(AgentInvoiceInfo::getAgentName, PilotageRequestDTO::getAgentName)
            .selectAs(AgentInvoiceInfo::getPayingUnitName, PilotageRequestDTO::getPayingUnitName)
            .selectAs(AgentInvoiceInfo::getInvoiceType, PilotageRequestDTO::getInvoiceType)
            .selectAs(AgentInvoiceInfo::getTaxpayerId, PilotageRequestDTO::getTaxpayerId)
            .selectAs(AgentInvoiceInfo::getPayingUnitAddress, PilotageRequestDTO::getPayingUnitAddress)
            .selectAs(AgentInvoiceInfo::getPayingUnitPhone, PilotageRequestDTO::getPayingUnitPhone)
            .selectAs(AgentInvoiceInfo::getPayingUnitBank, PilotageRequestDTO::getPayingUnitBank)
            .selectAs(AgentInvoiceInfo::getPayingUnitBankAccount, PilotageRequestDTO::getPayingUnitBankAccount)
            .selectAs(AgentInvoiceInfo::getBillingVoucherId, PilotageRequestDTO::getBillingVoucherId)
            .selectAs(AgentInvoiceInfo::getCreatedAt, PilotageRequestDTO::getCreatedAt)
            .selectAs(AgentInvoiceInfo::getUpdatedAt, PilotageRequestDTO::getUpdatedAt)

            // PortAreas 字段映射
            .selectAs(Wharves::getPortAreaId, PilotageRequestDTO::getPortAreaId)
            .selectAs(PortAreas::getAreaCode, PilotageRequestDTO::getPortAreaCode)
            .selectAs(PortAreas::getAreaNameZh, PilotageRequestDTO::getPortAreaNameZh)
            .selectAs(PortAreas::getAreaNameEn, PilotageRequestDTO::getPortAreaNameEn)

            // Wharves 字段映射
            .selectAs(Wharves::getId, PilotageRequestDTO::getWharfId)
            .selectAs(Wharves::getWharfCode, PilotageRequestDTO::getWharfCode)
            .selectAs(Wharves::getWharfNameZh, PilotageRequestDTO::getWharfNameZh)
            .selectAs(Wharves::getWharfNameEn, PilotageRequestDTO::getWharfNameEn)

            //pilotage_plans字段映射
            .selectAs(PilotagePlans::getId, PilotageRequestDTO::getPlanId)
            .selectAs(PilotagePlans::getPlanStatus, PilotageRequestDTO::getPlanStatus)
            .selectAs(PilotagePlans::getPriority, PilotageRequestDTO::getPlanPriority)
            
            // 使用CASE WHEN表达式根据位置类型选择合适的名称
            .select("CASE WHEN v.start_location_type = 'BERTH' THEN b1.berth_name " +
                   "WHEN v.start_location_type = 'ANCHORAGE' THEN a1.anchorage_name_zh " +
                   "ELSE NULL END AS plannedStartName")
            .select("CASE WHEN v.end_location_type = 'BERTH' THEN b2.berth_name " +
                   "WHEN v.end_location_type = 'ANCHORAGE' THEN a2.anchorage_name_zh " +
                   "ELSE NULL END AS plannedEndName")
                   
            .leftJoin(Vessels.class, Vessels::getId, PilotageRequests::getVesselId)
            .leftJoin(Voyages.class, "v", Voyages::getId, PilotageRequests::getVoyageId)
            .leftJoin(Users.class, Users::getId, PilotageRequests::getReviewerId)
            .leftJoin(Agents.class, Agents::getId, Voyages::getAgentId)
            .leftJoin(Berths.class, "b1", Berths::getId, Voyages::getPlannedStartId)
            .leftJoin(Anchorages.class, "a1", Anchorages::getId, Voyages::getPlannedStartId)
            .leftJoin(Berths.class, "b2", Berths::getId, Voyages::getPlannedEndId)
            .leftJoin(Anchorages.class, "a2", Anchorages::getId, Voyages::getPlannedEndId)
            .leftJoin(PilotLadderInfo.class, PilotLadderInfo::getVoyageId,Voyages::getId )
            .leftJoin(AgentInvoiceInfo.class, AgentInvoiceInfo::getVoyageId, Voyages::getId)
            .leftJoin(PilotagePlans.class, PilotagePlans::getVoyageId, Voyages::getId)

            .leftJoin(Wharves.class, Wharves::getId, Berths::getWharfId)  // 假设Berths中有wharf_id字段关联Wharves
            .leftJoin(PortAreas.class, PortAreas::getId, Wharves::getPortAreaId)

            .orderByDesc(PilotageRequests::getCreatedAt);
        
        if (query != null) {
            if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
                String keyword = query.getKeyword().trim();
                wrapper.and(w -> w.like(PilotageRequests::getRequestCode, keyword)
                        .or()
                        .like(Vessels::getNameZh, keyword)
                        .or()
                        .like(Vessels::getNameEn, keyword)
                        .or()
                        .like(Vessels::getImoNumber, keyword)
                        .or()
                        .like(Vessels::getVesselCallSign, keyword)
                        .or()
                        .like(Voyages::getVoyageNumber, keyword)
                        .or()
                        .like(Agents::getCompanyName, keyword)
                        .or()
                        .like(Agents::getShortName, keyword));
            }
            if (query.getParams() != null) {
                Map<String, Object> params = query.getParams();
                if (params.get("requestStatus") != null && !String.valueOf(params.get("requestStatus")).trim().isEmpty()) {
                    wrapper.eq(PilotageRequests::getRequestStatus, params.get("requestStatus"));
                }
                if (params.get("planStatus") != null && !String.valueOf(params.get("planStatus")).trim().isEmpty()) {
                    String ps = String.valueOf(params.get("planStatus")).trim();
                    // 特殊值：未计划（没有关联计划/计划状态为空）
                    if ("UNPLANNED".equalsIgnoreCase(ps) || "NOT_PLANNED".equalsIgnoreCase(ps) || "未计划".equals(ps)) {
                        wrapper.and(w -> w.isNull(PilotagePlans::getId).or().isNull(PilotagePlans::getPlanStatus));
                    } else {
                        wrapper.eq(PilotagePlans::getPlanStatus, ps);
                    }
                }
                if (params.get("nameZh") != null && !String.valueOf(params.get("nameZh")).trim().isEmpty()) {
                    wrapper.like(Vessels::getNameZh, params.get("nameZh"));
                }
                if (params.get("nameEn") != null && !String.valueOf(params.get("nameEn")).trim().isEmpty()) {
                    wrapper.like(Vessels::getNameEn, params.get("nameEn"));
                }
            }
        }
        return wrapper;

    }

    private void mapVesselData(PilotageRequestDTO dto, Vessels entity) {
        entity.setVesselCallSign(dto.getVesselCallSign());
        entity.setImoNumber(dto.getImoNumber());
        entity.setMmsiNumber(dto.getMmsiNumber());
        entity.setVesselType(dto.getVesselType());
        entity.setNationality(dto.getNationality());
        entity.setNameZh(dto.getNameZh());
        entity.setNameEn(dto.getNameEn());
        entity.setVesselLength(dto.getVesselLength());
        entity.setVesselWidth(dto.getVesselWidth());
        entity.setDepth(dto.getDepth());
        entity.setSpeed(dto.getSpeed());
        entity.setGrossTonnage(dto.getGrossTonnage());
        entity.setNetTonnage(dto.getNetTonnage());
        entity.setDeadweight(dto.getDeadweight());
        entity.setMainEngineType(dto.getMainEngineType());
        entity.setMainEnginePower(dto.getMainEnginePower());
        entity.setBowThrusterPower(dto.getBowThrusterPower());
        entity.setAisPilotInterface(dto.getAisPilotInterface());
        entity.setBuildDate(dto.getBuildDate());
        
        if (dto.getRouteType() != null) {
            if (dto.getRouteType() instanceof String) {
                String routeTypeStr = (String) dto.getRouteType();
                if ("INTERNATIONAL".equalsIgnoreCase(routeTypeStr) || "国际航线".equals(routeTypeStr)) {
                    entity.setRouteType(RouteTypeEnum.INTERNATIONAL);
                } else if ("DOMESTIC".equalsIgnoreCase(routeTypeStr) || "国内航线".equals(routeTypeStr)) {
                    entity.setRouteType(RouteTypeEnum.DOMESTIC);
                } else {
                    entity.setRouteType(RouteTypeEnum.DOMESTIC);
                }
            } else if (dto.getRouteType() instanceof RouteTypeEnum) {
                entity.setRouteType((RouteTypeEnum) dto.getRouteType());
            } else {
                // 如果是其他类型，尝试转换
                String routeTypeStr = dto.getRouteType().toString();
                if ("INTERNATIONAL".equalsIgnoreCase(routeTypeStr)) {
                    entity.setRouteType(RouteTypeEnum.INTERNATIONAL);
                } else {
                    entity.setRouteType(RouteTypeEnum.DOMESTIC);
                }
            }
        } else {
            entity.setRouteType(RouteTypeEnum.DOMESTIC);
        }
        
        entity.setOwnerZh(dto.getOwnerZh());
        entity.setOwnerEn(dto.getOwnerEn());
        entity.setRouteScope(dto.getRouteScope());
        entity.setFullLoadDraft(dto.getFullLoadDraft());
        entity.setMaxHeight(dto.getMaxHeight());
        entity.setAirDraft(dto.getAirDraft());
        entity.setVesselColor(dto.getVesselColor());
        entity.setFunnelColor(dto.getFunnelColor());
        entity.setFunnelMark(dto.getFunnelMark());
        entity.setBlacklistRecord(dto.getBlacklistRecord());
        entity.setBadStatusRecord(dto.getBadStatusRecord());
        entity.setOtherAbnormalRecord(dto.getOtherAbnormalRecord());
        entity.setPortOfRegistry(dto.getPortOfRegistry());
        entity.setClassificationSociety(dto.getClassificationSociety());
        entity.setManagementCompany(dto.getManagementCompany());
        entity.setInsuranceCompany(dto.getInsuranceCompany());
        entity.setPniClub(dto.getPniClub());
    }

    private void mapVoyageData(PilotageRequestDTO dto, Voyages entity) {
        entity.setEta(dto.getEta());
        entity.setEtd(dto.getEtd());
        entity.setEtaAnchorage(dto.getEtaAnchorage());
        entity.setAta(dto.getAta());
        entity.setAtd(dto.getAtd());
        entity.setPreviousPort(dto.getPreviousPort());
        entity.setNextPort(dto.getNextPort());
        entity.setPortOfDepartureId(dto.getPortOfDepartureId());
        entity.setPortOfDestinationId(dto.getPortOfDestinationId());
        entity.setDraftForward(dto.getDraftForward());
        entity.setDraftAft(dto.getDraftAft());
        // 处理航次状态，避免空值转换异常
        if (dto.getVoyageStatus() != null && !"".equals(dto.getVoyageStatus().toString().trim())) {
            entity.setVoyageStatus(VoyageStatusEnum.fromDescription(String.valueOf(dto.getVoyageStatus())));
        }
        // 处理航次类型，避免空值转换异常
        if (dto.getVoyageType() != null && !"".equals(dto.getVoyageType().toString().trim())) {
            entity.setVoyageType(VoyageTypeEnum.fromDescription(String.valueOf(dto.getVoyageType())));
        }
        // 使用前端传入的位置ID字段
        entity.setPlannedStartId(dto.getPlannedStartId());
        entity.setPlannedEndId(dto.getPlannedEndId());
        entity.setActualStartId(dto.getActualStartId());
        entity.setActualEndId(dto.getActualEndId());
        entity.setRemarks(dto.getVoyageRemarks());
        entity.setCargoName(dto.getCargoName());
        entity.setCargoType(dto.getCargoType());
        entity.setCargoQuantity(dto.getCargoQuantity());
        entity.setCargoUnit(dto.getCargoUnit());
        entity.setAgentId(dto.getAgentId());
        entity.setStartLocationType(dto.getStartLocationType());
        entity.setEndLocationType(dto.getEndLocationType());
    }

    private void mapPilotageRequestsData(PilotageRequestDTO dto, PilotageRequests entity) {
        entity.setRequestNotes(dto.getRequestNotes());
        entity.setReviewerId(dto.getReviewerId());
    }

    
    private void mapPaymentInfoData(PilotageRequestDTO dto, PaymentInfo entity) {
        entity.setPayeeUnit(dto.getPayee_unit());
        entity.setPayeeBankAccount(dto.getPayee_bank_account());
        entity.setPayeeBankName(dto.getPayee_bank_name());
    }

    private void mapAgentInvoiceInfoData(PilotageRequestDTO dto, AgentInvoiceInfo entity) {
        entity.setAgentId(dto.getAgentId());
        entity.setAgentName(dto.getAgentName());
        entity.setPayingUnitName(dto.getPayingUnitName());
        entity.setInvoiceType(dto.getInvoiceType());
        entity.setTaxpayerId(dto.getTaxpayerId());
        entity.setPayingUnitAddress(dto.getPayingUnitAddress());
        entity.setPayingUnitPhone(dto.getPayingUnitPhone());
        entity.setPayingUnitBank(dto.getPayingUnitBank());
        entity.setPayingUnitBankAccount(dto.getPayingUnitBankAccount());
    }

    private void mapPilotLadderInfoData(PilotageRequestDTO dto, PilotLadderInfo entity) {
        entity.setLadderStatus(dto.getLadderStatus());
        entity.setLadderType(dto.getLadderType());
        entity.setFreeboardHeight(dto.getFreeboardHeight());
        entity.setInspectionDate(dto.getInspectionDate());
        entity.setInspectionResult(dto.getInspectionResult());
        entity.setInspector(dto.getInspector());
        entity.setRemarks(dto.getLadderRemarks());
    }

    private void mapVesselToDTO(Vessels entity, PilotageRequestDTO dto) {
        dto.setVesselCallSign(entity.getVesselCallSign());
        dto.setImoNumber(entity.getImoNumber());
        dto.setMmsiNumber(entity.getMmsiNumber());
        dto.setVesselType(entity.getVesselType());
        dto.setNationality(entity.getNationality());
        dto.setNameZh(entity.getNameZh());
        dto.setNameEn(entity.getNameEn());
        dto.setVesselLength(entity.getVesselLength());
        dto.setVesselWidth(entity.getVesselWidth());
        dto.setDepth(entity.getDepth());
        dto.setSpeed(entity.getSpeed());
        dto.setGrossTonnage(entity.getGrossTonnage());
        dto.setNetTonnage(entity.getNetTonnage());
        dto.setDeadweight(entity.getDeadweight());
        dto.setMainEngineType(entity.getMainEngineType());
        dto.setMainEnginePower(entity.getMainEnginePower());
        dto.setBowThrusterPower(entity.getBowThrusterPower());
        dto.setAisPilotInterface(entity.getAisPilotInterface());
        dto.setBuildDate(entity.getBuildDate());
        dto.setRouteType(entity.getRouteType());
        dto.setOwnerZh(entity.getOwnerZh());
        dto.setOwnerEn(entity.getOwnerEn());
        dto.setRouteScope(entity.getRouteScope());
        dto.setFullLoadDraft(entity.getFullLoadDraft());
        dto.setMaxHeight(entity.getMaxHeight());
        dto.setAirDraft(entity.getAirDraft());
        dto.setVesselColor(entity.getVesselColor());
        dto.setFunnelColor(entity.getFunnelColor());
        dto.setFunnelMark(entity.getFunnelMark());
        dto.setBlacklistRecord(entity.getBlacklistRecord());
        dto.setBadStatusRecord(entity.getBadStatusRecord());
        dto.setOtherAbnormalRecord(entity.getOtherAbnormalRecord());
        dto.setPortOfRegistry(entity.getPortOfRegistry());
        dto.setClassificationSociety(entity.getClassificationSociety());
        dto.setManagementCompany(entity.getManagementCompany());
        dto.setInsuranceCompany(entity.getInsuranceCompany());
        dto.setPniClub(entity.getPniClub());

        dto.setVesselName(entity.getNameZh());
    }

    private void mapVoyageToDTO(Voyages entity, PilotageRequestDTO dto) {
        dto.setVoyageNumber(entity.getVoyageNumber());
        dto.setVoyageType(entity.getVoyageType());
        dto.setPreviousPort(entity.getPreviousPort());
        dto.setNextPort(entity.getNextPort());
        dto.setPortOfDepartureId(entity.getPortOfDepartureId());
        dto.setPortOfDestinationId(entity.getPortOfDestinationId());
        dto.setEta(entity.getEta());
        dto.setEtaAnchorage(entity.getEtaAnchorage());
        dto.setAta(entity.getAta());
        dto.setEtd(entity.getEtd());
        dto.setAtd(entity.getAtd());
        dto.setCargoName(entity.getCargoName());
        dto.setCargoType(entity.getCargoType());
        dto.setCargoQuantity(entity.getCargoQuantity());
        dto.setCargoUnit(entity.getCargoUnit());
        dto.setDraftForward(entity.getDraftForward());
        dto.setDraftAft(entity.getDraftAft());
        dto.setTotalPassengers(entity.getTotalPassengers());
        dto.setTotalCrew(entity.getTotalCrew());
        dto.setVoyageStatus(entity.getVoyageStatus());
        dto.setAgentId(entity.getAgentId());
        dto.setPlannedStartId(entity.getPlannedStartId());
        dto.setActualStartId(entity.getActualStartId());
        dto.setPlannedEndId(entity.getPlannedEndId());
        dto.setActualEndId(entity.getActualEndId());
        dto.setVoyageRemarks(entity.getRemarks());
    }

    private void mapAgentInvoiceInfoToDTO(AgentInvoiceInfo entity, PilotageRequestDTO dto) {
        dto.setAgentId(entity.getAgentId());
        dto.setVoyageId(entity.getVoyageId());
        dto.setAgentName(entity.getAgentName());
        dto.setPayingUnitName(entity.getPayingUnitName());
        dto.setInvoiceType(entity.getInvoiceType());
        dto.setTaxpayerId(entity.getTaxpayerId());
        dto.setPayingUnitAddress(entity.getPayingUnitAddress());
        dto.setPayingUnitPhone(entity.getPayingUnitPhone());
        dto.setPayingUnitBank(entity.getPayingUnitBank());
        dto.setPayingUnitBankAccount(entity.getPayingUnitBankAccount());
        dto.setBillingVoucherId(entity.getBillingVoucherId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
    }

    private void mapPilotageRequestsToDTO(PilotageRequests entity, PilotageRequestDTO dto) {
        dto.setId(entity.getId());
        dto.setRequestCode(entity.getRequestCode());
        dto.setVesselId(entity.getVesselId());
        dto.setVoyageId(entity.getVoyageId());
        dto.setExpectedBerthId(entity.getExpectedBerthId());
        dto.setBargeNumber(entity.getBargeNumber());
        dto.setInboundDraftForward(entity.getInboundDraftForward());
        dto.setInboundDraftAft(entity.getInboundDraftAft());
        dto.setTotalPassengers(entity.getTotalPassengers());
        dto.setTotalCrew(entity.getTotalCrew());
        dto.setInboundCargoName(entity.getInboundCargoName());
        dto.setInboundCargoTotalWeight(entity.getInboundCargoTotalWeight());
        dto.setInboundGeneralCargoDetails(entity.getInboundGeneralCargoDetails());
        dto.setInboundDangerousCargoDetails(entity.getInboundDangerousCargoDetails());
        dto.setOutboundCargoName(entity.getOutboundCargoName());
        dto.setOutboundCargoTotalWeight(entity.getOutboundCargoTotalWeight());
        dto.setOutboundGeneralCargoDetails(entity.getOutboundGeneralCargoDetails());
        dto.setOutboundDangerousCargoDetails(entity.getOutboundDangerousCargoDetails());
        dto.setRequestNotes(entity.getRequestNotes());
        dto.setApplicant(entity.getApplicant());
        dto.setApplicationDate(entity.getApplicationDate());
        dto.setRequestSource(entity.getRequestSource());
        dto.setVisaProcessingResult(entity.getVisaProcessingResult());
        dto.setRequestStatus(entity.getRequestStatus());
        dto.setReviewerId(entity.getReviewerId());
        dto.setReviewedAt(entity.getReviewedAt());
        dto.setRejectReason(entity.getRejectReason());
        dto.setPriority(entity.getPriority());
        dto.setSpecialRequirements(entity.getSpecialRequirements());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
    }

    private void mapPilotLadderInfoToDTO(PilotLadderInfo entity, PilotageRequestDTO dto) {
        dto.setLadderStatus(entity.getLadderStatus());
        dto.setLadderType(entity.getLadderType());
        dto.setFreeboardHeight(entity.getFreeboardHeight());
        dto.setInspectionDate(entity.getInspectionDate());
        dto.setInspectionResult(entity.getInspectionResult());
        dto.setInspector(entity.getInspector());
        dto.setLadderRemarks(entity.getRemarks());
    }
}
