package com.merge.final_project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FinalProjectApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
    }

    @Test
    void envTest() {
        System.out.println("DB_URL = " + System.getenv("DB_URL"));
    }

    @Test
    void dbConnectionTest() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("DB 연결 성공: " + conn.getMetaData().getURL());
            assertThat(conn).isNotNull();
        }
    }
}