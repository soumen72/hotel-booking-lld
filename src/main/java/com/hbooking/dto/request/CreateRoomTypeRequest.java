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
public class CreateRoomTypeRequest {
    private String category;
    private String name;
    private double basePrice;
    @Builder.Default
    private String currency = "USD";
    private int maxCapacity;
    private int totalInventory;
    private List<String> amenities;
}
