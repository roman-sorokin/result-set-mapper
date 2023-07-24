package com.github.romansorokin.resultset.mapper;

import com.github.romansorokin.resultset.ResultSetMapperFactoryUtils;
import com.github.romansorokin.resultset.annotations.ResultSetField;
import com.github.romansorokin.resultset.annotations.ResultSetType;
import com.github.romansorokin.resultset.field.ResultSetFieldMapper;
import com.github.romansorokin.resultset.field.ResultSetFieldMapperFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.romansorokin.resultset.ResultSetMapperFactoryUtils.DEFAULT_RESULT_SET_FIELD;
import static com.github.romansorokin.resultset.ResultSetMapperFactoryUtils.DEFAULT_RESULT_SET_TYPE;

public class BaseResultSetMapperFactory implements ResultSetMapperFactory {
    private static final Map<Class<? extends ResultSetFieldMapperFactory<?>>, ResultSetFieldMapperFactory<?>> FIELD_FACTORIES = new ConcurrentHashMap<>();

    @Override
    public <T> ResultSetMapper<T> getMapper(Class<T> entity, Supplier<T> supplier) {
        Map<Class<?>, Annotation> typeAnnotations = ResultSetMapperFactoryUtils.getResultSetChildrenAnnotationsByClass(entity);
        Map<String, ResultSetFieldMapper<T>> fieldMappers = getFieldMappers(entity, typeAnnotations);
        if (fieldMappers.isEmpty())
            throw new IllegalArgumentException("result-set annotations not found: " + entity);
        return new BaseResultSetMapper<>(supplier, fieldMappers);
    }

    protected <E> Map<String, ResultSetFieldMapper<E>> getFieldMappers(Class<E> entity, Map<Class<?>, Annotation> typeAnnotations) {
        Annotation annotation = typeAnnotations.get(ResultSetType.class);
        ResultSetType resultSetType = Objects.isNull(annotation) ? DEFAULT_RESULT_SET_TYPE : (ResultSetType) annotation;
        return getFieldMappers(resultSetType, ResultSetMapperFactoryUtils.getFields(entity));
    }

    protected <E> Map<String, ResultSetFieldMapper<E>> getFieldMappers(ResultSetType resultSetType, List<Field> fields) {
        if (fields.isEmpty())
            return Collections.emptyMap();
        Map<Field, Map<Class<?>, Annotation>> markedFields = fields.stream()
                .collect(Collectors.toMap(Function.identity(), ResultSetMapperFactoryUtils::getResultSetChildrenAnnotationsByAnnotatedElement));
        markedFields = getFieldsToMap(resultSetType, markedFields);

        Map<String, ResultSetFieldMapper<E>> fieldMappers = resultSetType.ignoreCase() ? new TreeMap<>(String.CASE_INSENSITIVE_ORDER) : new HashMap<>();
        for (var entry : markedFields.entrySet()) {
            Map<Class<?>, Annotation> annotations = entry.getValue();
            Annotation annotation = annotations.get(ResultSetField.class);
            ResultSetField resultSetField = Objects.isNull(annotation) ? DEFAULT_RESULT_SET_FIELD : (ResultSetField) annotation;
            Map.Entry<String, ResultSetFieldMapper<E>> setterByColumnName = getFieldMapper(entry.getKey(), resultSetType, resultSetField);
            fieldMappers.putIfAbsent(setterByColumnName.getKey(), setterByColumnName.getValue());
        }
        return fieldMappers;
    }

    protected Map<Field, Map<Class<?>, Annotation>> getFieldsToMap(ResultSetType resultSetType, Map<Field, Map<Class<?>, Annotation>> markedFields) {
        return markedFields.entrySet().stream().filter(entry -> {
            Annotation annotation = entry.getValue().get(ResultSetField.class);
            if (Objects.isNull(annotation))
                return resultSetType.mapAllFields();
            ResultSetField resultSetField = (ResultSetField) annotation;
            return !resultSetField.ignore();
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @SuppressWarnings("unchecked")
    protected <E> Map.Entry<String, ResultSetFieldMapper<E>> getFieldMapper(Field field, ResultSetType resultSetType, ResultSetField resultSetField) {
        String columnName = resultSetField.value();
        if (columnName.isBlank())
            columnName = resultSetType.naming().getColumnName(field);
        ResultSetFieldMapperFactory<E> factory = getFieldFactory(resultSetField.factory());
        ResultSetMapperFactoryUtils.setAccessible(field);
        ResultSetFieldMapper<E> mapper = factory.getFieldMapper(field, (e, v) -> ResultSetMapperFactoryUtils.setValue(e, field, v));
        return Map.entry(columnName, mapper);
    }

    @SuppressWarnings("unchecked")
    protected <T extends ResultSetFieldMapperFactory<?>> T getFieldFactory(Class<T> factoryClass) {
        ResultSetFieldMapperFactory<?> factory = FIELD_FACTORIES.get(factoryClass);
        if (Objects.nonNull(factory))
            return (T) factory;
        factory = ResultSetMapperFactoryUtils.getObjectByEmptyConstructor(factoryClass);
        FIELD_FACTORIES.put(factoryClass, factory);
        return (T) factory;
    }
}
