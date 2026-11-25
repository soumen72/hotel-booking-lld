package com.hbooking.service.impl;

import com.hbooking.enums.Amenity;
import com.hbooking.model.Location;
import com.hbooking.model.Money;
import com.hbooking.model.Owner;
import com.hbooking.model.Property;
import com.hbooking.enums.RoomCategory;
import com.hbooking.model.RoomType;
import com.hbooking.enums.StarRating;
import com.hbooking.exception.EntityNotFoundException;
import com.hbooking.repository.InventoryRepository;
import com.hbooking.repository.OwnerRepository;
import com.hbooking.repository.PropertyRepository;
import com.hbooking.service.PropertyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PropertyServiceImpl implements PropertyService {
    private final OwnerRepository ownerRepository;
    private final PropertyRepository propertyRepository;
    private final InventoryRepository inventoryRepository;

    public PropertyServiceImpl(OwnerRepository ownerRepository,
                               PropertyRepository propertyRepository,
                               InventoryRepository inventoryRepository) {
        this.ownerRepository = ownerRepository;
        this.propertyRepository = propertyRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Owner registerOwner(String name, String email, String phone) {
        String ownerId = "OWN-" + UUID.randomUUID().toString().substring(0, 8);
        Owner owner = Owner.builder()
                .id(ownerId)
                .name(name)
                .email(email)
                .phone(phone)
                .build();
        return ownerRepository.save(owner);
    }

    @Override
    public Optional<Owner> getOwner(String id) {
        return ownerRepository.findById(id);
    }

    @Override
    public Property addProperty(String ownerId, String name, Location location,
                                StarRating starRating, Set<Amenity> amenities) {
        ownerRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found with ID: " + ownerId));

        String propertyId = "PROP-" + UUID.randomUUID().toString().substring(0, 8);
        Property property = Property.builder()
                .id(propertyId)
                .ownerId(ownerId)
                .name(name)
                .location(location)
                .starRating(starRating)
                .amenities(amenities)
                .build();
        return propertyRepository.save(property);
    }

    @Override
    public RoomType addRoomType(String propertyId, RoomCategory category, String name, Money basePricePerNight,
                                int maxCapacity, int totalInventory, Set<Amenity> amenities) {
        Property property = getProperty(propertyId);
        String roomTypeId = "RT-" + UUID.randomUUID().toString().substring(0, 8);
        RoomCategory cat = category != null ? category : RoomCategory.fromString(name);
        RoomType roomType = RoomType.builder()
                .id(roomTypeId)
                .propertyId(propertyId)
                .category(cat)
                .name(name)
                .basePricePerNight(basePricePerNight)
                .maxCapacity(maxCapacity)
                .totalInventory(totalInventory)
                .amenities(amenities)
                .build();

        property.addRoomType(roomType);
        inventoryRepository.initializeInventory(roomTypeId, totalInventory);
        propertyRepository.save(property);
        return roomType;
    }

    @Override
    public RoomType addRoomType(String propertyId, String name, Money basePricePerNight,
                                int maxCapacity, int totalInventory, Set<Amenity> amenities) {
        return addRoomType(propertyId, RoomCategory.fromString(name), name, basePricePerNight,
                maxCapacity, totalInventory, amenities);
    }

    @Override
    public Property getProperty(String id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with ID: " + id));
    }

    @Override
    public List<Property> getPropertiesByOwner(String ownerId) {
        return propertyRepository.findByOwnerId(ownerId);
    }
}
