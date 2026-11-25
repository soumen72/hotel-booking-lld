package com.hbooking.payment.impl;

import com.hbooking.model.Money;
import com.hbooking.payment.PaymentGateway;
import com.hbooking.payment.PaymentMethod;
import com.hbooking.payment.PaymentReceipt;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class MockPaymentGateway implements PaymentGateway {
    private final AtomicBoolean forceFailure = new AtomicBoolean(false);

    public void setForceFailure(boolean failure) {
        this.forceFailure.set(failure);
    }

    @Override
    public PaymentReceipt processPayment(String idempotencyKey, Money amount, PaymentMethod method) {
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8);

        // Check forced failure flag or specific failure conditions
        if (forceFailure.get()) {
            return PaymentReceipt.failure(transactionId, idempotencyKey, amount, "Declined by test policy");
        }

        if (method instanceof CardPayment cardPayment) {
            if (cardPayment.getCardNumber().endsWith("0000")) {
                return PaymentReceipt.failure(transactionId, idempotencyKey, amount, "Card declined: invalid card number");
            }
        }

        return PaymentReceipt.success(transactionId, idempotencyKey, amount);
    }
}
