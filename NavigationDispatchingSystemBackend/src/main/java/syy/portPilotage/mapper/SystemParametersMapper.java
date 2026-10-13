package syy.portPilotage.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import syy.portPilotage.entity.SystemParameters;

@Mapper
public interface SystemParametersMapper {
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
    int insert(SystemParameters record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(SystemParameters record);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    SystemParameters selectByPrimaryKey(Long id);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(SystemParameters record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(SystemParameters record);

    int updateBatch(@Param("list") List<SystemParameters> list);

    int updateBatchSelective(@Param("list") List<SystemParameters> list);

    int batchInsert(@Param("list") List<SystemParameters> list);

        List<SystemParameters> selectByGroup(@Param("paramGroup") String paramGroup);

        List<SystemParameters> selectPage(@Param("offset") int offset,
                                      @Param("limit") int limit,
                                      @Param("paramGroup") String paramGroup,
                                      @Param("keyword") String keyword);

    Long countPage(@Param("paramGroup") String paramGroup,
                   @Param("keyword") String keyword);
}