package com.hbooking.policy;

import com.hbooking.model.Booking;
import com.hbooking.model.Money;

import java.time.Instant;

public interface CancellationPolicy {
    Money calculateRefund(Booking booking, Instant cancellationTime);
}
