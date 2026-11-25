package com.hbooking.model;

import com.hbooking.enums.Amenity;
import com.hbooking.enums.StarRating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class SearchCriteria {
    private final String city;
    private final String locality;
    private final DateRange dateRange;
    private final GuestCount guestCount;
    private final Money minPrice;
    private final Money maxPrice;
    @Builder.Default
    private final Set<Amenity> requiredAmenities = Collections.emptySet();
    private final StarRating minStarRating;

    public Set<Amenity> getRequiredAmenities() {
        return requiredAmenities != null ? requiredAmenities : Collections.emptySet();
    }
}
