package com.merge.final_project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.sql.DataSource;

import java.sql.Connection;

@SpringBootTest
public class DBConnectionTest {
    @Autowired
    DataSource dataSource;

    @Test
    void testConnection() throws Exception {
        Connection conn = dataSource.getConnection();
        System.out.println("DB 연결 성공: " + conn);
        conn.close();
    }
}