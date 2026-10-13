package syy.portPilotage.service.impl;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import syy.portPilotage.entity.SystemParameters;
import syy.portPilotage.mapper.SystemParametersMapper;
import syy.portPilotage.service.SystemParametersService;
@Service
public class SystemParametersServiceImpl implements SystemParametersService{

    @Autowired
    private SystemParametersMapper systemParametersMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return systemParametersMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int deleteByPrimaryKeyIn(List<Long> list) {
        return systemParametersMapper.deleteByPrimaryKeyIn(list);
    }

    @Override
    public int insert(SystemParameters record) {
        return systemParametersMapper.insert(record);
    }

    @Override
    public int insertSelective(SystemParameters record) {
        return systemParametersMapper.insertSelective(record);
    }

    @Override
    public SystemParameters selectByPrimaryKey(Long id) {
        return systemParametersMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(SystemParameters record) {
        return systemParametersMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(SystemParameters record) {
        return systemParametersMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<SystemParameters> list) {
        return systemParametersMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<SystemParameters> list) {
        return systemParametersMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<SystemParameters> list) {
        return systemParametersMapper.batchInsert(list);
    }

}