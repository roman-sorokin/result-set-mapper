package com.github.romansorokin.resultset;

import com.github.romansorokin.resultset.annotations.ResultSetField;
import com.github.romansorokin.resultset.annotations.ResultSetType;
import com.github.romansorokin.resultset.column.ResultSetFieldToColumnNameCase;
import com.github.romansorokin.resultset.mapper.ResultSetMapper;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class ResultSetMapperUtilsTest extends BaseTest {
    @Test
    void getMapper_resultSetAnnotationsNotFound() {
        class TestEntity {
            UUID id;
            String name;
        }
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            ResultSetMapperUtils.getMapper(TestEntity.class, TestEntity::new);
        });
        assertTrue(e.getMessage().startsWith("result-set annotations not found:"));
    }

    @Test
    void getMapper_ignoreCaseTrue() throws SQLException {
        @ToString
        @ResultSetType(ignoreCase = true)
        class TestEntity {
            @ResultSetField
            UUID id;
            @ResultSetField
            String name;
        }
        ResultSetMapper<TestEntity> mapper = ResultSetMapperUtils.getMapper(TestEntity.class, TestEntity::new);
        execute("drop table if exists test_entity");
        execute("create table if not exists test_entity (id uuid, name varchar)");

        UUID id = UUID.randomUUID();
        execute("insert into test_entity (id, name) values (?, ?)", id, "test-1");
        TestEntity entity = executeQuery("select * from test_entity where id = ? ", mapper, id).orElseThrow();
        log.info("entity: {}", entity);
        assertEquals(id, entity.id);
        assertEquals("test-1", entity.name);
    }

    @Test
    void getMapper_mapAllFields() throws SQLException {
        @ToString
        @ResultSetType(ignoreCase = true, mapAllFields = true)
        class TestEntity {
            UUID id;
            String name;
        }
        ResultSetMapper<TestEntity> mapper = ResultSetMapperUtils.getMapper(TestEntity.class, TestEntity::new);
        execute("drop table if exists test_entity");
        execute("create table if not exists test_entity (id uuid, name varchar)");

        UUID id = UUID.randomUUID();
        execute("insert into test_entity (id, name) values (?, ?)", id, "test-1");
        TestEntity entity = executeQuery("select * from test_entity where id = ? ", mapper, id).orElseThrow();
        log.info("entity: {}", entity);
        assertEquals(id, entity.id);
        assertEquals("test-1", entity.name);
    }

    @Test
    void getMapper_whenIgnoreCaseFalseAllFieldsIsNull() throws SQLException {
        @ToString
        @ResultSetType(mapAllFields = true)
        class TestEntity {
            UUID id;
            String name;
        }
        ResultSetMapper<TestEntity> mapper = ResultSetMapperUtils.getMapper(TestEntity.class, TestEntity::new);
        execute("drop table if exists test_entity");
        execute("create table if not exists test_entity (id uuid, name varchar)");

        UUID id = UUID.randomUUID();
        execute("insert into test_entity (id, name) values (?, ?)", id, "test-1");
        TestEntity entity = executeQuery("select * from test_entity where id = ? ", mapper, id).orElseThrow();
        log.info("entity: {}", entity);
        assertNull(entity.id);
        assertNull(entity.name);
    }

    @Test
    void getMapper_superClassFieldsMapped() throws SQLException {
        enum TestType {TYPE1, TYPE2}
        @ResultSetType(ignoreCase = true, mapAllFields = true)
        abstract class ParentEntity {
            UUID id;
            TestType type;
        }
        @ToString
        class TestEntity1 extends ParentEntity {
            String name1;
        }
        @ToString
        class TestEntity2 extends ParentEntity {
            String name2;
        }
        execute("drop table if exists test_entity");
        execute("create table if not exists test_entity (id uuid, type varchar, name1 varchar, name2 varchar)");
        UUID id1 = UUID.randomUUID();
        execute("insert into test_entity (id, type, name1, name2) values (?, 'TYPE1', 'name-1', 'not-mapped')", id1);
        UUID id2 = UUID.randomUUID();
        execute("insert into test_entity (id, type, name1, name2) values (?, 'TYPE2', 'not-mapped', 'name-2')", id2);
        ResultSetMapper<TestEntity1> mapper1 = ResultSetMapperUtils.getMapper(TestEntity1.class, TestEntity1::new);
        ResultSetMapper<TestEntity2> mapper2 = ResultSetMapperUtils.getMapper(TestEntity2.class, TestEntity2::new);
        // test
        TestEntity1 entity1 = executeQuery("select * from test_entity where id = ? ", mapper1, id1).orElseThrow();
        log.info("entity1: {}", entity1);
        TestEntity2 entity2 = executeQuery("select * from test_entity where id = ? ", mapper2, id2).orElseThrow();
        log.info("entity2: {}", entity2);
        assertEquals(TestType.TYPE1, entity1.type);
        assertEquals(id1, entity1.id);
        assertEquals("name-1", entity1.name1);
        assertEquals(TestType.TYPE2, entity2.type);
        assertEquals(id2, entity2.id);
        assertEquals("name-2", entity2.name2);
    }

    @Test
    void camelCaseToKebabCase() throws SQLException {
        @ToString
        @ResultSetType(mapAllFields = true, ignoreCase = true, naming = ResultSetFieldToColumnNameCase.KEBAB)
        class TestEntity {
            UUID entityId;
            String entityName;
        }
        ResultSetMapper<TestEntity> mapper = ResultSetMapperUtils.getMapper(TestEntity.class, TestEntity::new);
        execute("drop table if exists test_entity");
        execute("create table if not exists test_entity (`entity-id` uuid, `entity-name` varchar)");

        UUID id = UUID.randomUUID();
        execute("insert into test_entity (`entity-id`, `entity-name`) values (?, ?)", id, "test-1");
        TestEntity entity = executeQuery("select * from test_entity where `entity-id` = ? ", mapper, id).orElseThrow();
        log.info("entity: {}", entity);
        assertEquals(id, entity.entityId);
        assertEquals("test-1", entity.entityName);
    }

    @Test
    void camelCaseToSnakeCase() throws SQLException {
        @ToString
        @ResultSetType(mapAllFields = true, ignoreCase = true, naming = ResultSetFieldToColumnNameCase.SNAKE)
        class TestEntity {
            UUID entityId;
            String entityName;
        }
        ResultSetMapper<TestEntity> mapper = ResultSetMapperUtils.getMapper(TestEntity.class, TestEntity::new);
        execute("drop table if exists test_entity");
        execute("create table if not exists test_entity (entity_id uuid, entity_name varchar)");

        UUID id = UUID.randomUUID();
        execute("insert into test_entity (entity_id, entity_name) values (?, ?)", id, "test-1");
        TestEntity entity = executeQuery("select * from test_entity where entity_id = ? ", mapper, id).orElseThrow();
        log.info("entity: {}", entity);
        assertEquals(id, entity.entityId);
        assertEquals("test-1", entity.entityName);
    }
}