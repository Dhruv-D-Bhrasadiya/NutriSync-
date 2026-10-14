package com.nutrisync.web;

import com.nutrisync.domain.*;
import com.nutrisync.service.BookingService;
import com.nutrisync.service.SlotService;
import com.nutrisync.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class PageController {
    private final UserService users;
    private final SlotService slots;
    private final BookingService bookings;
    private final ZoneId zone;

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm");

    public PageController(UserService users, SlotService slots, BookingService bookings,
                          @Value("${app.time-zone:Asia/Kolkata}") String timeZone) {
        this.users = users;
        this.slots = slots;
        this.bookings = bookings;
        this.zone = ZoneId.of(timeZone);
    }

    @GetMapping("/")
    public String root() { return "redirect:/slots"; }

    @GetMapping("/slots")
    public String availableSlots(Model model) {
        List<SlotView> views = slots.availableSlots().stream().map(this::slotView).toList();
        model.addAttribute("slots", views);
        return "slots";
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email,
                           @RequestParam String password, RedirectAttributes redirect) {
        try {
            users.registerClient(name, email, password);
            redirect.addFlashAttribute("success", "Registration successful. Sign in to continue.");
            return "redirect:/login";
        } catch (ResponseStatusException exception) {
            redirect.addFlashAttribute("error", exception.getReason());
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/bookings")
    public String myBookings(@AuthenticationPrincipal AppUser actor, Model model) {
        try {
            model.addAttribute("bookings", bookings.myBookings(actor).stream().map(this::bookingView).toList());
        } catch (ResponseStatusException exception) {
            model.addAttribute("error", exception.getReason());
            model.addAttribute("bookings", List.of());
        }
        return "bookings";
    }

    @PostMapping("/bookings")
    public String requestBooking(@AuthenticationPrincipal AppUser actor, @RequestParam Long slotId,
                                 RedirectAttributes redirect) {
        try {
            bookings.requestBooking(actor, slotId);
            redirect.addFlashAttribute("success", "Booking request submitted. Its current status is PENDING.");
            return "redirect:/bookings";
        } catch (ResponseStatusException exception) {
            redirect.addFlashAttribute("error", exception.getReason());
            return "redirect:/slots";
        }
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public String cancelBooking(@AuthenticationPrincipal AppUser actor, @PathVariable Long bookingId,
                                RedirectAttributes redirect) {
        try {
            bookings.cancel(actor, bookingId);
            redirect.addFlashAttribute("success", "Booking cancelled.");
        } catch (ResponseStatusException exception) {
            redirect.addFlashAttribute("error", exception.getReason());
        }
        return "redirect:/bookings";
    }

    @GetMapping("/staff/slots/new")
    public String staffSlots(@AuthenticationPrincipal AppUser actor, Model model) {
        model.addAttribute("slots", slots.slotsForNutritionist(actor.getId()).stream().map(this::slotView).toList());
        return "staff/slots";
    }

    @PostMapping("/staff/slots")
    public String createSlot(@AuthenticationPrincipal AppUser actor,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsAt,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endsAt,
                             RedirectAttributes redirect) {
        try {
            slots.createSlot(actor, startsAt.atZone(zone).toInstant(), endsAt.atZone(zone).toInstant());
            redirect.addFlashAttribute("success", "Availability slot created.");
        } catch (ResponseStatusException exception) {
            redirect.addFlashAttribute("error", exception.getReason());
        }
        return "redirect:/staff/slots/new";
    }

    @GetMapping("/staff/bookings")
    public String staffBookings(@AuthenticationPrincipal AppUser actor, Model model) {
        model.addAttribute("bookings", bookings.pendingForStaff(actor).stream().map(this::bookingView).toList());
        return "staff/bookings";
    }

    @PostMapping("/staff/bookings/{bookingId}/confirm")
    public String confirmBooking(@AuthenticationPrincipal AppUser actor, @PathVariable Long bookingId,
                                 RedirectAttributes redirect) {
        return decide(actor, bookingId, true, redirect);
    }

    @PostMapping("/staff/bookings/{bookingId}/reject")
    public String rejectBooking(@AuthenticationPrincipal AppUser actor, @PathVariable Long bookingId,
                                RedirectAttributes redirect) {
        return decide(actor, bookingId, false, redirect);
    }

    private String decide(AppUser actor, Long bookingId, boolean confirm, RedirectAttributes redirect) {
        try {
            if (confirm) bookings.confirm(actor, bookingId); else bookings.reject(actor, bookingId);
            redirect.addFlashAttribute("success", confirm ? "Booking confirmed." : "Booking rejected; slot released.");
        } catch (ResponseStatusException exception) {
            redirect.addFlashAttribute("error", exception.getReason());
        }
        return "redirect:/staff/bookings";
    }

    private SlotView slotView(AvailabilitySlot slot) {
        return new SlotView(slot.getId(), slot.getNutritionist().getName(),
            DISPLAY.withZone(zone).format(slot.getStartsAt()), DISPLAY.withZone(zone).format(slot.getEndsAt()),
            slot.getStatus().name());
    }

    private BookingView bookingView(Booking booking) {
        return new BookingView(booking.getId(), booking.getClient().getName(),
            booking.getSlot().getNutritionist().getName(), DISPLAY.withZone(zone).format(booking.getSlot().getStartsAt()),
            DISPLAY.withZone(zone).format(booking.getSlot().getEndsAt()), booking.getStatus().name(),
            booking.getSlot().getStartsAt().isAfter(Instant.now().plus(Duration.ofHours(24))),
            booking.getSlot().getId());
    }

    public record SlotView(Long id, String nutritionistName, String startsAt, String endsAt, String status) {}
    public record BookingView(Long id, String clientName, String nutritionistName, String startsAt,
                              String endsAt, String status, boolean canCancel, Long slotId) {}
}
