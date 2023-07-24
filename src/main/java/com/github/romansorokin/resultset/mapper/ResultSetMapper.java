package com.github.romansorokin.resultset.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public interface ResultSetMapper<T> {
    Optional<T> map(ResultSet rs) throws SQLException;
}
