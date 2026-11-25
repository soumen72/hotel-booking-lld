package com.hbooking;

import com.hbooking.model.Booking;
import com.hbooking.model.DateRange;
import com.hbooking.model.GuestCount;
import com.hbooking.model.Money;
import com.hbooking.policy.impl.TieredCancellationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;



class CancellationPolicyTest {
    private TieredCancellationPolicy policy;
    private Booking booking;
    private LocalDate checkInDate;

    @BeforeEach
    void setUp() {
        policy = new TieredCancellationPolicy(ZoneOffset.UTC);
        checkInDate = LocalDate.of(2026, 12, 10);
        LocalDate checkOutDate = LocalDate.of(2026, 12, 15);
        DateRange dateRange = DateRange.of(checkInDate, checkOutDate);

        booking = Booking.builder()
                .id("BK-1")
                .propertyId("PROP-1")
                .roomTypeId("RT-1")
                .guestId("GUEST-1")
                .dateRange(dateRange)
                .guestCount(GuestCount.of(2))
                .totalPrice(Money.of(1000.0, "USD"))
                .build();
    }

    @Test
    void testTier1MoreThan48HoursBeforeCheckInFullRefund() {
        // Check-in is 2026-12-10 00:00:00 UTC.
        // 50 hours before: 2026-12-07 22:00:00 UTC
        Instant cancellationTime = Instant.parse("2026-12-07T22:00:00Z");

        Money refund = policy.calculateRefund(booking, cancellationTime);
        assertEquals(Money.of(1000.0, "USD"), refund);
    }

    @Test
    void testTier1Exactly48HoursBeforeCheckInFullRefund() {
        // Exactly 48 hours before: 2026-12-08 00:00:00 UTC
        Instant cancellationTime = Instant.parse("2026-12-08T00:00:00Z");

        Money refund = policy.calculateRefund(booking, cancellationTime);
        assertEquals(Money.of(1000.0, "USD"), refund);
    }

    @Test
    void testTier2Within48HoursBeforeCheckInFiftyPercentRefund() {
        // 47 hours before: 2026-12-08 01:00:00 UTC
        Instant cancellationTime47h = Instant.parse("2026-12-08T01:00:00Z");
        Money refund47h = policy.calculateRefund(booking, cancellationTime47h);
        assertEquals(Money.of(500.0, "USD"), refund47h);

        // 2 hours before: 2026-12-09 22:00:00 UTC
        Instant cancellationTime2h = Instant.parse("2026-12-09T22:00:00Z");
        Money refund2h = policy.calculateRefund(booking, cancellationTime2h);
        assertEquals(Money.of(500.0, "USD"), refund2h);
    }

    @Test
    void testTier3AtOrAfterCheckInNoRefund() {
        // Exactly at check-in: 2026-12-10 00:00:00 UTC
        Instant cancellationAtCheckIn = Instant.parse("2026-12-10T00:00:00Z");
        Money refundAtCheckIn = policy.calculateRefund(booking, cancellationAtCheckIn);
        assertEquals(Money.of(0.0, "USD"), refundAtCheckIn);

        // After check-in: 2026-12-10 12:00:00 UTC
        Instant cancellationAfterCheckIn = Instant.parse("2026-12-10T12:00:00Z");
        Money refundAfterCheckIn = policy.calculateRefund(booking, cancellationAfterCheckIn);
        assertEquals(Money.of(0.0, "USD"), refundAfterCheckIn);
    }
}
