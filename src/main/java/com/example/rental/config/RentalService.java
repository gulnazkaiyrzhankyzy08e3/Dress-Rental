package com.example.rental.config;

import com.example.rental.domain.*;
import com.example.rental.persistence.RentalJpa;
import com.example.rental.persistence.RentalMapper;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RentalService {

    private final RentalRule rules;
    private final RentalJpa repository;

    public RentalService(RentalRule rules, RentalJpa repository) {
        this.rules = rules;
        this.repository = repository;
    }

    public RentalStatus move(RentalStatus from, RentalStatus to) {
        rules.check(from, to);
        return to;
    }

    @Transactional
    public void createRental(String businessKey, String title) {
        try {
            repository.insert(RentalMapper.toEntity(
                    UUID.randomUUID(), businessKey,
                    new RentalStatus.Booked(), title));
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateRental(businessKey);
        }
    }

    @Transactional
    public void createTwoRentals(String businessKey, String title) {
        createRental(businessKey, title);
        createRental(businessKey, title);
    }
}