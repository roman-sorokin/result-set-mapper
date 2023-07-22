package com.github.romansorokin.sqliteutils;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface SqliteMapper<T> {
    T map(ResultSet rs) throws SQLException;
}
