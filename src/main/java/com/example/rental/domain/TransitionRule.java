package com.example.rental.domain;

public class StopFactorRule implements RentalRule {
    @Override
    public void check(RentalStatus from, RentalStatus to) {
        if (from instanceof RentalStatus.Booked && to instanceof RentalStatus.Returned) {
            throw new IllegalStateException("Стоп-фактор: Көйлек әлі берілген жоқ!");
        }
    }
}
