package com.hbooking;

import com.hbooking.model.Booking;
import com.hbooking.enums.BookingStatus;
import com.hbooking.model.DateRange;
import com.hbooking.model.GuestCount;
import com.hbooking.model.Money;
import com.hbooking.payment.PaymentReceipt;
import com.hbooking.payment.impl.CardPayment;
import com.hbooking.payment.impl.MockPaymentGateway;
import com.hbooking.payment.impl.UpiPayment;
import com.hbooking.repository.impl.InMemoryBookingRepository;
import com.hbooking.repository.impl.InMemoryInventoryRepository;
import com.hbooking.service.PaymentService;
import com.hbooking.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentServiceTest {
    private InMemoryBookingRepository bookingRepository;
    private InMemoryInventoryRepository inventoryRepository;
    private MockPaymentGateway mockPaymentGateway;
    private PaymentService paymentService;

    private String bookingId;
    private String roomTypeId;
    private DateRange dateRange;

    @BeforeEach
    void setUp() {
        bookingRepository = new InMemoryBookingRepository();
        inventoryRepository = new InMemoryInventoryRepository();
        mockPaymentGateway = new MockPaymentGateway();
        paymentService = new PaymentServiceImpl(bookingRepository, inventoryRepository, mockPaymentGateway);

        roomTypeId = "RT-100";
        inventoryRepository.initializeInventory(roomTypeId, 2);

        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = LocalDate.now().plusDays(5);
        dateRange = DateRange.of(checkIn, checkOut);

        // Reserve 1 room
        inventoryRepository.reserveInventory(roomTypeId, dateRange, 1);

        Booking booking = Booking.builder()
                .id("BK-PAY-1")
                .propertyId("PROP-1")
                .roomTypeId(roomTypeId)
                .guestId("GUEST-1")
                .dateRange(dateRange)
                .guestCount(GuestCount.of(2))
                .totalPrice(Money.of(300.0, "USD"))
                .build();
        bookingRepository.save(booking);
        bookingId = booking.getId();
    }

    @Test
    void testSuccessfulPaymentTransitionsBookingToConfirmed() {
        CardPayment card = CardPayment.builder()
                .cardNumber("4111111111111234")
                .cardHolderName("John Doe")
                .expiry("12/28")
                .cvv("123")
                .build();
        PaymentReceipt receipt = paymentService.processPayment(bookingId, "IDEMP-1001", card);

        assertTrue(receipt.isSuccessful());
        Booking updated = bookingRepository.findById(bookingId).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, updated.getStatus());
        // Inventory is still held (1 room remaining out of 2)
        assertEquals(1, inventoryRepository.getAvailableRooms(roomTypeId, dateRange.getCheckIn()));
    }

    @Test
    void testFailedPaymentTransitionsBookingToPaymentFailedAndReleasesInventory() {
        // Card ending in 0000 triggers failure in MockPaymentGateway
        CardPayment failingCard = CardPayment.builder()
                .cardNumber("4111111111110000")
                .cardHolderName("John Doe")
                .expiry("12/28")
                .cvv("123")
                .build();
        PaymentReceipt receipt = paymentService.processPayment(bookingId, "IDEMP-1002", failingCard);

        assertFalse(receipt.isSuccessful());
        Booking updated = bookingRepository.findById(bookingId).orElseThrow();
        assertEquals(BookingStatus.PAYMENT_FAILED, updated.getStatus());

        // Inventory should have been released back to 2 available rooms
        assertEquals(2, inventoryRepository.getAvailableRooms(roomTypeId, dateRange.getCheckIn()));
    }

    @Test
    void testIdempotentPaymentRetryReturnsCachedReceiptWithoutDuplicateStateChanges() {
        UpiPayment upi = UpiPayment.builder()
                .upiId("user@oksbi")
                .build();
        String idempotencyKey = "IDEMP-KEY-999";

        PaymentReceipt firstReceipt = paymentService.processPayment(bookingId, idempotencyKey, upi);
        assertTrue(firstReceipt.isSuccessful());

        // Call again with the exact same idempotency key
        PaymentReceipt secondReceipt = paymentService.processPayment(bookingId, idempotencyKey, upi);

        // Same transaction id and result
        assertEquals(firstReceipt.getTransactionId(), secondReceipt.getTransactionId());
        assertEquals(firstReceipt.getProcessedAt(), secondReceipt.getProcessedAt());

        Booking currentBooking = bookingRepository.findById(bookingId).orElseThrow();
        assertEquals(BookingStatus.CONFIRMED, currentBooking.getStatus());
    }
}
