package com.ssafy.yamyam_coach;

import com.ssafy.yamyam_coach.config.TestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
public abstract class IntegrationTestSupport {

    @ServiceConnection
    static final MySQLContainer<?> mysql;

    @ServiceConnection
    static final GenericContainer<?> redis;

    static {
        redis = new GenericContainer<>("redis/redis-stack-server:latest")
                .withExposedPorts(6379);

        redis.start();

        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", redis.getMappedPort(6379).toString());
    }

    static {
        mysql = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("yumyum_coach")
                .withUsername("root")
                .withPassword("1234")
                .withInitScript("schema.sql");

        mysql.start();
    }

}
