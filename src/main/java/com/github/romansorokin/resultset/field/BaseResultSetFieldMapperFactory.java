package com.github.romansorokin.resultset.field;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class BaseResultSetFieldMapperFactory<E> implements ResultSetFieldMapperFactory<E> {
    protected static final Map<Class<?>, ResultSetFieldValueByColumnIndexFunction<?>> MAPPERS = new ConcurrentHashMap<>();

    static {
        MAPPERS.put(boolean.class, ResultSet::getBoolean);
        MAPPERS.put(byte.class, ResultSet::getByte);
        MAPPERS.put(short.class, ResultSet::getShort);
        MAPPERS.put(int.class, ResultSet::getInt);
        MAPPERS.put(long.class, ResultSet::getLong);
        MAPPERS.put(float.class, ResultSet::getFloat);
        MAPPERS.put(double.class, ResultSet::getDouble);
        MAPPERS.put(byte[].class, ResultSet::getBytes);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <V> ResultSetFieldMapper<E> getFieldMapper(Field field, BiConsumer<E, V> setter) {
        Class<?> type = field.getType();
        if (type.isEnum()) {
            Class<Enum> enumType = (Class<Enum>) type;
            return (entity, rs, index) -> {
                V value = (V) Enum.valueOf(enumType, rs.getString(index));
                setter.accept(entity, value);
            };
        }
        ResultSetFieldValueByColumnIndexFunction<V> func = (ResultSetFieldValueByColumnIndexFunction<V>) MAPPERS.get(type);
        if (Objects.nonNull(func))
            return (entity, rs, index) -> {
                V value = func.getValue(rs, index);
                setter.accept(entity, value);
            };
        return (entity, rs, index) -> {
            V value = (V) rs.getObject(index, type);
            setter.accept(entity, value);
        };
    }

    public static <V> void register(Class<V> fieldType, ResultSetFieldValueByColumnIndexFunction<V> getter) {
        MAPPERS.put(fieldType, getter);
    }
}