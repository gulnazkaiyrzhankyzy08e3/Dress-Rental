package com.example.rental.persistence;

import com.example.rental.domain.RentalStatus;
import java.util.UUID;

public final class RentalMapper {

    private RentalMapper() {}

    public static RentalEntity toEntity(UUID id, String businessKey,
                                        RentalStatus status, String title) {
        var row = new RentalEntity();
        row.setId(id);
        row.setBusinessKey(businessKey);
        row.setTitle(title);
        row.setStatus(switch (status) {
            case RentalStatus.Booked b   -> "BOOKED";
            case RentalStatus.Rented r   -> "RENTED";
            case RentalStatus.Returned r -> "RETURNED";
        });
        return row;
    }
}