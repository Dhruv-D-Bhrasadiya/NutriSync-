package com.nutrisync;

import com.nutrisync.domain.*;
import com.nutrisync.repository.*;
import com.nutrisync.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BookingServiceIntegrationTest {
    @Autowired AppUserRepository users;
    @Autowired AvailabilitySlotRepository slots;
    @Autowired BookingRepository bookings;
    @Autowired BookingService bookingService;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void firstRequestClaimsSlotAndCreatesPendingBooking() {
        Fixture fixture = fixture();
        Booking booking = bookingService.requestBooking(fixture.client, fixture.slot.getId());
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(slots.findById(fixture.slot.getId()).orElseThrow().getStatus()).isEqualTo(SlotStatus.BOOKED);
        assertThat(bookings.findById(booking.getId())).isPresent();
    }

    @Test
    void secondRequestForClaimedSlotIsRejected() {
        Fixture fixture = fixture();
        bookingService.requestBooking(fixture.client, fixture.slot.getId());
        ResponseStatusException exception = catchThrowableOfType(
            () -> bookingService.requestBooking(fixture.otherClient, fixture.slot.getId()), ResponseStatusException.class);
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void staffRejectionReleasesFutureSlot() {
        Fixture fixture = fixture();
        Booking booking = bookingService.requestBooking(fixture.client, fixture.slot.getId());
        bookingService.reject(fixture.nutritionist, booking.getId());
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(slots.findById(fixture.slot.getId()).orElseThrow().getStatus()).isEqualTo(SlotStatus.OPEN);
    }

    private Fixture fixture() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        AppUser client = users.save(new AppUser("Test Client", "client-" + suffix + "@example.test", passwordEncoder.encode("CorrectHorseBattery1"), Role.CLIENT));
        AppUser other = users.save(new AppUser("Other Client", "other-" + suffix + "@example.test", passwordEncoder.encode("CorrectHorseBattery1"), Role.CLIENT));
        AppUser nutritionist = users.save(new AppUser("Test Nutritionist", "nutritionist-" + suffix + "@example.test", passwordEncoder.encode("CorrectHorseBattery1"), Role.NUTRITIONIST));
        Instant start = Instant.now().plusSeconds(3 * 24 * 60 * 60L);
        AvailabilitySlot slot = slots.save(new AvailabilitySlot(nutritionist, start, start.plusSeconds(45 * 60L)));
        return new Fixture(client, other, nutritionist, slot);
    }

    private record Fixture(AppUser client, AppUser otherClient, AppUser nutritionist, AvailabilitySlot slot) {}
}
