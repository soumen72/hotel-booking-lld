package com.hbooking.repository;

import com.hbooking.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(String id);
    List<Booking> findByPropertyId(String propertyId);
    List<Booking> findAll();
}
