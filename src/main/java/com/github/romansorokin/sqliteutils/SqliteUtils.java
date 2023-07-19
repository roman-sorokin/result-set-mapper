package com.github.romansorokin.sqliteutils;

import com.github.romansorokin.sqliteutils.annotations.Sqlite;
import com.github.romansorokin.sqliteutils.exceptions.SqliteMapperRuntimeException;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@UtilityClass
public class SqliteUtils {
    public static <T> SqliteMapper<T> getMapper(Class<T> entityClass, Supplier<T> entitySupplier) {
        try {
            return new BaseSqliteMapper<>(entityClass, entitySupplier).init();
        } catch (Exception e) {
            throw new SqliteMapperRuntimeException("sqlite mapper", e);
        }
    }

    public static <T> Set<T> hashSet(ResultSet rs, SqliteMapper<T> mapper) throws SQLException, IllegalAccessException {
        Set<T> result = collection(rs, mapper, HashSet::new, HashSet::add);
        if (Objects.isNull(result))
            return Collections.emptySet();
        return result;
    }

    public static <T> List<T> arrayList(ResultSet rs, SqliteMapper<T> mapper) throws SQLException, IllegalAccessException {
        List<T> result = collection(rs, mapper, ArrayList::new, ArrayList::add);
        if (Objects.isNull(result))
            return Collections.emptyList();
        return result;
    }

    public static <T, C> C collection(ResultSet rs, SqliteMapper<T> mapper, Supplier<C> collection, BiConsumer<C, T> accumulator) throws SQLException, IllegalAccessException {
        if (!rs.next())
            return null;
        C c = collection.get();
        do {
            accumulator.accept(c, mapper.map(rs));
        } while (rs.next());
        return c;
    }

    public static boolean isJavaLangAnnotation(Annotation annotation) {
        Objects.requireNonNull(annotation, "annotation must not be null");
        return annotation.annotationType().getPackage().getName().startsWith("java.lang.annotation");
    }

    public static Map<Class<?>, Annotation> getSqliteAnnotations(Annotation[] annotations) {
        return Arrays.stream(annotations)
                .filter(SqliteUtils::hasSqliteAnnotation)
                .collect(Collectors.toMap(Annotation::annotationType, Function.identity()));
    }

    public static boolean hasSqliteAnnotation(Annotation annotation) {
        if (isJavaLangAnnotation(annotation))
            return false;
        if (annotation instanceof Sqlite)
            throw new IllegalArgumentException("annotation: " + annotation + " is " + Sqlite.class);
        Annotation[] declared = annotation.annotationType().getDeclaredAnnotations();
        for (Annotation ann : declared) {
            if (ann instanceof Sqlite)
                return true;
            boolean exists = hasSqliteAnnotation(ann);
            if (exists)
                return true;
        }
        return false;
    }
}
