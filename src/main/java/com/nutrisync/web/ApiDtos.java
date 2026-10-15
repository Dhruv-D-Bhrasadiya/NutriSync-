package com.nutrisync.web;

import com.nutrisync.domain.AvailabilitySlot;
import com.nutrisync.domain.Booking;
import jakarta.validation.constraints.*;

import java.time.Instant;

public final class ApiDtos {
    private ApiDtos() {}

    public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 10, max = 72) String password
    ) {}

    public record SlotCreateRequest(@NotNull Instant startsAt, @NotNull Instant endsAt) {}
    public record BookingRequest(@NotNull Long slotId) {}

    public record UserResponse(Long id, String name, String email, String role) {}
    public record SlotResponse(Long id, String nutritionistName, Instant startsAt, Instant endsAt, String status) {
        public static SlotResponse from(AvailabilitySlot slot) {
            return new SlotResponse(slot.getId(), slot.getNutritionist().getName(),
                slot.getStartsAt(), slot.getEndsAt(), slot.getStatus().name());
        }
    }
    public record BookingResponse(Long id, String clientName, Long slotId, String nutritionistName,
                                  Instant startsAt, Instant endsAt, String status, Instant createdAt) {
        public static BookingResponse from(Booking booking) {
            return new BookingResponse(booking.getId(), booking.getClient().getName(),
                booking.getSlot().getId(), booking.getSlot().getNutritionist().getName(),
                booking.getSlot().getStartsAt(), booking.getSlot().getEndsAt(),
                booking.getStatus().name(), booking.getCreatedAt());
        }
    }
}
