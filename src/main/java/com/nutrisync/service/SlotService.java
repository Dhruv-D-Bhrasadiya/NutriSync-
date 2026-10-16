package com.nutrisync.service;

import com.nutrisync.domain.*;
import com.nutrisync.repository.AppUserRepository;
import com.nutrisync.repository.AvailabilitySlotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class SlotService {
    private final AvailabilitySlotRepository slots;
    private final AppUserRepository users;

    public SlotService(AvailabilitySlotRepository slots, AppUserRepository users) {
        this.slots = slots;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlot> availableSlots() {
        return slots.findOpenFutureWithNutritionist(SlotStatus.OPEN, Instant.now());
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlot> slotsForNutritionist(Long nutritionistId) {
        return slots.findAllForNutritionist(nutritionistId);
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlot> allSlotsForAdmin(AppUser principal) {
        AppUser actor = currentUser(principal);
        requireAdmin(actor);
        return slots.findAllWithNutritionistOrderByStartsAtDesc();
    }

    @Transactional(readOnly = true)
    public SlotMonitoringStats monitoringStats(AppUser principal) {
        AppUser actor = currentUser(principal);
        requireAdmin(actor);
        return new SlotMonitoringStats(
            slots.count(),
            slots.countByStatus(SlotStatus.OPEN),
            slots.countByStatus(SlotStatus.BOOKED),
            slots.countByStatus(SlotStatus.UNAVAILABLE),
            slots.countByStartsAtAfter(Instant.now())
        );
    }

    @Transactional
    public AvailabilitySlot createSlot(AppUser actor, Instant start, Instant end) {
        AppUser account = currentUser(actor);
        if (account.getRole() != Role.NUTRITIONIST && account.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only nutritionists or administrators can manage availability.");
        }
        if (account.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrators monitor availability; only nutritionists publish consultation slots.");
        }
        if (start == null || end == null || !start.isAfter(Instant.now()) || !end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a future start time and an end time after the start.");
        }
        if (end.isAfter(start.plusSeconds(4 * 60 * 60))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A consultation slot cannot exceed four hours.");
        }
        // Lock the owning user row so concurrent slot-creation transactions serialize
        // before checking for overlapping intervals.
        AppUser owner = users.findLockedById(account.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutritionist account not found."));
        if (owner.getRole() != Role.NUTRITIONIST) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only nutritionist accounts can publish availability.");
        }
        if (slots.hasOverlappingSlot(owner.getId(), start, end)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This availability overlaps an existing slot.");
        }
        return slots.save(new AvailabilitySlot(owner, start, end));
    }

    private AppUser currentUser(AppUser principal) {
        if (principal == null || principal.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again.");
        }
        return users.findById(principal.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your account is no longer available."));
    }

    private void requireAdmin(AppUser user) {
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access required.");
        }
    }

    public record SlotMonitoringStats(long total, long open, long booked, long unavailable, long future) {}
}
