package com.github.romansorokin.resultset.column;

import java.lang.reflect.Field;

public enum ResultSetFieldToColumnNameCase implements ResultSetFieldToColumnNameFunction {
    CAMEL_FIELD_NAME(Field::getName),
    SNAKE(f -> ""),
    KEBAB(f -> ""),
    PASCAL(f -> "");

    private final ResultSetFieldToColumnNameFunction func;

    ResultSetFieldToColumnNameCase(ResultSetFieldToColumnNameFunction func) {
        this.func = func;
    }

    @Override
    public String getColumnName(Field field) {
        return func.getColumnName(field);
    }
}
