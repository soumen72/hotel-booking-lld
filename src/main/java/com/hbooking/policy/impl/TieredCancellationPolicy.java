package com.hbooking.policy.impl;

import com.hbooking.model.Booking;
import com.hbooking.model.Money;
import com.hbooking.policy.CancellationPolicy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
public class TieredCancellationPolicy implements CancellationPolicy {
    private final ZoneId zoneId;

    public TieredCancellationPolicy() {
        this(ZoneOffset.UTC);
    }

    public TieredCancellationPolicy(ZoneId zoneId) {
        this.zoneId = zoneId != null ? zoneId : ZoneOffset.UTC;
    }

    @Override
    public Money calculateRefund(Booking booking, Instant cancellationTime) {
        if (booking == null || cancellationTime == null) {
            throw new IllegalArgumentException("Booking and cancellation time must not be null");
        }

        Instant checkInInstant = booking.getDateRange().getCheckIn()
                .atStartOfDay(zoneId)
                .toInstant();

        Money total = booking.getTotalPrice();

        // Already checked in or past check-in time: no refund
        if (!cancellationTime.isBefore(checkInInstant)) {
            return Money.zero(total.getCurrency());
        }

        Duration durationUntilCheckIn = Duration.between(cancellationTime, checkInInstant);
        long hoursUntilCheckIn = durationUntilCheckIn.toHours();

        // 48h+ before check-in: 100% full refund
        if (hoursUntilCheckIn >= 48) {
            return total;
        }

        // Within 48h before check-in: 50% refund
        return total.multiply(0.5);
    }
}
