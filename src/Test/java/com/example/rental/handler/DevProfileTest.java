package com.example.rental.handler;


import com.example.rental.config.Application;
import com.example.rental.config.DownstreamProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("dev")
class DevProfileTest {

    @Autowired
    private DownstreamProperties downstream;

    @Test
    void downstreamConfigurationIsLoaded() {

        assertEquals(
                URI.create("http://localhost:9090"),
                downstream.baseUrl()
        );

        assertEquals(
                Duration.ofSeconds(2),
                downstream.timeout()
        );
    }
}
