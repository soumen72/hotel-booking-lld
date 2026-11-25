package com.hbooking.dto.response;

import com.hbooking.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BookingResponse {
    private final String id;
    private final String propertyId;
    private final String roomTypeId;
    private final String guestId;
    private final String checkIn;
    private final String checkOut;
    private final int adults;
    private final int children;
    private final String totalPrice;
    private final String status;

    public BookingResponse(Booking booking) {
        this.id = booking.getId();
        this.propertyId = booking.getPropertyId();
        this.roomTypeId = booking.getRoomTypeId();
        this.guestId = booking.getGuestId();
        this.checkIn = booking.getDateRange().getCheckIn().toString();
        this.checkOut = booking.getDateRange().getCheckOut().toString();
        this.adults = booking.getGuestCount().getAdults();
        this.children = booking.getGuestCount().getChildren();
        this.totalPrice = booking.getTotalPrice().toString();
        this.status = booking.getStatus().name();
    }
}
