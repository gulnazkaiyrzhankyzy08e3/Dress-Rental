package com.example.rental.domain;

public class TransitionRule implements RentalRule {
    @Override
    public void check(RentalStatus from, RentalStatus to) {
        if (from instanceof RentalStatus.Booked && to instanceof RentalStatus.Rented) return;
        if (from instanceof RentalStatus.Rented && to instanceof RentalStatus.Returned) return;

        throw new IllegalStateException("Бұлай ауыстыруға болмайды!");
    }
}
//Бұл класс — біздің жүйедегі «Өту ережесі» (Transition Rule). Оның негізгі міндеті — көйлектің статусы тек дұрыс ретпен өтуін қадағалау 
