package syy.portPilotage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import syy.portPilotage.entity.FiscalYears;

import java.math.BigDecimal;

@Mapper
public interface FiscalYearsMapper extends BaseMapper<FiscalYears> {

    @Select("""
            SELECT COALESCE(SUM(b.total_amount), 0)
            FROM billing_vouchers b
            WHERE b.total_amount IS NOT NULL
              AND b.audit_status <> 'REJECTED'
              AND (
                    (b.pilotage_start_time >= #{start} AND b.pilotage_start_time < #{end})
                 OR (b.pilotage_start_time IS NULL AND b.created_at >= #{start} AND b.created_at < #{end})
              )
            """)
    BigDecimal sumBilledAmount(@Param("start") String start, @Param("end") String end);

    @Select("""
            SELECT COALESCE(SUM(b.total_amount), 0)
            FROM billing_vouchers b
            WHERE b.total_amount IS NOT NULL
              AND b.audit_status = 'APPROVED'
              AND (
                    (b.pilotage_start_time >= #{start} AND b.pilotage_start_time < #{end})
                 OR (b.pilotage_start_time IS NULL AND b.created_at >= #{start} AND b.created_at < #{end})
              )
            """)
    BigDecimal sumApprovedAmount(@Param("start") String start, @Param("end") String end);

    @Select("""
            SELECT COALESCE(SUM(v.net_tonnage), 0)
            FROM billing_vouchers b
            INNER JOIN vessels v ON v.id = b.vessel_id
            WHERE b.total_amount IS NOT NULL
              AND b.audit_status <> 'REJECTED'
              AND (
                    (b.pilotage_start_time >= #{start} AND b.pilotage_start_time < #{end})
                 OR (b.pilotage_start_time IS NULL AND b.created_at >= #{start} AND b.created_at < #{end})
              )
            """)
    BigDecimal sumNetTonnage(@Param("start") String start, @Param("end") String end);
}
