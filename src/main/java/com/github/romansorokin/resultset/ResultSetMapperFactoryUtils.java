package com.github.romansorokin.resultset;

import com.github.romansorokin.resultset.annotations.ResultSet;
import com.github.romansorokin.resultset.annotations.ResultSetField;
import com.github.romansorokin.resultset.annotations.ResultSetType;
import com.github.romansorokin.resultset.mapper.BaseResultSetMapperFactory;
import com.github.romansorokin.resultset.mapper.ResultSetMapperFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ResultSetType
public final class ResultSetMapperFactoryUtils {
    @ResultSetField
    private static final BaseResultSetMapperFactory BASE_RESULT_SET_MAPPER_FACTORY = new BaseResultSetMapperFactory();
    public static final ResultSetType DEFAULT_RESULT_SET_TYPE = ResultSetMapperFactoryUtils.getDefaultResultSetType();
    public static final ResultSetField DEFAULT_RESULT_SET_FIELD = ResultSetMapperFactoryUtils.getDefaultResultSetField();

    public static ResultSetType getDefaultResultSetType() {
        Map<Class<?>, Annotation> children = getResultSetChildrenAnnotationsByAnnotatedElement(ResultSetMapperFactoryUtils.class);
        Annotation annotation = children.get(ResultSetType.class);
        if (Objects.nonNull(annotation))
            return (ResultSetType) annotation;
        throw new IllegalStateException(ResultSetMapperFactoryUtils.class + " required annotation " + ResultSetType.class);
    }

    public static ResultSetField getDefaultResultSetField() {
        Field[] fields = ResultSetMapperFactoryUtils.class.getDeclaredFields();
        for (Field field : fields) {
            Map<Class<?>, Annotation> children = getResultSetChildrenAnnotationsByAnnotatedElement(field);
            Annotation annotation = children.get(ResultSetField.class);
            if (Objects.nonNull(annotation))
                return (ResultSetField) annotation;
        }
        throw new IllegalStateException(ResultSetMapperFactoryUtils.class + " required annotation " + ResultSetField.class);
    }

    public static ResultSetMapperFactory getMapperFactory(Class<?> entityClass) {
        Map<Class<?>, Annotation> children = getResultSetChildrenAnnotationsByAnnotatedElement(entityClass);
        ResultSetType type = (ResultSetType) children.get(ResultSetType.class);
        if (Objects.isNull(type) || type.factory() == BaseResultSetMapperFactory.class)
            return BASE_RESULT_SET_MAPPER_FACTORY;
        Class<? extends ResultSetMapperFactory> factory = type.factory();
        return getObjectByEmptyConstructor(factory);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObjectByEmptyConstructor(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        try {
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == 0)
                    return (T) constructor.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("error on create factory instance " + clazz, e);
        }
        throw new IllegalArgumentException("no-argument constructor required: " + clazz);
    }

    public static void setAccessible(Field field) {
        if (!field.trySetAccessible())
            throw new IllegalArgumentException("field is not accessible: " + field);
        field.setAccessible(true);
    }

    public static void setValue(Object entity, Field field, Object value) {
        try {
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("reflection, field set value", e);
        }
    }

    public static boolean isJavaLang(Annotation annotation) {
        return annotation.annotationType().getPackage().getName().startsWith("java.lang.annotation");
    }

    public static boolean isResultSetChild(Annotation annotation) {
        Annotation[] parents = annotation.annotationType().getDeclaredAnnotations();
        for (Annotation parent : parents)
            if (parent instanceof ResultSet)
                return true;
        return false;
    }

    public static List<Field> getFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> parent = clazz.getSuperclass();
        while (parent != Object.class) {
            result.addAll(Arrays.asList(parent.getDeclaredFields()));
            parent = parent.getSuperclass();
        }
        return result;
    }

    public static Map<Class<?>, Annotation> getResultSetChildrenAnnotationsByClass(Class<?> clazz) {
        Map<Class<?>, Annotation> result = getResultSetChildrenAnnotationsByAnnotatedElement(clazz);
        if (result.isEmpty())
            result = new HashMap<>();
        Class<?> parent = clazz.getSuperclass();
        while (parent != Object.class) {
            getResultSetChildrenAnnotationsByAnnotatedElement(parent).forEach(result::putIfAbsent);
            parent = parent.getSuperclass();
        }
        return result;
    }

    public static Map<Class<?>, Annotation> getResultSetChildrenAnnotationsByAnnotatedElement(AnnotatedElement element) {
        Annotation[] annotations = element.getDeclaredAnnotations();
        if (annotations.length == 0)
            return Collections.emptyMap();
        Map<Class<?>, Annotation> result = new HashMap<>();
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (isResultSetChild(annotation))
                result.putIfAbsent(annotationType, annotation);
            if (isJavaLang(annotation))
                continue;
            getResultSetChildrenAnnotationsByAnnotatedElement(annotationType).forEach(result::putIfAbsent);
        }
        return result;
    }

    private ResultSetMapperFactoryUtils() {}
}
