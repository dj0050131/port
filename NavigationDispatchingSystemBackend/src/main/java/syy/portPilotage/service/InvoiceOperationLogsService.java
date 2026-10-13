package syy.portPilotage.service;

import java.util.List;
import syy.portPilotage.entity.InvoiceOperationLogs;
public interface InvoiceOperationLogsService{

    int deleteByPrimaryKey(Long id);

    int deleteByPrimaryKeyIn(List<Long> list);

    int insert(InvoiceOperationLogs record);

    int insertSelective(InvoiceOperationLogs record);

    InvoiceOperationLogs selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(InvoiceOperationLogs record);

    int updateByPrimaryKey(InvoiceOperationLogs record);

    int updateBatch(List<InvoiceOperationLogs> list);

    int updateBatchSelective(List<InvoiceOperationLogs> list);

    int batchInsert(List<InvoiceOperationLogs> list);

        List<java.util.Map<String, Object>> selectPaymentNoticeFailed(int offset, int limit, String keyword);

        Long countPaymentNoticeFailed(String keyword);

}