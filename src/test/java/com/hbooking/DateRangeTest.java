package com.hbooking;

import com.hbooking.model.DateRange;
import com.hbooking.exception.InvalidDateRangeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateRangeTest {

    @Test
    void testCheckInMustBeBeforeCheckOut() {
        LocalDate today = LocalDate.now();
        assertThrows(InvalidDateRangeException.class, () -> DateRange.of(today, today));
        assertThrows(InvalidDateRangeException.class, () -> DateRange.of(today.plusDays(1), today));
    }

    @Test
    void testNullDatesThrowException() {
        assertThrows(InvalidDateRangeException.class, () -> DateRange.of(null, LocalDate.now()));
        assertThrows(InvalidDateRangeException.class, () -> DateRange.of(LocalDate.now(), null));
    }

    @Test
    void testNumberOfNights() {
        DateRange range = DateRange.of(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5));
        assertEquals(4, range.getNumberOfNights());
    }

    @Test
    void testHalfOpenOverlapSemantics() {
        DateRange r1 = DateRange.of(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5));

        // Overlapping ranges
        assertTrue(r1.overlaps(DateRange.of(LocalDate.of(2026, 6, 3), LocalDate.of(2026, 6, 7))));
        assertTrue(r1.overlaps(DateRange.of(LocalDate.of(2026, 5, 28), LocalDate.of(2026, 6, 2))));
        assertTrue(r1.overlaps(DateRange.of(LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 4))));

        // Adjacent ranges: Check-out of r1 equals check-in of r2 -> NO overlap
        assertFalse(r1.overlaps(DateRange.of(LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 8))));
        assertFalse(r1.overlaps(DateRange.of(LocalDate.of(2026, 5, 25), LocalDate.of(2026, 6, 1))));
    }
}
