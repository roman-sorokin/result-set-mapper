package com.github.romansorokin.resultset.annotations;

import com.github.romansorokin.resultset.column.ResultSetFieldToColumnNameCase;
import com.github.romansorokin.resultset.mapper.BaseResultSetMapperFactory;
import com.github.romansorokin.resultset.mapper.ResultSetMapperFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ResultSet
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResultSetType {
    /**
     * no-argument constructor required
     */
    Class<? extends ResultSetMapperFactory> factory() default BaseResultSetMapperFactory.class;

    ResultSetFieldToColumnNameCase naming() default ResultSetFieldToColumnNameCase.FIELD_NAME;

    boolean ignoreCase() default false;

    boolean mapAllFields() default false;
}
