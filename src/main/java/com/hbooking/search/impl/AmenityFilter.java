package com.hbooking.search.impl;

import com.hbooking.enums.Amenity;
import com.hbooking.model.Money;
import com.hbooking.model.Property;
import com.hbooking.model.RoomType;
import com.hbooking.model.SearchCriteria;
import com.hbooking.search.SearchFilter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AmenityFilter implements SearchFilter {

    @Override
    public boolean matches(Property property, RoomType roomType, Money calculatedPrice, SearchCriteria criteria) {
        if (criteria == null || criteria.getRequiredAmenities() == null || criteria.getRequiredAmenities().isEmpty()) {
            return true;
        }

        Set<Amenity> available = new HashSet<>(property.getAmenities());
        if (roomType != null) {
            available.addAll(roomType.getAmenities());
        }

        return available.containsAll(criteria.getRequiredAmenities());
    }
}
