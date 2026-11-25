package com.hbooking;

import com.hbooking.model.DateRange;
import com.hbooking.model.GuestCount;
import com.hbooking.model.Location;
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
import com.hbooking.service.impl.BookingServiceImpl;
import com.hbooking.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentBookingTest {
    private BookingService bookingService;
    private String propertyId;
    private String roomTypeId;

    @BeforeEach
    void setUp() {
        var ownerRepo = new InMemoryOwnerRepository();
        var propertyRepo = new InMemoryPropertyRepository();
        var inventoryRepo = new InMemoryInventoryRepository();
        var bookingRepo = new InMemoryBookingRepository();

        var propertyService = new PropertyServiceImpl(ownerRepo, propertyRepo, inventoryRepo);
        bookingService = new BookingServiceImpl(
                bookingRepo, propertyRepo, inventoryRepo, new DefaultPricingStrategy(), new TieredCancellationPolicy());

        Owner owner = propertyService.registerOwner("Host", "host@test.com", "9999999999");
        Property prop = propertyService.addProperty(
                owner.getId(), "Mountain Cabin", Location.builder().city("Denver").locality("Downtown").address("1st St").build(),
                StarRating.FOUR_STAR, Set.of());
        propertyId = prop.getId();

        // Exactly 1 room available
        RoomType rt = propertyService.addRoomType(
                propertyId, "Standard Room", Money.of(150.0, "USD"), 2, 1, Set.of());
        roomTypeId = rt.getId();
    }

    @Test
    void testConcurrentBookingOfLastRoomExactlyOneWins() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        LocalDate checkIn = LocalDate.now().plusDays(20);
        LocalDate checkOut = LocalDate.now().plusDays(25);
        DateRange dateRange = DateRange.of(checkIn, checkOut);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger roomUnavailableCount = new AtomicInteger(0);
        AtomicInteger unexpectedErrorCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final String guestId = "guest-" + i;
            futures.add(executor.submit(() -> {
                try {
                    startGate.await(); // Thundering herd
                    bookingService.createBooking(propertyId, roomTypeId, guestId, dateRange, GuestCount.of(1));
                    successCount.incrementAndGet();
                } catch (RoomUnavailableException e) {
                    roomUnavailableCount.incrementAndGet();
                } catch (Exception e) {
                    unexpectedErrorCount.incrementAndGet();
                } finally {
                    endGate.countDown();
                }
            }));
        }

        // Release all threads simultaneously
        startGate.countDown();
        endGate.await();
        executor.shutdown();

        // Wait for all futures
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (ExecutionException ignored) {}
        }

        // Exactly 1 should have secured the room, and 19 should have received RoomUnavailableException
        assertEquals(1, successCount.get(), "Exactly one booking should succeed");
        assertEquals(threadCount - 1, roomUnavailableCount.get(), "All other requests should fail with RoomUnavailableException");
        assertEquals(0, unexpectedErrorCount.get(), "No unexpected errors should occur");
    }
}
