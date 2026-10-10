
package org.example.rental;

import com.example.rental.config.Application;
import com.example.rental.config.RentalService;
import com.example.rental.domain.DuplicateRental;
import com.example.rental.persistence.RentalJdbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
class RentalJdbcTest {

    @Autowired
    private RentalService service;

    @Autowired
    private RentalJdbc repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void rollbackLeavesZeroRows() {
        String key = UUID.randomUUID().toString();

        assertThrows(DuplicateRental.class,
                () -> service.createTwoRentals(key, "Red dress"));

        assertEquals(0, repository.count(key));
    }

    @Test
    void duplicateRequestLeavesOneRow() {
        String key = UUID.randomUUID().toString();

        service.createRental(key, "Blue dress");

        assertThrows(DuplicateRental.class,
                () -> service.createRental(key, "Blue dress"));

        assertEquals(1, repository.count(key));
    }
}
