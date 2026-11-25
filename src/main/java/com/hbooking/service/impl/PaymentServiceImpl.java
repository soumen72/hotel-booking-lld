package com.hbooking.service.impl;

import com.hbooking.model.Booking;
import com.hbooking.enums.BookingStatus;
import com.hbooking.exception.EntityNotFoundException;
import com.hbooking.exception.InvalidBookingStateException;
import com.hbooking.payment.PaymentGateway;
import com.hbooking.payment.PaymentMethod;
import com.hbooking.payment.PaymentReceipt;
import com.hbooking.repository.BookingRepository;
import com.hbooking.repository.InventoryRepository;
import com.hbooking.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentGateway paymentGateway;

    private final Map<String, PaymentReceipt> idempotencyStore = new ConcurrentHashMap<>();
    private final Map<String, Object> idempotencyLocks = new ConcurrentHashMap<>();

    public PaymentServiceImpl(BookingRepository bookingRepository,
                              InventoryRepository inventoryRepository,
                              PaymentGateway paymentGateway) {
        this.bookingRepository = bookingRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentGateway = paymentGateway;
    }

    @Override
    public PaymentReceipt processPayment(String bookingId, String idempotencyKey, PaymentMethod method) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Booking ID cannot be blank");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key cannot be blank");
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }

        // Return cached receipt if idempotency key has already been processed
        PaymentReceipt existing = idempotencyStore.get(idempotencyKey);
        if (existing != null) {
            return existing;
        }

        Object lock = idempotencyLocks.computeIfAbsent(idempotencyKey, k -> new Object());
        synchronized (lock) {
            existing = idempotencyStore.get(idempotencyKey);
            if (existing != null) {
                return existing;
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));

            if (booking.getStatus() != BookingStatus.PENDING) {
                throw new InvalidBookingStateException(
                        "Cannot process payment for booking " + bookingId + " in status: " + booking.getStatus());
            }

            PaymentReceipt receipt = paymentGateway.processPayment(idempotencyKey, booking.getTotalPrice(), method);

            if (receipt.isSuccessful()) {
                booking.markConfirmed();
            } else {
                booking.markPaymentFailed();
                // Release held inventory on payment failure
                inventoryRepository.releaseInventory(booking.getRoomTypeId(), booking.getDateRange(), 1);
            }

            bookingRepository.save(booking);
            idempotencyStore.put(idempotencyKey, receipt);
            return receipt;
        }
    }
}
