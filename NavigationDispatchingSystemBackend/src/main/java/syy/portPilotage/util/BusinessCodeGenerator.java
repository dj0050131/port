package syy.portPilotage.util;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class BusinessCodeGenerator {

    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成业务编号：prefix + yyyyMMdd + seq(补0)
     *
     * @param bizType 业务类型（用于分序列），如 PLAN/REQUEST/INVOICE...
     * @param prefix  编号前缀（展示用），如 PLAN/REQ/INV/PC/BV/WS...
     * @param width   序号位数（如 4 -> 0001）
     */
    public String nextCode(String bizType, String prefix, int width) {
        String date = LocalDate.now().format(DATE_FMT);
        int seq = nextSeqAtLeast(bizType, date, 1);
        return prefix + date + String.format("%0" + width + "d", seq);
    }

    /**
     * 取下一个序号，并确保“下一次返回的序号”不小于 minNextSeq。
     *
     * <p>典型用法：当业务表已有历史编号，但序列表被清空/落后时，先根据历史最大编号计算 minNextSeq，
     * 再调用此方法即可避免生成重复编号。</p>
     */
    public int nextSeqAtLeast(String bizType, String date, int minNextSeq) {
        final int safeMinNextSeq = Math.max(1, minNextSeq);
        final int minBase = safeMinNextSeq - 1;
        return jdbcTemplate.execute((Connection conn) -> {
            try (var ps = conn.prepareStatement(
                    "INSERT INTO business_code_sequences(biz_type, biz_date, next_seq, updated_at) " +
                            // INSERT 分支也要把 LAST_INSERT_ID 设为 next_seq，否则会取到自增 id
                            "VALUES (?, ?, LAST_INSERT_ID(?), NOW()) " +
                            "ON DUPLICATE KEY UPDATE next_seq = LAST_INSERT_ID(GREATEST(next_seq, ?) + 1), updated_at = NOW()"
            )) {
                ps.setString(1, bizType);
                ps.setString(2, date);
                ps.setInt(3, safeMinNextSeq);
                ps.setInt(4, minBase);
                ps.executeUpdate();
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT LAST_INSERT_ID()")) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new IllegalStateException("无法获取业务编号序列号");
            }
        });
    }
}
