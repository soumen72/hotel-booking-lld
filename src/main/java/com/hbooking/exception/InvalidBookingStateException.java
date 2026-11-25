package com.hbooking.exception;

public class InvalidBookingStateException extends HotelBookingException {
    public InvalidBookingStateException(String message) {
        super(message);
    }
}
