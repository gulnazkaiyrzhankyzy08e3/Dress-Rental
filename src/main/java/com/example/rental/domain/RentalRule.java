package com.example.rental.domain;

public interface RentalRule {
    void check(RentalStatus from, RentalStatus to);
}
