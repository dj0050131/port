package syy.portPilotage.service.impl;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import syy.portPilotage.mapper.InvoiceOperationLogsMapper;
import syy.portPilotage.entity.InvoiceOperationLogs;
import syy.portPilotage.service.InvoiceOperationLogsService;
@Service
public class InvoiceOperationLogsServiceImpl implements InvoiceOperationLogsService{

    @Autowired
    private InvoiceOperationLogsMapper invoiceOperationLogsMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return invoiceOperationLogsMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int deleteByPrimaryKeyIn(List<Long> list) {
        return invoiceOperationLogsMapper.deleteByPrimaryKeyIn(list);
    }

    @Override
    public int insert(InvoiceOperationLogs record) {
        return invoiceOperationLogsMapper.insert(record);
    }

    @Override
    public int insertSelective(InvoiceOperationLogs record) {
        return invoiceOperationLogsMapper.insertSelective(record);
    }

    @Override
    public InvoiceOperationLogs selectByPrimaryKey(Long id) {
        return invoiceOperationLogsMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(InvoiceOperationLogs record) {
        return invoiceOperationLogsMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(InvoiceOperationLogs record) {
        return invoiceOperationLogsMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<InvoiceOperationLogs> list) {
        return invoiceOperationLogsMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<InvoiceOperationLogs> list) {
        return invoiceOperationLogsMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<InvoiceOperationLogs> list) {
        return invoiceOperationLogsMapper.batchInsert(list);
    }

    @Override
    public List<Map<String, Object>> selectPaymentNoticeFailed(int offset, int limit, String keyword) {
        return invoiceOperationLogsMapper.selectPaymentNoticeFailed(offset, limit, keyword);
    }

    @Override
    public Long countPaymentNoticeFailed(String keyword) {
        return invoiceOperationLogsMapper.countPaymentNoticeFailed(keyword);
    }

}