package com.github.romansorokin.sqliteutils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseTest {
    private static final String JDBC_URL = "jdbc:sqlite:test.db";

    protected <T> T executeQuery(String sql, SqliteMapper<T> map) {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet rs = statement.executeQuery(sql);
                return map.map(rs);
            }
        } catch (Exception e) {
            throw new RuntimeException("sql error:\n" + sql, e);
        }
    }

    protected void execute(String sql) {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException("sql error:\n" + sql, e);
        }
    }

    protected Connection getConnection() {
        try {
            return DriverManager.getConnection(JDBC_URL);
        } catch (SQLException e) {
            throw new RuntimeException("get connection", e);
        }
    }
}
