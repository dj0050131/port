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
 * 拖轮信息表
 * @TableName tugboats
 */
@TableName(value ="tugboats")
@Data
public class Tugboats implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 拖轮代码
     */
    @TableField(value = "tugboat_code")
    private String tugboatCode;

    /**
     * 拖轮名称
     */
    @TableField(value = "tugboat_name")
    private String tugboatName;

    /**
     * IMO编号
     */
    @TableField(value = "imo_number")
    private String imoNumber;

    /**
     * 呼号
     */
    @TableField(value = "call_sign")
    private String callSign;

    /**
     * 拖轮类型
     */
    @TableField(value = "tugboat_type")
    private Object tugboatType;

    /**
     * 所属拖轮公司ID（关联 tugboat_companies.id）
     */
    @TableField(value = "owner_company_id")
    private Long ownerCompanyId;

        @TableField(value = "owner_company")
    private String ownerCompany;

    
    @TableField(exist = false)
    private String ownerCompanyName;

    /**
     * 拖力(吨)
     */
    @TableField(value = "bollard_pull")
    private BigDecimal bollardPull;

    /**
     * 船长(米)
     */
    @TableField(value = "length")
    private BigDecimal length;

    /**
     * 功率(kW)
     */
    @TableField(value = "power")
    private BigDecimal power;

    /**
     * 建造年份
     */
    @TableField(value = "build_year")
    private Object buildYear;

    /**
     * 状态
     */
    @TableField(value = "status")
    private Object status;

    /**
     * 当前位置
     */
    @TableField(value = "current_location")
    private String currentLocation;

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
        Tugboats other = (Tugboats) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTugboatCode() == null ? other.getTugboatCode() == null : this.getTugboatCode().equals(other.getTugboatCode()))
            && (this.getTugboatName() == null ? other.getTugboatName() == null : this.getTugboatName().equals(other.getTugboatName()))
            && (this.getImoNumber() == null ? other.getImoNumber() == null : this.getImoNumber().equals(other.getImoNumber()))
            && (this.getCallSign() == null ? other.getCallSign() == null : this.getCallSign().equals(other.getCallSign()))
            && (this.getTugboatType() == null ? other.getTugboatType() == null : this.getTugboatType().equals(other.getTugboatType()))
            && (this.getOwnerCompany() == null ? other.getOwnerCompany() == null : this.getOwnerCompany().equals(other.getOwnerCompany()))
            && (this.getBollardPull() == null ? other.getBollardPull() == null : this.getBollardPull().equals(other.getBollardPull()))
            && (this.getLength() == null ? other.getLength() == null : this.getLength().equals(other.getLength()))
            && (this.getPower() == null ? other.getPower() == null : this.getPower().equals(other.getPower()))
            && (this.getBuildYear() == null ? other.getBuildYear() == null : this.getBuildYear().equals(other.getBuildYear()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCurrentLocation() == null ? other.getCurrentLocation() == null : this.getCurrentLocation().equals(other.getCurrentLocation()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTugboatCode() == null) ? 0 : getTugboatCode().hashCode());
        result = prime * result + ((getTugboatName() == null) ? 0 : getTugboatName().hashCode());
        result = prime * result + ((getImoNumber() == null) ? 0 : getImoNumber().hashCode());
        result = prime * result + ((getCallSign() == null) ? 0 : getCallSign().hashCode());
        result = prime * result + ((getTugboatType() == null) ? 0 : getTugboatType().hashCode());
        result = prime * result + ((getOwnerCompany() == null) ? 0 : getOwnerCompany().hashCode());
        result = prime * result + ((getBollardPull() == null) ? 0 : getBollardPull().hashCode());
        result = prime * result + ((getLength() == null) ? 0 : getLength().hashCode());
        result = prime * result + ((getPower() == null) ? 0 : getPower().hashCode());
        result = prime * result + ((getBuildYear() == null) ? 0 : getBuildYear().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCurrentLocation() == null) ? 0 : getCurrentLocation().hashCode());
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
        sb.append(", tugboatCode=").append(tugboatCode);
        sb.append(", tugboatName=").append(tugboatName);
        sb.append(", imoNumber=").append(imoNumber);
        sb.append(", callSign=").append(callSign);
        sb.append(", tugboatType=").append(tugboatType);
        sb.append(", ownerCompany=").append(ownerCompany);
        sb.append(", bollardPull=").append(bollardPull);
        sb.append(", length=").append(length);
        sb.append(", power=").append(power);
        sb.append(", buildYear=").append(buildYear);
        sb.append(", status=").append(status);
        sb.append(", currentLocation=").append(currentLocation);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}