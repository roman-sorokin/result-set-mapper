package com.github.romansorokin.resultset;

import com.github.romansorokin.resultset.field.BaseResultSetFieldMapperFactory;
import com.github.romansorokin.resultset.field.ResultSetFieldValueByColumnIndexFunction;
import com.github.romansorokin.resultset.mapper.ResultSetMapper;
import com.github.romansorokin.resultset.mapper.ResultSetMapperFactory;

import java.util.function.Supplier;

public final class ResultSetMapperUtils {
//    private static final ResultSetMapper<Map<String, Object>> DEFAULT_MAPPER = rs -> {
//        if (!rs.next())
//            return null;
//        ResultSetMetaData meta = rs.getMetaData();
//        int count = meta.getColumnCount();
//        HashMap<String, Object> map = new HashMap<>();
//        for (int i = 1; i < count + 1; i++)
//            map.put(meta.getColumnName(i), rs.getObject(i));
//        return map;
//    };
//
//    public static ResultSetMapper<Map<String, Object>> getDefaultMapper() {
//        return DEFAULT_MAPPER;
//    }

//    public static <T> ResultSetMapper<Set<T>> getSetMapper(ResultSetMapper<T> mapper) {
//        return getCollectionMapper(mapper, HashSet::new, Set::add, Collections.emptySet());
//    }
//
//    public static <T> ResultSetMapper<List<T>> getListMapper(ResultSetMapper<T> mapper) {
//        return getCollectionMapper(mapper, ArrayList::new, List::add, Collections.emptyList());
//    }

//    public static <T, C> ResultSetMapper<C> getCollectionMapper(ResultSetMapper<T> mapper, Supplier<C> collection, BiConsumer<C, T> accumulator, C onEmpty) {
//        return rs -> {
//            T result = mapper.map(rs);
//            if (Objects.isNull(result))
//                return onEmpty;
//            C c = collection.get();
//            do {
//                accumulator.accept(c, result);
//                result = mapper.map(rs);
//            } while (Objects.nonNull(result));
//            return c;
//        };
//    }

    public static <T> ResultSetMapper<T> getMapper(Class<T> entity, Supplier<T> supplier) {
        ResultSetMapperFactory factory = ResultSetMapperFactoryUtils.getMapperFactory(entity);
        return factory.getMapper(entity, supplier);
    }

    public static <V> void registerFieldType(Class<V> fieldType, ResultSetFieldValueByColumnIndexFunction<V> getter) {
        BaseResultSetFieldMapperFactory.register(fieldType, getter);
    }

    private ResultSetMapperUtils() {}
}
