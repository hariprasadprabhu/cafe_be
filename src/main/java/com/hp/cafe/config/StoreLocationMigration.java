package com.hp.cafe.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class StoreLocationMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public StoreLocationMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE orders ALTER COLUMN store_location SET DEFAULT 'Manipal'");
        jdbcTemplate.update("UPDATE orders SET store_location = 'Manipal' WHERE store_location IS NULL OR BTRIM(store_location) = ''");
        jdbcTemplate.execute("ALTER TABLE orders ALTER COLUMN store_location SET NOT NULL");
    }
}
