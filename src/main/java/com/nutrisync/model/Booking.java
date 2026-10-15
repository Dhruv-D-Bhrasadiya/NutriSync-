package com.nutrisync.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_client_requested", columnList = "client_id,requested_at"),
        @Index(name = "idx_bookings_status_requested", columnList = "status,requested_at")
})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private AppUser client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private AvailabilitySlot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant requestedAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @Column
    private Instant decisionAt;

    protected Booking() { }

    public Booking(AppUser client, AvailabilitySlot slot) {
        this.client = client;
        this.slot = slot;
        this.status = BookingStatus.PENDING;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (requestedAt == null) requestedAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public AppUser getClient() { return client; }
    public AvailabilitySlot getSlot() { return slot; }
    public BookingStatus getStatus() { return status; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDecisionAt() { return decisionAt; }

    public void setClient(AppUser client) { this.client = client; }
    public void setSlot(AvailabilitySlot slot) { this.slot = slot; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public void setDecisionAt(Instant decisionAt) { this.decisionAt = decisionAt; }
}
