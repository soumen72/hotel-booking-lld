package com.hbooking.service;

import com.hbooking.model.Booking;
import com.hbooking.model.CancellationResult;
import com.hbooking.model.DateRange;
import com.hbooking.model.GuestCount;

import java.time.Instant;

public interface BookingService {
    Booking createBooking(String propertyId, String roomTypeId, String guestId, DateRange dateRange, GuestCount guestCount);
    Booking getBooking(String bookingId);
    CancellationResult cancelBooking(String bookingId);
    CancellationResult cancelBooking(String bookingId, Instant cancellationTime);
}
