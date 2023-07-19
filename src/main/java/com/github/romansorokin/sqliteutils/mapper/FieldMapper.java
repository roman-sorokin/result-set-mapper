package com.github.romansorokin.sqliteutils.mapper;

import java.lang.reflect.Field;

public interface FieldMapper {
    FieldNameAndResultSetFunction<?> map(Field field);
}
