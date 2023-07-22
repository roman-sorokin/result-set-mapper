package com.github.romansorokin.sqliteutils;

import com.github.romansorokin.sqliteutils.annotations.Sqlite;
import com.github.romansorokin.sqliteutils.exceptions.SqliteMapperRuntimeException;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
    private static final SqliteMapper<Map<String, Object>> DEFAULT_MAPPER = rs -> {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        if (count < 1)
            return Collections.emptyMap();
        HashMap<String, Object> map = new HashMap<>();
        for (int i = 1; i < count + 1; i++)
            map.put(meta.getColumnName(i), rs.getObject(i));
        return map;
    };

    public static SqliteMapper<Map<String, Object>> getDefaultMapper() {
        return DEFAULT_MAPPER;
    }

    public static <T> SqliteMapper<T> getMapper(Class<T> entityClass, Supplier<T> entitySupplier) {
        try {
            return new BaseSqliteMapper<>(entityClass, entitySupplier).init();
        } catch (Exception e) {
            throw new SqliteMapperRuntimeException("sqlite mapper", e);
        }
    }

    public static <T> SqliteMapper<Set<T>> getSetMapper(SqliteMapper<T> mapper) {
        return getCollectionMapper(mapper, HashSet::new, Set::add, Collections.emptySet());
    }

    public static <T> SqliteMapper<List<T>> getListMapper(SqliteMapper<T> mapper) {
        return getCollectionMapper(mapper, ArrayList::new, List::add, Collections.emptyList());
    }

    public static <T, C> SqliteMapper<C> getCollectionMapper(SqliteMapper<T> mapper, Supplier<C> collection, BiConsumer<C, T> accumulator, C onEmpty) {
        return rs -> {
            if (!rs.next())
                return onEmpty;
            C c = collection.get();
            do {
                accumulator.accept(c, mapper.map(rs));
            } while (rs.next());
            return c;
        };
    }

    public static boolean enableForeignKeys(Connection connection) throws SQLException {
        return execute(connection, "pragma foreign_keys=ON");
    }

    public static boolean execute(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.execute(query);
        }
    }

    public static boolean execute(Connection connection, String query, Object... args) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < args.length; i++)
                statement.setObject(i + 1, args[i]);
            return statement.execute();
        }
    }

    public static long executeUpdate(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeLargeUpdate(query);
        }
    }

    public static long executeUpdate(Connection connection, String query, Object... args) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < args.length; i++)
                statement.setObject(i + 1, args[i]);
            return statement.executeLargeUpdate();
        }
    }

    public static <T> T executeQuery(Connection connection, SqliteMapper<T> mapper, String query) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(query);
            return mapper.map(rs);
        }
    }

    public static <T> T executeQuery(Connection connection, SqliteMapper<T> mapper, String query, Object... args) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < args.length; i++)
                statement.setObject(i + 1, args[i]);
            ResultSet rs = statement.executeQuery();
            return mapper.map(rs);
        }
    }

    static boolean isJavaLangAnnotation(Annotation annotation) {
        Objects.requireNonNull(annotation, "annotation must not be null");
        return annotation.annotationType().getPackage().getName().startsWith("java.lang.annotation");
    }

    static Map<Class<?>, Annotation> getSqliteAnnotations(Annotation[] annotations) {
        return Arrays.stream(annotations)
                .filter(SqliteUtils::hasSqliteAnnotation)
                .collect(Collectors.toMap(Annotation::annotationType, Function.identity()));
    }

    static boolean hasSqliteAnnotation(Annotation annotation) {
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
