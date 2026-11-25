package com.hbooking.service;

import com.hbooking.enums.Amenity;
import com.hbooking.model.Location;
import com.hbooking.model.Money;
import com.hbooking.model.Owner;
import com.hbooking.model.Property;
import com.hbooking.enums.RoomCategory;
import com.hbooking.model.RoomType;
import com.hbooking.enums.StarRating;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PropertyService {
    Owner registerOwner(String name, String email, String phone);
    Optional<Owner> getOwner(String id);
    Property addProperty(String ownerId, String name, Location location, StarRating starRating, Set<Amenity> amenities);
    RoomType addRoomType(String propertyId, RoomCategory category, String name, Money basePricePerNight, int maxCapacity, int totalInventory, Set<Amenity> amenities);
    RoomType addRoomType(String propertyId, String name, Money basePricePerNight, int maxCapacity, int totalInventory, Set<Amenity> amenities);
    Property getProperty(String id);
    List<Property> getPropertiesByOwner(String ownerId);
}
