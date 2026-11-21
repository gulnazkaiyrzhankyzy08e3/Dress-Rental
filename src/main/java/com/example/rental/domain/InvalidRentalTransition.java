package com.example.rental.domain;
public class InvalidRentalTransition extends RuntimeException {
    public InvalidRentalTransition(String message) { super(message); }
}