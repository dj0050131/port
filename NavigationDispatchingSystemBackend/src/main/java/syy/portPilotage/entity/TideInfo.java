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
 * 潮汐信息表
 * @TableName tide_info
 */
@TableName(value ="tide_info")
@Data
public class TideInfo implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 日期
     */
    @TableField(value = "tide_date")
    private Date tideDate;

    /**
     * 所属区域
     */
    @TableField(value = "region")
    private String region;

    /**
     * 高潮时间1
     */
    @TableField(value = "high_tide_time1")
    private Date highTideTime1;

    /**
     * 高潮潮高1
     */
    @TableField(value = "high_tide_height1")
    private BigDecimal highTideHeight1;

    /**
     * 高潮时间2
     */
    @TableField(value = "high_tide_time2")
    private Date highTideTime2;

    /**
     * 高潮潮高2
     */
    @TableField(value = "high_tide_height2")
    private BigDecimal highTideHeight2;

    /**
     * 低潮时间1
     */
    @TableField(value = "low_tide_time1")
    private Date lowTideTime1;

    /**
     * 低潮潮高1
     */
    @TableField(value = "low_tide_height1")
    private BigDecimal lowTideHeight1;

    /**
     * 低潮时间2
     */
    @TableField(value = "low_tide_time2")
    private Date lowTideTime2;

    /**
     * 低潮潮高2
     */
    @TableField(value = "low_tide_height2")
    private BigDecimal lowTideHeight2;

    /**
     * 潮汐类型
     */
    @TableField(value = "tide_type")
    private Object tideType;

    /**
     * 数据来源
     */
    @TableField(value = "data_source")
    private String dataSource;

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
        TideInfo other = (TideInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTideDate() == null ? other.getTideDate() == null : this.getTideDate().equals(other.getTideDate()))
            && (this.getRegion() == null ? other.getRegion() == null : this.getRegion().equals(other.getRegion()))
            && (this.getHighTideTime1() == null ? other.getHighTideTime1() == null : this.getHighTideTime1().equals(other.getHighTideTime1()))
            && (this.getHighTideHeight1() == null ? other.getHighTideHeight1() == null : this.getHighTideHeight1().equals(other.getHighTideHeight1()))
            && (this.getHighTideTime2() == null ? other.getHighTideTime2() == null : this.getHighTideTime2().equals(other.getHighTideTime2()))
            && (this.getHighTideHeight2() == null ? other.getHighTideHeight2() == null : this.getHighTideHeight2().equals(other.getHighTideHeight2()))
            && (this.getLowTideTime1() == null ? other.getLowTideTime1() == null : this.getLowTideTime1().equals(other.getLowTideTime1()))
            && (this.getLowTideHeight1() == null ? other.getLowTideHeight1() == null : this.getLowTideHeight1().equals(other.getLowTideHeight1()))
            && (this.getLowTideTime2() == null ? other.getLowTideTime2() == null : this.getLowTideTime2().equals(other.getLowTideTime2()))
            && (this.getLowTideHeight2() == null ? other.getLowTideHeight2() == null : this.getLowTideHeight2().equals(other.getLowTideHeight2()))
            && (this.getTideType() == null ? other.getTideType() == null : this.getTideType().equals(other.getTideType()))
            && (this.getDataSource() == null ? other.getDataSource() == null : this.getDataSource().equals(other.getDataSource()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTideDate() == null) ? 0 : getTideDate().hashCode());
        result = prime * result + ((getRegion() == null) ? 0 : getRegion().hashCode());
        result = prime * result + ((getHighTideTime1() == null) ? 0 : getHighTideTime1().hashCode());
        result = prime * result + ((getHighTideHeight1() == null) ? 0 : getHighTideHeight1().hashCode());
        result = prime * result + ((getHighTideTime2() == null) ? 0 : getHighTideTime2().hashCode());
        result = prime * result + ((getHighTideHeight2() == null) ? 0 : getHighTideHeight2().hashCode());
        result = prime * result + ((getLowTideTime1() == null) ? 0 : getLowTideTime1().hashCode());
        result = prime * result + ((getLowTideHeight1() == null) ? 0 : getLowTideHeight1().hashCode());
        result = prime * result + ((getLowTideTime2() == null) ? 0 : getLowTideTime2().hashCode());
        result = prime * result + ((getLowTideHeight2() == null) ? 0 : getLowTideHeight2().hashCode());
        result = prime * result + ((getTideType() == null) ? 0 : getTideType().hashCode());
        result = prime * result + ((getDataSource() == null) ? 0 : getDataSource().hashCode());
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
        sb.append(", tideDate=").append(tideDate);
        sb.append(", region=").append(region);
        sb.append(", highTideTime1=").append(highTideTime1);
        sb.append(", highTideHeight1=").append(highTideHeight1);
        sb.append(", highTideTime2=").append(highTideTime2);
        sb.append(", highTideHeight2=").append(highTideHeight2);
        sb.append(", lowTideTime1=").append(lowTideTime1);
        sb.append(", lowTideHeight1=").append(lowTideHeight1);
        sb.append(", lowTideTime2=").append(lowTideTime2);
        sb.append(", lowTideHeight2=").append(lowTideHeight2);
        sb.append(", tideType=").append(tideType);
        sb.append(", dataSource=").append(dataSource);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}