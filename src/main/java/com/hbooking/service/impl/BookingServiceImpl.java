package com.hbooking.service.impl;

import com.hbooking.model.Booking;
import com.hbooking.model.DateRange;
import com.hbooking.model.GuestCount;
import com.hbooking.model.Money;
import com.hbooking.model.Property;
import com.hbooking.model.RoomType;
import com.hbooking.exception.EntityNotFoundException;
import com.hbooking.exception.RoomUnavailableException;
import com.hbooking.policy.CancellationPolicy;
import com.hbooking.policy.PricingStrategy;
import com.hbooking.repository.BookingRepository;
import com.hbooking.repository.InventoryRepository;
import com.hbooking.repository.PropertyRepository;
import com.hbooking.service.BookingService;
import com.hbooking.model.CancellationResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final InventoryRepository inventoryRepository;
    private final PricingStrategy pricingStrategy;
    private final CancellationPolicy cancellationPolicy;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              PropertyRepository propertyRepository,
                              InventoryRepository inventoryRepository,
                              PricingStrategy pricingStrategy,
                              CancellationPolicy cancellationPolicy) {
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.inventoryRepository = inventoryRepository;
        this.pricingStrategy = pricingStrategy;
        this.cancellationPolicy = cancellationPolicy;
    }

    @Override
    public Booking createBooking(String propertyId, String roomTypeId, String guestId,
                                 DateRange dateRange, GuestCount guestCount) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with ID: " + propertyId));

        RoomType roomType = property.getRoomType(roomTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Room type not found with ID: " + roomTypeId));

        if (!roomType.canAccommodate(guestCount)) {
            throw new IllegalArgumentException(
                    "Room type " + roomType.getName() + " max capacity is " + roomType.getMaxCapacity()
                            + ", but requested guest count is " + guestCount.getTotal());
        }

        // Atomically reserve inventory for the requested date range
        boolean reserved = inventoryRepository.reserveInventory(roomTypeId, dateRange, 1);
        if (!reserved) {
            throw new RoomUnavailableException("No rooms available for room type: " + roomTypeId + " on dates: " + dateRange);
        }

        Money totalPrice = pricingStrategy.calculatePrice(roomType, dateRange);
        String bookingId = "BK-" + UUID.randomUUID().toString().substring(0, 8);
        Booking booking = Booking.builder()
                .id(bookingId)
                .propertyId(propertyId)
                .roomTypeId(roomTypeId)
                .guestId(guestId)
                .dateRange(dateRange)
                .guestCount(guestCount)
                .totalPrice(totalPrice)
                .build();

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));
    }

    @Override
    public CancellationResult cancelBooking(String bookingId) {
        return cancelBooking(bookingId, Instant.now());
    }

    @Override
    public CancellationResult cancelBooking(String bookingId, Instant cancellationTime) {
        Booking booking = getBooking(bookingId);

        // State machine transition will throw if state is invalid (e.g. already cancelled or PAYMENT_FAILED)
        booking.cancel();

        // Release inventory back to the pool
        inventoryRepository.releaseInventory(booking.getRoomTypeId(), booking.getDateRange(), 1);

        // Calculate refund amount based on cancellation policy
        Money refund = cancellationPolicy.calculateRefund(booking, cancellationTime);

        bookingRepository.save(booking);
        return new CancellationResult(booking, refund);
    }
}
