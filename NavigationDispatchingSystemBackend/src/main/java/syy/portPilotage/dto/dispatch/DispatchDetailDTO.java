package syy.portPilotage.dto.dispatch;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import syy.portPilotage.dto.base.TugboatAssignmentDTO;
import syy.portPilotage.dto.plan.*;
import syy.portPilotage.entity.Vessels;
import syy.portPilotage.entity.Voyages;
import syy.portPilotage.entity.Agents;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "引航调度详情DTO")
public class DispatchDetailDTO {

    @Schema(description = "计划基本信息")
    private PlanBaseInfoDTO planInfo;

    @Schema(description = "船舶信息")
    private Vessels vessel;

    @Schema(description = "航次信息")
    private Voyages voyage;

    @Schema(description = "代理信息")
    private Agents agent;

    @Schema(description = "引航员分配列表")
    private List<PlanPilotAssignmentDTO> pilotAssignments;

    @Schema(description = "驾驶员分配列表")
    private List<PlanDriverAssignmentDTO> driverAssignments;

    @Schema(description = "拖轮分配列表")
    private List<TugboatAssignmentDTO> tugboatAssignments;

    @Schema(description = "引航艇分配列表")
    private List<PilotBoatAssignmentDTO> pilotBoatAssignments;

    @Schema(description = "车辆分配列表")
    private List<PlanVehicleAssignmentDTO> vehicleAssignments;

    @Schema(description = "确认记录列表")
    private List<PlanConfirmationResultDTO> confirmations;

    @Schema(description = "确认进度")
    private Double confirmationProgress;

    @Schema(description = "调度操作历史")
    private List<DispatchOperationLogDTO> operationLogs;
}