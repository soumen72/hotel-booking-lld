package com.hbooking.payment;

import com.hbooking.model.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class PaymentReceipt {
    private final String transactionId;
    private final String idempotencyKey;
    private final boolean successful;
    private final Money amount;
    private final Instant processedAt;
    private final String message;

    public static PaymentReceipt success(String transactionId, String idempotencyKey, Money amount) {
        return new PaymentReceipt(transactionId, idempotencyKey, true, amount, Instant.now(), "Payment successful");
    }

    public static PaymentReceipt failure(String transactionId, String idempotencyKey, Money amount, String reason) {
        return new PaymentReceipt(transactionId, idempotencyKey, false, amount, Instant.now(), reason);
    }
}
