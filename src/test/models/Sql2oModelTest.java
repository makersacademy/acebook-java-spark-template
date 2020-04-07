package models;

import org.apache.log4j.BasicConfigurator;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.converters.UUIDConverter;
import org.sql2o.quirks.PostgresQuirks;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.postgresql.jdbc2.EscapedFunctions.NOW;

class Sql2oModelTest {

    Sql2o sql2o = new Sql2o("jdbc:postgresql://localhost:5432/" + "acebook-test",
            null, null, new PostgresQuirks() {
        {
            // make sure we use default UUID converter.
            converters.put(UUID.class, new UUIDConverter());
        }
    });

    UUID id = UUID.fromString("49921d6e-e210-4f68-ad7a-afac266278cb");
    UUID id2 = UUID.fromString("59921d6e-e210-4f68-ad7a-afac266278cb");
    Timestamp ts = new Timestamp(120,3,7,6,45,20,0);

    @BeforeAll
    static void setUpClass() {
        BasicConfigurator.configure();
        Flyway flyway = Flyway.configure().dataSource("jdbc:postgresql://localhost:5432/acebook-test", null, null).load();
        flyway.migrate();

    }
    @BeforeEach
    void setUp() {
        Connection conn = sql2o.beginTransaction();
        conn.createQuery("insert into posts(post_id, title, content, post_date) VALUES (:post_id, :title, :content, :post_date)")
                .addParameter("post_id", id)
                .addParameter("title", "example title")
                .addParameter("content", "example content")
                .addParameter("post_date",ts)
                .executeUpdate();

        conn.commit();
    }

    @AfterEach
    void tearDown() {
        Connection conn = sql2o.beginTransaction();
        conn.createQuery("TRUNCATE TABLE posts")
                .executeUpdate();
        conn.commit();
    }

    @Test
    void createPost() {
        Model model = new Sql2oModel(sql2o);
        model.createPost("Holiday", "Had such a great time", new Timestamp(120,3,7,6,45,20,0));
        assertEquals(model.getAllPosts().size(), 2);
    }

    @Test
    void getAllPosts() {
        Model model = new Sql2oModel(sql2o);
        List<Post> items = new ArrayList<Post>();
        items.add(new Post(id, "example title", "example content",ts));
        assertEquals(model.getAllPosts(), items);
    }
}