package com.example.rental.domain;

public record RentalId(String value) {
    public RentalId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ID бос болмауы керек!");
        }
    }
}
