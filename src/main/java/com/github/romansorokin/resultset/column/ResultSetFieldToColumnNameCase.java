package com.github.romansorokin.resultset.column;

import java.lang.reflect.Field;

public enum ResultSetFieldToColumnNameCase implements ResultSetFieldToColumnNameFunction {
    FIELD_NAME(Field::getName),
    /**
     * default for java
     * example: simpleFieldName
     */
    CAMEL(f -> {
        String name = f.getName();
        char[] chars = name.toCharArray();
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toLowerCase(chars[0]));
        boolean nextUp = false;
        for (int i = 1; i < chars.length; i++) {
            char ch = chars[i];
            if (nextUp) {
                nextUp = false;
                sb.append(Character.toUpperCase(ch));
                continue;
            }
            if (ch == '_') {
                nextUp = true;
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    }),
    /**
     * When using snake case, all letters need to be lowercase.
     * example: simple_field_name
     */
    SNAKE(f -> {
        char[] chars = f.getName().toCharArray();
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toLowerCase(chars[0]));
        for (int i = 1; i < chars.length; i++) {
            char ch = chars[i];
            if (Character.isLowerCase(ch) || ch == '_') {
                sb.append(ch);
                continue;
            }
            if (chars[i - 1] != '_')
                sb.append("_");
            sb.append(Character.toLowerCase(ch));
        }
        return sb.toString();
    }),
    /**
     * When using kebab case, all letters need to be lowercase.
     * example: simple-field-name
     */
    KEBAB(f -> {
        char[] chars = f.getName().replace("_", "-").toCharArray();
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toLowerCase(chars[0]));
        for (int i = 1; i < chars.length; i++) {
            char ch = chars[i];
            if (Character.isLowerCase(ch) || ch == '-') {
                sb.append(ch);
                continue;
            }
            if (chars[i - 1] != '-')
                sb.append("-");
            sb.append(Character.toLowerCase(ch));
        }
        return sb.toString();
    }),
    /**
     * example: SimpleFieldName
     */
    PASCAL(f -> {
        String name = f.getName();
        char[] chars = name.toCharArray();
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(chars[0]));
        boolean nextUp = false;
        for (int i = 1; i < chars.length; i++) {
            char ch = chars[i];
            if (nextUp) {
                nextUp = false;
                sb.append(Character.toUpperCase(ch));
                continue;
            }
            if (ch == '_') {
                nextUp = true;
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    });

    private final ResultSetFieldToColumnNameFunction func;

    ResultSetFieldToColumnNameCase(ResultSetFieldToColumnNameFunction func) {
        this.func = func;
    }

    @Override
    public String getColumnName(Field field) {
        return func.getColumnName(field);
    }
}
