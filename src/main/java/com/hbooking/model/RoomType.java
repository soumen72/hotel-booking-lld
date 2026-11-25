package com.hbooking.model;

import com.hbooking.enums.Amenity;
import com.hbooking.enums.RoomCategory;

import lombok.Builder;
import lombok.Getter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Builder
public class RoomType {
    private final String id;
    private final String propertyId;
    private final RoomCategory category;
    private final String name;
    private final Money basePricePerNight;
    private final int maxCapacity;
    private final int totalInventory;
    private final Set<Amenity> amenities;

    public RoomType(String id, String propertyId, RoomCategory category, String name,
                    Money basePricePerNight, int maxCapacity, int totalInventory, Set<Amenity> amenities) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Room type ID cannot be blank");
        }
        if (propertyId == null || propertyId.isBlank()) {
            throw new IllegalArgumentException("Property ID cannot be blank");
        }
        if (category == null) {
            throw new IllegalArgumentException("Room category cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room type name cannot be blank");
        }
        if (basePricePerNight == null) {
            throw new IllegalArgumentException("Base price per night cannot be null");
        }
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Max capacity must be greater than 0");
        }
        if (totalInventory <= 0) {
            throw new IllegalArgumentException("Total inventory must be greater than 0");
        }

        this.id = id;
        this.propertyId = propertyId;
        this.category = category;
        this.name = name;
        this.basePricePerNight = basePricePerNight;
        this.maxCapacity = maxCapacity;
        this.totalInventory = totalInventory;
        this.amenities = amenities != null ? new HashSet<>(amenities) : new HashSet<>();
    }

    public RoomType(String id, String propertyId, String name, Money basePricePerNight,
                    int maxCapacity, int totalInventory, Set<Amenity> amenities) {
        this(id, propertyId, RoomCategory.fromString(name), name, basePricePerNight, maxCapacity, totalInventory, amenities);
    }

    public boolean canAccommodate(GuestCount guestCount) {
        if (guestCount == null) {
            return false;
        }
        return this.maxCapacity >= guestCount.getTotal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomType roomType)) return false;
        return Objects.equals(id, roomType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
