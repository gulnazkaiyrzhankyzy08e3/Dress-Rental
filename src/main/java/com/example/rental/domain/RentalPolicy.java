package com.example.rental.domain;

public class RentalPolicy {
    public RentalStatus move(RentalStatus from, RentalStatus to) {
        if (from instanceof RentalStatus.Booked && to instanceof RentalStatus.Rented) {
            return to;
        }
        if (from instanceof RentalStatus.Rented && to instanceof RentalStatus.Returned) {
            return to;
        }
        throw new IllegalStateException("Forbidden status change: " + from + " -> " + to);
    }
}
