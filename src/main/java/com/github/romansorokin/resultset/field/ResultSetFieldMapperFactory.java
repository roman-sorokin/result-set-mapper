package com.github.romansorokin.resultset.field;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ResultSetFieldMapperFactory<E> {
    <V> ResultSetFieldMapper<E> getFieldMapper(Field field, BiConsumer<E, V> setter);
}
