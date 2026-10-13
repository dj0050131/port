package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import syy.portPilotage.enums.LocationTypeEnum;
import syy.portPilotage.enums.VoyageTypeEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 船舶航次信息表
 * @TableName voyages
 */
@TableName(value ="voyages")
@Data
public class Voyages implements Serializable {
    /**
     * 航次ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 船舶ID
     */
    @TableField(value = "vessel_id")
    private Long vesselId;

    /**
     * 航次编号
     */
    @TableField(value = "voyage_number")
    private String voyageNumber;

    /**
     * 航次类型
     */
    @TableField(value = "voyage_type")
    private VoyageTypeEnum voyageType;

    /**
     * 上一港
     */
    @TableField(value = "previous_port")
    private String previousPort;

    /**
     * 下一港
     */
    @TableField(value = "next_port")
    private String nextPort;

    /**
     * 预计到港时间
     */
    @TableField(value = "eta")
    private Date eta;

    /**
     * 预抵锚地时间
     */
    @TableField(value = "eta_anchorage")
    private Date etaAnchorage;

    /**
     * 实际到港时间
     */
    @TableField(value = "ata")
    private Date ata;

    /**
     * 预计离港时间
     */
    @TableField(value = "etd")
    private Date etd;

    /**
     * 实际离港时间
     */
    @TableField(value = "atd")
    private Date atd;

    /**
     * 装货名称
     */
    @TableField(value = "cargo_name")
    private String cargoName;

    /**
     * 货物类型
     */
    @TableField(value = "cargo_type")
    private String cargoType;

    /**
     * 货物数量
     */
    @TableField(value = "cargo_quantity")
    private BigDecimal cargoQuantity;

    /**
     * 货物单位
     */
    @TableField(value = "cargo_unit")
    private String cargoUnit;

    /**
     * 前吃水
     */
    @TableField(value = "draft_forward")
    private BigDecimal draftForward;

    /**
     * 后吃水
     */
    @TableField(value = "draft_aft")
    private BigDecimal draftAft;

    /**
     * 旅客总数
     */
    @TableField(value = "total_passengers")
    private Integer totalPassengers;

    /**
     * 船员总数
     */
    @TableField(value = "total_crew")
    private Integer totalCrew;

    /**
     * 航次状态
     */
    @TableField(value = "voyage_status")
    private Object voyageStatus;

    /**
     * 代理公司ID
     */
    @TableField(value = "agent_id")
    private Long agentId;

        @TableField(value = "planned_start_id")
    private Long plannedStartId;

        @TableField(value = "actual_start_id")
    private Long actualStartId;

    /**
     * 目的港ID
     */
    @TableField(value = "port_of_destination_id")
    private Long portOfDestinationId;

    /**
     * 出发港ID
     */
    @TableField(value = "port_of_departure_id")
    private Long portOfDepartureId;

        @TableField(value = "planned_end_id")
    private Long plannedEndId;

        @TableField(value = "actual_end_id")
    private Long actualEndId;

        @TableField(value = "start_location_type")
    private LocationTypeEnum startLocationType;
    
        @TableField(value = "end_location_type")
    private LocationTypeEnum endLocationType;

    /**
     * 航次备注
     */
    @TableField(value = "remarks")
    private String remarks;

    /**
     * 引航申请id
     */
    @TableField(value = "pilotage_requests_id")
    private Long pilotageRequestsId;

        @TableField(value = "created_at")
    private Date createdAt;

        @TableField(value = "updated_at")
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Voyages other = (Voyages) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getVesselId() == null ? other.getVesselId() == null : this.getVesselId().equals(other.getVesselId()))
            && (this.getVoyageNumber() == null ? other.getVoyageNumber() == null : this.getVoyageNumber().equals(other.getVoyageNumber()))
            && (this.getVoyageType() == null ? other.getVoyageType() == null : this.getVoyageType().equals(other.getVoyageType()))
            && (this.getPreviousPort() == null ? other.getPreviousPort() == null : this.getPreviousPort().equals(other.getPreviousPort()))
            && (this.getNextPort() == null ? other.getNextPort() == null : this.getNextPort().equals(other.getNextPort()))
            && (this.getEta() == null ? other.getEta() == null : this.getEta().equals(other.getEta()))
            && (this.getEtaAnchorage() == null ? other.getEtaAnchorage() == null : this.getEtaAnchorage().equals(other.getEtaAnchorage()))
            && (this.getAta() == null ? other.getAta() == null : this.getAta().equals(other.getAta()))
            && (this.getEtd() == null ? other.getEtd() == null : this.getEtd().equals(other.getEtd()))
            && (this.getAtd() == null ? other.getAtd() == null : this.getAtd().equals(other.getAtd()))
            && (this.getCargoName() == null ? other.getCargoName() == null : this.getCargoName().equals(other.getCargoName()))
            && (this.getCargoType() == null ? other.getCargoType() == null : this.getCargoType().equals(other.getCargoType()))
            && (this.getCargoQuantity() == null ? other.getCargoQuantity() == null : this.getCargoQuantity().equals(other.getCargoQuantity()))
            && (this.getCargoUnit() == null ? other.getCargoUnit() == null : this.getCargoUnit().equals(other.getCargoUnit()))
            && (this.getDraftForward() == null ? other.getDraftForward() == null : this.getDraftForward().equals(other.getDraftForward()))
            && (this.getDraftAft() == null ? other.getDraftAft() == null : this.getDraftAft().equals(other.getDraftAft()))
            && (this.getTotalPassengers() == null ? other.getTotalPassengers() == null : this.getTotalPassengers().equals(other.getTotalPassengers()))
            && (this.getTotalCrew() == null ? other.getTotalCrew() == null : this.getTotalCrew().equals(other.getTotalCrew()))
            && (this.getVoyageStatus() == null ? other.getVoyageStatus() == null : this.getVoyageStatus().equals(other.getVoyageStatus()))
            && (this.getAgentId() == null ? other.getAgentId() == null : this.getAgentId().equals(other.getAgentId()))
            && (this.getPlannedStartId() == null ? other.getPlannedStartId() == null : this.getPlannedStartId().equals(other.getPlannedStartId()))
            && (this.getActualStartId() == null ? other.getActualStartId() == null : this.getActualStartId().equals(other.getActualStartId()))
            && (this.getPortOfDestinationId() == null ? other.getPortOfDestinationId() == null : this.getPortOfDestinationId().equals(other.getPortOfDestinationId()))
            && (this.getPortOfDepartureId() == null ? other.getPortOfDepartureId() == null : this.getPortOfDepartureId().equals(other.getPortOfDepartureId()))
            && (this.getPlannedEndId() == null ? other.getPlannedEndId() == null : this.getPlannedEndId().equals(other.getPlannedEndId()))
            && (this.getActualEndId() == null ? other.getActualEndId() == null : this.getActualEndId().equals(other.getActualEndId()))
            && (this.getStartLocationType() == null ? other.getStartLocationType() == null : this.getStartLocationType().equals(other.getStartLocationType()))
            && (this.getEndLocationType() == null ? other.getEndLocationType() == null : this.getEndLocationType().equals(other.getEndLocationType()))
            && (this.getRemarks() == null ? other.getRemarks() == null : this.getRemarks().equals(other.getRemarks()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getPilotageRequestsId() == null ? other.getPilotageRequestsId() == null : this.getPilotageRequestsId().equals(other.getPilotageRequestsId()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getVesselId() == null) ? 0 : getVesselId().hashCode());
        result = prime * result + ((getVoyageNumber() == null) ? 0 : getVoyageNumber().hashCode());
        result = prime * result + ((getVoyageType() == null) ? 0 : getVoyageType().hashCode());
        result = prime * result + ((getPreviousPort() == null) ? 0 : getPreviousPort().hashCode());
        result = prime * result + ((getNextPort() == null) ? 0 : getNextPort().hashCode());
        result = prime * result + ((getEta() == null) ? 0 : getEta().hashCode());
        result = prime * result + ((getEtaAnchorage() == null) ? 0 : getEtaAnchorage().hashCode());
        result = prime * result + ((getAta() == null) ? 0 : getAta().hashCode());
        result = prime * result + ((getEtd() == null) ? 0 : getEtd().hashCode());
        result = prime * result + ((getAtd() == null) ? 0 : getAtd().hashCode());
        result = prime * result + ((getCargoName() == null) ? 0 : getCargoName().hashCode());
        result = prime * result + ((getCargoType() == null) ? 0 : getCargoType().hashCode());
        result = prime * result + ((getCargoQuantity() == null) ? 0 : getCargoQuantity().hashCode());
        result = prime * result + ((getCargoUnit() == null) ? 0 : getCargoUnit().hashCode());
        result = prime * result + ((getDraftForward() == null) ? 0 : getDraftForward().hashCode());
        result = prime * result + ((getDraftAft() == null) ? 0 : getDraftAft().hashCode());
        result = prime * result + ((getTotalPassengers() == null) ? 0 : getTotalPassengers().hashCode());
        result = prime * result + ((getTotalCrew() == null) ? 0 : getTotalCrew().hashCode());
        result = prime * result + ((getVoyageStatus() == null) ? 0 : getVoyageStatus().hashCode());
        result = prime * result + ((getAgentId() == null) ? 0 : getAgentId().hashCode());
        result = prime * result + ((getPlannedStartId() == null) ? 0 : getPlannedStartId().hashCode());
        result = prime * result + ((getActualStartId() == null) ? 0 : getActualStartId().hashCode());
        result = prime * result + ((getPortOfDestinationId() == null) ? 0 : getPortOfDestinationId().hashCode());
        result = prime * result + ((getPortOfDepartureId() == null) ? 0 : getPortOfDepartureId().hashCode());
        result = prime * result + ((getPlannedEndId() == null) ? 0 : getPlannedEndId().hashCode());
        result = prime * result + ((getActualEndId() == null) ? 0 : getActualEndId().hashCode());
        result = prime * result + ((getStartLocationType() == null) ? 0 : getStartLocationType().hashCode());
        result = prime * result + ((getEndLocationType() == null) ? 0 : getEndLocationType().hashCode());
        result = prime * result + ((getRemarks() == null) ? 0 : getRemarks().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getPilotageRequestsId() == null) ? 0 : getPilotageRequestsId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", vesselId=").append(vesselId);
        sb.append(", voyageNumber=").append(voyageNumber);
        sb.append(", voyageType=").append(voyageType);
        sb.append(", previousPort=").append(previousPort);
        sb.append(", nextPort=").append(nextPort);
        sb.append(", eta=").append(eta);
        sb.append(", etaAnchorage=").append(etaAnchorage);
        sb.append(", ata=").append(ata);
        sb.append(", etd=").append(etd);
        sb.append(", atd=").append(atd);
        sb.append(", cargoName=").append(cargoName);
        sb.append(", cargoType=").append(cargoType);
        sb.append(", cargoQuantity=").append(cargoQuantity);
        sb.append(", cargoUnit=").append(cargoUnit);
        sb.append(", draftForward=").append(draftForward);
        sb.append(", draftAft=").append(draftAft);
        sb.append(", totalPassengers=").append(totalPassengers);
        sb.append(", totalCrew=").append(totalCrew);
        sb.append(", voyageStatus=").append(voyageStatus);
        sb.append(", agentId=").append(agentId);
        sb.append(", plannedStartId=").append(plannedStartId);
        sb.append(", actualStartId=").append(actualStartId);
        sb.append(", portOfDestinationId=").append(portOfDestinationId);
        sb.append(", portOfDepartureId=").append(portOfDepartureId);
        sb.append(", plannedEndId=").append(plannedEndId);
        sb.append(", actualEndId=").append(actualEndId);
        sb.append(", startLocationType=").append(startLocationType);
        sb.append(", endLocationType=").append(endLocationType);
        sb.append(", remarks=").append(remarks);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", pilotageRequestsId=").append(pilotageRequestsId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}