package syy.portPilotage.service;

import java.util.List;
import syy.portPilotage.entity.SystemParameters;
public interface SystemParametersService{

    int deleteByPrimaryKey(Long id);

    int deleteByPrimaryKeyIn(List<Long> list);

    int insert(SystemParameters record);

    int insertSelective(SystemParameters record);

    SystemParameters selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SystemParameters record);

    int updateByPrimaryKey(SystemParameters record);

    int updateBatch(List<SystemParameters> list);

    int updateBatchSelective(List<SystemParameters> list);

    int batchInsert(List<SystemParameters> list);

}