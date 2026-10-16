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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    void concurrentRequestsForSameSlotCreateOnlyOneBooking() throws Exception {
        Fixture fixture = fixture();
        int contenders = 8;
        ExecutorService pool = Executors.newFixedThreadPool(contenders);
        CountDownLatch ready = new CountDownLatch(contenders);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger winners = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        List<AppUser> clients = java.util.stream.IntStream.range(0, contenders)
            .mapToObj(i -> users.save(new AppUser("Concurrent Client " + i + " " + UUID.randomUUID().toString().substring(0, 6),
                "race-" + UUID.randomUUID() + "@example.test",
                passwordEncoder.encode("CorrectHorseBattery1"), Role.CLIENT)))
            .toList();

        try {
            List<Future<?>> attempts = clients.stream().map(client -> pool.submit((Callable<Object>) () -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Concurrent test start timed out");
                }
                try {
                    bookingService.requestBooking(client, fixture.slot.getId());
                    winners.incrementAndGet();
                } catch (ResponseStatusException ex) {
                    if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                        conflicts.incrementAndGet();
                    } else {
                        throw ex;
                    }
                }
                return null;
            })).toList();

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            for (Future<?> attempt : attempts) {
                attempt.get(30, TimeUnit.SECONDS);
            }
        } finally {
            start.countDown();
            pool.shutdownNow();
            assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(winners).hasValue(1);
        assertThat(conflicts).hasValue(contenders - 1);
        assertThat(bookings.findAll()).filteredOn(b -> b.getSlot().getId().equals(fixture.slot.getId()))
            .hasSize(1);
        assertThat(slots.findById(fixture.slot.getId()).orElseThrow().getStatus()).isEqualTo(SlotStatus.BOOKED);
    }

    @Test
    void nutritionistCannotConfirmAnotherNutritionistsBooking() {
        Fixture fixture = fixture();
        AppUser anotherNutritionist = users.save(new AppUser("Other Nutritionist " + UUID.randomUUID().toString().substring(0, 8),
            "other-nutritionist-" + UUID.randomUUID().toString().substring(0, 8) + "@example.test",
            passwordEncoder.encode("CorrectHorseBattery1"), Role.NUTRITIONIST));
        Booking booking = bookingService.requestBooking(fixture.client, fixture.slot.getId());

        ResponseStatusException exception = catchThrowableOfType(
            () -> bookingService.confirm(anotherNutritionist, booking.getId()), ResponseStatusException.class);

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(bookings.findById(booking.getId()).orElseThrow().getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void staffRejectionReleasesFutureSlot() {
        Fixture fixture = fixture();
        Booking booking = bookingService.requestBooking(fixture.client, fixture.slot.getId());
        Booking rejected = bookingService.reject(fixture.nutritionist, booking.getId());
        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(bookings.findById(booking.getId()).orElseThrow().getStatus()).isEqualTo(BookingStatus.REJECTED);
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
