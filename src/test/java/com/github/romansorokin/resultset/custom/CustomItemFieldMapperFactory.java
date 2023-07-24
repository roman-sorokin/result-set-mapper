package com.github.romansorokin.resultset.custom;

import com.github.romansorokin.resultset.field.ResultSetFieldMapper;
import com.github.romansorokin.resultset.field.ResultSetFieldMapperFactory;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

public class CustomItemFieldMapperFactory implements ResultSetFieldMapperFactory<Object> {
    @Override
    @SuppressWarnings("unchecked")
    public <V> ResultSetFieldMapper<Object> getFieldMapper(Field field, BiConsumer<Object, V> setter) {
        return (e, rs, i) -> {
            String value = rs.getString(i)
                    .replace("{\"name\":\"", "")
                    .replace("\"}", "");
            CustomItem ci = new CustomItem();
            ci.name = value;
            setter.accept(e, (V) ci);
        };
    }
}
