package com.hbooking;

import com.hbooking.model.DateRange;
import com.hbooking.model.GuestCount;
import com.hbooking.model.Money;
import com.hbooking.model.Owner;
import com.hbooking.model.Property;
import com.hbooking.model.RoomType;
import com.hbooking.enums.StarRating;
import com.hbooking.exception.RoomUnavailableException;
import com.hbooking.policy.impl.DefaultPricingStrategy;
import com.hbooking.policy.impl.TieredCancellationPolicy;
import com.hbooking.repository.impl.InMemoryBookingRepository;
import com.hbooking.repository.impl.InMemoryInventoryRepository;
import com.hbooking.repository.impl.InMemoryOwnerRepository;
import com.hbooking.repository.impl.InMemoryPropertyRepository;
import com.hbooking.service.BookingService;
import com.hbooking.service.PropertyService;
import com.hbooking.service.impl.BookingServiceImpl;
import com.hbooking.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DoubleBookingTest {
    private PropertyService propertyService;
    private BookingService bookingService;

    private String propertyId;
    private String roomTypeId;

    @BeforeEach
    void setUp() {
        var ownerRepo = new InMemoryOwnerRepository();
        var propertyRepo = new InMemoryPropertyRepository();
        var inventoryRepo = new InMemoryInventoryRepository();
        var bookingRepo = new InMemoryBookingRepository();

        var pricing = new DefaultPricingStrategy();
        var cancellation = new TieredCancellationPolicy();

        propertyService = new PropertyServiceImpl(ownerRepo, propertyRepo, inventoryRepo);
        bookingService = new BookingServiceImpl(bookingRepo, propertyRepo, inventoryRepo, pricing, cancellation);

        Owner owner = propertyService.registerOwner("Alice", "alice@example.com", "1234567890");
        Property property = propertyService.addProperty(
                owner.getId(), "Seaside Resort",
                com.hbooking.model.Location.builder().city("Miami").locality("South Beach").address("Ocean Drive 101").build(),
                StarRating.FIVE_STAR, Set.of());
        propertyId = property.getId();

        // Single room inventory
        RoomType roomType = propertyService.addRoomType(
                propertyId, "Deluxe Suite", Money.of(200.0, "USD"), 2, 1, Set.of());
        roomTypeId = roomType.getId();
    }

    @Test
    void testExactDateOverlapPrevented() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(10);

        bookingService.createBooking(propertyId, roomTypeId, "guest-1",
                DateRange.of(checkIn, checkOut), GuestCount.of(2));

        assertThrows(RoomUnavailableException.class, () ->
                bookingService.createBooking(propertyId, roomTypeId, "guest-2",
                        DateRange.of(checkIn, checkOut), GuestCount.of(1)));
    }

    @Test
    void testPartialDateOverlapPrevented() {
        LocalDate start = LocalDate.now().plusDays(5);

        // Booking 1: Days 5 to 9
        bookingService.createBooking(propertyId, roomTypeId, "guest-1",
                DateRange.of(start, start.plusDays(4)), GuestCount.of(2));

        // Booking 2: Days 7 to 11 (overlaps days 7 and 8)
        assertThrows(RoomUnavailableException.class, () ->
                bookingService.createBooking(propertyId, roomTypeId, "guest-2",
                        DateRange.of(start.plusDays(2), start.plusDays(6)), GuestCount.of(1)));

        // Booking 3: Days 6 to 7 (fully within Days 5 to 9)
        assertThrows(RoomUnavailableException.class, () ->
                bookingService.createBooking(propertyId, roomTypeId, "guest-3",
                        DateRange.of(start.plusDays(1), start.plusDays(2)), GuestCount.of(1)));
    }

    @Test
    void testAdjacentBackToBackDatesAllowed() {
        LocalDate day1 = LocalDate.now().plusDays(10);
        LocalDate day5 = day1.plusDays(4);
        LocalDate day8 = day5.plusDays(3);

        // Guest 1: check-in day 1, check-out day 5 (inclusive 1, 2, 3, 4; check-out on 5)
        assertDoesNotThrow(() ->
                bookingService.createBooking(propertyId, roomTypeId, "guest-1",
                        DateRange.of(day1, day5), GuestCount.of(2)));

        // Guest 2: check-in day 5, check-out day 8 (check-in on 5 is allowed!)
        assertDoesNotThrow(() ->
                bookingService.createBooking(propertyId, roomTypeId, "guest-2",
                        DateRange.of(day5, day8), GuestCount.of(1)));
    }
}
