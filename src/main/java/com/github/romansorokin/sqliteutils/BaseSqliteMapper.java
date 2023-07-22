package com.github.romansorokin.sqliteutils;

import com.github.romansorokin.sqliteutils.annotations.Column;
import com.github.romansorokin.sqliteutils.exceptions.SqliteMapperRuntimeException;
import com.github.romansorokin.sqliteutils.mapper.FieldMapper;
import com.github.romansorokin.sqliteutils.mapper.FieldNameAndResultSetFunction;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

class BaseSqliteMapper<T> implements SqliteMapper<T> {
    protected final Map<String, MarkedField> markedFields = new HashMap<>();
    protected final Class<T> entityClass;
    protected final Supplier<T> entitySupplier;

    private record MarkedField(Field field, String columnName, FieldNameAndResultSetFunction<?> func) {}

    public BaseSqliteMapper(Class<T> entityClass, Supplier<T> entitySupplier) {
        this.entityClass = entityClass;
        this.entitySupplier = entitySupplier;
    }

    public BaseSqliteMapper<T> init() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        HashMap<Class<? extends FieldMapper>, FieldMapper> cache = new HashMap<>();
        for (Field field : entityClass.getDeclaredFields()) {
            Map<Class<?>, Annotation> annotations = SqliteUtils.getSqliteAnnotations(field.getDeclaredAnnotations());
            if (annotations.isEmpty())
                continue;
            MarkedField markedField = markField(field, annotations, cache);
            markedFields.put(markedField.columnName, markedField);
        }
        if (cache.isEmpty() || markedFields.isEmpty())
            throw new IllegalArgumentException("require column annotations on any fields");
        return this;
    }

    protected MarkedField markField(Field field, Map<Class<?>, Annotation> annotations, HashMap<Class<? extends FieldMapper>, FieldMapper> cache) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Column column = (Column) annotations.get(Column.class);
        if (Objects.isNull(column))
            throw new IllegalArgumentException("field must have column annotation");
        if (!field.trySetAccessible())
            throw new IllegalArgumentException("field is not accessible: " + field);
        field.setAccessible(true);

        Class<? extends FieldMapper> fieldMapperClass = column.fieldMapper();
        FieldMapper mapper = cache.get(fieldMapperClass);
        String columnName = column.value().isBlank() ? field.getName() : column.value();
        if (Objects.nonNull(mapper)) {
            FieldNameAndResultSetFunction<?> mapFunc = mapper.map(field);
            return new MarkedField(field, columnName, mapFunc);
        }
        mapper = createFieldMapper(fieldMapperClass);
        cache.put(fieldMapperClass, mapper);
        FieldNameAndResultSetFunction<?> mapFunc = mapper.map(field);
        return new MarkedField(field, columnName, mapFunc);
    }

    protected FieldMapper createFieldMapper(Class<? extends FieldMapper> fieldMapperClass) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?>[] constructors = fieldMapperClass.getConstructors();
        if (constructors.length > 1)
            throw new IllegalArgumentException("more than one constructor: " + fieldMapperClass.getName());
        Constructor<?> constructor = constructors[0];
        if (constructor.getParameterCount() > 0)
            throw new IllegalArgumentException("require constructor without args: " + fieldMapperClass.getName());
        return (FieldMapper) constructor.newInstance();
    }

    @Override
    public T map(ResultSet rs) throws SQLException {
        if (!rs.next())
            return null;

        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount() + 1;
        T entity = entitySupplier.get();
        for (int i = 1; i < count; i++) {
            String columnName = meta.getColumnName(i);
            if (Objects.isNull(rs.getObject(i))) // i don't know
                continue;
            MarkedField markedField = markedFields.get(columnName);
            if (Objects.isNull(markedField))
                continue;
            mapField(entity, rs, markedField);
        }
        return entity;
    }

    protected void mapField(T entity, ResultSet rs, MarkedField mf) throws SQLException {
        Object mapFieldResult = mf.func.apply(rs, mf.columnName);
        Field field = mf.field;
        try {
            field.set(entity, mapFieldResult);
        } catch (IllegalAccessException e) {
            /* impossible :) */
            throw new SqliteMapperRuntimeException("reflection, field set value", e);
        }
    }
}
