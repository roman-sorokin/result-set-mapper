package com.github.romansorokin.resultset.mapper;

import com.github.romansorokin.resultset.field.ResultSetFieldMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class BaseResultSetMapper<E> implements ResultSetMapper<E> {
    private final Supplier<E> supplier;
    private final Map<String, ResultSetFieldMapper<E>> mappers;

    public BaseResultSetMapper(Supplier<E> supplier, Map<String, ResultSetFieldMapper<E>> mappers) {
        this.supplier = supplier;
        this.mappers = mappers;
    }

    @Override
    public Optional<E> map(ResultSet rs) throws SQLException {
        if (!rs.next())
            return Optional.empty();
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount() + 1;
        E entity = supplier.get();
        for (int i = 1; i < count; i++) {
            String columnName = meta.getColumnName(i);
            ResultSetFieldMapper<E> mapper = mappers.get(columnName);
            if (Objects.isNull(mapper))
                continue;
            mapper.map(entity, rs, i);
        }
        return Optional.of(entity);
    }

}
