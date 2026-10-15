package com.nutrisync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrisync.domain.*;
import com.nutrisync.repository.AppUserRepository;
import com.nutrisync.repository.AvailabilitySlotRepository;
import com.nutrisync.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiBookingIntegrationTest {
    private static final String PASSWORD = "TestPassword!0123456789";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AppUserRepository users;
    @Autowired AvailabilitySlotRepository slots;
    @Autowired BookingRepository bookings;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void bookingCreateConfirmAndCancelReturnCompleteJsonResponses() throws Exception {
        Fixture fixture = fixture();

        MvcResult createResult = mvc.perform(post("/api/v1/bookings")
                .header("Authorization", basic(fixture.client.getEmail()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"slotId\":" + fixture.slot.getId() + "}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.clientName").value(fixture.client.getName()))
            .andExpect(jsonPath("$.nutritionistName").value(fixture.nutritionist.getName()))
            .andReturn();

        long bookingId = readId(createResult);
        mvc.perform(patch("/api/v1/bookings/{id}/confirm", bookingId)
                .header("Authorization", basic(fixture.nutritionist.getEmail())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(bookingId))
            .andExpect(jsonPath("$.status").value("CONFIRMED"))
            .andExpect(jsonPath("$.clientName").value(fixture.client.getName()))
            .andExpect(jsonPath("$.nutritionistName").value(fixture.nutritionist.getName()));

        mvc.perform(patch("/api/v1/bookings/{id}/cancel", bookingId)
                .header("Authorization", basic(fixture.client.getEmail())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"))
            .andExpect(jsonPath("$.slotId").value(fixture.slot.getId()));

        mvc.perform(get("/api/v1/bookings/{id}", bookingId)
                .header("Authorization", basic(fixture.client.getEmail())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void staffRejectionReturnsResolvedJsonAndReleasesSlot() throws Exception {
        Fixture fixture = fixture();
        MvcResult createResult = mvc.perform(post("/api/v1/bookings")
                .header("Authorization", basic(fixture.client.getEmail()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"slotId\":" + fixture.slot.getId() + "}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();

        long bookingId = readId(createResult);
        mvc.perform(patch("/api/v1/bookings/{id}/reject", bookingId)
                .header("Authorization", basic(fixture.nutritionist.getEmail())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REJECTED"))
            .andExpect(jsonPath("$.nutritionistName").value(fixture.nutritionist.getName()));

        assertThat(bookings.findById(bookingId).orElseThrow().getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(slots.findById(fixture.slot.getId()).orElseThrow().getStatus()).isEqualTo(SlotStatus.OPEN);
    }

    @Test
    void oneClientCannotReadAnotherClientsBooking() throws Exception {
        Fixture fixture = fixture();
        MvcResult createResult = mvc.perform(post("/api/v1/bookings")
                .header("Authorization", basic(fixture.client.getEmail()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"slotId\":" + fixture.slot.getId() + "}"))
            .andExpect(status().isCreated())
            .andReturn();
        long bookingId = readId(createResult);

        AppUser unrelatedClient = createUser("Unrelated Client", "unrelated", Role.CLIENT);
        mvc.perform(get("/api/v1/bookings/{id}", bookingId)
                .header("Authorization", basic(unrelatedClient.getEmail())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    private Fixture fixture() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        AppUser client = createUser("API Client " + suffix, "api-client-" + suffix, Role.CLIENT);
        AppUser nutritionist = createUser("API Nutritionist " + suffix, "api-nutritionist-" + suffix, Role.NUTRITIONIST);
        Instant start = Instant.now().plusSeconds(3 * 24 * 60 * 60L);
        AvailabilitySlot slot = slots.save(new AvailabilitySlot(nutritionist, start, start.plusSeconds(45 * 60L)));
        return new Fixture(client, nutritionist, slot);
    }

    private AppUser createUser(String name, String emailPrefix, Role role) {
        String email = emailPrefix + "@example.test";
        return users.save(new AppUser(name, email, passwordEncoder.encode(PASSWORD), role));
    }

    private long readId(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("id").asLong();
    }

    private String basic(String email) {
        String raw = email + ":" + PASSWORD;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private record Fixture(AppUser client, AppUser nutritionist, AvailabilitySlot slot) { }
}
