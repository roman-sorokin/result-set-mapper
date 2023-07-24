package com.github.romansorokin.resultset.column;

import java.lang.reflect.Field;

@FunctionalInterface
public interface ResultSetFieldToColumnNameFunction {
    String getColumnName(Field field);
}
