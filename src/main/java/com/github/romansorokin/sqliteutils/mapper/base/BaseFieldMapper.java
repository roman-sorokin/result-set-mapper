package com.github.romansorokin.sqliteutils.mapper.base;

import com.github.romansorokin.sqliteutils.mapper.FieldMapper;
import com.github.romansorokin.sqliteutils.mapper.FieldNameAndResultSetFunction;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BaseFieldMapper implements FieldMapper {
    protected static final Map<Class<?>, FieldNameAndResultSetFunction<?>> MAP = new HashMap<>();

    static {
        MAP.put(boolean.class, ResultSet::getBoolean);
        MAP.put(byte.class, ResultSet::getByte);
        MAP.put(short.class, ResultSet::getShort);
        MAP.put(int.class, ResultSet::getInt);
        MAP.put(long.class, ResultSet::getLong);
        MAP.put(float.class, ResultSet::getFloat);
        MAP.put(double.class, ResultSet::getDouble);
        MAP.put(byte[].class, ResultSet::getBytes);
        MAP.put(Blob.class, ResultSet::getBlob);
        MAP.put(BigDecimal.class, ResultSet::getBigDecimal);
        MAP.put(Instant.class, (rs, name) -> rs.getTimestamp(name).toInstant());
    }

    @Override
    public FieldNameAndResultSetFunction<?> map(Field field) {
        Class<?> type = field.getType();
        FieldNameAndResultSetFunction<?> func = MAP.get(type);
        if (Objects.nonNull(func))
            return func;
        return (rs, name) -> rs.getObject(name, type);
    }
}