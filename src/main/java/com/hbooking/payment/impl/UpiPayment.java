package com.hbooking.payment.impl;

import com.hbooking.payment.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpiPayment implements PaymentMethod {
    private final String upiId;

    @Override
    public String getMethodType() {
        return "UPI";
    }
}
