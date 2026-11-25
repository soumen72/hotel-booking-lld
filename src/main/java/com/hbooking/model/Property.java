package com.hbooking.model;

import com.hbooking.enums.Amenity;
import com.hbooking.enums.StarRating;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Property {
    private final String id;
    private final String ownerId;
    private final String name;
    private final Location location;
    private final StarRating starRating;
    private final Set<Amenity> amenities;
    private final Map<String, RoomType> roomTypes;

    @Builder
    public Property(String id, String ownerId, String name, Location location,
                    StarRating starRating, Set<Amenity> amenities) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Property ID cannot be blank");
        }
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("Owner ID cannot be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Property name cannot be blank");
        }
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }
        if (starRating == null) {
            throw new IllegalArgumentException("Star rating cannot be null");
        }

        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.location = location;
        this.starRating = starRating;
        this.amenities = amenities != null ? new HashSet<>(amenities) : new HashSet<>();
        this.roomTypes = new ConcurrentHashMap<>();
    }

    public void addRoomType(RoomType roomType) {
        if (roomType == null) {
            throw new IllegalArgumentException("Room type cannot be null");
        }
        this.roomTypes.put(roomType.getId(), roomType);
    }

    public Map<String, RoomType> getRoomTypes() {
        return Collections.unmodifiableMap(roomTypes);
    }

    public Optional<RoomType> getRoomType(String roomTypeId) {
        return Optional.ofNullable(roomTypes.get(roomTypeId));
    }

    public boolean hasAmenity(Amenity amenity) {
        return amenities.contains(amenity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Property property)) return false;
        return Objects.equals(id, property.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
