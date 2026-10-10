
package com.example.rental.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public class RentalJdbc {

    private final JdbcTemplate jdbc;

    public RentalJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(UUID id, String businessKey,
                       String status, String title) {

        jdbc.update("""
            INSERT INTO rental
            (id, business_key, status, title)
            VALUES (?, ?, ?, ?)
            """, id, businessKey, status, title);
    }

    public int count(String businessKey) {

        Integer result = jdbc.queryForObject(
                "SELECT count(*) FROM rental WHERE business_key = ?",
                Integer.class,
                businessKey
        );

        return result == null ? 0 : result;
    }
}
