package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 拖轮公司（服务商）主数据
 * @TableName tugboat_companies
 */
@TableName(value = "tugboat_companies")
@Data
public class TugboatCompanies implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "company_code")
    private String companyCode;

    @TableField(value = "company_name")
    private String companyName;

    @TableField(value = "contact_name")
    private String contactName;

    @TableField(value = "contact_phone")
    private String contactPhone;

    @TableField(value = "status")
    private String status; // ACTIVE/INACTIVE

    @TableField(value = "notes")
    private String notes;

    @TableField(value = "created_at")
    private Date createdAt;

    @TableField(value = "updated_at")
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
