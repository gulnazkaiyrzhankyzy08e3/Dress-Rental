
package com.example.rental.handler;

public record ApiError(
        String code,
        String message,
        String correlationId
) {}
