package com.hbooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePropertyRequest {
    private String name;
    private String city;
    private String locality;
    private String address;
    private int starRating;
    private List<String> amenities;
    private List<CreateRoomTypeRequest> roomTypes;
}
