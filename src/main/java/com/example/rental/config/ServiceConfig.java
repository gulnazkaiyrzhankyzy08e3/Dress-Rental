package com.example.rental.config;

import com.example.rental.domain.RentalRule;
import com.example.rental.domain.TransitionRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public RentalRule rentalRule() {
        return new TransitionRule();
    }
}
