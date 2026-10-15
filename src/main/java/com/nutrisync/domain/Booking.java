package com.nutrisync.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_client_created", columnList = "client_id,created_at"),
    @Index(name = "idx_booking_status_created", columnList = "status,created_at"),
    @Index(name = "idx_booking_slot_status", columnList = "slot_id,status")
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
    @Column(nullable = false, length = 24)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected Booking() {}

    public Booking(AppUser client, AvailabilitySlot slot) {
        this.client = client;
        this.slot = slot;
        this.status = BookingStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public AppUser getClient() { return client; }
    public AvailabilitySlot getSlot() { return slot; }
    public BookingStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setStatus(BookingStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
