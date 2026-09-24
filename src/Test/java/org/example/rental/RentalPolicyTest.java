package com.example.rental;

import com.example.rental.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RentalPolicyTest {

    private final RentalPolicy policy = new RentalPolicy(List.of(
        new TransitionRule(),
        new StopFactorRule()
    ));

    @Test
    void testInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> new RentalId(null));
        assertThrows(IllegalArgumentException.class, () -> new RentalId("   "));
    }

    @ParameterizedTest
    @CsvSource({
        "Booked, Rented, true",
        "Rented, Returned, true",
        "Booked, Returned, false",
        "Returned, Booked, false"
    })
    void testMove(String from, String to, boolean allowed) {
        RentalStatus statusFrom = parse(from);
        RentalStatus statusTo = parse(to);

        if (allowed) {
            RentalStatus result = policy.move(statusFrom, statusTo);
            assertNotNull(result);
        } else {
            assertThrows(IllegalStateException.class, () -> policy.move(statusFrom, statusTo));
        }
    }

    private RentalStatus parse(String name) {
        return switch (name) {
            case "Booked" -> new RentalStatus.Booked();
            case "Rented" -> new RentalStatus.Rented();
            case "Returned" -> new RentalStatus.Returned();
            default -> throw new IllegalArgumentException("Unknown status: " + name);
        };
    }
}
