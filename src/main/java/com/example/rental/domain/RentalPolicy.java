package com.example.rental;

public class RentalPolicy {

    public RentalStatus move(RentalStatus from, RentalStatus to) {
        // 1. Рұқсат етілген ауысу: Брондалды -> Жалға берілді (Booked -> Rented)
        if (from instanceof RentalStatus.Booked && to instanceof RentalStatus.Rented) {
            return to;
        }

        // 2. Рұқсат етілген ауысу: Жалға берілді -> Қайтарылды (Rented -> Returned)
        if (from instanceof RentalStatus.Rented && to instanceof RentalStatus.Returned) {
            return to;
        }

        // Қалған ауысулардың барлығына тыйым салынады
        throw new IllegalStateException("Forbidden status change: " + from + " -> " + to);
    }
}
