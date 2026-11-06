
package com.example.rental.domain;

public class MissingRental extends RuntimeException {

    public MissingRental() {
        super("rental not found");
    }
}
