package com.hbooking.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentRecord {
    private final String idempotencyKey;
    private final String bookingId;
    private final PaymentReceipt receipt;
}
