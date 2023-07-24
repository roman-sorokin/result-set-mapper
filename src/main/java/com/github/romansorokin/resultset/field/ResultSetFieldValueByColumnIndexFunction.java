package com.github.romansorokin.resultset.field;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetFieldValueByColumnIndexFunction<V> {
    V getValue(ResultSet rs, int columnIndex) throws SQLException;
}
