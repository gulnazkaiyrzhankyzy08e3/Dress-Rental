package com.example.rental.domain;

public sealed interface RentalStatus permits 
    RentalStatus.Booked, 
    RentalStatus.Rented, 
    RentalStatus.Returned {

    record Booked() implements RentalStatus {}
    record Rented() implements RentalStatus {}
    record Returned() implements RentalStatus {}
}
