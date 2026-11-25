package com.hbooking.service;

import com.hbooking.payment.PaymentMethod;
import com.hbooking.payment.PaymentReceipt;

public interface PaymentService {
    PaymentReceipt processPayment(String bookingId, String idempotencyKey, PaymentMethod method);
}
