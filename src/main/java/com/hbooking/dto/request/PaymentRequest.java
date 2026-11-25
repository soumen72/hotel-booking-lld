package com.hbooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String idempotencyKey;
    private String paymentMethod; // "CARD", "UPI", "WALLET"
    private String cardNumber;
    private String cardHolderName;
    private String expiry;
    private String cvv;
    private String upiId;
    private String walletProvider;
    private String walletAccountId;
}
