package com.example.rental.config;

import com.example.rental.domain.*;
import org.springframework.stereotype.Service;

@Service
public class RentalService {
    private final RentalPolicy policy;

    public RentalService(RentalPolicy policy) {
        this.policy = policy;
    }

    public RentalStatus move(RentalStatus from, RentalStatus to) {
        return policy.move(from, to);
    }
}
