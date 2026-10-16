
package com.example.rental.config;

import com.example.rental.domain.*;
import com.example.rental.persistence.RentalJdbc;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RentalService {

    private final RentalRule rules;
    private final RentalJdbc repository;

    public RentalService(
            RentalRule rules,
            RentalJdbc repository
    ) {
        this.rules = rules;
        this.repository = repository;
    }

    public RentalStatus move(
            RentalStatus from,
            RentalStatus to
    ) {
        rules.check(from, to);
        return to;
    }

    @Transactional
    public void createRental(
            String businessKey,
            String title
    ) {
        try {
            repository.insert(
                    UUID.randomUUID(),
                    businessKey,
                    "BOOKED",
                    title
            );
        } catch (DuplicateKeyException e) {
            throw new DuplicateRental(businessKey);
        }
    }

    @Transactional
    public void createTwoRentals(
            String businessKey,
            String title
    ) {
        createRental(businessKey, title);
        createRental(businessKey, title);
    }
}
