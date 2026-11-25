package com.hbooking.model;

import com.hbooking.enums.BookingStatus;

import com.hbooking.exception.InvalidBookingStateException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class Booking {
    private final String id;
    private final String propertyId;
    private final String roomTypeId;
    private final String guestId;
    private final DateRange dateRange;
    private final GuestCount guestCount;
    private final Money totalPrice;
    private BookingStatus status;
    private final Instant createdAt;
    private Instant confirmedAt;
    private Instant cancelledAt;

    @Builder
    public Booking(String id, String propertyId, String roomTypeId, String guestId,
                   DateRange dateRange, GuestCount guestCount, Money totalPrice) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Booking ID cannot be blank");
        }
        if (propertyId == null || propertyId.isBlank()) {
            throw new IllegalArgumentException("Property ID cannot be blank");
        }
        if (roomTypeId == null || roomTypeId.isBlank()) {
            throw new IllegalArgumentException("Room type ID cannot be blank");
        }
        if (guestId == null || guestId.isBlank()) {
            throw new IllegalArgumentException("Guest ID cannot be blank");
        }
        if (dateRange == null) {
            throw new IllegalArgumentException("Date range cannot be null");
        }
        if (guestCount == null) {
            throw new IllegalArgumentException("Guest count cannot be null");
        }
        if (totalPrice == null) {
            throw new IllegalArgumentException("Total price cannot be null");
        }

        this.id = id;
        this.propertyId = propertyId;
        this.roomTypeId = roomTypeId;
        this.guestId = guestId;
        this.dateRange = dateRange;
        this.guestCount = guestCount;
        this.totalPrice = totalPrice;
        this.status = BookingStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public synchronized void markConfirmed() {
        if (this.status != BookingStatus.PENDING) {
            throw new InvalidBookingStateException(
                    "Cannot confirm booking " + id + ": expected status PENDING but was " + status);
        }
        this.status = BookingStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
    }

    public synchronized void markPaymentFailed() {
        if (this.status != BookingStatus.PENDING) {
            throw new InvalidBookingStateException(
                    "Cannot mark payment failed for booking " + id + ": expected status PENDING but was " + status);
        }
        this.status = BookingStatus.PAYMENT_FAILED;
    }

    public synchronized void cancel() {
        if (this.status == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Booking " + id + " is already cancelled");
        }
        if (this.status == BookingStatus.PAYMENT_FAILED) {
            throw new InvalidBookingStateException("Cannot cancel a booking in PAYMENT_FAILED status");
        }
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking booking)) return false;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
