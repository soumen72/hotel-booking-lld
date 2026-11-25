package com.hbooking.dto.response;

import com.hbooking.enums.Amenity;
import com.hbooking.model.Property;
import com.hbooking.model.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class PropertyResponse {
    private final String id;
    private final String ownerId;
    private final String name;
    private final String city;
    private final String locality;
    private final String address;
    private final int starRating;
    private final List<String> amenities;
    private final List<RoomTypeResponse> roomTypes;

    public PropertyResponse(Property property) {
        this.id = property.getId();
        this.ownerId = property.getOwnerId();
        this.name = property.getName();
        this.city = property.getLocation().getCity();
        this.locality = property.getLocation().getLocality();
        this.address = property.getLocation().getAddress();
        this.starRating = property.getStarRating().getValue();
        this.amenities = property.getAmenities().stream().map(Amenity::name).collect(Collectors.toList());
        this.roomTypes = property.getRoomTypes().values().stream()
                .map(RoomTypeResponse::new)
                .collect(Collectors.toList());
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RoomTypeResponse {
        private final String id;
        private final String category;
        private final String name;
        private final String basePrice;
        private final int maxCapacity;
        private final int totalInventory;
        private final List<String> amenities;

        public RoomTypeResponse(RoomType roomType) {
            this.id = roomType.getId();
            this.category = roomType.getCategory() != null ? roomType.getCategory().name() : null;
            this.name = roomType.getName();
            this.basePrice = roomType.getBasePricePerNight().toString();
            this.maxCapacity = roomType.getMaxCapacity();
            this.totalInventory = roomType.getTotalInventory();
            this.amenities = roomType.getAmenities().stream().map(Amenity::name).collect(Collectors.toList());
        }
    }
}
