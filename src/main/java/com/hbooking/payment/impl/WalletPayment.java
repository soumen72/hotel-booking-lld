package com.hbooking.payment.impl;

import com.hbooking.payment.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WalletPayment implements PaymentMethod {
    private final String walletProvider;
    private final String accountId;

    @Override
    public String getMethodType() {
        return "WALLET";
    }
}
