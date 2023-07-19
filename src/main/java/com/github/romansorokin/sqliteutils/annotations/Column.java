package com.github.romansorokin.sqliteutils.annotations;

import com.github.romansorokin.sqliteutils.mapper.FieldMapper;
import com.github.romansorokin.sqliteutils.mapper.base.BaseFieldMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Sqlite
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String value() default "";

    Class<? extends FieldMapper> fieldMapper() default BaseFieldMapper.class;
}
