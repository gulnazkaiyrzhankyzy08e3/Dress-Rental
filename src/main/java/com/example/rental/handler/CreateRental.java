
package com.example.rental.handler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRental(
        @NotBlank(message = "must not be blank")
        String businessKey,

        @NotBlank(message = "must not be blank")
        @Size(max = 120)
        String title
) {}
