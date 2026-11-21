package com.example.rental.config;

import com.example.rental.domain.*;
import com.example.rental.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RentalService {

    private final RentalJpa rentals;

    private final RentalPolicy policy = new RentalPolicy(
            List.of(new TransitionRule(), new StopFactorRule())
    );

    public RentalService(RentalJpa rentals) {
        this.rentals = rentals;
    }

    @Transactional
    public RentalView createRental(String key, String title) {

        if (rentals.count(key) > 0) {
            throw new DuplicateRental(key);
        }

        var row = RentalMapper.toEntity(
                UUID.randomUUID(),
                key,
                new RentalStatus.Booked(),
                title
        );

        rentals.insert(row);
        rentals.flush();

        return view(row);
    }

    @Transactional
    public void createTwoRentals(String key, String title) {
        createRental(key, title);
        createRental(key, title);
    }

    @Transactional
    public RentalView require(UUID id) {

        return view(
                rentals.findById(id)
                        .orElseThrow(MissingRental::new)
        );
    }

    // STAFF AND MANAGER
    // BOOKED -> RENTED

    @Transactional
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public RentalView start(UUID id) {

        var row = rentals.findById(id)
                .orElseThrow(MissingRental::new);

        RentalStatus current = switch (row.getStatus()) {
            case "BOOKED" -> new RentalStatus.Booked();
            case "RENTED" -> new RentalStatus.Rented();
            case "RETURNED" -> new RentalStatus.Returned();
            default -> throw new IllegalStateException(
                    "Unknown rental status"
            );
        };

        try {
            policy.move(current, new RentalStatus.Rented());
        } catch (IllegalStateException e) {
            throw new InvalidRentalTransition(e.getMessage());
        }

        row.setStatus("RENTED");

        return view(row);
    }

    // MANAGER ONLY
    // RENTED -> RETURNED

    @Transactional
    @PreAuthorize("hasRole('MANAGER')")
    public RentalView complete(UUID id, String actor) {

        var row = rentals.findById(id)
                .orElseThrow(MissingRental::new);

        RentalStatus current = switch (row.getStatus()) {
            case "BOOKED" -> new RentalStatus.Booked();
            case "RENTED" -> new RentalStatus.Rented();
            case "RETURNED" -> new RentalStatus.Returned();
            default -> throw new IllegalStateException(
                    "Unknown rental status"
            );
        };

        try {
            policy.move(current, new RentalStatus.Returned());
        } catch (IllegalStateException e) {
            throw new InvalidRentalTransition(e.getMessage());
        }

        row.setStatus("RETURNED");

        org.slf4j.LoggerFactory
                .getLogger(RentalService.class)
                .info("Rental {} completed by {}", id, actor);

        return view(row);
    }

    private RentalView view(RentalEntity row) {

        return new RentalView(
                row.getId(),
                row.getBusinessKey(),
                row.getStatus(),
                row.getTitle()
        );
    }
}