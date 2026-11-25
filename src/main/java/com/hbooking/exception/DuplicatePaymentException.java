package com.hbooking.exception;

public class DuplicatePaymentException extends HotelBookingException {
    public DuplicatePaymentException(String message) {
        super(message);
    }
}
