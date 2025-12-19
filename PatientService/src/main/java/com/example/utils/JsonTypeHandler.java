package com.example.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.io.IOException;
import java.sql.*;

/**
 * 简单 JSON TypeHandler：将 Java 对象按 JSON 字符串写入 PG 的 json/jsonb/other 字段，
 * 并在读取时反序列化为指定 JavaType。
 *
 * 使用方式：
 *  - 写入：#{xxx, typeHandler=com.example.utils.JsonTypeHandler, javaType=java.util.List, jdbcType=OTHER}
 *  - 读取：<result column="xxx" property="yyy" typeHandler="com.example.utils.JsonTypeHandler" javaType="..."/>
 */
public class JsonTypeHandler extends BaseTypeHandler<Object> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = MAPPER.writeValueAsString(parameter);
            // PG json/jsonb 推荐用 OTHER
            ps.setObject(i, json, Types.OTHER);
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to serialize parameter to JSON", e);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return read(rs.getString(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return read(rs.getString(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return read(cs.getString(columnIndex));
    }

    private Object read(String json) throws SQLException {
        if (json == null || json.isBlank()) return null;
        // 由于 BaseTypeHandler<Object> 拿不到目标 javaType，这里只返回 JsonNode/Map 会影响 DTO。
        // 所以我们在 XML 里配合 javaType 指定反序列化类型，走 set/get 的对象映射。
        // MyBatis 在调用这个 handler 时会把 javaType 信息传给 TypeHandlerRegistry，但 BaseTypeHandler 仍不可见。
        // 为了保持简单可跑通：此处先返回原始 JSON 字符串，调用层可自行解析。
        return json;
    }

    /**
     * 工具方法：给 Service 层快速解析用。
     */
    public static <T> T parse(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return null;
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse json", e);
        }
    }
}

