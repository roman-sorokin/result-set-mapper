package com.github.romansorokin.resultset.column;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultSetFieldToColumnNameCaseTest {
    private String simpleFieldName;
    private String simple_field_name;
    private String simple_fieldName;
    private String a;
    private String aB;
    private String a_B;
    private String A_B;

    private static Field getField(String fieldName) {
        Field[] fields = ResultSetFieldToColumnNameCaseTest.class.getDeclaredFields();
        return Arrays.stream(fields).filter(a -> a.getName().equals(fieldName)).findAny().orElseThrow();
    }

    @Test
    void camelFieldName_fieldName() {
        Field field = getField("simpleFieldName");
        String columnName = ResultSetFieldToColumnNameCase.FIELD_NAME.getColumnName(field);
        assertEquals("simpleFieldName", columnName);
    }

    @Test
    void camelFieldName_camel() {
        Field field = getField("simpleFieldName");
        String columnName = ResultSetFieldToColumnNameCase.CAMEL.getColumnName(field);
        assertEquals("simpleFieldName", columnName);
    }

    @Test
    void pascalFieldName_simpleFieldName() {
        Field field = getField("simpleFieldName");
        String columnName = ResultSetFieldToColumnNameCase.PASCAL.getColumnName(field);
        assertEquals("SimpleFieldName", columnName);
    }

    @Test
    void pascalFieldName_a() {
        Field field = getField("a");
        String columnName = ResultSetFieldToColumnNameCase.PASCAL.getColumnName(field);
        assertEquals("A", columnName);
    }

    @Test
    void pascalFieldName_aB() {
        Field field = getField("aB");
        String columnName = ResultSetFieldToColumnNameCase.PASCAL.getColumnName(field);
        assertEquals("AB", columnName);
    }

    @Test
    void kebabFieldName_simpleFieldName() {
        Field field = getField("simpleFieldName");
        String columnName = ResultSetFieldToColumnNameCase.KEBAB.getColumnName(field);
        assertEquals("simple-field-name", columnName);
    }

    @Test
    void kebabFieldName_a() {
        Field field = getField("a");
        String columnName = ResultSetFieldToColumnNameCase.KEBAB.getColumnName(field);
        assertEquals("a", columnName);
    }

    @Test
    void kebabFieldName_aB() {
        Field field = getField("aB");
        String columnName = ResultSetFieldToColumnNameCase.KEBAB.getColumnName(field);
        assertEquals("a-b", columnName);
    }

    @Test
    void snakeFieldName_simpleFieldName() {
        Field field = getField("simpleFieldName");
        String columnName = ResultSetFieldToColumnNameCase.SNAKE.getColumnName(field);
        assertEquals("simple_field_name", columnName);
    }

    @Test
    void snakeFieldName_a() {
        Field field = getField("a");
        String columnName = ResultSetFieldToColumnNameCase.SNAKE.getColumnName(field);
        assertEquals("a", columnName);
    }

    @Test
    void snakeFieldName_aB() {
        Field field = getField("aB");
        String columnName = ResultSetFieldToColumnNameCase.SNAKE.getColumnName(field);
        assertEquals("a_b", columnName);
    }

    @Test
    void kebabFieldName_a_B() {
        Field field = getField("a_B");
        String columnName = ResultSetFieldToColumnNameCase.KEBAB.getColumnName(field);
        assertEquals("a-b", columnName);
    }

    @Test
    void kebabFieldName_A_B() {
        Field field = getField("A_B");
        String columnName = ResultSetFieldToColumnNameCase.KEBAB.getColumnName(field);
        assertEquals("a-b", columnName);
    }

    @Test
    void camelFieldName_A_B() {
        Field field = getField("A_B");
        String columnName = ResultSetFieldToColumnNameCase.CAMEL.getColumnName(field);
        assertEquals("aB", columnName);
    }

    @Test
    void snakeFieldName_a_B() {
        Field field = getField("a_B");
        String columnName = ResultSetFieldToColumnNameCase.SNAKE.getColumnName(field);
        assertEquals("a_b", columnName);
    }

    @Test
    void snakeFieldName_A_B() {
        Field field = getField("A_B");
        String columnName = ResultSetFieldToColumnNameCase.SNAKE.getColumnName(field);
        assertEquals("a_b", columnName);
    }

    @Test
    void snakeFieldName_simple_fieldName() {
        Field field = getField("simple_fieldName");
        String columnName = ResultSetFieldToColumnNameCase.SNAKE.getColumnName(field);
        assertEquals("simple_field_name", columnName);
    }

    @Test
    void kebabFieldName_simple_fieldName() {
        Field field = getField("simple_fieldName");
        String columnName = ResultSetFieldToColumnNameCase.KEBAB.getColumnName(field);
        assertEquals("simple-field-name", columnName);
    }

    @Test
    void camelFieldName_simple_fieldName() {
        Field field = getField("simple_fieldName");
        String columnName = ResultSetFieldToColumnNameCase.CAMEL.getColumnName(field);
        assertEquals("simpleFieldName", columnName);
    }

    @Test
    void camelFieldName_simple_field_name() {
        Field field = getField("simple_field_name");
        String columnName = ResultSetFieldToColumnNameCase.CAMEL.getColumnName(field);
        assertEquals("simpleFieldName", columnName);
    }

    @Test
    void kebabFieldName_simple_field_name() {
        Field field = getField("simple_field_name");
        String columnName = ResultSetFieldToColumnNameCase.KEBAB.getColumnName(field);
        assertEquals("simple-field-name", columnName);
    }

    @Test
    void snakeFieldName_simple_field_name() {
        Field field = getField("simple_field_name");
        String columnName = ResultSetFieldToColumnNameCase.SNAKE.getColumnName(field);
        assertEquals("simple_field_name", columnName);
    }
}