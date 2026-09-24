package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class RentalPolicyTest {

    private final RentalPolicy policy = new RentalPolicy();

    // README кестесінің РҰҚСАТ ЕТІЛГЕН жолдары
    @ParameterizedTest(name = "рұқсат: {0} -> {1}")
    @CsvSource({
            "Reserved, Rented",
            "Rented,   Returned"
    })
    void allowedTransitions(String from, String to) {
        RentalStatus result = policy.move(RentalStatus.of(from), RentalStatus.of(to));
        assertEquals(to, result.label());
    }

    // README кестесінің ТЫЙЫМ САЛЫНҒАН жолдары
    @ParameterizedTest(name = "тыйым: {0} -> {1}")
    @CsvSource({
            "Reserved, Returned",
            "Returned, Rented"
    })    void forbiddenTransitions(String from, String to) {
        RentalStatus f = RentalStatus.of(from);
        RentalStatus t = RentalStatus.of(to);
        assertThrows(IllegalStateException.class, () -> policy.move(f, t));
    }

    @Test
    @DisplayName("RentalId null бола алмайды")
    void idRejectsNull() {
        assertThrows(NullPointerException.class, () -> RentalId.of(null));
    }

    @Test
    @DisplayName("RentalId бос бола алмайды")
    void idRejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> RentalId.of("   "));
    }
