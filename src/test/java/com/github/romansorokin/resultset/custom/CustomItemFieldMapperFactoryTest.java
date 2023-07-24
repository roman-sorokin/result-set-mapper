package com.github.romansorokin.resultset.custom;

import com.github.romansorokin.resultset.BaseTest;
import com.github.romansorokin.resultset.ResultSetMapperUtils;
import com.github.romansorokin.resultset.annotations.ResultSetField;
import com.github.romansorokin.resultset.annotations.ResultSetType;
import com.github.romansorokin.resultset.mapper.ResultSetMapper;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class CustomItemFieldMapperFactoryTest extends BaseTest {
    @Test
    void getMapper_customResultSetMapperFactory() throws SQLException {
        @ToString
        @ResultSetType(ignoreCase = true, mapAllFields = true)
        class CustomEntity {
            UUID id;
            @ResultSetField(factory = CustomItemFieldMapperFactory.class)
            CustomItem item;
        }

        execute("drop table if exists test_entity");
        execute("create table if not exists test_entity (id uuid, item varchar)");
        UUID id = UUID.randomUUID();
        execute("insert into test_entity (id, item) values (?, '{\"name\":\"name-1\"}')", id);
        ResultSetMapper<CustomEntity> entityMapper = ResultSetMapperUtils.getMapper(CustomEntity.class, CustomEntity::new);
        // test
        CustomEntity entity = executeQuery("select * from test_entity where id = ? ", entityMapper, id).orElseThrow();
        log.info("entity: {}", entity);
        assertEquals("name-1", entity.item.name);
    }

    @Test
    void getMapper_registerCustomFieldType() throws SQLException {
        @ToString
        @ResultSetType(ignoreCase = true, mapAllFields = true)
        class CustomEntity {
            UUID id;
            CustomItem item;
        }
        ResultSetMapperUtils.registerFieldType(CustomItem.class, (rs, i) -> {
            String value = rs.getString(i)
                    .replace("{\"name\":\"", "")
                    .replace("\"}", "");
            CustomItem ci = new CustomItem();
            ci.name = value;
            return ci;
        });

        execute("drop table if exists test_entity");
        execute("create table if not exists test_entity (id uuid, item varchar)");
        UUID id = UUID.randomUUID();
        execute("insert into test_entity (id, item) values (?, '{\"name\":\"name-1\"}')", id);
        ResultSetMapper<CustomEntity> entityMapper = ResultSetMapperUtils.getMapper(CustomEntity.class, CustomEntity::new);
        // test

        CustomEntity entity = executeQuery("select * from test_entity where id = ? ", entityMapper, id).orElseThrow();
        log.info("entity: {}", entity);
        assertEquals("name-1", entity.item.name);
    }
}
