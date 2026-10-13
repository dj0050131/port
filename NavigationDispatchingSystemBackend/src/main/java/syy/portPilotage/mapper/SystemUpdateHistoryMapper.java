package syy.portPilotage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import syy.portPilotage.entity.SystemUpdateHistory;

import java.util.List;

@Mapper
public interface SystemUpdateHistoryMapper {
    List<SystemUpdateHistory> selectPage(@Param("offset") int offset,
                                         @Param("size") int size,
                                         @Param("keyword") String keyword,
                                         @Param("published") Integer published);

    Long countPage(@Param("keyword") String keyword,
                   @Param("published") Integer published);

    int insert(SystemUpdateHistory record);

    int updateById(SystemUpdateHistory record);

    int deleteById(@Param("id") Long id);

    SystemUpdateHistory selectById(@Param("id") Long id);
}
