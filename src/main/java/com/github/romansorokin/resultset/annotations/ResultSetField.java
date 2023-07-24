package com.github.romansorokin.resultset.annotations;

import com.github.romansorokin.resultset.field.BaseResultSetFieldMapperFactory;
import com.github.romansorokin.resultset.field.ResultSetFieldMapperFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ResultSet
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResultSetField {
    String value() default "";

    /**
     * no-argument constructor required
     */
    Class<? extends ResultSetFieldMapperFactory> factory() default BaseResultSetFieldMapperFactory.class;

    boolean ignore() default false;
}
