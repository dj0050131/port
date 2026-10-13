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
 * 引航梯信息表
 * @TableName pilot_ladder_info
 */
@TableName(value ="pilot_ladder_info")
@Data
public class PilotLadderInfo implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 船舶ID
     */
    @TableField(value = "vessel_id")
    private Long vesselId;

    /**
     * 航次ID
     */
    @TableField(value = "voyage_id")
    private Long voyageId;

    /**
     * 引航梯状态
     */
    @TableField(value = "ladder_status")
    private String ladderStatus;

    /**
     * 引航梯类型
     */
    @TableField(value = "ladder_type")
    private String ladderType;

    /**
     * 干舷高度(m)
     */
    @TableField(value = "freeboard_height")
    private BigDecimal freeboardHeight;

        @TableField(value = "inspection_date")
    private Date inspectionDate;

        @TableField(value = "inspection_result")
    private Object inspectionResult;

        @TableField(value = "inspector")
    private String inspector;

    /**
     * 备注
     */
    @TableField(value = "remarks")
    private String remarks;

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
        PilotLadderInfo other = (PilotLadderInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getVesselId() == null ? other.getVesselId() == null : this.getVesselId().equals(other.getVesselId()))
            && (this.getVoyageId() == null ? other.getVoyageId() == null : this.getVoyageId().equals(other.getVoyageId()))
            && (this.getLadderStatus() == null ? other.getLadderStatus() == null : this.getLadderStatus().equals(other.getLadderStatus()))
            && (this.getLadderType() == null ? other.getLadderType() == null : this.getLadderType().equals(other.getLadderType()))
            && (this.getFreeboardHeight() == null ? other.getFreeboardHeight() == null : this.getFreeboardHeight().equals(other.getFreeboardHeight()))
            && (this.getInspectionDate() == null ? other.getInspectionDate() == null : this.getInspectionDate().equals(other.getInspectionDate()))
            && (this.getInspectionResult() == null ? other.getInspectionResult() == null : this.getInspectionResult().equals(other.getInspectionResult()))
            && (this.getInspector() == null ? other.getInspector() == null : this.getInspector().equals(other.getInspector()))
            && (this.getRemarks() == null ? other.getRemarks() == null : this.getRemarks().equals(other.getRemarks()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getVesselId() == null) ? 0 : getVesselId().hashCode());
        result = prime * result + ((getVoyageId() == null) ? 0 : getVoyageId().hashCode());
        result = prime * result + ((getLadderStatus() == null) ? 0 : getLadderStatus().hashCode());
        result = prime * result + ((getLadderType() == null) ? 0 : getLadderType().hashCode());
        result = prime * result + ((getFreeboardHeight() == null) ? 0 : getFreeboardHeight().hashCode());
        result = prime * result + ((getInspectionDate() == null) ? 0 : getInspectionDate().hashCode());
        result = prime * result + ((getInspectionResult() == null) ? 0 : getInspectionResult().hashCode());
        result = prime * result + ((getInspector() == null) ? 0 : getInspector().hashCode());
        result = prime * result + ((getRemarks() == null) ? 0 : getRemarks().hashCode());
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
        sb.append(", vesselId=").append(vesselId);
        sb.append(", voyageId=").append(voyageId);
        sb.append(", ladderStatus=").append(ladderStatus);
        sb.append(", ladderType=").append(ladderType);
        sb.append(", freeboardHeight=").append(freeboardHeight);
        sb.append(", inspectionDate=").append(inspectionDate);
        sb.append(", inspectionResult=").append(inspectionResult);
        sb.append(", inspector=").append(inspector);
        sb.append(", remarks=").append(remarks);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}