
package com.example.rental.handler;

import com.example.rental.domain.RentalView;
import java.util.UUID;

public record RentalResponse(
        UUID id,
        String businessKey,
        String status,
        String title
) {
    public static RentalResponse from(RentalView view) {
        return new RentalResponse(
                view.id(),
                view.businessKey(),
                view.status(),
                view.title()
        );
    }
}
