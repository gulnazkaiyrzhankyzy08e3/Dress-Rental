

package com.example.rental.domain;

import java.util.UUID;

public record RentalView(
        UUID id,
        String businessKey,
        String status,
        String title
) {}

