package com.hbooking.payment;

import com.hbooking.model.Money;

public interface PaymentGateway {
    PaymentReceipt processPayment(String idempotencyKey, Money amount, PaymentMethod method);
}
