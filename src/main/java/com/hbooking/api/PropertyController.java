package com.hbooking.api;

import com.hbooking.enums.Amenity;
import com.hbooking.model.Location;
import com.hbooking.model.Money;
import com.hbooking.model.Property;
import com.hbooking.enums.RoomCategory;
import com.hbooking.enums.StarRating;
import com.hbooking.dto.request.CreatePropertyRequest;
import com.hbooking.dto.request.CreateRoomTypeRequest;
import com.hbooking.dto.response.PropertyResponse;
import com.hbooking.service.PropertyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api", ""})
public class PropertyController {
    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping("/owners/{id}/properties")
    public ResponseEntity<PropertyResponse> addProperty(
            @PathVariable("id") String ownerId,
            @RequestBody CreatePropertyRequest request) {

        Location location = Location.builder()
                .city(request.getCity())
                .locality(request.getLocality())
                .address(request.getAddress())
                .build();
        StarRating rating = StarRating.fromValue(request.getStarRating());
        Set<Amenity> propertyAmenities = parseAmenities(request.getAmenities());

        Property property = propertyService.addProperty(ownerId, request.getName(), location, rating, propertyAmenities);

        // Add room types if provided in request
        if (request.getRoomTypes() != null) {
            for (CreateRoomTypeRequest rt : request.getRoomTypes()) {
                String currency = rt.getCurrency() != null ? rt.getCurrency() : "USD";
                Money basePrice = Money.of(rt.getBasePrice(), currency);
                Set<Amenity> rtAmenities = parseAmenities(rt.getAmenities());
                RoomCategory category = rt.getCategory() != null ? RoomCategory.fromString(rt.getCategory()) : RoomCategory.fromString(rt.getName());
                propertyService.addRoomType(property.getId(), category, rt.getName(), basePrice,
                        rt.getMaxCapacity(), rt.getTotalInventory(), rtAmenities);
            }
        }

        Property updated = propertyService.getProperty(property.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new PropertyResponse(updated));
    }

    @GetMapping("/owners/{id}/properties")
    public ResponseEntity<List<PropertyResponse>> getPropertiesByOwner(@PathVariable("id") String ownerId) {
        List<PropertyResponse> list = propertyService.getPropertiesByOwner(ownerId).stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/properties/{id}/room-types")
    public ResponseEntity<PropertyResponse.RoomTypeResponse> addRoomType(
            @PathVariable("id") String propertyId,
            @RequestBody CreateRoomTypeRequest request) {

        String currency = request.getCurrency() != null ? request.getCurrency() : "USD";
        Money basePrice = Money.of(request.getBasePrice(), currency);
        Set<Amenity> amenities = parseAmenities(request.getAmenities());
        RoomCategory category = request.getCategory() != null ? RoomCategory.fromString(request.getCategory()) : RoomCategory.fromString(request.getName());

        var roomType = propertyService.addRoomType(propertyId, category, request.getName(), basePrice,
                request.getMaxCapacity(), request.getTotalInventory(), amenities);

        return ResponseEntity.status(HttpStatus.CREATED).body(new PropertyResponse.RoomTypeResponse(roomType));
    }

    @GetMapping("/properties/{id}")
    public ResponseEntity<PropertyResponse> getProperty(@PathVariable("id") String propertyId) {
        Property property = propertyService.getProperty(propertyId);
        return ResponseEntity.ok(new PropertyResponse(property));
    }

    private Set<Amenity> parseAmenities(List<String> rawAmenities) {
        if (rawAmenities == null) {
            return new HashSet<>();
        }
        return rawAmenities.stream()
                .map(String::toUpperCase)
                .map(Amenity::valueOf)
                .collect(Collectors.toSet());
    }
}
