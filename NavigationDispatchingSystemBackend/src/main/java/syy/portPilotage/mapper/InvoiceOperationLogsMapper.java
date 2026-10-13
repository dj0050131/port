package syy.portPilotage.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import syy.portPilotage.entity.InvoiceOperationLogs;

@Mapper
public interface InvoiceOperationLogsMapper {
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
    int insert(InvoiceOperationLogs record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(InvoiceOperationLogs record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    InvoiceOperationLogs selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(InvoiceOperationLogs record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(InvoiceOperationLogs record);

    int updateBatch(@Param("list") List<InvoiceOperationLogs> list);

    int updateBatchSelective(@Param("list") List<InvoiceOperationLogs> list);

    int batchInsert(@Param("list") List<InvoiceOperationLogs> list);

    /**
     * 查询“付款提示提交外部系统失败”的最新记录（分页）
     * 返回字段：voucherId, operationTime, errorMessage, operationDetails
     */
    List<Map<String, Object>> selectPaymentNoticeFailed(@Param("offset") int offset,
                                                        @Param("limit") int limit,
                                                        @Param("keyword") String keyword);

        Long countPaymentNoticeFailed(@Param("keyword") String keyword);
}