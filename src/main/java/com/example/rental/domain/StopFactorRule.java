package com.example.rental.domain;

public class StopFactorRule implements RentalRule {
    private final boolean isDamaged;

    public StopFactorRule() {
        this(false);
    }

    public StopFactorRule(boolean isDamaged) {
        this.isDamaged = isDamaged;
    }

    @Override
    public void check(RentalStatus from, RentalStatus to) {
        // Егер көйлек бүлінген болса, оны қайта жалға беруді бұғаттайды
        if (isDamaged && to instanceof RentalStatus.Rented) {
            throw new IllegalStateException("Cannot rent dress: item is marked as damaged");
        }
    }
}
