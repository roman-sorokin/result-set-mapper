package com.github.romansorokin.sqliteutils;

import com.github.romansorokin.sqliteutils.annotations.Sqlite;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class SqliteUtilsTest extends BaseTest {
    @Test
    void getMapper_executeQuery_set() {
        SqliteMapper<SimpleEntity> mapper = SqliteUtils.getMapper(SimpleEntity.class, SimpleEntity::new);
        execute("drop table if exists simple_entity");
        execute("create table if not exists simple_entity ( id text, remote_id integer, primitive integer, amount numeric )");
        execute("insert into simple_entity (id, remote_id, primitive, amount) values ('1', 2, 3, 4.5)");
        execute("insert into simple_entity (id, remote_id, primitive, amount) values ('2', 2, 3, 4.5)");
        execute("insert into simple_entity (id, remote_id, primitive, amount) values ('3', 2, 3, 4.5)");
        execute("insert into simple_entity (id, remote_id, primitive, amount) values ('4', 2, 3, 4.5)");
        execute("insert into simple_entity (id, remote_id, primitive, amount) values ('5', 2, 3, 4.5)");

        Set<SimpleEntity> entities = executeQuery("select * from simple_entity", rs -> SqliteUtils.hashSet(rs, mapper));
        assertEquals(5, entities.size());
        for (SimpleEntity se : entities) {
            log.info("simple_entity: {}", se);
            assertEquals(2L, se.getRemoteId());
            assertEquals(3, se.getPrimitive());
            assertEquals(new BigDecimal("4.5"), se.getAmount());
        }
    }

    @Test
    void getMapper_executeQuery_one() {
        SqliteMapper<SimpleEntity> mapper = SqliteUtils.getMapper(SimpleEntity.class, SimpleEntity::new);
        execute("drop table if exists simple_entity");
        execute("create table if not exists simple_entity ( id text, remote_id integer, primitive integer, amount numeric )");
        execute("insert into simple_entity (id, remote_id, primitive, amount) values ('1', 2, 3, 4.5)");
        SimpleEntity se = executeQuery("select * from simple_entity", mapper);
        log.info("simple_entity: {}", se);
        assertEquals("1", se.getId());
        assertEquals(2L, se.getRemoteId());
        assertEquals(3, se.getPrimitive());
        assertEquals(new BigDecimal("4.5"), se.getAmount());
    }

    @Test
    void getMapper() {
        SqliteMapper<SimpleEntity> mapper = SqliteUtils.getMapper(SimpleEntity.class, SimpleEntity::new);
        assertNotNull(mapper);
    }

    @Test
    void reflection() {
        Field[] fields = SimpleEntity.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Annotation[] annotations = field.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                log.info("annotation is sqlite: {}", annotation.annotationType());
            }
        }
    }

    @Test
    void hasSqliteAnnotation() {
        Field[] fields = SimpleEntity.class.getDeclaredFields();
        Field id = Arrays.stream(fields).filter(f -> f.getName().equals("id")).findAny().orElse(null);
        assert id != null;
        Annotation annotation = id.getDeclaredAnnotations()[0];
        assertTrue(SqliteUtils.hasSqliteAnnotation(annotation));
    }

    @Test
    void isJavaLangAnnotation() {
        Annotation[] annotations = Sqlite.class.getDeclaredAnnotations();
        Annotation retention = Arrays.stream(annotations).filter(a -> a.annotationType().getName().equals("java.lang.annotation.Retention")).findAny().orElse(null);
        assertTrue(SqliteUtils.isJavaLangAnnotation(retention));
    }
}