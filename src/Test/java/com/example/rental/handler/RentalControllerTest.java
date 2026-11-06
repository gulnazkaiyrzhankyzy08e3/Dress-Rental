
package com.example.rental.handler;

import com.example.rental.config.RentalService;
import com.example.rental.domain.DuplicateRental;
import com.example.rental.domain.MissingRental;
import com.example.rental.domain.RentalView;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RentalController.class)
@ContextConfiguration(classes = {
        RentalController.class,
        ApiErrors.class
})
class RentalControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RentalService service;

    @Test
    void createReturns201() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.createRental("RENT-001", "Blue dress"))
                .thenReturn(new RentalView(
                        id, "RENT-001", "BOOKED", "Blue dress"));

        mvc.perform(post("/api/v1/rentals")
                        .contentType("application/json")
                        .content("""
                                {"businessKey":"RENT-001","title":"Blue dress"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", "/api/v1/rentals/" + id))
                .andExpect(jsonPath("$.businessKey").value("RENT-001"))
                .andExpect(jsonPath("$.status").value("BOOKED"));
    }

    @Test
    void blankTitleReturns400() throws Exception {
        mvc.perform(post("/api/v1/rentals")
                        .contentType("application/json")
                        .header("X-Correlation-Id", "test-123")
                        .content("""
                                {"businessKey":"RENT-002","title":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message")
                        .value("title must not be blank"))
                .andExpect(jsonPath("$.correlationId")
                        .value("test-123"));

        verifyNoInteractions(service);
    }

    @Test
    void missingRentalReturns404() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.require(id))
                .thenThrow(new MissingRental());

        mvc.perform(get("/api/v1/rentals/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void duplicateReturns409() throws Exception {
        when(service.createRental(eq("RENT-001"), anyString()))
                .thenThrow(new DuplicateRental("RENT-001"));

        mvc.perform(post("/api/v1/rentals")
                        .contentType("application/json")
                        .content("""
                                {"businessKey":"RENT-001","title":"Red dress"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"));
    }

    @Test
    void invalidUuidReturns400() throws Exception {
        mvc.perform(get("/api/v1/rentals/RENT-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message")
                        .value("id must be a UUID"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/v1/rentals")
                        .contentType("application/json")
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    @Test
    void unsupportedMediaReturns415() throws Exception {
        mvc.perform(post("/api/v1/rentals")
                        .contentType("text/plain")
                        .content("Blue dress"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED"));
    }

    @Test
    void titleLongerThan120Returns400() throws Exception {
        String title = "A".repeat(121);

        mvc.perform(post("/api/v1/rentals")
                        .contentType("application/json")
                        .content("""
                                {"businessKey":"RENT-003","title":"%s"}
                                """.formatted(title)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));

        verifyNoInteractions(service);
    }

    @Test
    void titleExactly120Returns201() throws Exception {
        String title = "A".repeat(120);
        UUID id = UUID.randomUUID();

        when(service.createRental("RENT-120", title))
                .thenReturn(new RentalView(
                        id, "RENT-120", "BOOKED", title));

        mvc.perform(post("/api/v1/rentals")
                        .contentType("application/json")
                        .content("""
                                {"businessKey":"RENT-120","title":"%s"}
                                """.formatted(title)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title));
    }
}
