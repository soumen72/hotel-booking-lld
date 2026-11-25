package com.hbooking;

import com.hbooking.model.Booking;
import com.hbooking.model.DateRange;
import com.hbooking.model.GuestCount;
import com.hbooking.model.Money;
import com.hbooking.exception.InvalidBookingStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingStateTransitionTest {
    private Booking booking;

    @BeforeEach
    void setUp() {
        DateRange range = DateRange.of(LocalDate.now().plusDays(10), LocalDate.now().plusDays(12));
        booking = Booking.builder()
                .id("BK-100")
                .propertyId("PROP-1")
                .roomTypeId("RT-1")
                .guestId("GUEST-1")
                .dateRange(range)
                .guestCount(GuestCount.of(2))
                .totalPrice(Money.of(200.0, "USD"))
                .build();
    }

    @Test
    void testValidTransitionsFromPending() {
        // Can transition from PENDING to CONFIRMED
        assertDoesNotThrow(() -> booking.markConfirmed());

        // Reset
        Booking b2 = Booking.builder()
                .id("BK-101")
                .propertyId("PROP-1")
                .roomTypeId("RT-1")
                .guestId("GUEST-1")
                .dateRange(DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)))
                .guestCount(GuestCount.of(1))
                .totalPrice(Money.of(100.0, "USD"))
                .build();
        assertDoesNotThrow(b2::markPaymentFailed);

        Booking b3 = Booking.builder()
                .id("BK-102")
                .propertyId("PROP-1")
                .roomTypeId("RT-1")
                .guestId("GUEST-1")
                .dateRange(DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)))
                .guestCount(GuestCount.of(1))
                .totalPrice(Money.of(100.0, "USD"))
                .build();
        assertDoesNotThrow(b3::cancel);
    }

    @Test
    void testValidTransitionFromConfirmed() {
        booking.markConfirmed();
        assertDoesNotThrow(() -> booking.cancel());
    }

    @Test
    void testInvalidTransitionsFromConfirmed() {
        booking.markConfirmed();

        // Cannot confirm an already confirmed booking
        assertThrows(InvalidBookingStateException.class, () -> booking.markConfirmed());

        // Cannot mark payment failed on confirmed booking
        assertThrows(InvalidBookingStateException.class, () -> booking.markPaymentFailed());
    }

    @Test
    void testInvalidTransitionsFromCancelled() {
        booking.cancel();

        // Cannot cancel again
        assertThrows(InvalidBookingStateException.class, () -> booking.cancel());

        // Cannot confirm cancelled booking
        assertThrows(InvalidBookingStateException.class, () -> booking.markConfirmed());

        // Cannot fail payment on cancelled booking
        assertThrows(InvalidBookingStateException.class, () -> booking.markPaymentFailed());
    }

    @Test
    void testInvalidTransitionsFromPaymentFailed() {
        booking.markPaymentFailed();

        // Cannot confirm payment failed booking
        assertThrows(InvalidBookingStateException.class, () -> booking.markConfirmed());

        // Cannot cancel payment failed booking
        assertThrows(InvalidBookingStateException.class, () -> booking.cancel());
    }
}
