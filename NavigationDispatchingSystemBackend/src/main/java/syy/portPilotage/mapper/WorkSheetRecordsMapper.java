package syy.portPilotage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import syy.portPilotage.vo.pilotage.WorkSheetDriverRecordVO;
import syy.portPilotage.vo.pilotage.WorkSheetPilotRecordVO;
import syy.portPilotage.vo.pilotage.WorkSheetPilotBoatRecordVO;
import syy.portPilotage.vo.pilotage.WorkSheetTugboatRecordVO;
import syy.portPilotage.vo.pilotage.WorkSheetVehicleRecordVO;

import java.util.List;

@Mapper
public interface WorkSheetRecordsMapper {
    List<WorkSheetPilotRecordVO> selectPilotPage(@Param("offset") int offset,
                                                 @Param("size") int size,
                                                 @Param("keyword") String keyword,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate);

    Long countPilotPage(@Param("keyword") String keyword,
                        @Param("startDate") String startDate,
                        @Param("endDate") String endDate);

    List<WorkSheetDriverRecordVO> selectDriverPage(@Param("offset") int offset,
                                                   @Param("size") int size,
                                                   @Param("keyword") String keyword,
                                                   @Param("startDate") String startDate,
                                                   @Param("endDate") String endDate);

    Long countDriverPage(@Param("keyword") String keyword,
                         @Param("startDate") String startDate,
                         @Param("endDate") String endDate);

    List<WorkSheetVehicleRecordVO> selectVehiclePage(@Param("offset") int offset,
                                                     @Param("size") int size,
                                                     @Param("keyword") String keyword,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate);

    Long countVehiclePage(@Param("keyword") String keyword,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate);

    List<WorkSheetTugboatRecordVO> selectTugboatPage(@Param("offset") int offset,
                                                     @Param("size") int size,
                                                     @Param("keyword") String keyword,
                                                     @Param("startDate") String startDate,
                                                     @Param("endDate") String endDate);

    Long countTugboatPage(@Param("keyword") String keyword,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate);

    List<WorkSheetPilotBoatRecordVO> selectPilotBoatPage(@Param("offset") int offset,
                                                         @Param("size") int size,
                                                         @Param("keyword") String keyword,
                                                         @Param("startDate") String startDate,
                                                         @Param("endDate") String endDate);

    Long countPilotBoatPage(@Param("keyword") String keyword,
                            @Param("startDate") String startDate,
                            @Param("endDate") String endDate);
}
