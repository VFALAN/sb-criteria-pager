package com.vf.sb_criteria_pager.pager;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public abstract class TestConfiguration {
    @Container
    static MySQLContainer<?> mySql = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("pager")
            .withInitScript("script.sql")
            .withUsername("testUser")
            .withPassword("testPass");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySql::getJdbcUrl);
        registry.add("spring.datasource.username", mySql::getUsername);
        registry.add("spring.datasource.password", mySql::getPassword);
        registry.add("spring.datasource.driver-class-name", mySql::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.generate-ddl", () -> false);
        registry.add("spring.jpa.show-sql", () -> true);
    }


}