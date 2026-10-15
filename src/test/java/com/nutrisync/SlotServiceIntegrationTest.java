package com.nutrisync;

import com.nutrisync.domain.*;
import com.nutrisync.repository.AppUserRepository;
import com.nutrisync.repository.AvailabilitySlotRepository;
import com.nutrisync.service.SlotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest
@ActiveProfiles("test")
class SlotServiceIntegrationTest {
    @Autowired AppUserRepository users;
    @Autowired AvailabilitySlotRepository slots;
    @Autowired SlotService slotService;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void overlappingAvailabilityForSameNutritionistIsRejected() {
        AppUser nutritionist = createUser("Nutritionist", Role.NUTRITIONIST);
        Instant start = Instant.now().plusSeconds(3 * 24 * 60 * 60L);
        AvailabilitySlot existing = slots.save(new AvailabilitySlot(nutritionist, start, start.plusSeconds(45 * 60L)));

        ResponseStatusException exception = catchThrowableOfType(
            () -> slotService.createSlot(nutritionist, start.plusSeconds(30 * 60L), start.plusSeconds(75 * 60L)),
            ResponseStatusException.class
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(slots.findById(existing.getId()).orElseThrow().getStatus()).isEqualTo(SlotStatus.OPEN);
    }

    @Test
    void clientCannotCreateAvailability() {
        AppUser client = createUser("Client", Role.CLIENT);
        Instant start = Instant.now().plusSeconds(3 * 24 * 60 * 60L);

        ResponseStatusException exception = catchThrowableOfType(
            () -> slotService.createSlot(client, start, start.plusSeconds(45 * 60L)),
            ResponseStatusException.class
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private AppUser createUser(String label, Role role) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return users.save(new AppUser(label + " " + suffix, label.toLowerCase() + "-" + suffix + "@example.test",
            passwordEncoder.encode("TestPassword!0123456789"), role));
    }
}
