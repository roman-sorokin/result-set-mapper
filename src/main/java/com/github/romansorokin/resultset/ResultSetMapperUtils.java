package com.github.romansorokin.resultset;

import com.github.romansorokin.resultset.field.BaseResultSetFieldMapperFactory;
import com.github.romansorokin.resultset.field.ResultSetFieldValueByColumnIndexFunction;
import com.github.romansorokin.resultset.mapper.ResultSetMapper;
import com.github.romansorokin.resultset.mapper.ResultSetMapperFactory;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class ResultSetMapperUtils {
    public static final ResultSetMapper<Map<String, Object>> HASH_MAP_MAPPER = getMapper(HashMap::new);
    public static final ResultSetMapper<Map<String, Object>> TREE_MAP_IGNORE_CASE_MAPPER = getMapper(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER));

    public static <T> ResultSetMapper<Set<T>> getSetMapper(ResultSetMapper<T> mapper) {
        return getCollectionMapper(mapper, HashSet::new, Set::add, Collections.emptySet());
    }

    public static <T> ResultSetMapper<List<T>> getListMapper(ResultSetMapper<T> mapper) {
        return getCollectionMapper(mapper, ArrayList::new, List::add, Collections.emptyList());
    }

    public static <T, C> ResultSetMapper<C> getCollectionMapper(ResultSetMapper<T> mapper, Supplier<C> collection, BiConsumer<C, T> accumulator, C onEmpty) {
        return rs -> {
            Optional<T> result = mapper.map(rs);
            if (result.isEmpty())
                return Optional.of(onEmpty);
            C c = collection.get();
            do {
                accumulator.accept(c, result.get());
                result = mapper.map(rs);
            } while (result.isPresent());
            return Optional.of(c);
        };
    }

    public static <M extends Map<String, Object>> ResultSetMapper<M> getMapper(Supplier<M> getter) {
        return rs -> {
            if (!rs.next())
                return Optional.empty();
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            M map = getter.get();
            for (int i = 1; i < count + 1; i++)
                map.put(meta.getColumnName(i), rs.getObject(i));
            return Optional.of(map);
        };
    }

    public static <T> ResultSetMapper<T> getMapper(Class<T> entity, Supplier<T> supplier) {
        ResultSetMapperFactory factory = ResultSetMapperFactoryUtils.getMapperFactory(entity);
        return factory.getMapper(entity, supplier);
    }

    public static <V> void registerFieldType(Class<V> fieldType, ResultSetFieldValueByColumnIndexFunction<V> getter) {
        BaseResultSetFieldMapperFactory.register(fieldType, getter);
    }

    private ResultSetMapperUtils() {}
}
