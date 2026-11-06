
package com.example.rental.handler;

import com.example.rental.config.RentalService;
import com.example.rental.domain.RentalView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rentals")
public class RentalController {

    private final RentalService service;

    public RentalController(RentalService service) {
        this.service = service;
    }

    @Operation(summary = "Create a clothing rental")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Rental created"),
            @ApiResponse(responseCode = "400",
                    description = "Validation failed"),
            @ApiResponse(responseCode = "409",
                    description = "Duplicate business key")
    })
    @PostMapping
    public ResponseEntity<RentalResponse> create(
            @Valid @RequestBody CreateRental body) {

        RentalView created = service.createRental(
                body.businessKey(), body.title());

        URI location = URI.create(
                "/api/v1/rentals/" + created.id());

        return ResponseEntity.created(location)
                .body(RentalResponse.from(created));
    }

    @Operation(summary = "Get rental by UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Rental found"),
            @ApiResponse(responseCode = "404",
                    description = "Rental not found")
    })
    @GetMapping("/{id}")
    public RentalResponse get(@PathVariable UUID id) {
        return RentalResponse.from(service.require(id));
    }
}
