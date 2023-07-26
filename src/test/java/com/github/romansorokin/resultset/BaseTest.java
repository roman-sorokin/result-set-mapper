package com.github.romansorokin.resultset;

import com.github.romansorokin.resultset.mapper.ResultSetMapper;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public abstract class BaseTest {
    private static final String JDBC_URL = "jdbc:h2:file:./store/result-set-mapper";

    protected <T> Optional<T> executeQuery(Connection connection, String sql, ResultSetMapper<T> mapper, Object... args) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++)
                statement.setObject(i + 1, args[i]);
            ResultSet rs = statement.executeQuery();
            var result = mapper.map(rs);
            if (result.isPresent() && !(result.get() instanceof Collection<?>))
                printResultSet(rs);
            return result;
        }
    }

    protected <T> Optional<T> executeQuery(String sql, ResultSetMapper<T> mapper, Object... args) throws SQLException {
        try (Connection connection = getConnection()) {
            return executeQuery(connection, sql, mapper, args);
        }
    }

    protected <T> Optional<T> executeQuery(Connection connection, String sql, ResultSetMapper<T> mapper) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(sql);
            var result = mapper.map(rs);
            if (result.isPresent() && !(result.get() instanceof Collection<?>))
                printResultSet(rs);
            return result;
        }
    }

    protected <T> Optional<T> executeQuery(String sql, ResultSetMapper<T> mapper) throws SQLException {
        try (Connection connection = getConnection()) {
            return executeQuery(connection, sql, mapper);
        }
    }

    protected void execute(String sql) throws SQLException {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
    }

    protected void execute(Connection connection, String sql, Object... args) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++)
                statement.setObject(i + 1, args[i]);
            statement.execute();
        }
    }

    protected void execute(String sql, Object... args) throws SQLException {
        try (Connection connection = getConnection()) {
            execute(connection, sql, args);
        }
    }

    protected Connection getConnection() {
        try {
            return DriverManager.getConnection(JDBC_URL);
        } catch (SQLException e) {
            throw new RuntimeException("get connection", e);
        }
    }

    protected void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount() + 1;
        HashMap<String, String> result = new HashMap<>();
        for (int i = 1; i < count; i++) {
            Object value = rs.getObject(i);
            result.put(meta.getColumnName(i), Objects.nonNull(value) ? value.toString() : null);
        }
        log.info("result-set: {}", result);
    }
}
