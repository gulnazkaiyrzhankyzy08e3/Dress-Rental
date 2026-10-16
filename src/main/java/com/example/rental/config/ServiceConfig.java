package com.example.rental.config;

import com.example.rental.domain.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configurationpublic class ServiceConfig {

    @Bean
    public RentalPolicy rentalPolicy() {
        return new RentalPolicy(List.of(
            new TransitionRule(),
            new StopFactorRule()
        ));
    }
}
