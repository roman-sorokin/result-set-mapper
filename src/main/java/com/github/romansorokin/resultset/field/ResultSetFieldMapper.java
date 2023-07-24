package com.github.romansorokin.resultset.field;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetFieldMapper<E> {
    void map(E entity, ResultSet rs, int columnIndex) throws SQLException;
}
