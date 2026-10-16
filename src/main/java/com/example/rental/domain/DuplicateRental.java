
package com.example.rental.domain;

public class DuplicateRental extends RuntimeException {

    public DuplicateRental(String businessKey) {
        super("Duplicate rental: " + businessKey);
    }
}
