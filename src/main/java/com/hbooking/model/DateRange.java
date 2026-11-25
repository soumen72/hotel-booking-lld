package com.hbooking.model;

import com.hbooking.exception.InvalidDateRangeException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public final class DateRange {
    private final LocalDate checkIn;
    private final LocalDate checkOut;

    public DateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidDateRangeException("Check-in and check-out dates must not be null");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new InvalidDateRangeException("Check-in date (" + checkIn + ") must be before check-out date (" + checkOut + ")");
        }
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public static DateRange of(LocalDate checkIn, LocalDate checkOut) {
        return new DateRange(checkIn, checkOut);
    }

    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public List<LocalDate> getDates() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = checkIn;
        while (current.isBefore(checkOut)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return Collections.unmodifiableList(dates);
    }

    public boolean overlaps(DateRange other) {
        if (other == null) {
            return false;
        }
        // [A, B) overlaps [C, D) iff A < D and C < B
        return this.checkIn.isBefore(other.checkOut) && other.checkIn.isBefore(this.checkOut);
    }

    public boolean contains(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(checkIn) && date.isBefore(checkOut);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateRange dateRange)) return false;
        return checkIn.equals(dateRange.checkIn) && checkOut.equals(dateRange.checkOut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkIn, checkOut);
    }

    @Override
    public String toString() {
        return "[" + checkIn + " to " + checkOut + ")";
    }
}
