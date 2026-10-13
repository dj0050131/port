package syy.portPilotage.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import syy.portPilotage.entity.BillingVouchers;

@Mapper
public interface BillingVouchersMapper extends BaseMapper<BillingVouchers> {
    /**
     * delete by primary key
     *
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long id);

    int deleteByPrimaryKeyIn(List<Long> list);

    /**
     * insert record to table
     *
     * @param record the record
     * @return insert count
     */
    int insert(BillingVouchers record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(BillingVouchers record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    BillingVouchers selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(BillingVouchers record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(BillingVouchers record);

    int updateBatch(@Param("list") List<BillingVouchers> list);

    int updateBatchSelective(@Param("list") List<BillingVouchers> list);

    int batchInsert(@Param("list") List<BillingVouchers> list);

    /**
     * 按收费类型统计费用
     *
     * @return 费用统计列表
     */
    @MapKey("chargeType")
    List<Map<String, Object>> getFeeBreakdownByType();

    /**
     * 按月份统计费用趋势
     *
     * @return 费用趋势列表
     */
    @MapKey("month")
    List<Map<String, Object>> getFeeTrendByMonth();

    /**
     * 获取当前期间总金额
     *
     * @return 当前期间总金额
     */
    BigDecimal getCurrentPeriodTotal();

    /**
     * 获取上一期间总金额
     *
     * @return 上一期间总金额
     */
    BigDecimal getPreviousPeriodTotal();

    /**
     * 按类别统计费用
     *
     * @return 类别统计列表
     */
    @MapKey("categoryName")
    List<Map<String, Object>> getCategoryStats();
}
