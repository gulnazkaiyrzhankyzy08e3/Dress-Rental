package org.example;

import java.util.Objects;

ello

public record RentalId(String value) {

    public RentalId {
        Objects.requireNonNull(value, "RentalId null бола алмайды");
        if (value.isBlank()) {
            throw new IllegalArgumentException("RentalId бос бола алмайды");
        }
        value = value.trim();
    }

    public static RentalId of(String value) {
        return new RentalId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
