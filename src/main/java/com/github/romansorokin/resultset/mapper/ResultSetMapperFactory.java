package com.github.romansorokin.resultset.mapper;

import java.util.function.Supplier;

@FunctionalInterface
public interface ResultSetMapperFactory {
    <T> ResultSetMapper<T> getMapper(Class<T> entity, Supplier<T> supplier);
}
