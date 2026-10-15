package com.nutrisync.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "availability_slots", indexes = {
        @Index(name = "idx_slots_status_start", columnList = "status,starts_at"),
        @Index(name = "idx_slots_nutritionist_start", columnList = "nutritionist_id,starts_at")
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

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Version
    private long version;

    protected AvailabilitySlot() { }

    public AvailabilitySlot(AppUser nutritionist, Instant startsAt, Instant endsAt) {
        this.nutritionist = nutritionist;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = SlotStatus.OPEN;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public AppUser getNutritionist() { return nutritionist; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public SlotStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public long getVersion() { return version; }

    public void setNutritionist(AppUser nutritionist) { this.nutritionist = nutritionist; }
    public void setStartsAt(Instant startsAt) { this.startsAt = startsAt; }
    public void setEndsAt(Instant endsAt) { this.endsAt = endsAt; }
    public void setStatus(SlotStatus status) { this.status = status; }
}
