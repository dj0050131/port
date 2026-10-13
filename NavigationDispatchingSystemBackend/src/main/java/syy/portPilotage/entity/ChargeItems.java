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
 * 收费项目表
 * @TableName charge_items
 */
@TableName(value ="charge_items")
@Data
public class ChargeItems implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目编号
     */
    @TableField(value = "item_code")
    private String itemCode;

    /**
     * 类别ID
     */
    @TableField(value = "category_id")
    private Long categoryId;

    /**
     * 项目名称
     */
    @TableField(value = "item_name")
    private String itemName;

    /**
     * 项目备注
     */
    @TableField(value = "item_notes")
    private String itemNotes;

    /**
     * 计税方式
     */
    @TableField(value = "tax_method")
    private Object taxMethod;

    /**
     * 税率
     */
    @TableField(value = "tax_rate")
    private BigDecimal taxRate;

        @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 状态
     */
    @TableField(value = "status")
    private Object status;

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
        ChargeItems other = (ChargeItems) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getItemCode() == null ? other.getItemCode() == null : this.getItemCode().equals(other.getItemCode()))
            && (this.getCategoryId() == null ? other.getCategoryId() == null : this.getCategoryId().equals(other.getCategoryId()))
            && (this.getItemName() == null ? other.getItemName() == null : this.getItemName().equals(other.getItemName()))
            && (this.getItemNotes() == null ? other.getItemNotes() == null : this.getItemNotes().equals(other.getItemNotes()))
            && (this.getTaxMethod() == null ? other.getTaxMethod() == null : this.getTaxMethod().equals(other.getTaxMethod()))
            && (this.getTaxRate() == null ? other.getTaxRate() == null : this.getTaxRate().equals(other.getTaxRate()))
            && (this.getSortOrder() == null ? other.getSortOrder() == null : this.getSortOrder().equals(other.getSortOrder()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getItemCode() == null) ? 0 : getItemCode().hashCode());
        result = prime * result + ((getCategoryId() == null) ? 0 : getCategoryId().hashCode());
        result = prime * result + ((getItemName() == null) ? 0 : getItemName().hashCode());
        result = prime * result + ((getItemNotes() == null) ? 0 : getItemNotes().hashCode());
        result = prime * result + ((getTaxMethod() == null) ? 0 : getTaxMethod().hashCode());
        result = prime * result + ((getTaxRate() == null) ? 0 : getTaxRate().hashCode());
        result = prime * result + ((getSortOrder() == null) ? 0 : getSortOrder().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
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
        sb.append(", itemCode=").append(itemCode);
        sb.append(", categoryId=").append(categoryId);
        sb.append(", itemName=").append(itemName);
        sb.append(", itemNotes=").append(itemNotes);
        sb.append(", taxMethod=").append(taxMethod);
        sb.append(", taxRate=").append(taxRate);
        sb.append(", sortOrder=").append(sortOrder);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}