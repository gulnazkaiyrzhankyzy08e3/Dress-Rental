
package com.example.rental.config;

import com.example.rental.domain.*;
import com.example.rental.persistence.RentalJpa;
import com.example.rental.persistence.RentalMapper;
import com.example.rental.persistence.RentalEntity;

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
    public RentalView createRental(String businessKey, String title) {
        UUID id = UUID.randomUUID();

        try {
            RentalEntity entity = RentalMapper.toEntity(
                    id, businessKey,
                    new RentalStatus.Booked(), title);

            repository.insert(entity);
            repository.flush();

            return new RentalView(
                    id, businessKey, "BOOKED", title);

        } catch (DataIntegrityViolationException e) {
            throw new DuplicateRental(businessKey);
        }
    }

    @Transactional(readOnly = true)
    public RentalView require(UUID id) {
        RentalEntity entity = repository.findById(id)
                .orElseThrow(MissingRental::new);

        return new RentalView(
                entity.getId(),
                entity.getBusinessKey(),
                entity.getStatus(),
                entity.getTitle()
        );
    }

    @Transactional
    public void createTwoRentals(String businessKey, String title) {
        createRental(businessKey, title);
        createRental(businessKey, title);
    }
}
