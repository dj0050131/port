package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 船舶基本资料表
 * @TableName vessels
 */
@TableName(value ="vessels")
@Data
public class Vessels implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 船舶呼号
     */
    @TableField(value = "vessel_call_sign")
    private String vesselCallSign;

    /**
     * IMO编号
     */
    @TableField(value = "imo_number")
    private String imoNumber;

    /**
     * MMSI编号
     */
    @TableField(value = "mmsi_number")
    private String mmsiNumber;

    /**
     * 船舶类型
     */
    @TableField(value = "vessel_type")
    private String vesselType;

    /**
     * 国籍
     */
    @TableField(value = "nationality")
    private String nationality;

    /**
     * 船舶所有人（中文）
     */
    @TableField(value = "owner_zh")
    private String ownerZh;

    /**
     * 船舶所有人（英文）
     */
    @TableField(value = "owner_en")
    private String ownerEn;

    /**
     * 航线范围
     */
    @TableField(value = "route_scope")
    private String routeScope;

    /**
     * 航线种类
     */
    @TableField(value = "route_type")
    private Object routeType;

    /**
     * 中文船名
     */
    @TableField(value = "name_zh")
    private String nameZh;

    /**
     * 英文船名
     */
    @TableField(value = "name_en")
    private String nameEn;

    /**
     * 船舶长度
     */
    @TableField(value = "vessel_length")
    private BigDecimal vesselLength;

    /**
     * 船舶宽度
     */
    @TableField(value = "vessel_width")
    private BigDecimal vesselWidth;

    /**
     * 满载吃水
     */
    @TableField(value = "full_load_draft")
    private BigDecimal fullLoadDraft;

    /**
     * 型深
     */
    @TableField(value = "depth")
    private BigDecimal depth;

    /**
     * 最大高度
     */
    @TableField(value = "max_height")
    private BigDecimal maxHeight;

    /**
     * 水面上高度
     */
    @TableField(value = "air_draft")
    private BigDecimal airDraft;

    /**
     * 船速
     */
    @TableField(value = "speed")
    private BigDecimal speed;

    /**
     * 总吨
     */
    @TableField(value = "gross_tonnage")
    private BigDecimal grossTonnage;

    /**
     * 净吨
     */
    @TableField(value = "net_tonnage")
    private BigDecimal netTonnage;

    /**
     * 载重吨
     */
    @TableField(value = "deadweight")
    private BigDecimal deadweight;

    /**
     * 主机种类
     */
    @TableField(value = "main_engine_type")
    private String mainEngineType;

    /**
     * 主机功率(kW)
     */
    @TableField(value = "main_engine_power")
    private BigDecimal mainEnginePower;

    /**
     * 侧推功率(kW)
     */
    @TableField(value = "bow_thruster_power")
    private BigDecimal bowThrusterPower;

    /**
     * AIS引航接口
     */
    @TableField(value = "ais_pilot_interface")
    private String aisPilotInterface;

    /**
     * 船舶颜色
     */
    @TableField(value = "vessel_color")
    private String vesselColor;

    /**
     * 烟囱颜色
     */
    @TableField(value = "funnel_color")
    private String funnelColor;

    /**
     * 烟囱标志
     */
    @TableField(value = "funnel_mark")
    private String funnelMark;

    /**
     * 建造年月
     */
    @TableField(value = "build_date")
    private Date buildDate;

        @TableField(value = "blacklist_record")
    private String blacklistRecord;

        @TableField(value = "bad_status_record")
    private String badStatusRecord;

        @TableField(value = "other_abnormal_record")
    private String otherAbnormalRecord;

    /**
     * 船籍港
     */
    @TableField(value = "port_of_registry")
    private String portOfRegistry;

    /**
     * 船级社
     */
    @TableField(value = "classification_society")
    private String classificationSociety;

        @TableField(value = "management_company")
    private String managementCompany;

    /**
     * 保险公司
     */
    @TableField(value = "insurance_company")
    private String insuranceCompany;

    /**
     * 保赔协会
     */
    @TableField(value = "pni_club")
    private String pniClub;

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
        Vessels other = (Vessels) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getVesselCallSign() == null ? other.getVesselCallSign() == null : this.getVesselCallSign().equals(other.getVesselCallSign()))
            && (this.getImoNumber() == null ? other.getImoNumber() == null : this.getImoNumber().equals(other.getImoNumber()))
            && (this.getMmsiNumber() == null ? other.getMmsiNumber() == null : this.getMmsiNumber().equals(other.getMmsiNumber()))
            && (this.getVesselType() == null ? other.getVesselType() == null : this.getVesselType().equals(other.getVesselType()))
            && (this.getNationality() == null ? other.getNationality() == null : this.getNationality().equals(other.getNationality()))
            && (this.getOwnerZh() == null ? other.getOwnerZh() == null : this.getOwnerZh().equals(other.getOwnerZh()))
            && (this.getOwnerEn() == null ? other.getOwnerEn() == null : this.getOwnerEn().equals(other.getOwnerEn()))
            && (this.getRouteScope() == null ? other.getRouteScope() == null : this.getRouteScope().equals(other.getRouteScope()))
            && (this.getRouteType() == null ? other.getRouteType() == null : this.getRouteType().equals(other.getRouteType()))
            && (this.getNameZh() == null ? other.getNameZh() == null : this.getNameZh().equals(other.getNameZh()))
            && (this.getNameEn() == null ? other.getNameEn() == null : this.getNameEn().equals(other.getNameEn()))
            && (this.getVesselLength() == null ? other.getVesselLength() == null : this.getVesselLength().equals(other.getVesselLength()))
            && (this.getVesselWidth() == null ? other.getVesselWidth() == null : this.getVesselWidth().equals(other.getVesselWidth()))
            && (this.getFullLoadDraft() == null ? other.getFullLoadDraft() == null : this.getFullLoadDraft().equals(other.getFullLoadDraft()))
            && (this.getDepth() == null ? other.getDepth() == null : this.getDepth().equals(other.getDepth()))
            && (this.getMaxHeight() == null ? other.getMaxHeight() == null : this.getMaxHeight().equals(other.getMaxHeight()))
            && (this.getAirDraft() == null ? other.getAirDraft() == null : this.getAirDraft().equals(other.getAirDraft()))
            && (this.getSpeed() == null ? other.getSpeed() == null : this.getSpeed().equals(other.getSpeed()))
            && (this.getGrossTonnage() == null ? other.getGrossTonnage() == null : this.getGrossTonnage().equals(other.getGrossTonnage()))
            && (this.getNetTonnage() == null ? other.getNetTonnage() == null : this.getNetTonnage().equals(other.getNetTonnage()))
            && (this.getDeadweight() == null ? other.getDeadweight() == null : this.getDeadweight().equals(other.getDeadweight()))
            && (this.getMainEngineType() == null ? other.getMainEngineType() == null : this.getMainEngineType().equals(other.getMainEngineType()))
            && (this.getMainEnginePower() == null ? other.getMainEnginePower() == null : this.getMainEnginePower().equals(other.getMainEnginePower()))
            && (this.getBowThrusterPower() == null ? other.getBowThrusterPower() == null : this.getBowThrusterPower().equals(other.getBowThrusterPower()))
            && (this.getAisPilotInterface() == null ? other.getAisPilotInterface() == null : this.getAisPilotInterface().equals(other.getAisPilotInterface()))
            && (this.getVesselColor() == null ? other.getVesselColor() == null : this.getVesselColor().equals(other.getVesselColor()))
            && (this.getFunnelColor() == null ? other.getFunnelColor() == null : this.getFunnelColor().equals(other.getFunnelColor()))
            && (this.getFunnelMark() == null ? other.getFunnelMark() == null : this.getFunnelMark().equals(other.getFunnelMark()))
            && (this.getBuildDate() == null ? other.getBuildDate() == null : this.getBuildDate().equals(other.getBuildDate()))
            && (this.getBlacklistRecord() == null ? other.getBlacklistRecord() == null : this.getBlacklistRecord().equals(other.getBlacklistRecord()))
            && (this.getBadStatusRecord() == null ? other.getBadStatusRecord() == null : this.getBadStatusRecord().equals(other.getBadStatusRecord()))
            && (this.getOtherAbnormalRecord() == null ? other.getOtherAbnormalRecord() == null : this.getOtherAbnormalRecord().equals(other.getOtherAbnormalRecord()))
            && (this.getPortOfRegistry() == null ? other.getPortOfRegistry() == null : this.getPortOfRegistry().equals(other.getPortOfRegistry()))
            && (this.getClassificationSociety() == null ? other.getClassificationSociety() == null : this.getClassificationSociety().equals(other.getClassificationSociety()))
            && (this.getManagementCompany() == null ? other.getManagementCompany() == null : this.getManagementCompany().equals(other.getManagementCompany()))
            && (this.getInsuranceCompany() == null ? other.getInsuranceCompany() == null : this.getInsuranceCompany().equals(other.getInsuranceCompany()))
            && (this.getPniClub() == null ? other.getPniClub() == null : this.getPniClub().equals(other.getPniClub()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getVesselCallSign() == null) ? 0 : getVesselCallSign().hashCode());
        result = prime * result + ((getImoNumber() == null) ? 0 : getImoNumber().hashCode());
        result = prime * result + ((getMmsiNumber() == null) ? 0 : getMmsiNumber().hashCode());
        result = prime * result + ((getVesselType() == null) ? 0 : getVesselType().hashCode());
        result = prime * result + ((getNationality() == null) ? 0 : getNationality().hashCode());
        result = prime * result + ((getOwnerZh() == null) ? 0 : getOwnerZh().hashCode());
        result = prime * result + ((getOwnerEn() == null) ? 0 : getOwnerEn().hashCode());
        result = prime * result + ((getRouteScope() == null) ? 0 : getRouteScope().hashCode());
        result = prime * result + ((getRouteType() == null) ? 0 : getRouteType().hashCode());
        result = prime * result + ((getNameZh() == null) ? 0 : getNameZh().hashCode());
        result = prime * result + ((getNameEn() == null) ? 0 : getNameEn().hashCode());
        result = prime * result + ((getVesselLength() == null) ? 0 : getVesselLength().hashCode());
        result = prime * result + ((getVesselWidth() == null) ? 0 : getVesselWidth().hashCode());
        result = prime * result + ((getFullLoadDraft() == null) ? 0 : getFullLoadDraft().hashCode());
        result = prime * result + ((getDepth() == null) ? 0 : getDepth().hashCode());
        result = prime * result + ((getMaxHeight() == null) ? 0 : getMaxHeight().hashCode());
        result = prime * result + ((getAirDraft() == null) ? 0 : getAirDraft().hashCode());
        result = prime * result + ((getSpeed() == null) ? 0 : getSpeed().hashCode());
        result = prime * result + ((getGrossTonnage() == null) ? 0 : getGrossTonnage().hashCode());
        result = prime * result + ((getNetTonnage() == null) ? 0 : getNetTonnage().hashCode());
        result = prime * result + ((getDeadweight() == null) ? 0 : getDeadweight().hashCode());
        result = prime * result + ((getMainEngineType() == null) ? 0 : getMainEngineType().hashCode());
        result = prime * result + ((getMainEnginePower() == null) ? 0 : getMainEnginePower().hashCode());
        result = prime * result + ((getBowThrusterPower() == null) ? 0 : getBowThrusterPower().hashCode());
        result = prime * result + ((getAisPilotInterface() == null) ? 0 : getAisPilotInterface().hashCode());
        result = prime * result + ((getVesselColor() == null) ? 0 : getVesselColor().hashCode());
        result = prime * result + ((getFunnelColor() == null) ? 0 : getFunnelColor().hashCode());
        result = prime * result + ((getFunnelMark() == null) ? 0 : getFunnelMark().hashCode());
        result = prime * result + ((getBuildDate() == null) ? 0 : getBuildDate().hashCode());
        result = prime * result + ((getBlacklistRecord() == null) ? 0 : getBlacklistRecord().hashCode());
        result = prime * result + ((getBadStatusRecord() == null) ? 0 : getBadStatusRecord().hashCode());
        result = prime * result + ((getOtherAbnormalRecord() == null) ? 0 : getOtherAbnormalRecord().hashCode());
        result = prime * result + ((getPortOfRegistry() == null) ? 0 : getPortOfRegistry().hashCode());
        result = prime * result + ((getClassificationSociety() == null) ? 0 : getClassificationSociety().hashCode());
        result = prime * result + ((getManagementCompany() == null) ? 0 : getManagementCompany().hashCode());
        result = prime * result + ((getInsuranceCompany() == null) ? 0 : getInsuranceCompany().hashCode());
        result = prime * result + ((getPniClub() == null) ? 0 : getPniClub().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", vesselCallSign=").append(vesselCallSign);
        sb.append(", imoNumber=").append(imoNumber);
        sb.append(", mmsiNumber=").append(mmsiNumber);
        sb.append(", vesselType=").append(vesselType);
        sb.append(", nationality=").append(nationality);
        sb.append(", ownerZh=").append(ownerZh);
        sb.append(", ownerEn=").append(ownerEn);
        sb.append(", routeScope=").append(routeScope);
        sb.append(", routeType=").append(routeType);
        sb.append(", nameZh=").append(nameZh);
        sb.append(", nameEn=").append(nameEn);
        sb.append(", vesselLength=").append(vesselLength);
        sb.append(", vesselWidth=").append(vesselWidth);
        sb.append(", fullLoadDraft=").append(fullLoadDraft);
        sb.append(", depth=").append(depth);
        sb.append(", maxHeight=").append(maxHeight);
        sb.append(", airDraft=").append(airDraft);
        sb.append(", speed=").append(speed);
        sb.append(", grossTonnage=").append(grossTonnage);
        sb.append(", netTonnage=").append(netTonnage);
        sb.append(", deadweight=").append(deadweight);
        sb.append(", mainEngineType=").append(mainEngineType);
        sb.append(", mainEnginePower=").append(mainEnginePower);
        sb.append(", bowThrusterPower=").append(bowThrusterPower);
        sb.append(", aisPilotInterface=").append(aisPilotInterface);
        sb.append(", vesselColor=").append(vesselColor);
        sb.append(", funnelColor=").append(funnelColor);
        sb.append(", funnelMark=").append(funnelMark);
        sb.append(", buildDate=").append(buildDate);
        sb.append(", blacklistRecord=").append(blacklistRecord);
        sb.append(", badStatusRecord=").append(badStatusRecord);
        sb.append(", otherAbnormalRecord=").append(otherAbnormalRecord);
        sb.append(", portOfRegistry=").append(portOfRegistry);
        sb.append(", classificationSociety=").append(classificationSociety);
        sb.append(", managementCompany=").append(managementCompany);
        sb.append(", insuranceCompany=").append(insuranceCompany);
        sb.append(", pniClub=").append(pniClub);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}