package com.hbooking.dto.response;

import com.hbooking.model.Booking;
import com.hbooking.model.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CancelBookingResponse {
    private final String bookingId;
    private final String status;
    private final String refundAmount;
    private final String message;

    public CancelBookingResponse(Booking booking, Money refundAmount) {
        this.bookingId = booking.getId();
        this.status = booking.getStatus().name();
        this.refundAmount = refundAmount.toString();
        this.message = "Booking cancelled successfully";
    }
}
