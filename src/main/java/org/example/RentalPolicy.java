package org.example;

import java.util.Objects;

/**
 * Автокөлік жалдау күйлерінің өту ережелері.
 *
 * Рұқсат: Reserved -> Rented, Rented -> Returned
 * Тыйым:  Reserved -> Returned, Returned -> Rented
 */
public final class RentalPolicy {

    /**
     * Өтуге тырысады. Рұқсат етілсе — жаңа күйді қайтарады,
     * әйтпесе IllegalStateException лақтырады.
     */
    public RentalStatus move(RentalStatus from, RentalStatus to) {
        Objects.requireNonNull(from, "from null бола алмайды");
        Objects.requireNonNull(to, "to null бола алмайды");

        if (!isAllowed(from, to)) {
            throw new IllegalStateException(
                    "Тыйым салынған өту: " + from.label() + " -> " + to.label()
                            + " (" + reasonForRefusal(from, to) + ")");
        }
        return to;
    }

    /** Өту рұқсат па? */
    public boolean isAllowed(RentalStatus from, RentalStatus to) {
        if (from instanceof RentalStatus.Reserved) {
            return to instanceof RentalStatus.Rented;
        }
        if (from instanceof RentalStatus.Rented) {
            return to instanceof RentalStatus.Returned;
        }
        return false; // Returned — соңғы күй, одан ары өту жоқ
    }

    private String reasonForRefusal(RentalStatus from, RentalStatus to) {
        if (from instanceof RentalStatus.Reserved && to instanceof RentalStatus.Returned) {
            return "берілмеген көлікті қайтару мүмкін емес: акт жасалмаған, километраж алынбаған";
        }
        if (from instanceof RentalStatus.Returned && to instanceof RentalStatus.Rented) {
            return "аяқталған жалдауды қайта ашуға болмайды: жаңа брондау жасау керек";
        }
        return "мұндай өту ережеде жоқ";
    }
}
