package com.github.romansorokin.sqliteutils.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface FieldNameAndResultSetFunction<T> {
    T apply(ResultSet rs, String fieldName) throws SQLException;
}
