package com.hbooking.payment.impl;

import com.hbooking.payment.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CardPayment implements PaymentMethod {
    private final String cardNumber;
    private final String cardHolderName;
    private final String expiry;
    private final String cvv;

    @Override
    public String getMethodType() {
        return "CARD";
    }
}
