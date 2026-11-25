package com.hbooking.model;

public record CancellationResult(Booking booking, Money refundAmount) {
}
