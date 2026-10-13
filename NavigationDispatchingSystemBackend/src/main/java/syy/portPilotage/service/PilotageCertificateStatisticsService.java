package syy.portPilotage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import syy.portPilotage.dto.statistics.*;
import syy.portPilotage.entity.PilotageCertificates;

import java.time.LocalDate;
import java.util.List;

public interface PilotageCertificateStatisticsService {

        List<SafetyMonthlyDTO> getSafetyMonthlyReport(LocalDate startDate, LocalDate endDate);

        List<PilotageMonthlyDTO> getPilotageMonthlyReport(LocalDate startDate, LocalDate endDate, String shipName, String pilot);

        List<PilotageSummaryDTO> getPilotageSummaryReport(LocalDate startDate, LocalDate endDate);

        List<LengthGt250DTO> getLengthGt250Report(LocalDate startDate, LocalDate endDate, Double minLength);

        List<HolidayDTO> getHolidayReport(LocalDate startDate, LocalDate endDate);

        List<WeekendDTO> getWeekendReport(LocalDate startDate, LocalDate endDate);

        List<NightDTO> getNightReport(LocalDate startDate, LocalDate endDate);

        List<SpecialDTO> getSpecialReport(LocalDate startDate, LocalDate endDate, String shipName);

        List<RecordBreakDTO> getRecordBreakReport(LocalDate startDate, LocalDate endDate);

        List<NewWharfDTO> getNewWharfReport(LocalDate startDate, LocalDate endDate);
    
        List<MealDTO> getMealReport(LocalDate startDate, LocalDate endDate);
}