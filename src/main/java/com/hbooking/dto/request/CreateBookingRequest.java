package com.hbooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    private String propertyId;
    private String roomTypeId;
    private String guestId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    @Builder.Default
    private int adults = 1;
    @Builder.Default
    private int children = 0;
}
