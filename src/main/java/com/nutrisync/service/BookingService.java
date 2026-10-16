package com.nutrisync.service;

import com.nutrisync.domain.*;
import com.nutrisync.repository.AppUserRepository;
import com.nutrisync.repository.AvailabilitySlotRepository;
import com.nutrisync.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class BookingService {
    private static final Duration CANCELLATION_NOTICE = Duration.ofHours(24);
    private final BookingRepository bookings;
    private final AvailabilitySlotRepository slots;
    private final AppUserRepository users;

    public BookingService(BookingRepository bookings, AvailabilitySlotRepository slots, AppUserRepository users) {
        this.bookings = bookings;
        this.slots = slots;
        this.users = users;
    }

    @Transactional
    public Booking requestBooking(AppUser principal, Long slotId) {
        AppUser client = currentUser(principal);
        if (client.getRole() != Role.CLIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only client accounts can request consultations.");
        }
        AvailabilitySlot slot = slots.findLockedById(slotId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability slot not found."));
        if (slot.getStatus() != SlotStatus.OPEN || !slot.getStartsAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This slot is no longer available.");
        }
        slot.markBooked();
        return bookings.save(new Booking(client, slot));
    }

    @Transactional(readOnly = true)
    public List<Booking> myBookings(AppUser principal) {
        AppUser user = currentUser(principal);
        if (user.getRole() != Role.CLIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This page is for client bookings.");
        }
        return bookings.findAllForClient(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Booking> pendingForStaff(AppUser principal) {
        AppUser staff = currentUser(principal);
        requireStaff(staff);
        return staff.getRole() == Role.ADMIN
            ? bookings.findAllByStatusWithDetails(BookingStatus.PENDING)
            : bookings.findByNutritionistAndStatus(staff.getId(), BookingStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Booking> allBookingsForAdmin(AppUser principal) {
        AppUser staff = currentUser(principal);
        requireAdmin(staff);
        return bookings.findAllDetailedOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public BookingMonitoringStats bookingMonitoringStats(AppUser principal) {
        AppUser staff = currentUser(principal);
        requireAdmin(staff);
        return new BookingMonitoringStats(
            bookings.count(), bookings.countByStatus(BookingStatus.PENDING),
            bookings.countByStatus(BookingStatus.CONFIRMED), bookings.countByStatus(BookingStatus.REJECTED),
            bookings.countByStatus(BookingStatus.CANCELLED), bookings.countByStatus(BookingStatus.COMPLETED),
            users.countByRole(Role.CLIENT), users.countByRole(Role.NUTRITIONIST),
            users.countByRole(Role.ADMIN), users.count()
        );
    }

    @Transactional(readOnly = true)
    public List<AppUser> recentUsersForAdmin(AppUser principal) {
        AppUser staff = currentUser(principal);
        requireAdmin(staff);
        return users.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Booking bookingForViewer(AppUser principal, Long bookingId) {
        AppUser viewer = currentUser(principal);
        Booking booking = bookings.findDetailedById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        if (viewer.getRole() == Role.ADMIN || booking.getClient().getId().equals(viewer.getId())
                || (viewer.getRole() == Role.NUTRITIONIST && booking.getSlot().getNutritionist().getId().equals(viewer.getId()))) {
            return booking;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found.");
    }

    @Transactional
    public Booking confirm(AppUser principal, Long bookingId) {
        return decide(principal, bookingId, BookingStatus.CONFIRMED);
    }

    @Transactional
    public Booking reject(AppUser principal, Long bookingId) {
        return decide(principal, bookingId, BookingStatus.REJECTED);
    }

    private Booking decide(AppUser principal, Long bookingId, BookingStatus target) {
        AppUser staff = currentUser(principal);
        requireStaff(staff);
        Booking booking = bookings.findLockedById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        if (staff.getRole() == Role.NUTRITIONIST && !booking.getSlot().getNutritionist().getId().equals(staff.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can decide only requests for your own slots.");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending bookings can be confirmed or rejected.");
        }
        booking.setStatus(target);
        if (target == BookingStatus.REJECTED && booking.getSlot().getStartsAt().isAfter(Instant.now())) {
            booking.getSlot().markOpen();
        }
        return booking;
    }

    @Transactional
    public Booking cancel(AppUser principal, Long bookingId) {
        AppUser actor = currentUser(principal);
        Booking booking = bookings.findLockedById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));
        boolean owner = actor.getRole() == Role.CLIENT && booking.getClient().getId().equals(actor.getId());
        boolean staff = actor.getRole() == Role.ADMIN ||
            (actor.getRole() == Role.NUTRITIONIST && booking.getSlot().getNutritionist().getId().equals(actor.getId()));
        if (!owner && !staff) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found.");
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking can no longer be cancelled.");
        }
        if (owner && booking.getSlot().getStartsAt().isBefore(Instant.now().plus(CANCELLATION_NOTICE))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Client cancellations must be made at least 24 hours before the consultation.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        if (booking.getSlot().getStartsAt().isAfter(Instant.now())) {
            booking.getSlot().markOpen();
        }
        return booking;
    }

    private AppUser currentUser(AppUser principal) {
        if (principal == null || principal.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in.");
        }
        return users.findById(principal.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is no longer available."));
    }

    private void requireStaff(AppUser user) {
        if (user.getRole() != Role.NUTRITIONIST && user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff access required.");
        }
    }

    private void requireAdmin(AppUser user) {
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access required.");
        }
    }

    public record BookingMonitoringStats(long total, long pending, long confirmed, long rejected, long cancelled,
                                         long completed, long clients, long nutritionists, long administrators, long users) {}
}
