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

    @Transactional
    public AvailabilitySlot createSlot(AppUser actor, Instant start, Instant end) {
        if (actor == null || (actor.getRole() != Role.NUTRITIONIST && actor.getRole() != Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only nutritionists or administrators can manage availability.");
        }
        if (start == null || end == null || !start.isAfter(Instant.now()) || !end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a future start time and an end time after the start.");
        }
        if (end.isAfter(start.plusSeconds(4 * 60 * 60))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A consultation slot cannot exceed four hours.");
        }
        AppUser owner = users.findById(actor.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutritionist account not found."));
        if (slots.hasOverlappingSlot(owner.getId(), start, end)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This availability overlaps an existing slot.");
        }
        return slots.save(new AvailabilitySlot(owner, start, end));
    }
}
