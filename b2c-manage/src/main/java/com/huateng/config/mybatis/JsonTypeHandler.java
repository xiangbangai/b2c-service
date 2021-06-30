//package com.huateng.config.mybatis;
//
//import com.huateng.common.util.JacksonUtil;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ibatis.type.JdbcType;
//import org.apache.ibatis.type.BaseTypeHandler;
//import java.sql.CallableStatement;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import org.postgresql.util.PGobject;
//
//@Slf4j
//public class JsonTypeHandler<T> extends BaseTypeHandler<T> {
//
//    private static final PGobject pgObject = new PGobject();
//    private Class<T> type;
//
//    public JsonTypeHandler(Class<T> type) {
//        if(log.isTraceEnabled()) {
//            log.trace("JsonTypeHandler(" + type + ")");
//        }
//        if (type == null) {
//            throw new IllegalArgumentException("Type argument cannot be null");
//        }
//        this.type = type;
//    }
//
//
//    @SneakyThrows
//    @Override
//    public void setNonNullParameter(PreparedStatement preparedStatement, int i, T t, JdbcType jdbcType) {
//        pgObject.setType("json");
//        pgObject.setValue(JacksonUtil.toJson(t));
//        preparedStatement.setObject(i, pgObject);
//    }
//
//    @SneakyThrows
//    @Override
//    public T getNullableResult(ResultSet resultSet, String s) {
//        return JacksonUtil.toObject(resultSet.getString(s), type);
//    }
//
//    @SneakyThrows
//    @Override
//    public T getNullableResult(ResultSet resultSet, int i) {
//        return JacksonUtil.toObject(resultSet.getString(i), type);
//    }
//
//    @SneakyThrows
//    @Override
//    public T getNullableResult(CallableStatement callableStatement, int i) {
//        return JacksonUtil.toObject(callableStatement.getString(i), type);
//    }
//
//}
