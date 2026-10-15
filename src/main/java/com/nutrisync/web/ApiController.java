package com.nutrisync.web;

import com.nutrisync.domain.AppUser;
import com.nutrisync.domain.Booking;
import com.nutrisync.service.BookingService;
import com.nutrisync.service.SlotService;
import com.nutrisync.service.UserService;
import com.nutrisync.web.ApiDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ApiController {
    private final UserService users;
    private final SlotService slots;
    private final BookingService bookings;

    public ApiController(UserService users, SlotService slots, BookingService bookings) {
        this.users = users;
        this.slots = slots;
        this.bookings = bookings;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = users.registerClient(request.name(), request.email(), request.password());
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    @GetMapping("/slots")
    public List<SlotResponse> availableSlots() {
        return slots.availableSlots().stream().map(SlotResponse::from).toList();
    }

    @PostMapping("/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public SlotResponse createSlot(@AuthenticationPrincipal AppUser actor, @Valid @RequestBody SlotCreateRequest request) {
        return SlotResponse.from(slots.createSlot(actor, request.startsAt(), request.endsAt()));
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse requestBooking(@AuthenticationPrincipal AppUser actor, @Valid @RequestBody BookingRequest request) {
        Booking created = bookings.requestBooking(actor, request.slotId());
        // Re-read through the fetch-join query so DTO mapping never touches lazy proxies
        // after the service transaction has closed.
        return BookingResponse.from(bookings.bookingForViewer(actor, created.getId()));
    }

    @GetMapping("/bookings/mine")
    public List<BookingResponse> myBookings(@AuthenticationPrincipal AppUser actor) {
        return bookings.myBookings(actor).stream().map(BookingResponse::from).toList();
    }

    @GetMapping("/bookings/{bookingId}")
    public BookingResponse getBooking(@AuthenticationPrincipal AppUser actor, @PathVariable Long bookingId) {
        return BookingResponse.from(bookings.bookingForViewer(actor, bookingId));
    }

    @PatchMapping("/bookings/{bookingId}/confirm")
    public BookingResponse confirm(@AuthenticationPrincipal AppUser actor, @PathVariable Long bookingId) {
        bookings.confirm(actor, bookingId);
        return BookingResponse.from(bookings.bookingForViewer(actor, bookingId));
    }

    @PatchMapping("/bookings/{bookingId}/reject")
    public BookingResponse reject(@AuthenticationPrincipal AppUser actor, @PathVariable Long bookingId) {
        bookings.reject(actor, bookingId);
        return BookingResponse.from(bookings.bookingForViewer(actor, bookingId));
    }

    @PatchMapping("/bookings/{bookingId}/cancel")
    public BookingResponse cancel(@AuthenticationPrincipal AppUser actor, @PathVariable Long bookingId) {
        bookings.cancel(actor, bookingId);
        return BookingResponse.from(bookings.bookingForViewer(actor, bookingId));
    }
}
