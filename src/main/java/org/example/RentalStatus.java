package org.example;

/**
 * Автокөлік жалдаудың статусы. Бұл String ЕМЕС — sealed тип.
 * Сондықтан жоқ статусты жазу мүмкін емес.
 */
public sealed interface RentalStatus
        permits RentalStatus.Reserved, RentalStatus.Rented, RentalStatus.Returned {

    /** Көлік клиентке брондалған, әлі берілмеген. */
    record Reserved() implements RentalStatus { }

    /** Көлік клиентке берілді — қазір жолда. */
    record Rented() implements RentalStatus { }

    /** Көлік автопаркке қайтарылды, жалдау аяқталды. */
    record Returned() implements RentalStatus { }

    RentalStatus RESERVED = new Reserved();
    RentalStatus RENTED = new Rented();
    RentalStatus RETURNED = new Returned();

    /** Статустың оқуға ыңғайлы аты. */
    default String label() {
        if (this instanceof Reserved) return "Reserved";
        if (this instanceof Rented) return "Rented";
        return "Returned";
    }

    /** Мәтіннен статусқа айналдыру (тесттер үшін). */
    static RentalStatus of(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Статус аты бос бола алмайды");
        }
        return switch (name.trim()) {
            case "Reserved" -> RESERVED;
            case "Rented" -> RENTED;
            case "Returned" -> RETURNED;
            default -> throw new IllegalArgumentException("Белгісіз статус: " + name);
        };
    }
}
