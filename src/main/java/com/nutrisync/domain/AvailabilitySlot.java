package com.nutrisync.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "availability_slots", indexes = {
    @Index(name = "idx_slot_start_status", columnList = "starts_at,status"),
    @Index(name = "idx_slot_nutritionist_start", columnList = "nutritionist_id,starts_at")
})
public class AvailabilitySlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nutritionist_id", nullable = false)
    private AppUser nutritionist;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlotStatus status = SlotStatus.OPEN;

    @Version
    private long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AvailabilitySlot() {}

    public AvailabilitySlot(AppUser nutritionist, Instant startsAt, Instant endsAt) {
        this.nutritionist = nutritionist;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = SlotStatus.OPEN;
    }

    public Long getId() { return id; }
    public AppUser getNutritionist() { return nutritionist; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public SlotStatus getStatus() { return status; }
    public long getVersion() { return version; }
    public void markBooked() { this.status = SlotStatus.BOOKED; }
    public void markOpen() { this.status = SlotStatus.OPEN; }
    public void markUnavailable() { this.status = SlotStatus.UNAVAILABLE; }
}
