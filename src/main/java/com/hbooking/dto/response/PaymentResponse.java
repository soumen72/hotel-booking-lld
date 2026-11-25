package com.hbooking.dto.response;

import com.hbooking.payment.PaymentReceipt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResponse {
    private final String transactionId;
    private final String idempotencyKey;
    private final boolean successful;
    private final String amount;
    private final String message;
    private final String bookingStatus;

    public PaymentResponse(PaymentReceipt receipt, String bookingStatus) {
        this.transactionId = receipt.getTransactionId();
        this.idempotencyKey = receipt.getIdempotencyKey();
        this.successful = receipt.isSuccessful();
        this.amount = receipt.getAmount().toString();
        this.message = receipt.getMessage();
        this.bookingStatus = bookingStatus;
    }
}
