package com.example.Config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.time.ZonedDateTime;

/**
 * MyBatis 类型处理器：处理 ZonedDateTime 与 PostgreSQL TIMESTAMP 的转换
 */
@MappedTypes(ZonedDateTime.class)
public class ZonedDateTimeTypeHandler extends BaseTypeHandler<ZonedDateTime> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ZonedDateTime parameter, JdbcType jdbcType) throws SQLException {
        // 将 ZonedDateTime 转换为 Timestamp 存储
        ps.setTimestamp(i, Timestamp.from(parameter.toInstant()));
    }

    @Override
    public ZonedDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp == null ? null : ZonedDateTime.ofInstant(timestamp.toInstant(), java.time.ZoneId.systemDefault());
    }

    @Override
    public ZonedDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        return timestamp == null ? null : ZonedDateTime.ofInstant(timestamp.toInstant(), java.time.ZoneId.systemDefault());
    }

    @Override
    public ZonedDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        return timestamp == null ? null : ZonedDateTime.ofInstant(timestamp.toInstant(), java.time.ZoneId.systemDefault());
    }
}

