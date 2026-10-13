package syy.portPilotage.util;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;
import java.util.Objects;

public class EnumTypeHandler<T extends Enum<T>> implements TypeHandler<T> {

    private final Class<T> enumClass;

    public EnumTypeHandler(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, Types.VARCHAR);
        } else {
            ps.setString(i, parameter.name());
        }
    }

    @Override
    public T getResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? Enum.valueOf(enumClass, value) : null;
    }

    @Override
    public T getResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? Enum.valueOf(enumClass, value) : null;
    }

    @Override
    public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? Enum.valueOf(enumClass, value) : null;
    }
}